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
import edu.uci.ics.asterix.transaction.management.service.transaction.DatasetId;
import edu.uci.ics.asterix.transaction.management.service.transaction.ITransactionSubsystemProvider;
import edu.uci.ics.asterix.transaction.management.service.transaction.JobId;
import edu.uci.ics.asterix.transaction.management.service.transaction.TransactionContext;
import edu.uci.ics.asterix.transaction.management.service.transaction.TransactionSubsystem;
import edu.uci.ics.hyracks.api.context.IHyracksTaskContext;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryHashFunction;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.storage.am.common.api.IModificationOperationCallback;
import edu.uci.ics.hyracks.storage.am.common.api.IOperationCallbackFactory;
import edu.uci.ics.hyracks.storage.am.common.api.ISearchOperationCallback;

public class OperationCallbackFactory implements IOperationCallbackFactory {

    private static final long serialVersionUID = 1L;
    
    private final JobId jobId;
    private final DatasetId datasetId;
    private final int[] entityIdFields;
    private final IBinaryHashFunction[] entityIdFieldHashFunctions;
    private final ITransactionSubsystemProvider txnSubsystemProvider;
    
    public OperationCallbackFactory(JobId jobId, DatasetId datasetId, int[] entityIdFields,
            IBinaryHashFunction[] entityIdFieldHashFunctions, ITransactionSubsystemProvider txnSubsystemProvider) {
        this.jobId = jobId;
        this.datasetId = datasetId;
        this.entityIdFields = entityIdFields;
        this.entityIdFieldHashFunctions = entityIdFieldHashFunctions;
        this.txnSubsystemProvider = txnSubsystemProvider;
    }
    
    @Override
    public IModificationOperationCallback createModificationOperationCallback(long resourceId, IHyracksTaskContext ctx) throws HyracksDataException {
        // TODO: Implement this one.
        TransactionSubsystem txnSubsystem = txnSubsystemProvider.getTransactionSubsystem(ctx);
        return null;
    }

    @Override
    public ISearchOperationCallback createSearchOperationCallback(long resourceId, IHyracksTaskContext ctx)
            throws HyracksDataException {
        TransactionSubsystem txnSubsystem = txnSubsystemProvider.getTransactionSubsystem(ctx);
        try {
            TransactionContext txnCtx = txnSubsystem.getTransactionManager().getTransactionContext(jobId);
            return new SearchOperationCallback(datasetId, entityIdFields, entityIdFieldHashFunctions,
                    txnSubsystem.getLockManager(), txnCtx);
        } catch (ACIDException e) {
            throw new HyracksDataException(e);
        }
    }

}
