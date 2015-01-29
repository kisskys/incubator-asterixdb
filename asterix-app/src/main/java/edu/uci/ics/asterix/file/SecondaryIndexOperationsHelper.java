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

package edu.uci.ics.asterix.file;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.uci.ics.asterix.common.config.AsterixStorageProperties;
import edu.uci.ics.asterix.common.config.DatasetConfig.DatasetType;
import edu.uci.ics.asterix.common.config.DatasetConfig.ExternalFilePendingOp;
import edu.uci.ics.asterix.common.config.DatasetConfig.IndexType;
import edu.uci.ics.asterix.common.config.DatasetConfig.IndexTypeProperty;
import edu.uci.ics.asterix.common.config.IAsterixPropertiesProvider;
import edu.uci.ics.asterix.common.context.AsterixVirtualBufferCacheProvider;
import edu.uci.ics.asterix.common.context.ITransactionSubsystemProvider;
import edu.uci.ics.asterix.common.context.TransactionSubsystemProvider;
import edu.uci.ics.asterix.common.exceptions.AsterixException;
import edu.uci.ics.asterix.common.ioopcallbacks.LSMBTreeIOOperationCallbackFactory;
import edu.uci.ics.asterix.common.transactions.IRecoveryManager.ResourceType;
import edu.uci.ics.asterix.common.transactions.JobId;
import edu.uci.ics.asterix.external.indexing.operators.ExternalIndexBulkModifyOperatorDescriptor;
import edu.uci.ics.asterix.formats.nontagged.AqlBinaryBooleanInspectorImpl;
import edu.uci.ics.asterix.formats.nontagged.AqlBinaryComparatorFactoryProvider;
import edu.uci.ics.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import edu.uci.ics.asterix.formats.nontagged.AqlTypeTraitProvider;
import edu.uci.ics.asterix.metadata.MetadataException;
import edu.uci.ics.asterix.metadata.declared.AqlMetadataProvider;
import edu.uci.ics.asterix.metadata.entities.Dataset;
import edu.uci.ics.asterix.metadata.entities.ExternalFile;
import edu.uci.ics.asterix.metadata.entities.Index;
import edu.uci.ics.asterix.metadata.external.IndexingConstants;
import edu.uci.ics.asterix.metadata.feeds.ExternalDataScanOperatorDescriptor;
import edu.uci.ics.asterix.metadata.utils.DatasetUtils;
import edu.uci.ics.asterix.om.types.ARecordType;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.IAType;
import edu.uci.ics.asterix.om.util.AsterixAppContextInfo;
import edu.uci.ics.asterix.runtime.evaluators.functions.AndDescriptor;
import edu.uci.ics.asterix.runtime.evaluators.functions.IsNullDescriptor;
import edu.uci.ics.asterix.runtime.evaluators.functions.NotDescriptor;
import edu.uci.ics.asterix.runtime.job.listener.JobEventListenerFactory;
import edu.uci.ics.asterix.transaction.management.opcallbacks.PrimaryIndexInstantSearchOperationCallbackFactory;
import edu.uci.ics.asterix.transaction.management.opcallbacks.PrimaryIndexOperationTrackerProvider;
import edu.uci.ics.asterix.transaction.management.service.transaction.AsterixRuntimeComponentsProvider;
import edu.uci.ics.asterix.transaction.management.service.transaction.JobIdFactory;
import edu.uci.ics.hyracks.algebricks.common.constraints.AlgebricksPartitionConstraint;
import edu.uci.ics.hyracks.algebricks.common.constraints.AlgebricksPartitionConstraintHelper;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.common.utils.Pair;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.LogicalExpressionJobGenToExpressionRuntimeProviderAdapter;
import edu.uci.ics.hyracks.algebricks.core.rewriter.base.PhysicalOptimizationConfig;
import edu.uci.ics.hyracks.algebricks.data.IBinaryComparatorFactoryProvider;
import edu.uci.ics.hyracks.algebricks.data.ISerializerDeserializerProvider;
import edu.uci.ics.hyracks.algebricks.data.ITypeTraitProvider;
import edu.uci.ics.hyracks.algebricks.runtime.base.ICopyEvaluatorFactory;
import edu.uci.ics.hyracks.algebricks.runtime.base.IPushRuntimeFactory;
import edu.uci.ics.hyracks.algebricks.runtime.base.IScalarEvaluatorFactory;
import edu.uci.ics.hyracks.algebricks.runtime.evaluators.ColumnAccessEvalFactory;
import edu.uci.ics.hyracks.algebricks.runtime.operators.meta.AlgebricksMetaOperatorDescriptor;
import edu.uci.ics.hyracks.algebricks.runtime.operators.std.AssignRuntimeFactory;
import edu.uci.ics.hyracks.algebricks.runtime.operators.std.StreamSelectRuntimeFactory;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;
import edu.uci.ics.hyracks.api.dataflow.value.ISerializerDeserializer;
import edu.uci.ics.hyracks.api.dataflow.value.ITypeTraits;
import edu.uci.ics.hyracks.api.dataflow.value.RecordDescriptor;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.api.job.IJobletEventListenerFactory;
import edu.uci.ics.hyracks.api.job.JobSpecification;
import edu.uci.ics.hyracks.dataflow.common.comm.io.ArrayTupleBuilder;
import edu.uci.ics.hyracks.dataflow.common.data.marshalling.IntegerSerializerDeserializer;
import edu.uci.ics.hyracks.dataflow.std.base.AbstractOperatorDescriptor;
import edu.uci.ics.hyracks.dataflow.std.file.IFileSplitProvider;
import edu.uci.ics.hyracks.dataflow.std.misc.ConstantTupleSourceOperatorDescriptor;
import edu.uci.ics.hyracks.dataflow.std.sort.ExternalSortOperatorDescriptor;
import edu.uci.ics.hyracks.storage.am.btree.dataflow.BTreeSearchOperatorDescriptor;
import edu.uci.ics.hyracks.storage.am.common.api.ISearchOperationCallbackFactory;
import edu.uci.ics.hyracks.storage.am.common.dataflow.IIndexDataflowHelperFactory;
import edu.uci.ics.hyracks.storage.am.common.dataflow.TreeIndexBulkLoadOperatorDescriptor;
import edu.uci.ics.hyracks.storage.am.common.impls.NoOpOperationCallbackFactory;
import edu.uci.ics.hyracks.storage.am.lsm.btree.dataflow.LSMBTreeDataflowHelperFactory;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMMergePolicyFactory;

