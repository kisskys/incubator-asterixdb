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
package org.apache.asterix.runtime.evaluators.constructors;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import org.apache.asterix.om.base.ACircle;
import org.apache.asterix.om.base.AMutableCircle;
import org.apache.asterix.om.base.AMutablePoint;
import org.apache.asterix.om.base.ANull;
import org.apache.asterix.om.functions.AsterixBuiltinFunctions;
import org.apache.asterix.om.functions.IFunctionDescriptor;
import org.apache.asterix.om.functions.IFunctionDescriptorFactory;
import org.apache.asterix.om.types.ATypeTag;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import org.apache.hyracks.algebricks.runtime.base.IScalarEvaluator;
import org.apache.hyracks.algebricks.runtime.base.IScalarEvaluatorFactory;
import org.apache.hyracks.api.context.IHyracksTaskContext;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.data.std.api.IPointable;
import org.apache.hyracks.data.std.primitive.UTF8StringPointable;
import org.apache.hyracks.data.std.primitive.VoidPointable;
import org.apache.hyracks.data.std.util.ArrayBackedValueStorage;
import org.apache.hyracks.dataflow.common.data.accessors.IFrameTupleReference;

public class ACircleConstructorDescriptor extends AbstractScalarFunctionDynamicDescriptor {

    private static final long serialVersionUID = 1L;
    public static final IFunctionDescriptorFactory FACTORY = new IFunctionDescriptorFactory() {
        @Override
        public IFunctionDescriptor createFunctionDescriptor() {
            return new ACircleConstructorDescriptor();
        }
    };

    @Override
    public IScalarEvaluatorFactory createEvaluatorFactory(final IScalarEvaluatorFactory[] args) {
        return new IScalarEvaluatorFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            public IScalarEvaluator createScalarEvaluator(IHyracksTaskContext ctx) throws AlgebricksException {
                return new IScalarEvaluator() {
                    private final ArrayBackedValueStorage resultStorage = new ArrayBackedValueStorage();
                    private final DataOutput out = resultStorage.getDataOutput();
                    private final IPointable inputArg = new VoidPointable();
                    private final IScalarEvaluator eval = args[0].createScalarEvaluator(ctx);
                    private final String errorMessage = "This can not be an instance of circle";
                    private final AMutablePoint aPoint = new AMutablePoint(0, 0);
                    private AMutableCircle aCircle = new AMutableCircle(null, 0);
                    @SuppressWarnings("unchecked")
                    private ISerializerDeserializer<ACircle> circleSerde = AqlSerializerDeserializerProvider.INSTANCE
                            .getSerializerDeserializer(BuiltinType.ACIRCLE);
                    @SuppressWarnings("unchecked")
                    private ISerializerDeserializer<ANull> nullSerde = AqlSerializerDeserializerProvider.INSTANCE
                            .getSerializerDeserializer(BuiltinType.ANULL);

                    private final UTF8StringPointable utf8Ptr = new UTF8StringPointable();

                    @Override
                    public void evaluate(IFrameTupleReference tuple, IPointable result) throws AlgebricksException {
                        try {
                            resultStorage.reset();
                            eval.evaluate(tuple, inputArg);

                            byte[] serString = inputArg.getByteArray();
                            int offset = inputArg.getStartOffset();
                            int len = inputArg.getLength();

                            if (serString[offset] == ATypeTag.SERIALIZED_STRING_TYPE_TAG) {
                                utf8Ptr.set(serString, offset + 1, len - 1);
                                String s = utf8Ptr.toString();
                                int commaIndex = s.indexOf(',');
                                int spaceIndex = s.indexOf(' ', commaIndex + 1);
                                aPoint.setValue(Double.parseDouble(s.substring(0, commaIndex)),
                                        Double.parseDouble(s.substring(commaIndex + 1, spaceIndex)));
                                aCircle.setValue(aPoint, Double.parseDouble(s.substring(spaceIndex + 1, s.length())));
                                circleSerde.serialize(aCircle, out);
                            } else if (serString[offset] == ATypeTag.SERIALIZED_NULL_TYPE_TAG) {
                                nullSerde.serialize(ANull.NULL, out);
                            } else {
                                throw new AlgebricksException(errorMessage);
                            }
                            result.set(resultStorage);
                        } catch (IOException e1) {
                            throw new AlgebricksException(errorMessage);
                        }
                    }
                };
            }
        };

    }

    @Override
    public FunctionIdentifier getIdentifier() {
        return AsterixBuiltinFunctions.CIRCLE_CONSTRUCTOR;
    }

}
