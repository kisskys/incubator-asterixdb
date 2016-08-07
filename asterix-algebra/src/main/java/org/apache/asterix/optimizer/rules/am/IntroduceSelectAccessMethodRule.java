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
import org.apache.hyracks.algebricks.core.algebra.expressions.VariableReferenceExpression;
import org.apache.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.AbstractLogicalOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.DistinctOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.LimitOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.OrderOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.OrderOperator.IOrder;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.OrderOperator.IOrder.OrderKind;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.SelectOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.UnnestMapOperator;
import org.apache.hyracks.algebricks.core.algebra.operators.logical.visitors.VariableUtilities;
import org.apache.hyracks.algebricks.core.algebra.util.OperatorPropertiesUtil;

/**
 * This rule optimizes simple selections with secondary or primary indexes. The use of an
 * index is expressed as an unnest-map over an index-search function which will be
 * replaced with the appropriate embodiment during codegen.
 * .
 * Matches the following operator patterns:
 * Standard secondary index pattern:
 * There must be at least one assign, but there may be more, e.g., when matching similarity-jaccard-check().
 * (select) <-- (assign | unnest)+ <-- (datasource scan)
 * Primary index lookup pattern:
 * Since no assign is necessary to get the primary key fields (they are already stored fields in the BTree tuples).
 * (select) <-- (datasource scan)
 * .
 * Replaces the above patterns with this plan if it is an index-only plan (only using PK and/or secondary key field):
 * OLD:(select) <-- (assign) <-- (btree search) <-- (sort) <-- (unnest-map(index search)) <-- (assign)
 * NEW: (union) <-- (select) <-- (assign)+ <-- (b-tree search) <-- (sort) <-- (split) <-- (unnest-map(index search)) <-- (assign)
 * .... (union) <-- ..................................................... <-- (split)
 * In an index-only plan, sort is not required.
 * .
 * If an index-only plan is not possible, the original plan will be transformed into this:
 * OLD:(select) <-- (assign | unnest)+ <-- (datasource scan)
 * NEW:(select) <-- (assign) <-- (btree search) <-- (sort) <-- (unnest-map(index search)) <-- (assign)
 * In this case, the sort is optional, and some access methods implementations may choose not to sort.
 * Note that for some index-based optimizations we do not remove the triggering
 * condition from the select, since the index may only acts as a filter, and the
 * final verification must still be done with the original select condition.
 * .
 * The basic outline of this rule is:
 * 1. Match operator pattern.
 * 2. Analyze select condition to see if there are optimizable functions (delegated to IAccessMethods).
 * 3. Check metadata to see if there are applicable indexes.
 * 4. Choose an index to apply (for now only a single index will be chosen).
 * 5. Rewrite plan using index (delegated to IAccessMethods).
 * .
 * Optionally, LIMIT can be applied early to the secondary index search to generate only certain amount of results
 * when an index-only plan or reducing the number of SELECT operations optimizations are possible.
 */
public class IntroduceSelectAccessMethodRule extends AbstractIntroduceAccessMethodRule {

    // Operators representing the patterns to be matched:
    // These ops are set in matchesPattern()
    protected List<Mutable<ILogicalOperator>> afterSelectRefs = null;
    protected Mutable<ILogicalOperator> selectRef = null;
    protected SelectOperator selectOp = null;
    protected AbstractFunctionCallExpression selectCond = null;
    protected final OptimizableOperatorSubTree subTree = new OptimizableOperatorSubTree();
    protected IOptimizationContext context = null;

    // Used to logically push-down LIMIT operator
    protected long limitNumberOfResult = -1;
    protected boolean canPushDownLimit = true;
    List<Pair<IOrder, Mutable<ILogicalExpression>>> orderByExpressions;
    protected boolean leftOuterJoinFound = false;
    protected boolean leftOuterJoinVisited = false;
    protected boolean appliedDistinctForSHBtree = false;
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

        afterSelectRefs = new ArrayList<Mutable<ILogicalOperator>>();
        boolean planTransformed = false;

        // Recursively check the plan whether the desired pattern exists in it. If so, try to optimize the plan.
        planTransformed = checkAndApplyTheRule(opRef, -1);

        if (selectOp != null) {
            context.addToDontApplySet(this, selectOp);
        }