@SuppressWarnings("rawtypes")
// TODO: We should eventually have a hierarchy of classes that can create all
// possible index job specs,
// not just for creation.
public abstract class SecondaryIndexOperationsHelper {
    protected final PhysicalOptimizationConfig physOptConf;
    protected int numPrimaryKeys;
    protected int numSecondaryKeys;
    protected AqlMetadataProvider metadataProvider;
    protected String dataverseName;
    protected String datasetName;
    protected Dataset dataset;
    protected ARecordType itemType;
    protected ISerializerDeserializer payloadSerde;
    protected IFileSplitProvider primaryFileSplitProvider;
    protected AlgebricksPartitionConstraint primaryPartitionConstraint;
    protected IFileSplitProvider secondaryFileSplitProvider;
    protected AlgebricksPartitionConstraint secondaryPartitionConstraint;
    protected String secondaryIndexName;
    protected boolean anySecondaryKeyIsNullable = false;

    protected long numElementsHint;
    protected IBinaryComparatorFactory[] primaryComparatorFactories;
    protected int[] primaryBloomFilterKeyFields;
    protected RecordDescriptor primaryRecDesc;
    protected IBinaryComparatorFactory[] secondaryComparatorFactories;
    protected ITypeTraits[] secondaryTypeTraits;
    protected int[] secondaryBloomFilterKeyFields;
    protected RecordDescriptor secondaryRecDesc;
    protected ICopyEvaluatorFactory[] secondaryFieldAccessEvalFactories;

