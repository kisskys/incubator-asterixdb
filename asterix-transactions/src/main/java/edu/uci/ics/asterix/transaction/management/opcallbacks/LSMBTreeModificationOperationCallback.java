/*
 * Copyright 2009-2010 by The Regents of the University of California
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

package edu.uci.ics.asterix.transaction.management.opcallbacks;

import edu.uci.ics.asterix.transaction.management.exception.ACIDException;
import edu.uci.ics.asterix.transaction.management.service.locking.ILockManager;
import edu.uci.ics.asterix.transaction.management.service.logging.IndexLogger;
import edu.uci.ics.asterix.transaction.management.service.transaction.DatasetId;
import edu.uci.ics.asterix.transaction.management.service.transaction.IResourceManager.ResourceType;
import edu.uci.ics.asterix.transaction.management.service.transaction.TransactionContext;
import edu.uci.ics.asterix.transaction.management.service.transaction.TransactionManagementConstants.LockManagerConstants.LockMode;
import edu.uci.ics.asterix.transaction.management.service.transaction.TransactionSubsystem;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryHashFunction;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;
import edu.uci.ics.hyracks.storage.am.common.api.IModificationOperationCallback;
import edu.uci.ics.hyracks.storage.am.common.ophelpers.IndexOperation;
import edu.uci.ics.hyracks.storage.am.lsm.btree.tuples.LSMBTreeTupleReference;

public class LSMBTreeModificationOperationCallback extends AbstractOperationCallback implements
        IModificationOperationCallback {

    protected final long resourceId;
    protected final IndexOperation indexOp;
    protected final TransactionSubsystem txnSubsystem;
    
    public LSMBTreeModificationOperationCallback(DatasetId datasetId, int[] primaryKeyFields,
            IBinaryHashFunction[] primaryKeyHashFunctions, TransactionContext txnCtx, ILockManager lockManager,
            TransactionSubsystem txnSubsystem, long resourceId, IndexOperation indexOp) {
        super(datasetId, primaryKeyFields, primaryKeyHashFunctions, txnCtx, lockManager);
        this.resourceId = resourceId;
        this.indexOp = indexOp;
        this.txnSubsystem = txnSubsystem;
    }

    @Override
    public void before(ITupleReference tuple) throws HyracksDataException {
        int pkHash = computePrimaryKeyHashValue(tuple, primaryKeyFields, primaryKeyHashFunctions);
        try {
            lockManager.lock(datasetId, pkHash, LockMode.X, txnCtx);
        } catch (ACIDException e) {
            throw new HyracksDataException(e);
        }
    }

    @Override
    public void found(ITupleReference before, ITupleReference after) throws HyracksDataException {
        IndexLogger logger = txnSubsystem.getTreeLoggerRepository().getIndexLogger(resourceId, ResourceType.LSM_BTREE);
        int pkHash = computePrimaryKeyHashValue(after, primaryKeyFields, primaryKeyHashFunctions);
        LSMBTreeTupleReference lsmBTreeTuple = (LSMBTreeTupleReference) before;
        IndexOperation oldOp = IndexOperation.INSERT;
        if (lsmBTreeTuple.isAntimatter()) {
            oldOp = IndexOperation.DELETE;
        }
        try {
            logger.generateLogRecord(txnSubsystem, txnCtx, datasetId.getId(), pkHash, resourceId, indexOp, after, oldOp, before);
        } catch (ACIDException e) {
            throw new HyracksDataException(e);
        }
    }
}
