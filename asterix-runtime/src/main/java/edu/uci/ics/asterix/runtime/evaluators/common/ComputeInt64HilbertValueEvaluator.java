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
package edu.uci.ics.asterix.runtime.evaluators.common;

import java.io.DataOutput;

import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ADoubleSerializerDeserializer;
import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.om.base.AMutableInt64;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.asterix.runtime.linearizer.GeoCoordinates2HilbertValueConverter;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluator;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluatorFactory;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.data.std.api.IDataOutputProvider;
import edu.uci.ics.hyracks.data.std.util.ArrayBackedValueStorage;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.IFrameTupleReference;

public class ComputeInt64HilbertValueEvaluator implements ICopyEvaluator {
    private final DataOutput out;
    private final ArrayBackedValueStorage argOut = new ArrayBackedValueStorage();
    private final ICopyEvaluator pointEval;
    private final GeoCoordinates2HilbertValueConverter hilbertConverter;
    private final AMutableInt64 amInt64 = new AMutableInt64(0);
    private final ISerializerDeserializer int64Serde;

    public ComputeInt64HilbertValueEvaluator(ICopyEvaluatorFactory pointEvalFactory, IDataOutputProvider output)
            throws AlgebricksException {
        out = output.getDataOutput();
        pointEval = pointEvalFactory.createEvaluator(argOut);
        hilbertConverter = new GeoCoordinates2HilbertValueConverter();
        int64Serde = AqlSerializerDeserializerProvider.INSTANCE.getSerializerDeserializer(BuiltinType.AINT64);
    }

    @Override
    public void evaluate(IFrameTupleReference tuple) throws AlgebricksException {
        argOut.reset();
        pointEval.evaluate(tuple);
        byte[] bytes = argOut.getByteArray();

        //APoint type field: [APOINTTypeTag (1byte) | double (8bytes) | double (8bytes)]   
        double x = ADoubleSerializerDeserializer.getDouble(bytes, 1);
        double y = ADoubleSerializerDeserializer.getDouble(bytes, 9);

        //compute hilbert value
        long hilbertValue = hilbertConverter.computeInt64HilbertValue(x, y);

        //output
        amInt64.setValue(hilbertValue);
        try {
            int64Serde.serialize(amInt64, out);
        } catch (HyracksDataException e) {
            throw new AlgebricksException(e);
        }
    }
}