    protected IAsterixPropertiesProvider propertiesProvider;
    protected ILSMMergePolicyFactory mergePolicyFactory;
    protected Map<String, String> mergePolicyFactoryProperties;

    protected int numFilterFields;
    protected String filterFieldName;
    protected ITypeTraits[] filterTypeTraits;
    protected IBinaryComparatorFactory[] filterCmpFactories;
    protected int[] secondaryFilterFields;
    protected int[] primaryFilterFields;
    protected int[] primaryBTreeFields;
    protected int[] secondaryBTreeFields;
    protected List<ExternalFile> externalFiles;

    // Prevent public construction. Should be created via createIndexCreator().
    protected SecondaryIndexOperationsHelper(PhysicalOptimizationConfig physOptConf,
            IAsterixPropertiesProvider propertiesProvider) {
        this.physOptConf = physOptConf;
        this.propertiesProvider = propertiesProvider;
    }

    public static SecondaryIndexOperationsHelper createIndexOperationsHelper(IndexType indexType, IndexTypeProperty indexTypeProperty,
            String dataverseName, String datasetName, String indexName, List<String> secondaryKeyFields, AqlMetadataProvider metadataProvider,
            PhysicalOptimizationConfig physOptConf) throws AsterixException,
            AlgebricksException {
        IAsterixPropertiesProvider asterixPropertiesProvider = AsterixAppContextInfo.getInstance();
        SecondaryIndexOperationsHelper indexOperationsHelper = null;
        switch (indexType) {
            case BTREE: 
            case STATIC_HILBERT_BTREE: {
                indexOperationsHelper = new SecondaryBTreeOperationsHelper(physOptConf, asterixPropertiesProvider);
                break;
            }
            case RTREE: {
                indexOperationsHelper = new SecondaryRTreeOperationsHelper(physOptConf, asterixPropertiesProvider);
                break;
            }
            case SINGLE_PARTITION_WORD_INVIX:
            case SINGLE_PARTITION_NGRAM_INVIX:
            case LENGTH_PARTITIONED_WORD_INVIX:
            case LENGTH_PARTITIONED_NGRAM_INVIX: {
                indexOperationsHelper = new SecondaryInvertedIndexOperationsHelper(physOptConf,
                        asterixPropertiesProvider);
                break;
            }
            default: {
                throw new AsterixException("Unknown Index Type: " + indexType);
            }
        }
        indexOperationsHelper.init(indexType, indexTypeProperty, dataverseName, datasetName, indexName, secondaryKeyFields,
                metadataProvider);
        return indexOperationsHelper;
    }
    
    public abstract void setSecondaryRecDescAndComparators(IndexType indexType, IndexTypeProperty indexTypeProperty,
            List<String> secondaryKeyFields, AqlMetadataProvider metadataProvider) throws AlgebricksException, AsterixException;

    public abstract JobSpecification buildCreationJobSpec() throws AsterixException, AlgebricksException;

    public abstract JobSpecification buildLoadingJobSpec() throws AsterixException, AlgebricksException;

    public abstract JobSpecification buildCompactJobSpec() throws AsterixException, AlgebricksException;

