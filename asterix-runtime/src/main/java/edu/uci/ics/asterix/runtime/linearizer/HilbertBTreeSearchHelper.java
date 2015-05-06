package edu.uci.ics.asterix.runtime.linearizer;

import java.io.DataOutput;

import edu.uci.ics.asterix.dataflow.data.nontagged.Coordinate;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ADoubleSerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.AInt64SerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.APointSerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ARectangleSerializerDeserializer;
import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.om.base.AInt64;
import edu.uci.ics.asterix.om.base.AMutableInt64;
import edu.uci.ics.asterix.om.base.AMutablePoint;
import edu.uci.ics.asterix.om.base.APoint;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;
import edu.uci.ics.hyracks.storage.am.common.api.ILinearizerSearchHelper;
import edu.uci.ics.hyracks.storage.am.common.tuples.PermutingFrameTupleReference;

public class HilbertBTreeSearchHelper implements ILinearizerSearchHelper {

    private ISerializerDeserializer<APoint> pointSerde;
    private AMutablePoint aPoint; 
    private ISerializerDeserializer<AInt64> int64Serde;
    private AMutableInt64 aInt64; 
    private final double qBottomLeftX;
    private final double qBottomLeftY;
    private final double qTopRightX;
    private final double qTopRightY;
    private PermutingFrameTupleReference queryRegion; //for debugging

    @SuppressWarnings("unchecked")
    public HilbertBTreeSearchHelper(PermutingFrameTupleReference queryRegion) throws HyracksDataException {
        aPoint = new AMutablePoint(0, 0);
        pointSerde = AqlSerializerDeserializerProvider.INSTANCE.getSerializerDeserializer(BuiltinType.APOINT);
        aInt64 = new AMutableInt64(0);
        int64Serde = AqlSerializerDeserializerProvider.INSTANCE.getSerializerDeserializer(BuiltinType.AINT64);
        
        int fieldStartOffset = queryRegion.getFieldStart(0);
        qBottomLeftX = ADoubleSerializerDeserializer.getDouble(queryRegion.getFieldData(0), fieldStartOffset
                + ARectangleSerializerDeserializer.getBottomLeftCoordinateOffset(Coordinate.X));
        qBottomLeftY = ADoubleSerializerDeserializer.getDouble(queryRegion.getFieldData(0), fieldStartOffset
                + ARectangleSerializerDeserializer.getBottomLeftCoordinateOffset(Coordinate.Y));
        qTopRightX = ADoubleSerializerDeserializer.getDouble(queryRegion.getFieldData(0), fieldStartOffset
                + ARectangleSerializerDeserializer.getUpperRightCoordinateOffset(Coordinate.X));
        qTopRightY = ADoubleSerializerDeserializer.getDouble(queryRegion.getFieldData(0), fieldStartOffset
                + ARectangleSerializerDeserializer.getUpperRightCoordinateOffset(Coordinate.Y));
        this.queryRegion = queryRegion;
    }

    @Override
    public double getQueryBottomLeftX() {
        return qBottomLeftX;
    }

    @Override
    public double getQueryBottomLeftY() {
        return qBottomLeftY;
    }

    @Override
    public double getQueryTopRightX() {
        return qTopRightX;
    }
    
    @Override
    public double getQueryTopRightY() {
        return qTopRightY;
    }

    @Override
    public void convertPointField2TwoDoubles(byte[] bytes, int fieldStartOffset, double[] out)
            throws HyracksDataException {
        //deserialize a point field in the byte array
        out[0] = ADoubleSerializerDeserializer.getDouble(bytes,
                fieldStartOffset + APointSerializerDeserializer.getCoordinateOffset(Coordinate.X));
        out[1] = ADoubleSerializerDeserializer.getDouble(bytes,
                fieldStartOffset + APointSerializerDeserializer.getCoordinateOffset(Coordinate.Y));
    }

    @Override
    public void convertTwoDoubles2PointField(double[] in, ArrayTupleBuilder tupleBuilder) throws HyracksDataException {
        aPoint.setValue(in[0], in[1]);
        DataOutput dos = tupleBuilder.getDataOutput();
        tupleBuilder.reset();
        pointSerde.serialize(aPoint, dos);
        tupleBuilder.addFieldEndOffset();
    }

    @Override
    public void convertLong2Int64Field(long in, ArrayTupleBuilder tupleBuilder) throws HyracksDataException {
        aInt64.setValue(in);
        DataOutput dos = tupleBuilder.getDataOutput();
        tupleBuilder.reset();
        int64Serde.serialize(aInt64, dos);
        tupleBuilder.addFieldEndOffset();
    }

    @Override
    public long convertInt64Field2Long(byte[] bytes, int fieldStartOffset) throws HyracksDataException {
        return AInt64SerializerDeserializer.getLong(bytes, fieldStartOffset+1);
    }
}
