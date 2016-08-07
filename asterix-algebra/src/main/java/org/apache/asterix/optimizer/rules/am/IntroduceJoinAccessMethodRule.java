/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.optimizer.rules.am;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.asterix.common.config.DatasetConfig.IndexType;
import org.apache.asterix.metadata.declared.AqlMetadataProvider;
import org.apache.asterix.metadata.entities.Index;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.common.utils.Pair;
import org.apache.hyracks.algebricks.core.algebra.base.ILogicalExpression;
import org.apache.hyracks.algebricks.core.algebra.base.ILogicalOperator;
import org.apache.hyracks.algebricks.core.algebra.base.IOptimizationContext;
import org.apache.hyracks.algebricks.core.algebra.base.LogicalExpressionTag;
import org.apache.hyracks.algebricks.core.algebra.base.LogicalOperatorTag;
import org.apache.hyracks.algebricks.core.algebra.base.LogicalVariable;
import org.apache.hyracks.algebricks.core.algebra.expressions.AbstractFunctionCallExpression;
import org.apache.hyracks.algebricks.core.algebra.expressions.ScalarFunctionCallExpression;
import org.apache.hyracks.algebricks.core.algebra.expressions.VariableReferenceExpression;
import org.apache.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.AbstractBinaryJoinOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.AbstractLogicalOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.DataSourceScanOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.DistinctOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.GroupByOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.InnerJoinOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.LeftOuterJoinOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.UnnestMapOperator;
import org.apache.hyracks.algebricks.core.algebra.util.OperatorPropertiesUtil;

/**
 * This rule optimizes a join with secondary indexes into an indexed nested-loop join.
 * Matches the following operator pattern:
 * (join) <-- (select)? <-- (assign | unnest)+ <-- (datasource scan)
 * ...... <-- (select)? <-- (assign | unnest)+ <-- (datasource scan | unnest-map)
 * The order of the join inputs matters (left-outer relation, right-inner relation).
 * This rule tries to utilize an index on the inner relation first.
 * If that's not possible, it tries to use an index on the outer relation.
 * Replaces the above pattern with the following simplified plan:
 * (select) <-- (assign) <-- (btree search) <-- (sort) <-- (unnest(index search)) <-- (assign) <-- (datasource scan | unnest-map)
 * The sort is optional, and some access methods may choose not to sort.
 * Note that for some index-based optimizations we do not remove the triggering
 * condition from the join, since the secondary index may only act as a filter, and the
 * final verification must still be done with the original join condition.
 * The basic outline of this rule is:
 * 1. Match operator pattern.
 * 2. Analyze join condition to see if there are optimizable functions (delegated to IAccessMethods).
 * 3. Check metadata to see if there are applicable indexes.
 * 4. Choose an index to apply (for now only a single index will be chosen).
 * 5. Rewrite plan using index (delegated to IAccessMethods).
 * For left-outer-join, additional patterns are checked and additional treatment is needed as follows:
 * 1. First it checks if there is a groupByOp above the join: (groupby) <-- (leftouterjoin)
 * 2. Inherently, only the right-subtree of the lojOp can be used as indexSubtree.
 * So, the right-subtree must have at least one applicable index on join field(s)
 * 3. If there is a groupByOp, the null placeholder variable introduced in groupByOp should be taken care of correctly.
 * Here, the primary key variable from datasourceScanOp replaces the introduced null placeholder variable.
 * If the primary key is composite key, then the first variable of the primary key variables becomes the
 * null place holder variable. This null placeholder variable works for all three types of indexes.
 */
public class IntroduceJoinAccessMethodRule extends AbstractIntroduceAccessMethodRule {