        if (!planTransformed) {
            return false;
        } else {
            //            StringBuilder sb = new StringBuilder();
            //            LogicalOperatorPrettyPrintVisitor pvisitor = context.getPrettyPrintVisitor();
            //            PlanPrettyPrinter.printOperator((AbstractLogicalOperator) opRef.getValue(), sb, pvisitor, 0);
            //            System.out.println("\n" + sb.toString());
            OperatorPropertiesUtil.typeOpRec(opRef, context);
        }

        return planTransformed;
    }

    protected boolean checkSelectOperatorCondition() {
        // Set and analyze select.
        // Check that the SELECT condition is a function call.
        ILogicalExpression condExpr = selectOp.getCondition().getValue();
        if (condExpr.getExpressionTag() != LogicalExpressionTag.FUNCTION_CALL) {
            return false;
        }
        selectCond = (AbstractFunctionCallExpression) condExpr;

        // Match and put assign, un-nest, datasource information
        boolean res = subTree.initFromSubTree(selectOp.getInputs().get(0));
        return res && subTree.hasDataSourceScan();
    }

    // Recursively traverse the given plan and check whether SELECT operator exists.
    // If one is found, maintain the path from the root to SELECT operator if it is not already optimized.
    protected boolean checkAndApplyTheRule(Mutable<ILogicalOperator> opRef, int nthChild) throws AlgebricksException {
        AbstractLogicalOperator op = (AbstractLogicalOperator) opRef.getValue();
        boolean selectFoundAndOptimizationApplied = false;

        // Found SELECT operator
        if (op.getOperatorTag() == LogicalOperatorTag.SELECT) {
            selectRef = opRef;
            selectOp = (SelectOperator) op;

            // Already checked? If not, this operator can be optimized.
            if (!context.checkIfInDontApplySet(this, selectOp)) {
                Map<IAccessMethod, AccessMethodAnalysisContext> analyzedAMs = new HashMap<IAccessMethod, AccessMethodAnalysisContext>();

                // Check the condition of SELECT operator is a function call and initialize operator members.
                if (checkSelectOperatorCondition()) {

                    // Analyze the condition of SELECT operator.
                    if (analyzeCondition(selectCond, subTree.assignsAndUnnests, analyzedAMs)) {

                        // Set dataset and type metadata.
                        if (subTree.setDatasetAndTypeMetadata((AqlMetadataProvider) context.getMetadataProvider())) {

                            // Map variables to the applicable indexes.
                            fillSubTreeIndexExprs(subTree, analyzedAMs, context);

                            // Prune the access methods if there is no applicable index for them.
                            pruneIndexCandidates(analyzedAMs);

                            // Choose index to be applied.
                            Pair<IAccessMethod, Index> chosenIndex = chooseIndex(analyzedAMs);

                            // We can't apply any index for this SELECT operator
                            if (chosenIndex == null) {
                                context.addToDontApplySet(this, selectRef.getValue());
                            } else {

                                // Get the access method context for the chosen index.
                                AccessMethodAnalysisContext analysisCtx = analyzedAMs.get(chosenIndex.first);

                                // Set whether the chosen index is SHBtree or not.
                                isSHBtree = chosenIndex.second.getIndexType() == IndexType.STATIC_HILBERT_BTREE;

                                // Find the field name of each variable in the sub-tree - required when checking index-only plan.
                                fillFieldNamesInTheSubTree(subTree);

                                // There is a LIMIT operator in the plan and we can push down this to the secondary index search.
                                if (canPushDownLimit && limitNumberOfResult > -1) {

                                    int varOrder = 0;
                                    List<List<String>> chosenIndexFieldNames = chosenIndex.second.getKeyFieldNames();

                                    // Check whether the variables in ORDER operator matches the variables in the SELECT operator
                                    List<LogicalVariable> usedVarsInSelectOp = new ArrayList<LogicalVariable>();
                                    VariableUtilities.getUsedVariables(selectOp, usedVarsInSelectOp);

                                    for (Pair<IOrder, Mutable<ILogicalExpression>> orderPair : orderByExpressions) {
                                        // We don't yet support a complex ORDER BY clause to do the LIMIT push-down.
                                        // So, the ORDER BY expression should only include variables.
                                        if (orderPair.second.getValue().getExpressionTag() != LogicalExpressionTag.VARIABLE) {
                                            limitNumberOfResult = -1;
                                            canPushDownLimit = false;
                                            break;
                                        } else {
                                            VariableReferenceExpression varRef = (VariableReferenceExpression) orderPair.second
                                                    .getValue();
                                            LogicalVariable var = varRef.getVariableReference();

                                            // Try to match the attribute order in the ORDER BY to the attribute order in the index.
                                            int sIndexIdx = chosenIndexFieldNames.indexOf(subTree.fieldNames.get(var));
                                            if (sIndexIdx != varOrder || orderPair.first.getKind() != OrderKind.ASC) {
                                                // Either the attribute order doesn't match or
                                                // the attribute in the ORDER BY is not found on the index.
                                                // Also, for now, since we only support an ascending index, the ORDER BY should be ascending.
                                                canPushDownLimit = false;
                                                limitNumberOfResult = -1;
                                                break;
                                            } else {
                                                // Increase the index of the attribute in case of the composite indexes
                                                varOrder++;
                                            }
                                        }
                                    }

                                    if (canPushDownLimit) {
                                        analysisCtx.setLimitNumberOfResult(limitNumberOfResult);
                                        analysisCtx.setOrderByExpressions(orderByExpressions);
                                    }
                                }

                                // Try to apply plan transformation using chosen index.
                                boolean res = chosenIndex.first.applySelectPlanTransformation(afterSelectRefs,
                                        selectRef, subTree, chosenIndex.second, analysisCtx, context);

                                // If the plan transformation is successful, we don't need to traverse the plan any more,
                                // since if there are more SELECT operators, the next trigger on this plan will find them.
                                if (res) {
                                    return res;
                                }
                            }
                        }
                    }
                }
            }
            selectRef = null;
            selectOp = null;
            afterSelectRefs.add(opRef);
        } else {
            afterSelectRefs.add(opRef);

            // If there is a LEFT-OUTER-JOIN in the path, we can only push down the LIMIT to the first (left) branch.
            // If there is a JOIN or UNION in the path, we can't push down the LIMIT to the secondary index search.
            if ((leftOuterJoinFound && nthChild != 0) || op.getOperatorTag() == LogicalOperatorTag.INNERJOIN
                    || op.getOperatorTag() == LogicalOperatorTag.UNIONALL) {
                canPushDownLimit = false;
                limitNumberOfResult = -1;
            } else if (op.getOperatorTag() == LogicalOperatorTag.LEFTOUTERJOIN) {
                leftOuterJoinFound = true;
            } else if (op.getOperatorTag() == LogicalOperatorTag.LIMIT && canPushDownLimit) {
                // Keep the limit number of Result
                LimitOperator limitOp = (LimitOperator) op;
                if (limitOp.getMaxObjects().getValue().getExpressionTag() == LogicalExpressionTag.CONSTANT) {
                    limitNumberOfResult = AccessMethodUtils.getInt64Constant(limitOp.getMaxObjects());
                }
            } else if (op.getOperatorTag() == LogicalOperatorTag.ORDER && canPushDownLimit) {
                // Check the order by property
                OrderOperator orderOp = (OrderOperator) op;
                orderByExpressions = orderOp.getOrderExpressions();
            }
        }

        // Recursively check the plan and try to optimize it.
        for (int i = 0; i < op.getInputs().size(); i++) {
            selectFoundAndOptimizationApplied = checkAndApplyTheRule(op.getInputs().get(i), i);
            if (selectFoundAndOptimizationApplied) {
                removeDuplicationForSHBtree(op, op.getInputs().get(i));
                return true;
            }
        }

        // Clean the path above SELECT operator by removing the current operator
        afterSelectRefs.remove(opRef);

        // If we reach here, that means there is a left outer join and the optimization was not possible.
        // For the second branch, there should not be any LIMIT push-down.
        if (op.getOperatorTag() == LogicalOperatorTag.LEFTOUTERJOIN && !leftOuterJoinVisited) {
            leftOuterJoinVisited = true;
        } else if (leftOuterJoinVisited) {
            leftOuterJoinVisited = false;
        }

        return false;
    }

    @Override
    public Map<FunctionIdentifier, List<IAccessMethod>> getAccessMethods() {
        return accessMethods;
    }

    private void clear() {
        afterSelectRefs = null;
        selectRef = null;
        selectOp = null;
        selectCond = null;
        context = null;
        limitNumberOfResult = -1;
        canPushDownLimit = true;
        orderByExpressions = null;
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