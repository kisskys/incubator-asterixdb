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
package org.apache.asterix.runtime.evaluators.functions.temporal;

import java.io.DataOutput;

import org.apache.asterix.dataflow.data.nontagged.serde.ADateSerializerDeserializer;
import org.apache.asterix.dataflow.data.nontagged.serde.ADateTimeSerializerDeserializer;
import org.apache.asterix.dataflow.data.nontagged.serde.ADayTimeDurationSerializerDeserializer;
import org.apache.asterix.dataflow.data.nontagged.serde.ATimeSerializerDeserializer;
import org.apache.asterix.dataflow.data.nontagged.serde.AYearMonthDurationSerializerDeserializer;
import org.apache.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import org.apache.asterix.om.base.AInterval;
import org.apache.asterix.om.base.AMutableInterval;
import org.apache.asterix.om.base.ANull;
import org.apache.asterix.om.base.temporal.DurationArithmeticOperations;
import org.apache.asterix.om.base.temporal.GregorianCalendarSystem;
import org.apache.asterix.om.functions.AsterixBuiltinFunctions;
import org.apache.asterix.om.functions.IFunctionDescriptor;
import org.apache.asterix.om.functions.IFunctionDescriptorFactory;
import org.apache.asterix.om.types.ATypeTag;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.asterix.om.types.EnumDeserializer;
import org.apache.asterix.runtime.evaluators.base.AbstractScalarFunctionDynamicDescriptor;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.core.algebra.functions.FunctionIdentifier;
import org.apache.hyracks.algebricks.runtime.base.IScalarEvaluator;
import org.apache.hyracks.algebricks.runtime.base.IScalarEvaluatorFactory;
import org.apache.hyracks.api.context.IHyracksTaskContext;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.data.std.api.IPointable;
import org.apache.hyracks.data.std.primitive.VoidPointable;
import org.apache.hyracks.data.std.util.ArrayBackedValueStorage;
import org.apache.hyracks.dataflow.common.data.accessors.IFrameTupleReference;

public class IntervalBinDescriptor extends AbstractScalarFunctionDynamicDescriptor {

    private static final long serialVersionUID = 1L;
    public static final IFunctionDescriptorFactory FACTORY = new IFunctionDescriptorFactory() {

        @Override
        public IFunctionDescriptor createFunctionDescriptor() {
            return new IntervalBinDescriptor();
        }
    };