    protected Mutable<ILogicalOperator> joinRef = null;
    protected AbstractBinaryJoinOperator joinOp = null;
    protected AbstractFunctionCallExpression joinCond = null;
    protected final OptimizableOperatorSubTree leftSubTree = new OptimizableOperatorSubTree();
    protected final OptimizableOperatorSubTree rightSubTree = new OptimizableOperatorSubTree();
    protected boolean isLeftOuterJoin = false;
    protected boolean hasGroupBy = true;
    protected IOptimizationContext context = null;
    protected List<Mutable<ILogicalOperator>> afterJoinRefs = null;
    protected boolean isSHBtree = false;

    // Register access methods.
    protected static Map<FunctionIdentifier, List<IAccessMethod>> accessMethods = new HashMap<FunctionIdentifier, List<IAccessMethod>>();
    static {
        registerAccessMethod(BTreeAccessMethod.INSTANCE, accessMethods);
        registerAccessMethod(RTreeAccessMethod.INSTANCE, accessMethods);
        registerAccessMethod(InvertedIndexAccessMethod.INSTANCE, accessMethods);
    }

    @Override
    public boolean rewritePost(Mutable<ILogicalOperator> opRef, IOptimizationContext context)
            throws AlgebricksException {
        clear();
        setMetadataDeclarations(context);
        this.context = context;

        // Check whether this operator is the root, which is DISTRIBUTE_RESULT
        AbstractLogicalOperator op = (AbstractLogicalOperator) opRef.getValue();

        if (context.checkIfInDontApplySet(this, op)) {
            return false;
        }

        // Begin from the root operator - DISTRIBUTE_RESULT or SINK
        if (op.getOperatorTag() != LogicalOperatorTag.DISTRIBUTE_RESULT) {
            if (op.getOperatorTag() != LogicalOperatorTag.SINK) {
                return false;
            }
        }

        afterJoinRefs = new ArrayList<Mutable<ILogicalOperator>>();
        boolean planTransformed = false;

        // Recursively check the plan whether the desired pattern exists in it. If so, try to optimize the plan.
        planTransformed = checkAndApplyTheRule(opRef);

        if (joinOp != null) {
            context.addToDontApplySet(this, joinOp);
        }

        if (!planTransformed) {
            return false;
        } else {
            //            StringBuilder sb = new StringBuilder();
            //            LogicalOperatorPrettyPrintVisitor pvisitor = context.getPrettyPrintVisitor();
            //            PlanPrettyPrinter.printOperator((AbstractLogicalOperator) opRef.getValue(), sb, pvisitor, 0);
            OperatorPropertiesUtil.typeOpRec(opRef, context);
        }

        return planTransformed;
    }

    // Check whether (Groupby)? <-- Leftouterjoin
    private boolean isLeftOuterJoin(AbstractLogicalOperator op1) {
        if (op1.getInputs().size() != 1) {
            return false;
        }
        if (((AbstractLogicalOperator) op1.getInputs().get(0).getValue()).getOperatorTag() != LogicalOperatorTag.LEFTOUTERJOIN) {
            return false;
        }
        if (op1.getOperatorTag() == LogicalOperatorTag.GROUP) {
            return true;
        }
        hasGroupBy = false;
        return true;
    }

    private boolean isInnerJoin(AbstractLogicalOperator op1) {
        return op1.getOperatorTag() == LogicalOperatorTag.INNERJOIN;
    }

    @Override
    public Map<FunctionIdentifier, List<IAccessMethod>> getAccessMethods() {
        return accessMethods;
    }

    private void clear() {
        joinRef = null;
        joinOp = null;
        joinCond = null;
        isLeftOuterJoin = false;
        context = null;
        afterJoinRefs = null;
    }