    protected void init(IndexType indexType, IndexTypeProperty indexTypeProperty, String dvn, String dsn, String in,
            List<String> secondaryKeyFields, AqlMetadataProvider metadataProvider) throws AsterixException, AlgebricksException {
        this.metadataProvider = metadataProvider;
        dataverseName = dvn == null ? metadataProvider.getDefaultDataverseName() : dvn;
        datasetName = dsn;
        secondaryIndexName = in;
        dataset = metadataProvider.findDataset(dataverseName, datasetName);
        if (dataset == null) {
            throw new AsterixException("Unknown dataset " + datasetName);
        }

        itemType = (ARecordType) metadataProvider.findType(dataset.getDataverseName(), dataset.getItemTypeName());
        payloadSerde = AqlSerializerDeserializerProvider.INSTANCE.getSerializerDeserializer(itemType);
        numSecondaryKeys = secondaryKeyFields.size();
        Pair<IFileSplitProvider, AlgebricksPartitionConstraint> secondarySplitsAndConstraint = metadataProvider
                .splitProviderAndPartitionConstraintsForDataset(dataverseName, datasetName, secondaryIndexName);
        secondaryFileSplitProvider = secondarySplitsAndConstraint.first;
        secondaryPartitionConstraint = secondarySplitsAndConstraint.second;

        if (dataset.getDatasetType() == DatasetType.EXTERNAL) {
            numPrimaryKeys = ExternalIndexingOperations.getRIDSize(dataset);
        } else {
            filterFieldName = DatasetUtils.getFilterField(dataset);
            if (filterFieldName != null) {
                numFilterFields = 1;
            } else {
                numFilterFields = 0;
            }

            numPrimaryKeys = DatasetUtils.getPartitioningKeys(dataset).size();
            Pair<IFileSplitProvider, AlgebricksPartitionConstraint> primarySplitsAndConstraint = metadataProvider
                    .splitProviderAndPartitionConstraintsForDataset(dataverseName, datasetName, datasetName);
            primaryFileSplitProvider = primarySplitsAndConstraint.first;
            primaryPartitionConstraint = primarySplitsAndConstraint.second;
            setPrimaryRecDescAndComparators();
        }
        setSecondaryRecDescAndComparators(indexType, indexTypeProperty, secondaryKeyFields, metadataProvider);
        numElementsHint = metadataProvider.getCardinalityPerPartitionHint(dataset);
        Pair<ILSMMergePolicyFactory, Map<String, String>> compactionInfo = DatasetUtils.getMergePolicyFactory(dataset,
                metadataProvider.getMetadataTxnContext());
        mergePolicyFactory = compactionInfo.first;
        mergePolicyFactoryProperties = compactionInfo.second;

        if (numFilterFields > 0) {
            setFilterTypeTraitsAndComparators();
        }
    }

    protected void setFilterTypeTraitsAndComparators() throws AlgebricksException {
        filterTypeTraits = new ITypeTraits[numFilterFields];
        filterCmpFactories = new IBinaryComparatorFactory[numFilterFields];
        secondaryFilterFields = new int[numFilterFields];
        primaryFilterFields = new int[numFilterFields];
        primaryBTreeFields = new int[numPrimaryKeys + 1];
        secondaryBTreeFields = new int[numSecondaryKeys + numPrimaryKeys];
        for (int i = 0; i < primaryBTreeFields.length; i++) {
            primaryBTreeFields[i] = i;
        }
        for (int i = 0; i < secondaryBTreeFields.length; i++) {
            secondaryBTreeFields[i] = i;
        }

        IAType type;
        try {
            type = itemType.getFieldType(filterFieldName);
        } catch (IOException e) {
            throw new AlgebricksException(e);
        }
        filterCmpFactories[0] = AqlBinaryComparatorFactoryProvider.INSTANCE.getBinaryComparatorFactory(type, true);
        filterTypeTraits[0] = AqlTypeTraitProvider.INSTANCE.getTypeTrait(type);
        secondaryFilterFields[0] = getNumSecondaryKeys() + numPrimaryKeys;
        primaryFilterFields[0] = numPrimaryKeys + 1;
    }

    protected abstract int getNumSecondaryKeys();

