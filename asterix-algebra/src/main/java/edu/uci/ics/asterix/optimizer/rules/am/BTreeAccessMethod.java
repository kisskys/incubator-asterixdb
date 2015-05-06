/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.asterix.optimizer.rules.am;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import edu.uci.ics.asterix.aql.util.FunctionUtils;
import edu.uci.ics.asterix.common.annotations.SkipSecondaryIndexSearchExpressionAnnotation;
import edu.uci.ics.asterix.common.config.DatasetConfig.DatasetType;
import edu.uci.ics.asterix.common.config.DatasetConfig.IndexType;
import edu.uci.ics.asterix.metadata.declared.AqlIndex;
import edu.uci.ics.asterix.metadata.declared.AqlMetadataProvider;
import edu.uci.ics.asterix.metadata.entities.Dataset;
import edu.uci.ics.asterix.metadata.entities.Index;
import edu.uci.ics.asterix.om.base.AInt32;
import edu.uci.ics.asterix.om.constants.AsterixConstantValue;
import edu.uci.ics.asterix.om.functions.AsterixBuiltinFunctions;
import edu.uci.ics.asterix.om.types.ARecordType;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.asterix.om.types.IAType;
import edu.uci.ics.asterix.om.util.NonTaggedFormatUtil;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.common.utils.Pair;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.ILogicalExpression;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.ILogicalOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.IOptimizationContext;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.LogicalExpressionTag;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.LogicalVariable;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.AbstractFunctionCallExpression;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.ConstantExpression;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.IndexedNLJoinExpressionAnnotation;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.ScalarFunctionCallExpression;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.VariableReferenceExpression;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.AlgebricksBuiltinFunctions;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.AlgebricksBuiltinFunctions.ComparisonKind;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.IFunctionInfo;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.AbstractBinaryJoinOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.AbstractLogicalOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.AbstractLogicalOperator.ExecutionMode;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.AssignOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.DataSourceScanOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.ExternalDataLookupOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.SelectOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.TokenizeOperator;
import edu.uci.ics.hyracks.algebricks.core.algebra.operators.logical.UnnestMapOperator;

/**
 * Class for helping rewrite rules to choose and apply BTree indexes.
 */
public class BTreeAccessMethod implements IAccessMethod {

    // Describes whether a search predicate is an open/closed interval.
    private enum LimitType {
        LOW_INCLUSIVE,
        LOW_EXCLUSIVE,
        HIGH_INCLUSIVE,
        HIGH_EXCLUSIVE,
        EQUAL
    }

    // TODO: There is some redundancy here, since these are listed in AlgebricksBuiltinFunctions as well.
    private static List<FunctionIdentifier> funcIdents = new ArrayList<FunctionIdentifier>();
    static {
        funcIdents.add(AlgebricksBuiltinFunctions.EQ);
        funcIdents.add(AlgebricksBuiltinFunctions.LE);
        funcIdents.add(AlgebricksBuiltinFunctions.GE);
        funcIdents.add(AlgebricksBuiltinFunctions.LT);
        funcIdents.add(AlgebricksBuiltinFunctions.GT);
        funcIdents.add(AsterixBuiltinFunctions.SPATIAL_INTERSECT);
    }

    public static BTreeAccessMethod INSTANCE = new BTreeAccessMethod();

    @Override
    public List<FunctionIdentifier> getOptimizableFunctions() {
        return funcIdents;
    }

    @Override
    public boolean analyzeFuncExprArgs(AbstractFunctionCallExpression funcExpr,
            List<AbstractLogicalOperator> assignsAndUnnests, AccessMethodAnalysisContext analysisCtx) {
        boolean matches = AccessMethodUtils.analyzeFuncExprArgsForOneConstAndVar(funcExpr, analysisCtx);
        if (!matches) {
            matches = AccessMethodUtils.analyzeFuncExprArgsForTwoVars(funcExpr, analysisCtx);
        }
        return matches;
    }

    @Override
    public boolean matchAllIndexExprs() {
        return false;
    }

    @Override
    public boolean matchPrefixIndexExprs() {
        return true;
    }

    @Override
    public boolean applySelectPlanTransformation(Mutable<ILogicalOperator> selectRef,
            OptimizableOperatorSubTree subTree, Index chosenIndex, AccessMethodAnalysisContext analysisCtx,
            IOptimizationContext context) throws AlgebricksException {
        SelectOperator select = (SelectOperator) selectRef.getValue();
        Mutable<ILogicalExpression> conditionRef = select.getCondition();
        ILogicalOperator primaryIndexUnnestOp = createSecondaryToPrimaryPlan(selectRef, conditionRef, subTree, null,
                chosenIndex, analysisCtx, false, false, false, context);
        if (primaryIndexUnnestOp == null) {
            return false;
        }
        Mutable<ILogicalOperator> opRef = (subTree.assignsAndUnnestsRefs.isEmpty()) ? null
                : subTree.assignsAndUnnestsRefs.get(0);
        ILogicalOperator op = null;
        if (opRef != null) {
            op = opRef.getValue();
        }
        // Generate new select using the new condition.
        if (conditionRef.getValue() != null) {
            select.getInputs().clear();
            if (op != null) {
                subTree.dataSourceRef.setValue(primaryIndexUnnestOp);
                select.getInputs().add(new MutableObject<ILogicalOperator>(op));
            } else {
                select.getInputs().add(new MutableObject<ILogicalOperator>(primaryIndexUnnestOp));
            }
        } else {
            ((AbstractLogicalOperator) primaryIndexUnnestOp).setExecutionMode(ExecutionMode.PARTITIONED);
            if (op != null) {
                subTree.dataSourceRef.setValue(primaryIndexUnnestOp);
                selectRef.setValue(op);
            } else {
                selectRef.setValue(primaryIndexUnnestOp);
            }
        }
        return true;
    }