    // Recursively traverse the given plan and check whether JOIN operator exists.
    // If one is found, maintain the path from the root to JOIN operator if it is not already optimized.
    protected boolean checkAndApplyTheRule(Mutable<ILogicalOperator> opRef) throws AlgebricksException {
        AbstractLogicalOperator op = (AbstractLogicalOperator) opRef.getValue();
        boolean joinFoundAndOptimizationApplied = false;

        // Check the operator pattern to see whether it is JOIN or not.
        boolean isInnerJoin = isInnerJoin(op);
        isLeftOuterJoin = isLeftOuterJoin(op);

        Mutable<ILogicalOperator> joinRefFromThisOp = null;
        AbstractBinaryJoinOperator joinOpFromThisOp = null;

        if (isInnerJoin) {
            joinRef = opRef;
            joinOp = (InnerJoinOperator) op;
            joinRefFromThisOp = opRef;
            joinOpFromThisOp = (InnerJoinOperator) op;
        } else if (isLeftOuterJoin) {
            joinRef = op.getInputs().get(0);
            joinOp = (LeftOuterJoinOperator) joinRef.getValue();
            joinRefFromThisOp = op.getInputs().get(0);
            joinOpFromThisOp = (LeftOuterJoinOperator) joinRefFromThisOp.getValue();
        } else {
            afterJoinRefs.add(opRef);
        }

        // Recursively check the plan and try to optimize it.
        // We first check the children of the given operator to make sure an earlier join in the path is optimized first.
        for (int i = 0; i < op.getInputs().size(); i++) {
            joinFoundAndOptimizationApplied = checkAndApplyTheRule(op.getInputs().get(i));
            if (joinFoundAndOptimizationApplied) {
                removeDuplicationForSHBtree(op, op.getInputs().get(i));
                return true;
            }
        }

        // For JOIN case, try to transform the given plan.
        if (isInnerJoin || isLeftOuterJoin) {

            // Restore the join operator from this operator since it might be set to null
            // if there are other join operators in the earlier path.
            joinRef = joinRefFromThisOp;
            joinOp = joinOpFromThisOp;

            // Already checked? If not, this operator can be optimized.
            if (!context.checkIfInDontApplySet(this, joinOp)) {
                Map<IAccessMethod, AccessMethodAnalysisContext> analyzedAMs = new HashMap<IAccessMethod, AccessMethodAnalysisContext>();

                // Check the condition of JOIN operator is a function call and initialize operator members.
                if (checkJoinOperatorCondition()) {

                    // Analyze condition on those optimizable subtrees that have a datasource scan.
                    boolean matchInLeftSubTree = false;
                    boolean matchInRightSubTree = false;
                    if (leftSubTree.hasDataSource()) {
                        matchInLeftSubTree = analyzeCondition(joinCond, leftSubTree.assignsAndUnnests, analyzedAMs);
                    }
                    if (rightSubTree.hasDataSource()) {
                        matchInRightSubTree = analyzeCondition(joinCond, rightSubTree.assignsAndUnnests, analyzedAMs);
                    }

                    if (matchInLeftSubTree || matchInRightSubTree) {
                        // Set dataset and type metadata.
                        AqlMetadataProvider metadataProvider = (AqlMetadataProvider) context.getMetadataProvider();
                        boolean checkLeftSubTreeMetadata = false;
                        boolean checkRightSubTreeMetadata = false;
                        if (matchInLeftSubTree) {
                            checkLeftSubTreeMetadata = leftSubTree.setDatasetAndTypeMetadata(metadataProvider);
                        }
                        if (matchInRightSubTree) {
                            checkRightSubTreeMetadata = rightSubTree.setDatasetAndTypeMetadata(metadataProvider);
                        }

                        if (checkLeftSubTreeMetadata || checkRightSubTreeMetadata) {
                            // Map variables to the applicable indexes.
                            if (checkLeftSubTreeMetadata) {
                                fillSubTreeIndexExprs(leftSubTree, analyzedAMs, context);
                            }
                            if (checkRightSubTreeMetadata) {
                                fillSubTreeIndexExprs(rightSubTree, analyzedAMs, context);
                            }

                            // Prune the access methods if there is no applicable index for them.
                            pruneIndexCandidates(analyzedAMs);

                            // Prioritize the order of index that will be applied. If the right subtree (inner branch) has indexes,
                            // those indexes will be used.
                            List<String> innerDatasets = new ArrayList<String>();
                            if (rightSubTree.dataset != null) {
                                innerDatasets.add(rightSubTree.dataset.getDatasetName());
                            }
                            if (rightSubTree.ixJoinOuterAdditionalDatasets != null) {
                                for (int i = 0; i < rightSubTree.ixJoinOuterAdditionalDatasets.size(); i++) {
                                    if (rightSubTree.ixJoinOuterAdditionalDatasets.get(i) != null) {
                                        innerDatasets.add(rightSubTree.ixJoinOuterAdditionalDatasets.get(i)
                                                .getDatasetName());
                                    }
                                }
                            }
                            removeIndexCandidatesFromOuterRelation(analyzedAMs, innerDatasets);

                            // For the case of left-outer-join, we have to use indexes from the inner branch.
                            // For the inner-join, we try to use the indexes from the inner branch first.
                            // If no index is available, then we use the indexes from the outer branch.
                            Pair<IAccessMethod, Index> chosenIndex = chooseIndex(analyzedAMs);
                            if (chosenIndex != null) {

                                // Find the field name of each variable in the sub-tree - required when checking index-only plan.
                                if (checkLeftSubTreeMetadata) {
                                    fillFieldNamesInTheSubTree(leftSubTree);
                                }
                                if (checkRightSubTreeMetadata) {
                                    fillFieldNamesInTheSubTree(rightSubTree);
                                }

                                // Apply plan transformation using chosen index.
                                AccessMethodAnalysisContext analysisCtx = analyzedAMs.get(chosenIndex.first);

                                // Set whether the chosen index is SHBtree or not.
                                isSHBtree = chosenIndex.second.getIndexType() == IndexType.STATIC_HILBERT_BTREE;

                                //For LOJ with GroupBy, prepare objects to reset LOJ nullPlaceHolderVariable in GroupByOp
                                if (isLeftOuterJoin && hasGroupBy) {
                                    analysisCtx.setLOJGroupbyOpRef(opRef);
                                    ScalarFunctionCallExpression isNullFuncExpr = AccessMethodUtils
                                            .findLOJIsNullFuncInGroupBy((GroupByOperator) opRef.getValue());
                                    analysisCtx.setLOJIsNullFuncInGroupBy(isNullFuncExpr);
                                }

                                boolean res = chosenIndex.first.applyJoinPlanTransformation(afterJoinRefs, joinRef,
                                        leftSubTree, rightSubTree, chosenIndex.second, analysisCtx, context,
                                        isLeftOuterJoin, hasGroupBy);

                                // If the plan transformation is successful, we don't need to traverse the plan any more,
                                // since if there are more JOIN operators, the next trigger on this plan will find them.
                                if (res) {
                                    return res;
                                }
                            } else {
                                context.addToDontApplySet(this, joinOp);
                            }

                        }

                    }
                }

            }

            joinRef = null;
            joinOp = null;
            afterJoinRefs.add(opRef);
        }

        // Clean the path above JOIN operator by removing the current operator
        afterJoinRefs.remove(opRef);

        return false;
    }