    protected void setPrimaryRecDescAndComparators() throws AlgebricksException {
        List<String> partitioningKeys = DatasetUtils.getPartitioningKeys(dataset);
        int numPrimaryKeys = partitioningKeys.size();
        ISerializerDeserializer[] primaryRecFields = new ISerializerDeserializer[numPrimaryKeys + 1];
        ITypeTraits[] primaryTypeTraits = new ITypeTraits[numPrimaryKeys + 1];
        primaryComparatorFactories = new IBinaryComparatorFactory[numPrimaryKeys];
        primaryBloomFilterKeyFields = new int[numPrimaryKeys];
        ISerializerDeserializerProvider serdeProvider = metadataProvider.getFormat().getSerdeProvider();
        for (int i = 0; i < numPrimaryKeys; i++) {
            IAType keyType;
            try {
                keyType = itemType.getFieldType(partitioningKeys.get(i));
            } catch (IOException e) {
                throw new AlgebricksException(e);
            }
            primaryRecFields[i] = serdeProvider.getSerializerDeserializer(keyType);
            primaryComparatorFactories[i] = AqlBinaryComparatorFactoryProvider.INSTANCE.getBinaryComparatorFactory(
                    keyType, true);
            primaryTypeTraits[i] = AqlTypeTraitProvider.INSTANCE.getTypeTrait(keyType);
            primaryBloomFilterKeyFields[i] = i;
        }
        primaryRecFields[numPrimaryKeys] = payloadSerde;
        primaryTypeTraits[numPrimaryKeys] = AqlTypeTraitProvider.INSTANCE.getTypeTrait(itemType);
        primaryRecDesc = new RecordDescriptor(primaryRecFields, primaryTypeTraits);
    }