    @Override
    public boolean applyJoinPlanTransformation(Mutable<ILogicalOperator> joinRef,
            OptimizableOperatorSubTree leftSubTree, OptimizableOperatorSubTree rightSubTree, Index chosenIndex,
            AccessMethodAnalysisContext analysisCtx, IOptimizationContext context, boolean isLeftOuterJoin)
            throws AlgebricksException {
        AbstractBinaryJoinOperator joinOp = (AbstractBinaryJoinOperator) joinRef.getValue();
        Mutable<ILogicalExpression> conditionRef = joinOp.getCondition();
        // Determine if the index is applicable on the left or right side (if both, we arbitrarily prefer the left side).
        Dataset dataset = analysisCtx.indexDatasetMap.get(chosenIndex);
        // Determine probe and index subtrees based on chosen index.
        OptimizableOperatorSubTree indexSubTree = null;
        OptimizableOperatorSubTree probeSubTree = null;
        if (!isLeftOuterJoin && leftSubTree.hasDataSourceScan()
                && dataset.getDatasetName().equals(leftSubTree.dataset.getDatasetName())) {
            indexSubTree = leftSubTree;
            probeSubTree = rightSubTree;
        } else if (rightSubTree.hasDataSourceScan()
                && dataset.getDatasetName().equals(rightSubTree.dataset.getDatasetName())) {
            indexSubTree = rightSubTree;
            probeSubTree = leftSubTree;
        }
        if (indexSubTree == null) {
            //This may happen for left outer join case
            return false;
        }

        LogicalVariable newNullPlaceHolderVar = null;
        if (isLeftOuterJoin) {
            //get a new null place holder variable that is the first field variable of the primary key 
            //from the indexSubTree's datasourceScanOp
            newNullPlaceHolderVar = indexSubTree.getDataSourceVariables().get(0);
        }

        ILogicalOperator primaryIndexUnnestOp = createSecondaryToPrimaryPlan(joinRef, conditionRef, indexSubTree,
                probeSubTree, chosenIndex, analysisCtx, true, isLeftOuterJoin, true, context);
        if (primaryIndexUnnestOp == null) {
            return false;
        }

        if (isLeftOuterJoin) {
            //reset the null place holder variable
            AccessMethodUtils.resetLOJNullPlaceholderVariableInGroupByOp(analysisCtx, newNullPlaceHolderVar, context);
        }

        // If there are conditions left, add a new select operator on top.
        indexSubTree.dataSourceRef.setValue(primaryIndexUnnestOp);
        if (conditionRef.getValue() != null) {
            SelectOperator topSelect = new SelectOperator(conditionRef, isLeftOuterJoin, newNullPlaceHolderVar);
            topSelect.getInputs().add(indexSubTree.rootRef);
            topSelect.setExecutionMode(ExecutionMode.LOCAL);
            context.computeAndSetTypeEnvironmentForOperator(topSelect);
            // Replace the original join with the new subtree rooted at the select op.
            joinRef.setValue(topSelect);
        } else {
            joinRef.setValue(indexSubTree.rootRef.getValue());
        }
        return true;
    }

