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
package edu.uci.ics.asterix.runtime.linearizer;

import java.io.DataOutput;
import java.nio.ByteBuffer;

import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.om.base.AInt64;
import edu.uci.ics.asterix.om.types.BuiltinType;
import edu.uci.ics.hyracks.api.context.IHyracksTaskContext;
import edu.uci.ics.hyracks.api.dataflow.value.IRecordDescriptorProvider;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleReference;
import edu.uci.ics.hyracks.storage.am.btree.util.BTreeUtils;
import edu.uci.ics.hyracks.storage.am.common.api.ISearchPredicate;
import edu.uci.ics.hyracks.storage.am.common.api.ITreeIndex;
import edu.uci.ics.hyracks.storage.am.common.dataflow.AbstractTreeIndexOperatorDescriptor;
import edu.uci.ics.hyracks.storage.am.common.dataflow.IndexSearchOperatorNodePushable;
import edu.uci.ics.hyracks.storage.am.common.ophelpers.MultiComparator;
import edu.uci.ics.hyracks.storage.am.common.tuples.PermutingFrameTupleReference;

public class HilbertValueBTreeSearchOperatorNodePushable extends IndexSearchOperatorNodePushable {

    protected PermutingFrameTupleReference queryRegion;
    protected final boolean btreeLowKeyInclusive;
    protected final boolean btreeHighKeyInclusive;
    protected ArrayTupleReference btreeLowKey;
    protected ArrayTupleReference btreeHighKey;
    protected MultiComparator btreeLowKeyCmp;
    protected MultiComparator btreeHighKeyCmp;
    protected ArrayTupleBuilder btreeKeyBuilder;
    protected ArrayTupleReference btreeKeyReference;

    protected ByteBuffer btreeKeyFrame;
    private HilbertValueBTreeRangePredicate hilbertValueBTreeRangePredicate;

    public HilbertValueBTreeSearchOperatorNodePushable(AbstractTreeIndexOperatorDescriptor opDesc,
            IHyracksTaskContext ctx, int partition, IRecordDescriptorProvider recordDescProvider, int[] lowKeyFields,
            int[] highKeyFields, boolean lowKeyInclusive, boolean highKeyInclusive, int[] minFilterFieldIndexes,
            int[] maxFilterFieldIndexes) throws HyracksDataException {
        super(opDesc, ctx, partition, recordDescProvider, minFilterFieldIndexes, maxFilterFieldIndexes);
        queryRegion = new PermutingFrameTupleReference();
        queryRegion.setFieldPermutation(lowKeyFields);
        //Currently, HilbertValueBTree always has an int64 type key field in the first field.
        btreeLowKey = new ArrayTupleReference();
        btreeLowKey.reset(new int[] { 0 }, null);
        btreeHighKey = null;
        this.btreeLowKeyInclusive = true;
        this.btreeHighKeyInclusive = true;
        btreeKeyBuilder = new ArrayTupleBuilder(1);

        //set initial search key to a first Hilbert value: 0
        AInt64 initialSearchKey = new AInt64(0L);
        ISerializerDeserializer serde = AqlSerializerDeserializerProvider.INSTANCE
                .getSerializerDeserializer(BuiltinType.AINT64);
        DataOutput dos = btreeKeyBuilder.getDataOutput();
        btreeKeyBuilder.reset();
        serde.serialize(initialSearchKey, dos);
        btreeKeyBuilder.addFieldEndOffset();
        btreeLowKey.reset(btreeKeyBuilder.getFieldEndOffsets(), btreeKeyBuilder.getByteArray());
    }

    @Override
    protected ISearchPredicate createSearchPredicate() throws HyracksDataException {
        ITreeIndex treeIndex = (ITreeIndex) index;
        btreeLowKeyCmp = BTreeUtils.getSearchMultiComparator(treeIndex.getComparatorFactories(), btreeLowKey);
        btreeHighKeyCmp = BTreeUtils.getSearchMultiComparator(treeIndex.getComparatorFactories(), btreeHighKey);
        hilbertValueBTreeRangePredicate = new HilbertValueBTreeRangePredicate(btreeLowKey, btreeHighKey,
                btreeLowKeyInclusive, btreeHighKeyInclusive, btreeLowKeyCmp, btreeHighKeyCmp, minFilterKey,
                maxFilterKey, queryRegion);
        return hilbertValueBTreeRangePredicate;
    }

    @Override
    protected void resetSearchPredicate(int tupleIndex) throws HyracksDataException {
        queryRegion.reset(accessor, tupleIndex);

        if (minFilterKey != null) {
            minFilterKey.reset(accessor, tupleIndex);
        }
        if (maxFilterKey != null) {
            maxFilterKey.reset(accessor, tupleIndex);
        }
    }

    @Override
    protected int getFieldCount() {
        return ((ITreeIndex) index).getFieldCount();
    }
}