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
package edu.uci.ics.asterix.runtime.evaluators.functions;

import edu.uci.ics.asterix.common.config.DatasetConfig.CellBasedSpatialIndex;
import edu.uci.ics.asterix.common.config.OptimizationConfUtil;
import edu.uci.ics.asterix.dataflow.data.common.SpatialCellBinaryTokenizer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ADoubleSerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.AInt16SerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.AInt32SerializerDeserializer;
import edu.uci.ics.asterix.om.functions.AsterixBuiltinFunctions;
import edu.uci.ics.asterix.om.functions.IFunctionDescriptor;
import edu.uci.ics.asterix.om.functions.IFunctionDescriptorFactory;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import edu.uci.ics.asterix.runtime.evaluators.common.SpatialCellTokensEvaluator;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluator;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluatorFactory;
import edu.uci.ics.hyracks.data.std.api.IDataOutputProvider;
import edu.uci.ics.hyracks.data.std.util.ArrayBackedValueStorage;
import edu.uci.ics.hyracks.storage.am.common.api.IBinaryTokenizer;
import edu.uci.ics.hyracks.storage.am.common.api.ITokenFactory;
import edu.uci.ics.hyracks.storage.am.common.tokenizer.ByteArrayTokenFactory;

public class SpatialCellTokensDescriptor extends AbstractScalarFunctionDynamicDescriptor {

    private static final long serialVersionUID = 1L;
    public static final IFunctionDescriptorFactory FACTORY = new IFunctionDescriptorFactory() {
        public IFunctionDescriptor createFunctionDescriptor() {
            return new SpatialCellTokensDescriptor();
        }
    };

    @Override
    public FunctionIdentifier getIdentifier() {
        return AsterixBuiltinFunctions.SPATIAL_CELL_TOKENS;
    }

    @Override
    public ICopyEvaluatorFactory createEvaluatorFactory(final ICopyEvaluatorFactory[] args) throws AlgebricksException {
        return new ICopyEvaluatorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public ICopyEvaluator createEvaluator(IDataOutputProvider output) throws AlgebricksException {

                ArrayBackedValueStorage outBottomLeftX = new ArrayBackedValueStorage();
                ArrayBackedValueStorage outBottomLeftY = new ArrayBackedValueStorage();
                ArrayBackedValueStorage outTopRightX = new ArrayBackedValueStorage();
                ArrayBackedValueStorage outTopRightY = new ArrayBackedValueStorage();

                int maxLevel = CellBasedSpatialIndex.MAX_LEVEL.getValue();
                short[] levelDensity = new short[maxLevel];
                ArrayBackedValueStorage[] outLevelDensity = new ArrayBackedValueStorage[maxLevel];
                ArrayBackedValueStorage outCellsPerObject = new ArrayBackedValueStorage();

                args[1].createEvaluator(outBottomLeftX).evaluate(null);
                args[2].createEvaluator(outBottomLeftY).evaluate(null);
                args[3].createEvaluator(outTopRightX).evaluate(null);
                args[4].createEvaluator(outTopRightY).evaluate(null);
                for (int i = 0; i < maxLevel; i++) {
                    outLevelDensity[i] = new ArrayBackedValueStorage();
                    args[5 + i].createEvaluator(outLevelDensity[i]).evaluate(null);
                }
                args[5 + maxLevel].createEvaluator(outCellsPerObject).equals(null);

                double bottomLeftX = ADoubleSerializerDeserializer.getDouble(outBottomLeftX.getByteArray(), 1);
                double bottomLeftY = ADoubleSerializerDeserializer.getDouble(outBottomLeftY.getByteArray(), 1);
                double topRightX = ADoubleSerializerDeserializer.getDouble(outTopRightX.getByteArray(), 1);
                double topRightY = ADoubleSerializerDeserializer.getDouble(outTopRightY.getByteArray(), 1);
                for (int i = 0; i < maxLevel; i++) {
                    levelDensity[i] = AInt16SerializerDeserializer.getShort(outLevelDensity[i].getByteArray(), 1);
                }
                int cellsPerObject = AInt32SerializerDeserializer.getInt(outCellsPerObject.getByteArray(), 1);
                ITokenFactory tokenFactory = new ByteArrayTokenFactory(ATypeTag.BINARY.serialize());
                IBinaryTokenizer tokenizer = new SpatialCellBinaryTokenizer(bottomLeftX, bottomLeftY, topRightX,
                        topRightY, levelDensity, cellsPerObject, tokenFactory, OptimizationConfUtil
                                .getPhysicalOptimizationConfig().getFrameSize(), true);
                return new SpatialCellTokensEvaluator(args, output, tokenizer, BuiltinType.ABINARY);
            }
        };
    }

}