    @Override
    public IScalarEvaluatorFactory createEvaluatorFactory(final IScalarEvaluatorFactory[] args)
            throws AlgebricksException {
        return new IScalarEvaluatorFactory() {

            private static final long serialVersionUID = 1L;

            @Override
            public IScalarEvaluator createScalarEvaluator(final IHyracksTaskContext ctx) throws AlgebricksException {
                return new IScalarEvaluator() {

                    private final ArrayBackedValueStorage resultStorage = new ArrayBackedValueStorage();
                    private final DataOutput out = resultStorage.getDataOutput();

                    private final IPointable argPtr0 = new VoidPointable();
                    private final IPointable argPtr1 = new VoidPointable();
                    private final IPointable argPtr2 = new VoidPointable();

                    private final IScalarEvaluator eval0 = args[0].createScalarEvaluator(ctx);
                    private final IScalarEvaluator eval1 = args[1].createScalarEvaluator(ctx);
                    private final IScalarEvaluator eval2 = args[2].createScalarEvaluator(ctx);

                    private final AMutableInterval aInterval = new AMutableInterval(0, 0, (byte) -1);

                    @SuppressWarnings("unchecked")
                    private final ISerializerDeserializer<ANull> nullSerde = AqlSerializerDeserializerProvider.INSTANCE
                            .getSerializerDeserializer(BuiltinType.ANULL);
                    @SuppressWarnings("unchecked")
                    private final ISerializerDeserializer<AInterval> intervalSerde = AqlSerializerDeserializerProvider.INSTANCE
                            .getSerializerDeserializer(BuiltinType.AINTERVAL);

                    private final GregorianCalendarSystem GREG_CAL = GregorianCalendarSystem.getInstance();

                    @Override
                    public void evaluate(IFrameTupleReference tuple, IPointable result) throws AlgebricksException {
                        resultStorage.reset();
                        eval0.evaluate(tuple, argPtr0);
                        byte[] bytes0 = argPtr0.getByteArray();
                        int offset0 = argPtr0.getStartOffset();

                        ATypeTag type0 = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(bytes0[offset0]);

                        long chrononToBin = 0;
                        switch (type0) {
                            case DATE:
                                chrononToBin = ADateSerializerDeserializer.getChronon(bytes0, offset0 + 1)
                                        * GregorianCalendarSystem.CHRONON_OF_DAY;
                                break;
                            case TIME:
                                chrononToBin = ATimeSerializerDeserializer.getChronon(bytes0, offset0 + 1);
                                break;
                            case DATETIME:
                                chrononToBin = ADateTimeSerializerDeserializer.getChronon(bytes0, offset0 + 1);
                                break;
                            case NULL:
                                try {
                                    nullSerde.serialize(ANull.NULL, out);
                                } catch (HyracksDataException e) {
                                    throw new AlgebricksException(e);
                                }
                                result.set(resultStorage);
                                return;
                            default:
                                throw new AlgebricksException(getIdentifier().getName()
                                        + ": the first argument should be DATE/TIME/DATETIME/NULL but got " + type0);

                        }

                        eval1.evaluate(tuple, argPtr1);
                        byte[] bytes1 = argPtr1.getByteArray();
                        int offset1 = argPtr1.getStartOffset();
                        ATypeTag type1 = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(bytes1[offset1]);

                        if (type0 != type1) {
                            if (type0 != ATypeTag.NULL && type1 != ATypeTag.NULL) {
                                throw new AlgebricksException(getIdentifier().getName() + ": expecting " + type0
                                        + " for the second argument but got " + type1);
                            }
                        }

                        long chrononToStart = 0;
                        switch (type1) {
                            case DATE:
                                chrononToStart = ADateSerializerDeserializer.getChronon(bytes1, offset1 + 1)
                                        * GregorianCalendarSystem.CHRONON_OF_DAY;
                                break;
                            case TIME:
                                chrononToStart = ATimeSerializerDeserializer.getChronon(bytes1, offset1 + 1);
                                break;
                            case DATETIME:
                                chrononToStart = ADateTimeSerializerDeserializer.getChronon(bytes1, offset1 + 1);
                                break;
                            case NULL:
                                try {
                                    nullSerde.serialize(ANull.NULL, out);
                                } catch (HyracksDataException e) {
                                    throw new AlgebricksException(e);
                                }
                                result.set(resultStorage);
                                return;
                            default:
                                throw new AlgebricksException(getIdentifier().getName() + ": expecting " + type0
                                        + " for the second argument but got " + type1);
                        }

                        eval2.evaluate(tuple, argPtr2);
                        byte[] bytes2 = argPtr2.getByteArray();
                        int offset2 = argPtr2.getStartOffset();
                        ATypeTag type2 = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(bytes2[offset2]);

                        int yearMonth = 0;
                        long dayTime = 0;
                        long binIndex, binStartChronon, binEndChronon;
                        switch (type2) {
                            case YEARMONTHDURATION:
                                yearMonth = AYearMonthDurationSerializerDeserializer.getYearMonth(bytes2, offset2 + 1);
                                int yearStart = GREG_CAL.getYear(chrononToStart);
                                int monthStart = GREG_CAL.getMonthOfYear(chrononToStart, yearStart);
                                int yearToBin = GREG_CAL.getYear(chrononToBin);
                                int monthToBin = GREG_CAL.getMonthOfYear(chrononToBin, yearToBin);

                                int totalMonths = (yearToBin - yearStart) * 12 + (monthToBin - monthStart);

                                binIndex = totalMonths / yearMonth
                                        + ((totalMonths < 0 && totalMonths % yearMonth != 0) ? -1 : 0);

                                if (binIndex > Integer.MAX_VALUE) {
                                    throw new AlgebricksException(
                                            getIdentifier().getName() + ": Overflowing time value to be binned!");
                                }

                                if (binIndex < Integer.MIN_VALUE) {
                                    throw new AlgebricksException(
                                            getIdentifier().getName() + ": Underflowing time value to be binned!");
                                }

                                break;

                            case DAYTIMEDURATION:
                                dayTime = ADayTimeDurationSerializerDeserializer.getDayTime(bytes2, offset2 + 1);
                                long totalChronon = chrononToBin - chrononToStart;

                                binIndex = totalChronon / dayTime
                                        + ((totalChronon < 0 && totalChronon % dayTime != 0) ? -1 : 0);

                                break;
                            case NULL:
                                try {
                                    nullSerde.serialize(ANull.NULL, out);
                                } catch (HyracksDataException e) {
                                    throw new AlgebricksException(e);
                                }
                                result.set(resultStorage);
                                return;
                            default:
                                throw new AlgebricksException(getIdentifier().getName()
                                        + ": expecting YEARMONTHDURATION/DAYTIMEDURATION for the thrid argument but got "
                                        + type2);
                        }

                        switch (type0) {
                            case DATE:
                                binStartChronon = DurationArithmeticOperations.addDuration(chrononToStart,
                                        yearMonth * (int) binIndex, dayTime * binIndex, false);
                                binEndChronon = DurationArithmeticOperations.addDuration(chrononToStart,
                                        yearMonth * ((int) binIndex + 1), dayTime * (binIndex + 1), false);

                                binStartChronon = binStartChronon / GregorianCalendarSystem.CHRONON_OF_DAY
                                        + ((binStartChronon < 0
                                                && binStartChronon % GregorianCalendarSystem.CHRONON_OF_DAY != 0) ? -1
                                                        : 0);
                                binEndChronon = binEndChronon / GregorianCalendarSystem.CHRONON_OF_DAY
                                        + ((binEndChronon < 0
                                                && binEndChronon % GregorianCalendarSystem.CHRONON_OF_DAY != 0) ? -1
                                                        : 0);
                                break;
                            case TIME:
                                if (yearMonth != 0) {
                                    throw new AlgebricksException(getIdentifier().getName()
                                            + ": cannot create year-month bin for a time value");
                                }
                                binStartChronon = DurationArithmeticOperations.addDuration(chrononToStart,
                                        yearMonth * (int) binIndex, dayTime * binIndex, true);
                                binEndChronon = DurationArithmeticOperations.addDuration(chrononToStart,
                                        yearMonth * ((int) binIndex + 1), dayTime * (binIndex + 1), true);
                                break;
                            case DATETIME:
                                binStartChronon = DurationArithmeticOperations.addDuration(chrononToStart,
                                        yearMonth * (int) binIndex, dayTime * binIndex, false);
                                binEndChronon = DurationArithmeticOperations.addDuration(chrononToStart,
                                        yearMonth * ((int) binIndex + 1), dayTime * (binIndex + 1), false);
                                break;
                            case NULL:
                                try {
                                    nullSerde.serialize(ANull.NULL, out);
                                } catch (HyracksDataException e) {
                                    throw new AlgebricksException(e);
                                }
                                result.set(resultStorage);
                                return;
                            default:
                                throw new AlgebricksException(getIdentifier().getName()
                                        + ": the first argument should be DATE/TIME/DATETIME/NULL but got " + type0);

                        }
                        try {
                            aInterval.setValue(binStartChronon, binEndChronon, type0.serialize());
                            intervalSerde.serialize(aInterval, out);
                        } catch (HyracksDataException ex) {
                            throw new AlgebricksException(ex);
                        }
                        result.set(resultStorage);
                    }
                };
            }

        };
    }

    /* (non-Javadoc)
     * @see org.apache.asterix.om.functions.AbstractFunctionDescriptor#getIdentifier()
     */
    @Override
    public FunctionIdentifier getIdentifier() {
        return AsterixBuiltinFunctions.INTERVAL_BIN;
    }

}