    protected AbstractOperatorDescriptor createDummyKeyProviderOp(JobSpecification spec) throws AsterixException,
            AlgebricksException {
        // Build dummy tuple containing one field with a dummy value inside.
        ArrayTupleBuilder tb = new ArrayTupleBuilder(1);
        DataOutput dos = tb.getDataOutput();
        tb.reset();
        try {
            // Serialize dummy value into a field.
            IntegerSerializerDeserializer.INSTANCE.serialize(0, dos);
        } catch (HyracksDataException e) {
            throw new AsterixException(e);
        }
        // Add dummy field.
        tb.addFieldEndOffset();
        ISerializerDeserializer[] keyRecDescSers = { IntegerSerializerDeserializer.INSTANCE };
        RecordDescriptor keyRecDesc = new RecordDescriptor(keyRecDescSers);
        ConstantTupleSourceOperatorDescriptor keyProviderOp = new ConstantTupleSourceOperatorDescriptor(spec,
                keyRecDesc, tb.getFieldEndOffsets(), tb.getByteArray(), tb.getSize());
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, keyProviderOp,
                primaryPartitionConstraint);
        return keyProviderOp;
    }

    protected BTreeSearchOperatorDescriptor createPrimaryIndexScanOp(JobSpecification spec) throws AlgebricksException {
        // -Infinity
        int[] lowKeyFields = null;
        // +Infinity
        int[] highKeyFields = null;
        ITransactionSubsystemProvider txnSubsystemProvider = new TransactionSubsystemProvider();
        JobId jobId = JobIdFactory.generateJobId();
        metadataProvider.setJobId(jobId);
        boolean isWriteTransaction = metadataProvider.isWriteTransaction();
        IJobletEventListenerFactory jobEventListenerFactory = new JobEventListenerFactory(jobId, isWriteTransaction);
        spec.setJobletEventListenerFactory(jobEventListenerFactory);

        ISearchOperationCallbackFactory searchCallbackFactory = new PrimaryIndexInstantSearchOperationCallbackFactory(
                jobId, dataset.getDatasetId(), primaryBloomFilterKeyFields, txnSubsystemProvider,
                ResourceType.LSM_BTREE);
        AsterixStorageProperties storageProperties = propertiesProvider.getStorageProperties();
        BTreeSearchOperatorDescriptor primarySearchOp = new BTreeSearchOperatorDescriptor(spec, primaryRecDesc,
                AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER, AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                primaryFileSplitProvider, primaryRecDesc.getTypeTraits(), primaryComparatorFactories,
                primaryBloomFilterKeyFields, lowKeyFields, highKeyFields, true, true,
                new LSMBTreeDataflowHelperFactory(new AsterixVirtualBufferCacheProvider(dataset.getDatasetId()),
                        mergePolicyFactory, mergePolicyFactoryProperties, new PrimaryIndexOperationTrackerProvider(
                                dataset.getDatasetId()), AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                        LSMBTreeIOOperationCallbackFactory.INSTANCE, storageProperties
                                .getBloomFilterFalsePositiveRate(), true, filterTypeTraits, filterCmpFactories,
                        primaryBTreeFields, primaryFilterFields, null), false, false, null,
                searchCallbackFactory, null, null);

        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, primarySearchOp,
                primaryPartitionConstraint);
        return primarySearchOp;
    }

    protected AlgebricksMetaOperatorDescriptor createAssignOp(JobSpecification spec,
            BTreeSearchOperatorDescriptor primaryScanOp, int numSecondaryKeyFields) throws AlgebricksException {
        int[] outColumns = new int[numSecondaryKeyFields + numFilterFields];
        int[] projectionList = new int[numSecondaryKeyFields + numPrimaryKeys + numFilterFields];
        for (int i = 0; i < numSecondaryKeyFields + numFilterFields; i++) {
            outColumns[i] = numPrimaryKeys + i;
        }
        int projCount = 0;
        for (int i = 0; i < numSecondaryKeyFields; i++) {
            projectionList[projCount++] = numPrimaryKeys + i;
        }
        for (int i = 0; i < numPrimaryKeys; i++) {
            projectionList[projCount++] = i;
        }
        if (numFilterFields > 0) {
            projectionList[projCount++] = numPrimaryKeys + numSecondaryKeyFields;
        }

        IScalarEvaluatorFactory[] sefs = new IScalarEvaluatorFactory[secondaryFieldAccessEvalFactories.length];
        for (int i = 0; i < secondaryFieldAccessEvalFactories.length; ++i) {
            sefs[i] = new LogicalExpressionJobGenToExpressionRuntimeProviderAdapter.ScalarEvaluatorFactoryAdapter(
                    secondaryFieldAccessEvalFactories[i]);
        }
        AssignRuntimeFactory assign = new AssignRuntimeFactory(outColumns, sefs, projectionList);
        AlgebricksMetaOperatorDescriptor asterixAssignOp = new AlgebricksMetaOperatorDescriptor(spec, 1, 1,
                new IPushRuntimeFactory[] { assign }, new RecordDescriptor[] { secondaryRecDesc });
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, asterixAssignOp,
                primaryPartitionConstraint);
        return asterixAssignOp;
    }

    protected ExternalSortOperatorDescriptor createSortOp(JobSpecification spec,
            IBinaryComparatorFactory[] secondaryComparatorFactories, RecordDescriptor secondaryRecDesc) {
        int[] sortFields = new int[secondaryComparatorFactories.length];
        for (int i = 0; i < secondaryComparatorFactories.length; i++) {
            sortFields[i] = i;
        }
        ExternalSortOperatorDescriptor sortOp = new ExternalSortOperatorDescriptor(spec,
                physOptConf.getMaxFramesExternalSort(), sortFields, secondaryComparatorFactories, secondaryRecDesc);
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, sortOp, primaryPartitionConstraint);
        return sortOp;
    }

    protected TreeIndexBulkLoadOperatorDescriptor createTreeIndexBulkLoadOp(JobSpecification spec,
            int numSecondaryKeyFields, IIndexDataflowHelperFactory dataflowHelperFactory, float fillFactor)
            throws MetadataException, AlgebricksException {
        int[] fieldPermutation = new int[numSecondaryKeyFields + numPrimaryKeys + numFilterFields];
        for (int i = 0; i < fieldPermutation.length; i++) {
            fieldPermutation[i] = i;
        }
        TreeIndexBulkLoadOperatorDescriptor treeIndexBulkLoadOp = new TreeIndexBulkLoadOperatorDescriptor(spec,
                secondaryRecDesc, AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER, secondaryFileSplitProvider,
                secondaryRecDesc.getTypeTraits(), secondaryComparatorFactories, secondaryBloomFilterKeyFields,
                fieldPermutation, fillFactor, false, numElementsHint, false, dataflowHelperFactory);
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, treeIndexBulkLoadOp,
                secondaryPartitionConstraint);
        return treeIndexBulkLoadOp;
    }

    public AlgebricksMetaOperatorDescriptor createFilterNullsSelectOp(JobSpecification spec, int numSecondaryKeyFields)
            throws AlgebricksException {
        ICopyEvaluatorFactory[] andArgsEvalFactories = new ICopyEvaluatorFactory[numSecondaryKeyFields];
        NotDescriptor notDesc = new NotDescriptor();
        IsNullDescriptor isNullDesc = new IsNullDescriptor();
        for (int i = 0; i < numSecondaryKeyFields; i++) {
            // Access column i, and apply 'is not null'.
            ColumnAccessEvalFactory columnAccessEvalFactory = new ColumnAccessEvalFactory(i);
            ICopyEvaluatorFactory isNullEvalFactory = isNullDesc
                    .createEvaluatorFactory(new ICopyEvaluatorFactory[] { columnAccessEvalFactory });
            ICopyEvaluatorFactory notEvalFactory = notDesc
                    .createEvaluatorFactory(new ICopyEvaluatorFactory[] { isNullEvalFactory });
            andArgsEvalFactories[i] = notEvalFactory;
        }
        ICopyEvaluatorFactory selectCond = null;
        if (numSecondaryKeyFields > 1) {
            // Create conjunctive condition where all secondary index keys must
            // satisfy 'is not null'.
            AndDescriptor andDesc = new AndDescriptor();
            selectCond = andDesc.createEvaluatorFactory(andArgsEvalFactories);
        } else {
            selectCond = andArgsEvalFactories[0];
        }
        StreamSelectRuntimeFactory select = new StreamSelectRuntimeFactory(
                new LogicalExpressionJobGenToExpressionRuntimeProviderAdapter.ScalarEvaluatorFactoryAdapter(selectCond),
                null, AqlBinaryBooleanInspectorImpl.FACTORY, false, -1, null);
        AlgebricksMetaOperatorDescriptor asterixSelectOp = new AlgebricksMetaOperatorDescriptor(spec, 1, 1,
                new IPushRuntimeFactory[] { select }, new RecordDescriptor[] { secondaryRecDesc });
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, asterixSelectOp,
                primaryPartitionConstraint);
        return asterixSelectOp;
    }

    // This method creates a source indexing operator for external data
    protected ExternalDataScanOperatorDescriptor createExternalIndexingOp(JobSpecification spec)
            throws AlgebricksException, AsterixException {
        // A record + primary keys
        ISerializerDeserializer[] serdes = new ISerializerDeserializer[1 + numPrimaryKeys];
        ITypeTraits[] typeTraits = new ITypeTraits[1 + numPrimaryKeys];
        // payload serde and type traits for the record slot
        serdes[0] = payloadSerde;
        typeTraits[0] = AqlTypeTraitProvider.INSTANCE.getTypeTrait(itemType);
        //  serdes and type traits for rid fields
        for (int i = 1; i < serdes.length; i++) {
            serdes[i] = IndexingConstants.getSerializerDeserializer(i - 1);
            typeTraits[i] = IndexingConstants.getTypeTraits(i - 1);
        }
        // output record desc
        RecordDescriptor indexerDesc = new RecordDescriptor(serdes, typeTraits);

        // Create the operator and its partition constraits
        Pair<ExternalDataScanOperatorDescriptor, AlgebricksPartitionConstraint> indexingOpAndConstraints;
        try {
            indexingOpAndConstraints = ExternalIndexingOperations.createExternalIndexingOp(spec, metadataProvider,
                    dataset, itemType, indexerDesc, externalFiles);
        } catch (Exception e) {
            throw new AlgebricksException(e);
        }
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, indexingOpAndConstraints.first,
                indexingOpAndConstraints.second);

        // Set the primary partition constraints to this partition constraints
        primaryPartitionConstraint = indexingOpAndConstraints.second;
        return indexingOpAndConstraints.first;
    }

    protected AlgebricksMetaOperatorDescriptor createExternalAssignOp(JobSpecification spec, int numSecondaryKeys)
            throws AlgebricksException {
        int[] outColumns = new int[numSecondaryKeys];
        int[] projectionList = new int[numSecondaryKeys + numPrimaryKeys];
        for (int i = 0; i < numSecondaryKeys; i++) {
            outColumns[i] = i + numPrimaryKeys + 1;
            projectionList[i] = i + numPrimaryKeys + 1;
        }

        IScalarEvaluatorFactory[] sefs = new IScalarEvaluatorFactory[secondaryFieldAccessEvalFactories.length];
        for (int i = 0; i < secondaryFieldAccessEvalFactories.length; ++i) {
            sefs[i] = new LogicalExpressionJobGenToExpressionRuntimeProviderAdapter.ScalarEvaluatorFactoryAdapter(
                    secondaryFieldAccessEvalFactories[i]);
        }
        //add External RIDs to the projection list
        for (int i = 0; i < numPrimaryKeys; i++) {
            projectionList[numSecondaryKeys + i] = i + 1;
        }

        AssignRuntimeFactory assign = new AssignRuntimeFactory(outColumns, sefs, projectionList);
        AlgebricksMetaOperatorDescriptor asterixAssignOp = new AlgebricksMetaOperatorDescriptor(spec, 1, 1,
                new IPushRuntimeFactory[] { assign }, new RecordDescriptor[] { secondaryRecDesc });
        return asterixAssignOp;
    }

    protected ExternalIndexBulkModifyOperatorDescriptor createExternalIndexBulkModifyOp(JobSpecification spec,
            int numSecondaryKeyFields, IIndexDataflowHelperFactory dataflowHelperFactory, float fillFactor)
            throws MetadataException, AlgebricksException {
        int[] fieldPermutation = new int[numSecondaryKeyFields + numPrimaryKeys];
        for (int i = 0; i < numSecondaryKeyFields + numPrimaryKeys; i++) {
            fieldPermutation[i] = i;
        }
        // create a list of file ids
        int numOfDeletedFiles = 0;
        for (ExternalFile file : externalFiles) {
            if (file.getPendingOp() == ExternalFilePendingOp.PENDING_DROP_OP)
                numOfDeletedFiles++;
        }
        int[] deletedFiles = new int[numOfDeletedFiles];
        int i = 0;
        for (ExternalFile file : externalFiles) {
            if (file.getPendingOp() == ExternalFilePendingOp.PENDING_DROP_OP) {
                deletedFiles[i] = file.getFileNumber();
            }
        }
        ExternalIndexBulkModifyOperatorDescriptor treeIndexBulkLoadOp = new ExternalIndexBulkModifyOperatorDescriptor(
                spec, AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER,
                AsterixRuntimeComponentsProvider.RUNTIME_PROVIDER, secondaryFileSplitProvider, secondaryTypeTraits,
                secondaryComparatorFactories, secondaryBloomFilterKeyFields, dataflowHelperFactory,
                NoOpOperationCallbackFactory.INSTANCE, deletedFiles, fieldPermutation, fillFactor, numElementsHint);
        AlgebricksPartitionConstraintHelper.setPartitionConstraintInJobSpec(spec, treeIndexBulkLoadOp,
                secondaryPartitionConstraint);
        return treeIndexBulkLoadOp;
    }

    public List<ExternalFile> getExternalFiles() {
        return externalFiles;
    }

    public void setExternalFiles(List<ExternalFile> externalFiles) {
        this.externalFiles = externalFiles;
    }
}