    /**
     * After the pattern is matched, check the condition and initialize the data sources from the both sub trees.
     */
    protected boolean checkJoinOperatorCondition() {
        // Check that the join's condition is a function call.
        ILogicalExpression condExpr = joinOp.getCondition().getValue();
        if (condExpr.getExpressionTag() != LogicalExpressionTag.FUNCTION_CALL) {
            return false;
        }
        joinCond = (AbstractFunctionCallExpression) condExpr;
        leftSubTree.initFromSubTree(joinOp.getInputs().get(0));
        rightSubTree.initFromSubTree(joinOp.getInputs().get(1));
        // One of the subtrees must have a datasource scan.
        if (leftSubTree.hasDataSourceScan() || rightSubTree.hasDataSourceScan()) {
            return true;
        }
        return false;
    }

    private void removeDuplicationForSHBtree(AbstractLogicalOperator topOp, Mutable<ILogicalOperator> op)
            throws AlgebricksException {
        try {
            if (isSHBtree) {
                //find the primary index unnest-map operator which is always the first unnest-map operator from the topOp.
                ILogicalOperator childOp = op.getValue();
                while (childOp.getOperatorTag() != LogicalOperatorTag.UNNEST_MAP) {
                    if (childOp.getInputs().size() == 0) {
                        throw new AlgebricksException("UNNEST_MAP operator wasn't found");
                    }
                    childOp = childOp.getInputs().get(0).getValue();
                }
                UnnestMapOperator unnestMapOp = (UnnestMapOperator) childOp;

                //use the PK variables produced by the unnest-map operator as a key of the distinctOperator
                List<LogicalVariable> pkVars = unnestMapOp.getVariables();
                ArrayList<Mutable<ILogicalExpression>> distinctExprList = new ArrayList<Mutable<ILogicalExpression>>();
                for (int j = 0; j < pkVars.size() - 1 /* ignore the record variable */; j++) {
                    distinctExprList.add(new MutableObject<ILogicalExpression>(new VariableReferenceExpression(pkVars
                            .get(j))));
                }

                //find the PK variables from the outer branch.
                ILogicalOperator dataSourceScanOrUnnestMapOp = childOp;
                LogicalOperatorTag opTag;
                int dataSourceScanOrUnnestMapOpCount = 0;
                while (childOp.getInputs().size() != 0) {
                    childOp = childOp.getInputs().get(0).getValue();
                    opTag = childOp.getOperatorTag();
                    if (opTag == LogicalOperatorTag.DATASOURCESCAN || opTag == LogicalOperatorTag.UNNEST_MAP) {
                        dataSourceScanOrUnnestMapOp = childOp;
                        ++dataSourceScanOrUnnestMapOpCount;
                        if (dataSourceScanOrUnnestMapOpCount == 2) {
                            // The count == 1 means that the childOp is the secondary index for the innner branch.
                            // In order to find outer branch's source operator, the count must be two. 
                            break;
                        }
                    }
                }

                if (dataSourceScanOrUnnestMapOpCount != 2) {
                    throw new AlgebricksException(
                            "Couldn't find outer branch's data-source operator which must be either DATASOURCESCAN or UNNEST_MAP operator");
                }

                opTag = dataSourceScanOrUnnestMapOp.getOperatorTag();
                if (opTag == LogicalOperatorTag.DATASOURCESCAN) {
                    DataSourceScanOperator dssOp = (DataSourceScanOperator) dataSourceScanOrUnnestMapOp;
                    pkVars = dssOp.getVariables();
                } else if (opTag == LogicalOperatorTag.UNNEST_MAP) {
                    unnestMapOp = (UnnestMapOperator) dataSourceScanOrUnnestMapOp;
                    pkVars = unnestMapOp.getVariables();
                } else {
                    throw new AlgebricksException("Unknown data-source operator tag [" + opTag + "] in join plan");
                }

                for (int j = 0; j < pkVars.size() - 1 /* ignore the record variable */; j++) {
                    distinctExprList.add(new MutableObject<ILogicalExpression>(new VariableReferenceExpression(pkVars
                            .get(j))));
                }

                DistinctOperator distinctOp = new DistinctOperator(distinctExprList);
                distinctOp.getInputs().add(op);
                context.computeAndSetTypeEnvironmentForOperator(distinctOp);
                topOp.getInputs().clear();
                topOp.getInputs().add(new MutableObject<ILogicalOperator>(distinctOp));
                context.computeAndSetTypeEnvironmentForOperator(topOp);
            }
        } finally {
            isSHBtree = false;
        }
    }

}