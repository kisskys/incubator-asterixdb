package edu.uci.ics.asterix.runtime.aggregates.scalar;

import edu.uci.ics.asterix.om.functions.AsterixBuiltinFunctions;
import edu.uci.ics.asterix.om.functions.FunctionManagerHolder;
import edu.uci.ics.asterix.om.functions.IFunctionDescriptor;
import edu.uci.ics.asterix.om.functions.IFunctionManager;
import edu.uci.ics.asterix.runtime.aggregates.base.AbstractAggregateFunctionDynamicDescriptor;
import edu.uci.ics.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyAggregateFunctionFactory;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluator;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluatorFactory;
import edu.uci.ics.hyracks.algebricks.runtime.evaluators.ColumnAccessEvalFactory;
import edu.uci.ics.hyracks.data.std.api.IDataOutputProvider;

public abstract class AbstractScalarAggregateDescriptor extends AbstractScalarFunctionDynamicDescriptor {
    private static final long serialVersionUID = 1L;

    @Override
    public ICopyEvaluatorFactory createEvaluatorFactory(final ICopyEvaluatorFactory[] args) throws AlgebricksException {
        return new ICopyEvaluatorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public ICopyEvaluator createEvaluator(IDataOutputProvider output) throws AlgebricksException {
                // Always access column 0.
                ICopyEvaluatorFactory[] aggFuncArgs = new ICopyEvaluatorFactory[1];
                aggFuncArgs[0] = new ColumnAccessEvalFactory(0);
                // Create aggregate function from this scalar version.
                FunctionIdentifier fid = AsterixBuiltinFunctions.getAggregateFunction(getIdentifier());
                IFunctionManager mgr = FunctionManagerHolder.getFunctionManager();
                IFunctionDescriptor fd = mgr.lookupFunction(fid);
                AbstractAggregateFunctionDynamicDescriptor aggFuncDesc = (AbstractAggregateFunctionDynamicDescriptor) fd;
                ICopyAggregateFunctionFactory aggFuncFactory = aggFuncDesc.createAggregateFunctionFactory(aggFuncArgs);
                return new GenericScalarAggregateFunction(args, aggFuncFactory.createAggregateFunction(output));
            }
        };
    }
}