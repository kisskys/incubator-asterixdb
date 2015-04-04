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
package edu.uci.ics.asterix.common.dataflow;

import java.nio.ByteBuffer;

import edu.uci.ics.asterix.common.api.IAsterixAppRuntimeContext;
import edu.uci.ics.asterix.common.ioopcallbacks.AbstractLSMIOOperationCallback;
import edu.uci.ics.asterix.common.transactions.ILogManager;
import edu.uci.ics.hyracks.api.context.IHyracksTaskContext;
import edu.uci.ics.hyracks.api.dataflow.value.IRecordDescriptorProvider;
import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.api.util.ExperimentProfiler;
import edu.uci.ics.hyracks.api.util.SpatialIndexProfiler;
import edu.uci.ics.hyracks.dataflow.common.comm.io.FrameTupleAccessor;
import edu.uci.ics.hyracks.dataflow.common.comm.util.FrameUtils;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.FrameTupleReference;
import edu.uci.ics.hyracks.storage.am.common.api.ITupleFilterFactory;
import edu.uci.ics.hyracks.storage.am.common.dataflow.IIndexOperatorDescriptor;
import edu.uci.ics.hyracks.storage.am.common.impls.NoOpOperationCallback;
import edu.uci.ics.hyracks.storage.am.common.ophelpers.IndexOperation;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMIndexAccessor;
import edu.uci.ics.hyracks.storage.am.lsm.common.dataflow.LSMIndexInsertUpdateDeleteOperatorNodePushable;
import edu.uci.ics.hyracks.storage.am.lsm.common.impls.AbstractLSMIndex;

public class AsterixLSMInsertDeleteOperatorNodePushable extends LSMIndexInsertUpdateDeleteOperatorNodePushable {

    private final boolean isPrimary;

    //for profiler
    protected long profilerPidxInsertCount;
    protected long profilerSidxInsertCount;

    public AsterixLSMInsertDeleteOperatorNodePushable(IIndexOperatorDescriptor opDesc, IHyracksTaskContext ctx,
            int partition, int[] fieldPermutation, IRecordDescriptorProvider recordDescProvider, IndexOperation op,
            boolean isPrimary) {
        super(opDesc, ctx, partition, fieldPermutation, recordDescProvider, op);
        this.isPrimary = isPrimary;

        if (ExperimentProfiler.PROFILE_MODE) {
            profilerPidxInsertCount = 0;
            profilerSidxInsertCount = 0;
        }
    }

    @Override
    public void open() throws HyracksDataException {
        RecordDescriptor inputRecDesc = recordDescProvider.getInputRecordDescriptor(opDesc.getActivityId(), 0);
        accessor = new FrameTupleAccessor(ctx.getFrameSize(), inputRecDesc);
        writeBuffer = ctx.allocateFrame();
        writer.open();
        indexHelper.open();
        AbstractLSMIndex lsmIndex = (AbstractLSMIndex) indexHelper.getIndexInstance();
        try {
            modCallback = opDesc.getModificationOpCallbackFactory().createModificationOperationCallback(
                    indexHelper.getResourceID(), lsmIndex, ctx);
            indexAccessor = lsmIndex.createAccessor(modCallback, NoOpOperationCallback.INSTANCE);
            ITupleFilterFactory tupleFilterFactory = opDesc.getTupleFilterFactory();
            if (tupleFilterFactory != null) {
                tupleFilter = tupleFilterFactory.createTupleFilter(indexHelper.getTaskContext());
                frameTuple = new FrameTupleReference();
            }
            // If the index has an empty memory component, we need to set its first LSN (For soft checkpoint)
            if (lsmIndex.isCurrentMutableComponentEmpty()) {
                //prevent transactions from incorrectly setting the first LSN on a modified component
                synchronized (lsmIndex.getOperationTracker()) {
                    if (lsmIndex.isCurrentMutableComponentEmpty()) {
                        AbstractLSMIOOperationCallback ioOpCallback = (AbstractLSMIOOperationCallback) lsmIndex
                                .getIOOperationCallback();
                        IAsterixAppRuntimeContext runtimeCtx = (IAsterixAppRuntimeContext) ctx.getJobletContext()
                                .getApplicationContext().getApplicationObject();
                        ILogManager logManager = runtimeCtx.getTransactionSubsystem().getLogManager();
                        ioOpCallback.setFirstLSN(logManager.getAppendLSN());
                    }
                }
            }
        } catch (Exception e) {
            indexHelper.close();
            throw new HyracksDataException(e);
        }
    }

    @Override
    public void nextFrame(ByteBuffer buffer) throws HyracksDataException {
        boolean first = true;
        accessor.reset(buffer);
        ILSMIndexAccessor lsmAccessor = (ILSMIndexAccessor) indexAccessor;
        int tupleCount = accessor.getTupleCount();
        try {
            for (int i = 0; i < tupleCount; i++) {
                if (tupleFilter != null) {
                    frameTuple.reset(accessor, i);
                    if (!tupleFilter.accept(frameTuple)) {
                        continue;
                    }
                }
                tuple.reset(accessor, i);
                switch (op) {
                    case INSERT:
                        if (first && isPrimary) {
                            lsmAccessor.insert(tuple);
                            first = false;
                        } else {
                            lsmAccessor.forceInsert(tuple);
                        }
                        break;
                    case DELETE:
                        if (first && isPrimary) {
                            lsmAccessor.delete(tuple);
                            first = false;
                        } else {
                            lsmAccessor.forceDelete(tuple);
                        }
                        break;
                    default: {
                        throw new HyracksDataException("Unsupported operation " + op
                                + " in tree index InsertDelete operator");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new HyracksDataException(e);
        }
        
        if (ExperimentProfiler.PROFILE_MODE) {
            if (isPrimary) {
                profilerPidxInsertCount += tupleCount;
                SpatialIndexProfiler.INSTANCE.pidxInsertCount.add("" + profilerPidxInsertCount + "\n");
            } else {
                profilerSidxInsertCount += tupleCount;
                SpatialIndexProfiler.INSTANCE.sidxInsertCount.add("" + profilerSidxInsertCount + "\n");
            }
        }
        
        System.arraycopy(buffer.array(), 0, writeBuffer.array(), 0, buffer.capacity());
        FrameUtils.flushFrame(writeBuffer, writer);
    }

}