    private ILogicalOperator createSecondaryToPrimaryPlan(Mutable<ILogicalOperator> topOpRef,
            Mutable<ILogicalExpression> conditionRef, OptimizableOperatorSubTree indexSubTree,
            OptimizableOperatorSubTree probeSubTree, Index chosenIndex, AccessMethodAnalysisContext analysisCtx,
            boolean retainInput, boolean retainNull, boolean requiresBroadcast, IOptimizationContext context)
            throws AlgebricksException {
        Dataset dataset = indexSubTree.dataset;
        ARecordType recordType = indexSubTree.recordType;
        // we made sure indexSubTree has datasource scan
        DataSourceScanOperator dataSourceScan = (DataSourceScanOperator) indexSubTree.dataSourceRef.getValue();
        List<Integer> exprList = analysisCtx.indexExprs.get(chosenIndex);
        List<IOptimizableFuncExpr> matchedFuncExprs = analysisCtx.matchedFuncExprs;
        int numSecondaryKeys = analysisCtx.indexNumMatchedKeys.get(chosenIndex);
        // List of function expressions that will be replaced by the secondary-index search.
        // These func exprs will be removed from the select condition at the very end of this method.
        Set<ILogicalExpression> replacedFuncExprs = new HashSet<ILogicalExpression>();

        // Info on high and low keys for the BTree search predicate.
        ILogicalExpression[] lowKeyExprs = new ILogicalExpression[numSecondaryKeys];
        ILogicalExpression[] highKeyExprs = new ILogicalExpression[numSecondaryKeys];
        LimitType[] lowKeyLimits = new LimitType[numSecondaryKeys];
        LimitType[] highKeyLimits = new LimitType[numSecondaryKeys];
        boolean[] lowKeyInclusive = new boolean[numSecondaryKeys];
        boolean[] highKeyInclusive = new boolean[numSecondaryKeys];

        // TODO: For now we don't do any sophisticated analysis of the func exprs to come up with "the best" range predicate.
        // If we can't figure out how to integrate a certain funcExpr into the current predicate, we just bail by setting this flag.
        boolean couldntFigureOut = false;
        boolean doneWithExprs = false;
        boolean isEqCondition = false;
        // TODO: For now don't consider prefix searches.
        BitSet setLowKeys = new BitSet(numSecondaryKeys);
        BitSet setHighKeys = new BitSet(numSecondaryKeys);

        //flag for using Hilbert btree
        boolean useLinearizerBTree = false;

        // Go through the func exprs listed as optimizable by the chosen index, 
        // and formulate a range predicate on the secondary-index keys.
        ILogicalExpression searchKeyExpr = null;
        for (Integer exprIndex : exprList) {
            // Position of the field of matchedFuncExprs.get(exprIndex) in the chosen index's indexed exprs.
            IOptimizableFuncExpr optFuncExpr = matchedFuncExprs.get(exprIndex);
            int keyPos = indexOf(optFuncExpr.getFieldName(0), chosenIndex.getKeyFieldNames());
            if (keyPos < 0) {
                if (optFuncExpr.getNumLogicalVars() > 1) {
                    // If we are optimizing a join, the matching field may be the second field name.
                    keyPos = indexOf(optFuncExpr.getFieldName(1), chosenIndex.getKeyFieldNames());
                }
            }
            if (keyPos < 0) {
                throw new AlgebricksException(
                        "Could not match optimizable function expression to any index field name.");
            }

            LimitType limit;
            searchKeyExpr = AccessMethodUtils.createSearchKeyExpr(optFuncExpr, indexSubTree, probeSubTree);

            //Get the key field type from the optFuncExpr
            Pair<IAType, Boolean> keyPairType = null;
            for (String fieldName : ((OptimizableFuncExpr) optFuncExpr).getFieldNames()) {
                try {
                    if (fieldName != null && recordType.getFieldType(fieldName) != null) {
                        keyPairType = Index.getNonNullableKeyFieldType(fieldName, recordType);
                        break;
                    }
                } catch (IOException e) {
                    throw new AlgebricksException(e);
                }
            }
            if (keyPairType == null) {
                return null;
            }
            IAType keyType = keyPairType.first;

            //TODO extend HilbertBTree to support composite key.
            IndexType indexType = chosenIndex.getIndexType();
            if (indexType == IndexType.DYNAMIC_HILBERT_BTREE || indexType == IndexType.DYNAMIC_HILBERTVALUE_BTREE
                    || indexType == IndexType.STATIC_HILBERT_BTREE) {
                useLinearizerBTree = true;
                if (indexType == IndexType.DYNAMIC_HILBERT_BTREE || indexType == IndexType.DYNAMIC_HILBERTVALUE_BTREE) {
                    limit = LimitType.LOW_INCLUSIVE;
                } else { //STATIC_HILBERT_BTREE
                    limit = LimitType.EQUAL;
                    lowKeyLimits[keyPos] = LimitType.LOW_INCLUSIVE;
                    lowKeyExprs[keyPos] = searchKeyExpr;
                    lowKeyInclusive[keyPos] = true;
                    highKeyLimits[keyPos] = LimitType.HIGH_INCLUSIVE;
                    highKeyExprs[keyPos] = searchKeyExpr;
                    highKeyInclusive[keyPos] = true;
                    couldntFigureOut = false;
                    isEqCondition = false;
                    replacedFuncExprs.add(matchedFuncExprs.get(exprIndex).getFuncExpr());
                    break;
                }
            } else {
                limit = getLimitType(optFuncExpr, probeSubTree);
            }

            switch (limit) {
                case EQUAL: {
                    if (lowKeyLimits[keyPos] == null && highKeyLimits[keyPos] == null) {
                        lowKeyLimits[keyPos] = highKeyLimits[keyPos] = limit;
                        lowKeyInclusive[keyPos] = highKeyInclusive[keyPos] = true;
                        lowKeyExprs[keyPos] = highKeyExprs[keyPos] = searchKeyExpr;
                        setLowKeys.set(keyPos);
                        setHighKeys.set(keyPos);
                        isEqCondition = true;
                    } else {
                        // Has already been set to the identical values. When optimizing join we may encounter the same optimizable expression twice
                        // (once from analyzing each side of the join)
                        if (lowKeyLimits[keyPos] == limit && lowKeyInclusive[keyPos] == true
                                && lowKeyExprs[keyPos].equals(searchKeyExpr) && highKeyLimits[keyPos] == limit
                                && highKeyInclusive[keyPos] == true && highKeyExprs[keyPos].equals(searchKeyExpr)) {
                            isEqCondition = true;
                            break;
                        }
                        couldntFigureOut = true;
                    }
                    // TODO: For now don't consider prefix searches.
                    // If high and low keys are set, we exit for now.
                    if (setLowKeys.cardinality() == numSecondaryKeys && setHighKeys.cardinality() == numSecondaryKeys) {
                        doneWithExprs = true;
                    }
                    break;
                }
                case HIGH_EXCLUSIVE: {
                    if (highKeyLimits[keyPos] == null || (highKeyLimits[keyPos] != null && highKeyInclusive[keyPos])) {
                        highKeyLimits[keyPos] = limit;
                        highKeyExprs[keyPos] = searchKeyExpr;
                        highKeyInclusive[keyPos] = false;
                    } else {
                        // Has already been set to the identical values. When optimizing join we may encounter the same optimizable expression twice
                        // (once from analyzing each side of the join)
                        if (highKeyLimits[keyPos] == limit && highKeyInclusive[keyPos] == false
                                && highKeyExprs[keyPos].equals(searchKeyExpr)) {
                            break;
                        }
                        couldntFigureOut = true;
                        doneWithExprs = true;
                    }
                    break;
                }
                case HIGH_INCLUSIVE: {
                    if (highKeyLimits[keyPos] == null) {
                        highKeyLimits[keyPos] = limit;
                        highKeyExprs[keyPos] = searchKeyExpr;
                        highKeyInclusive[keyPos] = true;
                    } else {
                        // Has already been set to the identical values. When optimizing join we may encounter the same optimizable expression twice
                        // (once from analyzing each side of the join)
                        if (highKeyLimits[keyPos] == limit && highKeyInclusive[keyPos] == true
                                && highKeyExprs[keyPos].equals(searchKeyExpr)) {
                            break;
                        }
                        couldntFigureOut = true;
                        doneWithExprs = true;
                    }
                    break;
                }
                case LOW_EXCLUSIVE: {
                    if (lowKeyLimits[keyPos] == null || (lowKeyLimits[keyPos] != null && lowKeyInclusive[keyPos])) {
                        lowKeyLimits[keyPos] = limit;
                        lowKeyExprs[keyPos] = searchKeyExpr;
                        lowKeyInclusive[keyPos] = false;
                    } else {
                        // Has already been set to the identical values. When optimizing join we may encounter the same optimizable expression twice
                        // (once from analyzing each side of the join)
                        if (lowKeyLimits[keyPos] == limit && lowKeyInclusive[keyPos] == false
                                && lowKeyExprs[keyPos].equals(searchKeyExpr)) {
                            break;
                        }
                        couldntFigureOut = true;
                        doneWithExprs = true;
                    }
                    break;
                }
                case LOW_INCLUSIVE: {
                    if (lowKeyLimits[keyPos] == null) {
                        lowKeyLimits[keyPos] = limit;
                        lowKeyExprs[keyPos] = searchKeyExpr;
                        lowKeyInclusive[keyPos] = true;
                    } else {
                        // Has already been set to the identical values. When optimizing join we may encounter the same optimizable expression twice
                        // (once from analyzing each side of the join)
                        if (lowKeyLimits[keyPos] == limit && lowKeyInclusive[keyPos] == true
                                && lowKeyExprs[keyPos].equals(searchKeyExpr)) {
                            break;
                        }
                        couldntFigureOut = true;
                        doneWithExprs = true;
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            if (!couldntFigureOut) {
                // Remember to remove this funcExpr later.
                replacedFuncExprs.add(matchedFuncExprs.get(exprIndex).getFuncExpr());
            }
            if (doneWithExprs) {
                break;
            }
        }
        if (couldntFigureOut) {
            return null;
        }

        // If the select condition contains mixed open/closed intervals on multiple keys, then we make all intervals closed to obtain a superset of answers and leave the original selection in place.
        boolean primaryIndexPostProccessingIsNeeded = false;
        for (int i = 1; i < numSecondaryKeys; ++i) {
            if (lowKeyInclusive[i] != lowKeyInclusive[0]) {
                Arrays.fill(lowKeyInclusive, true);
                primaryIndexPostProccessingIsNeeded = true;
                break;
            }
        }
        for (int i = 1; i < numSecondaryKeys; ++i) {
            if (highKeyInclusive[i] != highKeyInclusive[0]) {
                Arrays.fill(highKeyInclusive, true);
                primaryIndexPostProccessingIsNeeded = true;
                break;
            }
        }

        // Rule out the cases unsupported by the current btree search
        // implementation.
        for (int i = 1; i < numSecondaryKeys; i++) {
            if (lowKeyLimits[0] == null && lowKeyLimits[i] != null || lowKeyLimits[0] != null
                    && lowKeyLimits[i] == null) {
                return null;
            }
            if (highKeyLimits[0] == null && highKeyLimits[i] != null || highKeyLimits[0] != null
                    && highKeyLimits[i] == null) {
                return null;
            }
        }
        if (lowKeyLimits[0] == null) {
            lowKeyInclusive[0] = true;
        }
        if (highKeyLimits[0] == null) {
            highKeyInclusive[0] = true;
        }

        ILogicalOperator inputOp = null;
        IndexType chosenIndexType = chosenIndex.getIndexType();
        BTreeJobGenParams jobGenParams = new BTreeJobGenParams(chosenIndex.getIndexName(), chosenIndexType,
                dataset.getDataverseName(), dataset.getDatasetName(), retainInput, retainNull, requiresBroadcast);
        if (useLinearizerBTree) {
            //In order to use the linearizer btree, the input to the btree index need to form a rectangle which minimally covers a given query region.
            //The process is as follows:
            //step 1. Using CREATE_MBR function, create an MBR which covers a given query region which could be any two dimensional spatial type.
            //   The function generates four doubles, the first two represents bottom left point of the MBR and the last two represents top right point. 
            //step 2. Using CREATE_POINT function, create two points using the four points from the MBR function. 
            //step 3. Using CREATE_RECTANGLE function, create a rectangle using two points from the step 2.

            LogicalVariable keyVar = null;
            int numDimensions = NonTaggedFormatUtil.getNumDimensions(ATypeTag.POINT);
            int numMBRs = numDimensions * 2;
            // List of variables for the assign.
            ArrayList<LogicalVariable> assignKeyVarList = new ArrayList<LogicalVariable>();
            // List of expressions for the assign.
            ArrayList<Mutable<ILogicalExpression>> assignKeyExprList = new ArrayList<Mutable<ILogicalExpression>>();

            //step 1.
            for (int i = 0; i < numMBRs; i++) {
                // The create MBR function "extracts" one field of an MBR around the given spatial object.
                AbstractFunctionCallExpression createMBR = new ScalarFunctionCallExpression(
                        FunctionUtils.getFunctionInfo(AsterixBuiltinFunctions.CREATE_MBR));
                // Spatial object is the constant from the func expr we are optimizing.
                createMBR.getArguments().add(new MutableObject<ILogicalExpression>(searchKeyExpr));
                // The number of dimensions.
                createMBR.getArguments().add(
                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                                new AInt32(numDimensions)))));
                // Which part of the MBR to extract.
                createMBR.getArguments().add(
                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                                new AInt32(i)))));
                // Add a variable and its expr to the lists which will be passed into an assign op.
                keyVar = context.newVar();
                assignKeyVarList.add(keyVar);
                assignKeyExprList.add(new MutableObject<ILogicalExpression>(createMBR));
            }
            AssignOperator assignConstantSearchKeys = new AssignOperator(assignKeyVarList, assignKeyExprList);

            //step 2.
            ArrayList<LogicalVariable> assignOpPointKeyVarList = new ArrayList<LogicalVariable>();
            ArrayList<Mutable<ILogicalExpression>> assignOpPointKeyExprList = new ArrayList<Mutable<ILogicalExpression>>();
            AbstractFunctionCallExpression createPoint1 = new ScalarFunctionCallExpression(
                    FunctionUtils.getFunctionInfo(AsterixBuiltinFunctions.CREATE_POINT));
            createPoint1.getArguments().add(
                    new MutableObject<ILogicalExpression>(new VariableReferenceExpression(assignKeyVarList.get(0))));
            createPoint1.getArguments().add(
                    new MutableObject<ILogicalExpression>(new VariableReferenceExpression(assignKeyVarList.get(1))));
            assignOpPointKeyVarList.add(context.newVar());
            assignOpPointKeyExprList.add(new MutableObject<ILogicalExpression>(createPoint1));
            AbstractFunctionCallExpression createPoint2 = new ScalarFunctionCallExpression(
                    FunctionUtils.getFunctionInfo(AsterixBuiltinFunctions.CREATE_POINT));
            createPoint2.getArguments().add(
                    new MutableObject<ILogicalExpression>(new VariableReferenceExpression(assignKeyVarList.get(2))));
            createPoint2.getArguments().add(
                    new MutableObject<ILogicalExpression>(new VariableReferenceExpression(assignKeyVarList.get(3))));
            assignOpPointKeyVarList.add(context.newVar());
            assignOpPointKeyExprList.add(new MutableObject<ILogicalExpression>(createPoint2));
            AssignOperator assignOpPoints = new AssignOperator(assignOpPointKeyVarList, assignOpPointKeyExprList);

            //step 3.
            ArrayList<LogicalVariable> assignOpRectangleKeyVarList = new ArrayList<LogicalVariable>();
            ArrayList<Mutable<ILogicalExpression>> assignOpRectangleKeyExprList = new ArrayList<Mutable<ILogicalExpression>>();
            AbstractFunctionCallExpression createRectangle = new ScalarFunctionCallExpression(
                    FunctionUtils.getFunctionInfo(AsterixBuiltinFunctions.CREATE_RECTANGLE));
            createRectangle.getArguments().add(
                    new MutableObject<ILogicalExpression>(new VariableReferenceExpression(assignOpPointKeyVarList
                            .get(0))));
            createRectangle.getArguments().add(
                    new MutableObject<ILogicalExpression>(new VariableReferenceExpression(assignOpPointKeyVarList
                            .get(1))));
            assignOpRectangleKeyVarList.add(context.newVar());
            assignOpRectangleKeyExprList.add(new MutableObject<ILogicalExpression>(createRectangle));
            AssignOperator assignOpRectangle = new AssignOperator(assignOpRectangleKeyVarList,
                    assignOpRectangleKeyExprList);

            if (probeSubTree == null) {
                // We are optimizing a selection query.
                // Input to this assign is the EmptyTupleSource (which the dataSourceScan also must have had as input).
                assignConstantSearchKeys.getInputs().add(dataSourceScan.getInputs().get(0));
                assignConstantSearchKeys.setExecutionMode(dataSourceScan.getExecutionMode());
            } else {
                // We are optimizing a join, place the assign op top of the probe subtree.
                assignConstantSearchKeys.getInputs().add(probeSubTree.rootRef);
            }
            assignOpPoints.getInputs().add(new MutableObject<ILogicalOperator>(assignConstantSearchKeys));
            assignOpRectangle.getInputs().add(new MutableObject<ILogicalOperator>(assignOpPoints));

            if (chosenIndexType == IndexType.DYNAMIC_HILBERT_BTREE
                    || chosenIndexType == IndexType.DYNAMIC_HILBERTVALUE_BTREE) {
                inputOp = assignOpRectangle;
                jobGenParams.setLowKeyInclusive(lowKeyInclusive[0]);
                jobGenParams.setHighKeyInclusive(highKeyInclusive[0]);
                jobGenParams.setIsEqCondition(isEqCondition);
                jobGenParams.setLowKeyVarList(assignOpRectangleKeyVarList, 0, 1);
                jobGenParams.setHighKeyVarList(assignOpRectangleKeyVarList, 1, 0);
            } else {
                ///////////////////////
                //add spatial-cell-tokens function
                //                ArrayList<LogicalVariable> assignSCTKeyVarList = new ArrayList<LogicalVariable>();
                //                ArrayList<Mutable<ILogicalExpression>> assignSCTKeyExprList = new ArrayList<Mutable<ILogicalExpression>>();
                //                AbstractFunctionCallExpression scTokens = new ScalarFunctionCallExpression(
                //                        FunctionUtils.getFunctionInfo(AsterixBuiltinFunctions.SPATIAL_CELL_TOKENS));
                //                IndexTypeProperty itp = chosenIndex.getIndexTypeProperty();
                //                scTokens.getArguments().add(
                //                        new MutableObject<ILogicalExpression>(new VariableReferenceExpression(
                //                                assignOpRectangleKeyVarList.get(0))));
                //                scTokens.getArguments().add(
                //                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                //                                new ADouble(itp.bottomLeftX)))));
                //                scTokens.getArguments().add(
                //                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                //                                new ADouble(itp.bottomLeftY)))));
                //                scTokens.getArguments().add(
                //                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                //                                new ADouble(itp.topRightX)))));
                //                scTokens.getArguments().add(
                //                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                //                                new ADouble(itp.topRightY)))));
                //                for (int i = 0; i < CellBasedSpatialIndex.MAX_LEVEL.getValue(); i++) {
                //                    scTokens.getArguments().add(
                //                            new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                //                                    new AInt16(itp.levelDensity[i])))));
                //                }
                //                scTokens.getArguments().add(
                //                        new MutableObject<ILogicalExpression>(new ConstantExpression(new AsterixConstantValue(
                //                                new AInt32(itp.cellsPerObject)))));
                //                assignSCTKeyVarList.add(context.newVar());
                //                assignSCTKeyExprList.add(new MutableObject<ILogicalExpression>(scTokens));
                //                AssignOperator assignOpSCTokens = new AssignOperator(assignSCTKeyVarList, assignSCTKeyExprList);
                //                assignOpSCTokens.getInputs().add(new MutableObject<ILogicalOperator>(assignOpRectangle));
                //                assignOpSCTokens.setExecutionMode(dataSourceScan.getExecutionMode());
                //
                //                inputOp = assignOpSCTokens;
                //                jobGenParams.setLowKeyInclusive(lowKeyInclusive[0]);
                //                jobGenParams.setHighKeyInclusive(highKeyInclusive[0]);
                //                jobGenParams.setIsEqCondition(isEqCondition);
                //                jobGenParams.setLowKeyVarList(assignSCTKeyVarList, 0, 1);
                //                jobGenParams.setHighKeyVarList(assignSCTKeyVarList, 1, 0);
                ///////////////////////

                ///////////////////////
                // add tokenizer

                // Create a new logical variable - token
                List<LogicalVariable> tokenizeKeyVars = new ArrayList<LogicalVariable>();
                List<Mutable<ILogicalExpression>> tokenizeKeyExprs = new ArrayList<Mutable<ILogicalExpression>>();
                List<Object> varTypes = new ArrayList<Object>();

                //low key token
                LogicalVariable tokenVar = context.newVar();
                tokenizeKeyVars.add(tokenVar);
                tokenizeKeyExprs.add(new MutableObject<ILogicalExpression>(new VariableReferenceExpression(tokenVar)));
                varTypes.add(BuiltinType.ABINARY); // The secondary key field type of static hilbert btree is always ABinary. 

                //high key token
                tokenVar = context.newVar();
                tokenizeKeyVars.add(tokenVar);
                tokenizeKeyExprs.add(new MutableObject<ILogicalExpression>(new VariableReferenceExpression(tokenVar)));
                varTypes.add(BuiltinType.ABINARY);

                // TokenizeOperator to tokenize SK
                AqlIndex dataSourceIndex = new AqlIndex(chosenIndex, chosenIndex.getDataverseName(),
                        chosenIndex.getDatasetName(), (AqlMetadataProvider) context.getMetadataProvider());
                List<Mutable<ILogicalExpression>> primaryExpressions = new ArrayList<Mutable<ILogicalExpression>>();
                List<Mutable<ILogicalExpression>> secondaryExpressions = new ArrayList<Mutable<ILogicalExpression>>();
                for (LogicalVariable secondaryKeyVar : assignOpRectangle.getVariables()) {
                    secondaryExpressions.add(new MutableObject<ILogicalExpression>(new VariableReferenceExpression(
                            secondaryKeyVar)));
                }
                TokenizeOperator tokenizeOp = new TokenizeOperator(dataSourceIndex, primaryExpressions,
                        secondaryExpressions, tokenizeKeyVars, null, null, false, false, varTypes, true);
                tokenizeOp.getInputs().add(new MutableObject<ILogicalOperator>(assignOpRectangle));
                context.computeAndSetTypeEnvironmentForOperator(tokenizeOp);

                inputOp = tokenizeOp;
                jobGenParams.setLowKeyInclusive(lowKeyInclusive[0]);
                jobGenParams.setHighKeyInclusive(highKeyInclusive[0]);
                jobGenParams.setIsEqCondition(isEqCondition);
                jobGenParams.setLowKeyVarList(tokenizeKeyVars, 0, 1);
                jobGenParams.setHighKeyVarList(tokenizeKeyVars, 1, 1);
            }
        } else {
            // Here we generate vars and funcs for assigning the secondary-index keys to be fed into the secondary-index search.
            // List of variables for the assign.
            ArrayList<LogicalVariable> keyVarList = new ArrayList<LogicalVariable>();
            // List of variables and expressions for the assign.
            ArrayList<LogicalVariable> assignKeyVarList = new ArrayList<LogicalVariable>();
            ArrayList<Mutable<ILogicalExpression>> assignKeyExprList = new ArrayList<Mutable<ILogicalExpression>>();
            int numLowKeys = createKeyVarsAndExprs(lowKeyLimits, lowKeyExprs, assignKeyVarList, assignKeyExprList,
                    keyVarList, context);
            int numHighKeys = createKeyVarsAndExprs(highKeyLimits, highKeyExprs, assignKeyVarList, assignKeyExprList,
                    keyVarList, context);

            jobGenParams.setLowKeyInclusive(lowKeyInclusive[0]);
            jobGenParams.setHighKeyInclusive(highKeyInclusive[0]);
            jobGenParams.setIsEqCondition(isEqCondition);
            jobGenParams.setLowKeyVarList(keyVarList, 0, numLowKeys);
            jobGenParams.setHighKeyVarList(keyVarList, numLowKeys, numHighKeys);

            if (!assignKeyVarList.isEmpty()) {
                // Assign operator that sets the constant secondary-index search-key fields if necessary.
                AssignOperator assignConstantSearchKeys = new AssignOperator(assignKeyVarList, assignKeyExprList);
                // Input to this assign is the EmptyTupleSource (which the dataSourceScan also must have had as input).
                assignConstantSearchKeys.getInputs().add(dataSourceScan.getInputs().get(0));
                assignConstantSearchKeys.setExecutionMode(dataSourceScan.getExecutionMode());
                inputOp = assignConstantSearchKeys;
            } else {
                // All index search keys are variables.
                inputOp = probeSubTree.root;
            }
        }

        //TODO return primary key without secondary key if possible
        UnnestMapOperator secondaryIndexUnnestOp = AccessMethodUtils.createSecondaryIndexUnnestMap(dataset, recordType,
                chosenIndex, inputOp, jobGenParams, context, false, retainInput);

        // Generate the rest of the upstream plan which feeds the search results into the primary index.        
        UnnestMapOperator primaryIndexUnnestOp = null;
        boolean isPrimaryIndex = chosenIndex.isPrimaryIndex();
        if (dataset.getDatasetType() == DatasetType.EXTERNAL) {
            // External dataset
            ExternalDataLookupOperator externalDataAccessOp = AccessMethodUtils.createExternalDataLookupUnnestMap(
                    dataSourceScan, dataset, recordType, secondaryIndexUnnestOp, context, chosenIndex, retainInput,
                    retainNull);
            indexSubTree.dataSourceRef.setValue(externalDataAccessOp);
            return externalDataAccessOp;
        } else if (!isPrimaryIndex) {
            primaryIndexUnnestOp = AccessMethodUtils.createPrimaryIndexUnnestMap(dataSourceScan, dataset, recordType,
                    secondaryIndexUnnestOp, context, true, retainInput, retainNull, false);

            // Replace the datasource scan with the new plan rooted at
            // primaryIndexUnnestMap.
            indexSubTree.dataSourceRef.setValue(primaryIndexUnnestOp);
        } else {
            List<Object> primaryIndexOutputTypes = new ArrayList<Object>();
            try {
                AccessMethodUtils.appendPrimaryIndexTypes(dataset, recordType, primaryIndexOutputTypes);
            } catch (IOException e) {
                throw new AlgebricksException(e);
            }
            primaryIndexUnnestOp = new UnnestMapOperator(dataSourceScan.getVariables(),
                    secondaryIndexUnnestOp.getExpressionRef(), primaryIndexOutputTypes, retainInput);
            primaryIndexUnnestOp.getInputs().add(new MutableObject<ILogicalOperator>(inputOp));

            if (!primaryIndexPostProccessingIsNeeded) {
                List<Mutable<ILogicalExpression>> remainingFuncExprs = new ArrayList<Mutable<ILogicalExpression>>();
                getNewConditionExprs(conditionRef, replacedFuncExprs, remainingFuncExprs);
                // Generate new condition.
                if (!remainingFuncExprs.isEmpty()) {
                    ILogicalExpression pulledCond = createSelectCondition(remainingFuncExprs);
                    conditionRef.setValue(pulledCond);
                } else {
                    conditionRef.setValue(null);
                }
            }
        }

        return primaryIndexUnnestOp;
    }

    private int createKeyVarsAndExprs(LimitType[] keyLimits, ILogicalExpression[] searchKeyExprs,
            ArrayList<LogicalVariable> assignKeyVarList, ArrayList<Mutable<ILogicalExpression>> assignKeyExprList,
            ArrayList<LogicalVariable> keyVarList, IOptimizationContext context) {
        if (keyLimits[0] == null) {
            return 0;
        }
        int numKeys = keyLimits.length;
        for (int i = 0; i < numKeys; i++) {
            ILogicalExpression searchKeyExpr = searchKeyExprs[i];
            LogicalVariable keyVar = null;
            if (searchKeyExpr.getExpressionTag() == LogicalExpressionTag.CONSTANT) {
                keyVar = context.newVar();
                assignKeyExprList.add(new MutableObject<ILogicalExpression>(searchKeyExpr));
                assignKeyVarList.add(keyVar);
            } else {
                keyVar = ((VariableReferenceExpression) searchKeyExpr).getVariableReference();
            }
            keyVarList.add(keyVar);
        }
        return numKeys;
    }

    private void getNewConditionExprs(Mutable<ILogicalExpression> conditionRef,
            Set<ILogicalExpression> replacedFuncExprs, List<Mutable<ILogicalExpression>> remainingFuncExprs) {
        remainingFuncExprs.clear();
        if (replacedFuncExprs.isEmpty()) {
            return;
        }
        AbstractFunctionCallExpression funcExpr = (AbstractFunctionCallExpression) conditionRef.getValue();
        if (replacedFuncExprs.size() == 1) {
            Iterator<ILogicalExpression> it = replacedFuncExprs.iterator();
            if (!it.hasNext()) {
                return;
            }
            if (funcExpr == it.next()) {
                // There are no remaining function exprs.
                return;
            }
        }
        // The original select cond must be an AND. Check it just to be sure.
        if (funcExpr.getFunctionIdentifier() != AlgebricksBuiltinFunctions.AND) {
            throw new IllegalStateException();
        }
        // Clean the conjuncts.
        for (Mutable<ILogicalExpression> arg : funcExpr.getArguments()) {
            ILogicalExpression argExpr = arg.getValue();
            if (argExpr.getExpressionTag() != LogicalExpressionTag.FUNCTION_CALL) {
                continue;
            }
            // If the function expression was not replaced by the new index
            // plan, then add it to the list of remaining function expressions.
            if (!replacedFuncExprs.contains(argExpr)) {
                remainingFuncExprs.add(arg);
            }
        }
    }

    private <T> int indexOf(T value, List<T> coll) {
        int i = 0;
        for (T member : coll) {
            if (member.equals(value)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private LimitType getLimitType(IOptimizableFuncExpr optFuncExpr, OptimizableOperatorSubTree probeSubTree) {
        ComparisonKind ck = AlgebricksBuiltinFunctions.getComparisonType(optFuncExpr.getFuncExpr()
                .getFunctionIdentifier());
        LimitType limit = null;
        switch (ck) {
            case EQ: {
                limit = LimitType.EQUAL;
                break;
            }
            case GE: {
                limit = probeIsOnLhs(optFuncExpr, probeSubTree) ? LimitType.HIGH_INCLUSIVE : LimitType.LOW_INCLUSIVE;
                break;
            }
            case GT: {
                limit = probeIsOnLhs(optFuncExpr, probeSubTree) ? LimitType.HIGH_EXCLUSIVE : LimitType.LOW_EXCLUSIVE;
                break;
            }
            case LE: {
                limit = probeIsOnLhs(optFuncExpr, probeSubTree) ? LimitType.LOW_INCLUSIVE : LimitType.HIGH_INCLUSIVE;
                break;
            }
            case LT: {
                limit = probeIsOnLhs(optFuncExpr, probeSubTree) ? LimitType.LOW_EXCLUSIVE : LimitType.HIGH_EXCLUSIVE;
                break;
            }
            case NEQ: {
                limit = null;
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return limit;
    }

    private boolean probeIsOnLhs(IOptimizableFuncExpr optFuncExpr, OptimizableOperatorSubTree probeSubTree) {
        if (probeSubTree == null) {
            // We are optimizing a selection query. Search key is a constant. Return true if constant is on lhs.
            return optFuncExpr.getFuncExpr().getArguments().get(0) == optFuncExpr.getConstantVal(0);
        } else {
            // We are optimizing a join query. Determine whether the feeding variable is on the lhs. 
            return (optFuncExpr.getOperatorSubTree(0) == null || optFuncExpr.getOperatorSubTree(0) == probeSubTree);
        }
    }

    private ILogicalExpression createSelectCondition(List<Mutable<ILogicalExpression>> predList) {
        if (predList.size() > 1) {
            IFunctionInfo finfo = FunctionUtils.getFunctionInfo(AlgebricksBuiltinFunctions.AND);
            return new ScalarFunctionCallExpression(finfo, predList);
        }
        return predList.get(0).getValue();
    }

    @Override
    public boolean exprIsOptimizable(Index index, IOptimizableFuncExpr optFuncExpr) {
        // If we are optimizing a join, check for the indexed nested-loop join hint.
        if (optFuncExpr.getNumLogicalVars() == 2) {
            if (index.getIndexType() == IndexType.BTREE
                    && !optFuncExpr.getFuncExpr().getAnnotations()
                            .containsKey(IndexedNLJoinExpressionAnnotation.INSTANCE)) {
                return false;
            }
        }
        if (!index.isPrimaryIndex()
                && optFuncExpr.getFuncExpr().getAnnotations()
                        .containsKey(SkipSecondaryIndexSearchExpressionAnnotation.INSTANCE)) {
            return false;
        }
        // No additional analysis required for BTrees.
        return true;
    }
}