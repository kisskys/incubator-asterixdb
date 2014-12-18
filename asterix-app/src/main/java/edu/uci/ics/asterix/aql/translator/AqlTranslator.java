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
package edu.uci.ics.asterix.aql.translator;

import java.io.File;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.asterix.api.common.APIFramework;
import edu.uci.ics.asterix.api.common.APIFramework.OutputFormat;
import edu.uci.ics.asterix.api.common.Job;
import edu.uci.ics.asterix.api.common.SessionConfig;
import edu.uci.ics.asterix.aql.base.Statement;
import edu.uci.ics.asterix.aql.expression.CompactStatement;
import edu.uci.ics.asterix.aql.expression.ConnectFeedStatement;
import edu.uci.ics.asterix.aql.expression.CreateDataverseStatement;
import edu.uci.ics.asterix.aql.expression.CreateFeedStatement;
import edu.uci.ics.asterix.aql.expression.CreateFunctionStatement;
import edu.uci.ics.asterix.aql.expression.CreateIndexStatement;
import edu.uci.ics.asterix.aql.expression.DatasetDecl;
import edu.uci.ics.asterix.aql.expression.DataverseDecl;
import edu.uci.ics.asterix.aql.expression.DataverseDropStatement;
import edu.uci.ics.asterix.aql.expression.DeleteStatement;
import edu.uci.ics.asterix.aql.expression.DisconnectFeedStatement;
import edu.uci.ics.asterix.aql.expression.DropStatement;
import edu.uci.ics.asterix.aql.expression.ExternalDetailsDecl;
import edu.uci.ics.asterix.aql.expression.FeedDropStatement;
import edu.uci.ics.asterix.aql.expression.FunctionDecl;
import edu.uci.ics.asterix.aql.expression.FunctionDropStatement;
import edu.uci.ics.asterix.aql.expression.Identifier;
import edu.uci.ics.asterix.aql.expression.IndexDropStatement;
import edu.uci.ics.asterix.aql.expression.InsertStatement;
import edu.uci.ics.asterix.aql.expression.InternalDetailsDecl;
import edu.uci.ics.asterix.aql.expression.LoadStatement;
import edu.uci.ics.asterix.aql.expression.NodeGroupDropStatement;
import edu.uci.ics.asterix.aql.expression.NodegroupDecl;
import edu.uci.ics.asterix.aql.expression.Query;
import edu.uci.ics.asterix.aql.expression.RefreshExternalDatasetStatement;
import edu.uci.ics.asterix.aql.expression.SetStatement;
import edu.uci.ics.asterix.aql.expression.TypeDecl;
import edu.uci.ics.asterix.aql.expression.TypeDropStatement;
import edu.uci.ics.asterix.aql.expression.WriteStatement;
import edu.uci.ics.asterix.aql.util.FunctionUtils;
import edu.uci.ics.asterix.common.config.DatasetConfig.DatasetType;
import edu.uci.ics.asterix.common.config.DatasetConfig.ExternalDatasetTransactionState;
import edu.uci.ics.asterix.common.config.DatasetConfig.ExternalFilePendingOp;
import edu.uci.ics.asterix.common.config.DatasetConfig.IndexType;
import edu.uci.ics.asterix.common.config.GlobalConfig;
import edu.uci.ics.asterix.common.exceptions.ACIDException;
import edu.uci.ics.asterix.common.exceptions.AsterixException;
import edu.uci.ics.asterix.common.feeds.FeedConnectionId;
import edu.uci.ics.asterix.common.functions.FunctionSignature;
import edu.uci.ics.asterix.file.DatasetOperations;
import edu.uci.ics.asterix.file.DataverseOperations;
import edu.uci.ics.asterix.file.ExternalIndexingOperations;
import edu.uci.ics.asterix.file.FeedOperations;
import edu.uci.ics.asterix.file.IndexOperations;
import edu.uci.ics.asterix.metadata.IDatasetDetails;
import edu.uci.ics.asterix.metadata.MetadataException;
import edu.uci.ics.asterix.metadata.MetadataManager;
import edu.uci.ics.asterix.metadata.MetadataTransactionContext;
import edu.uci.ics.asterix.metadata.api.IMetadataEntity;
import edu.uci.ics.asterix.metadata.bootstrap.MetadataConstants;
import edu.uci.ics.asterix.metadata.dataset.hints.DatasetHints;
import edu.uci.ics.asterix.metadata.dataset.hints.DatasetHints.DatasetNodegroupCardinalityHint;
import edu.uci.ics.asterix.metadata.declared.AqlMetadataProvider;
import edu.uci.ics.asterix.metadata.entities.CompactionPolicy;
import edu.uci.ics.asterix.metadata.entities.Dataset;
import edu.uci.ics.asterix.metadata.entities.Datatype;
import edu.uci.ics.asterix.metadata.entities.Dataverse;
import edu.uci.ics.asterix.metadata.entities.ExternalDatasetDetails;
import edu.uci.ics.asterix.metadata.entities.ExternalFile;
import edu.uci.ics.asterix.metadata.entities.Feed;
import edu.uci.ics.asterix.metadata.entities.FeedActivity;
import edu.uci.ics.asterix.metadata.entities.FeedPolicy;
import edu.uci.ics.asterix.metadata.entities.Function;
import edu.uci.ics.asterix.metadata.entities.Index;
import edu.uci.ics.asterix.metadata.entities.InternalDatasetDetails;
import edu.uci.ics.asterix.metadata.entities.NodeGroup;
import edu.uci.ics.asterix.metadata.feeds.BuiltinFeedPolicies;
import edu.uci.ics.asterix.metadata.feeds.FeedUtil;
import edu.uci.ics.asterix.metadata.utils.ExternalDatasetsRegistry;
import edu.uci.ics.asterix.metadata.utils.MetadataLockManager;
import edu.uci.ics.asterix.om.types.ARecordType;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.asterix.om.types.IAType;
import edu.uci.ics.asterix.om.types.TypeSignature;
import edu.uci.ics.asterix.om.util.AsterixAppContextInfo;
import edu.uci.ics.asterix.result.ResultReader;
import edu.uci.ics.asterix.result.ResultUtils;
import edu.uci.ics.asterix.transaction.management.service.transaction.DatasetIdFactory;
import edu.uci.ics.asterix.translator.AbstractAqlTranslator;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledConnectFeedStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledCreateIndexStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledDatasetDropStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledDeleteStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledIndexCompactStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledIndexDropStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledInsertStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.CompiledLoadFromFileStatement;
import edu.uci.ics.asterix.translator.CompiledStatements.ICompiledDmlStatement;
import edu.uci.ics.asterix.translator.TypeTranslator;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.common.utils.Pair;
import edu.uci.ics.hyracks.algebricks.core.algebra.expressions.AbstractFunctionCallExpression.FunctionKind;
import edu.uci.ics.hyracks.algebricks.data.IAWriterFactory;
import edu.uci.ics.hyracks.algebricks.data.IResultSerializerFactoryProvider;
import edu.uci.ics.hyracks.algebricks.runtime.serializer.ResultSerializerFactoryProvider;
import edu.uci.ics.hyracks.algebricks.runtime.writers.PrinterBasedWriterFactory;
import edu.uci.ics.hyracks.api.client.IHyracksClientConnection;
import edu.uci.ics.hyracks.api.dataset.IHyracksDataset;
import edu.uci.ics.hyracks.api.dataset.ResultSetId;
import edu.uci.ics.hyracks.api.io.FileReference;
import edu.uci.ics.hyracks.api.job.JobId;
import edu.uci.ics.hyracks.api.job.JobSpecification;
import edu.uci.ics.hyracks.dataflow.std.file.FileSplit;
import edu.uci.ics.hyracks.storage.am.lsm.common.api.ILSMMergePolicyFactory;

/*
 * Provides functionality for executing a batch of AQL statements (queries included)
 * sequentially.
 */
public class AqlTranslator extends AbstractAqlTranslator {

    private static Logger LOGGER = Logger.getLogger(AqlTranslator.class.getName());

    private enum ProgressState {
        NO_PROGRESS,
        ADDED_PENDINGOP_RECORD_TO_METADATA
    }

    public static enum ResultDelivery {
        SYNC,
        ASYNC,
        ASYNC_DEFERRED
    }

    public static final boolean IS_DEBUG_MODE = false;//true
    private final List<Statement> aqlStatements;
    private final PrintWriter out;
    private final SessionConfig sessionConfig;
    private final OutputFormat pdf;
    private Dataverse activeDefaultDataverse;
    private List<FunctionDecl> declaredFunctions;

    public AqlTranslator(List<Statement> aqlStatements, PrintWriter out, SessionConfig pc, APIFramework.OutputFormat pdf)
            throws MetadataException, AsterixException {
        this.aqlStatements = aqlStatements;
        this.out = out;
        this.sessionConfig = pc;
        this.pdf = pdf;
        declaredFunctions = getDeclaredFunctions(aqlStatements);
    }

    private List<FunctionDecl> getDeclaredFunctions(List<Statement> statements) {
        List<FunctionDecl> functionDecls = new ArrayList<FunctionDecl>();
        for (Statement st : statements) {
            if (st.getKind().equals(Statement.Kind.FUNCTION_DECL)) {
                functionDecls.add((FunctionDecl) st);
            }
        }
        return functionDecls;
    }

    /**
     * Compiles and submits for execution a list of AQL statements.
     * 
     * @param hcc
     *            A Hyracks client connection that is used to submit a jobspec to Hyracks.
     * @param hdc
     *            A Hyracks dataset client object that is used to read the results.
     * @param resultDelivery
     *            True if the results should be read asynchronously or false if we should wait for results to be read.
     * @return A List<QueryResult> containing a QueryResult instance corresponding to each submitted query.
     * @throws Exception
     */
    public void compileAndExecute(IHyracksClientConnection hcc, IHyracksDataset hdc,
            ResultDelivery resultDelivery) throws Exception {
        int resultSetIdCounter = 0;
        FileSplit outputFile = null;
        IAWriterFactory writerFactory = PrinterBasedWriterFactory.INSTANCE;
        IResultSerializerFactoryProvider resultSerializerFactoryProvider = ResultSerializerFactoryProvider.INSTANCE;
        Map<String, String> config = new HashMap<String, String>();

        for (Statement stmt : aqlStatements) {
            validateOperation(activeDefaultDataverse, stmt);
            AqlMetadataProvider metadataProvider = new AqlMetadataProvider(activeDefaultDataverse);
            metadataProvider.setWriterFactory(writerFactory);
            metadataProvider.setResultSerializerFactoryProvider(resultSerializerFactoryProvider);
            metadataProvider.setOutputFile(outputFile);
            metadataProvider.setConfig(config);
            switch (stmt.getKind()) {
                case SET: {
                    handleSetStatement(metadataProvider, stmt, config);
                    break;
                }
                case DATAVERSE_DECL: {
                    activeDefaultDataverse = handleUseDataverseStatement(metadataProvider, stmt);
                    break;
                }
                case CREATE_DATAVERSE: {
                    handleCreateDataverseStatement(metadataProvider, stmt);
                    break;
                }
                case DATASET_DECL: {
                    handleCreateDatasetStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case CREATE_INDEX: {
                    handleCreateIndexStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case TYPE_DECL: {
                    handleCreateTypeStatement(metadataProvider, stmt);
                    break;
                }
                case NODEGROUP_DECL: {
                    handleCreateNodeGroupStatement(metadataProvider, stmt);
                    break;
                }
                case DATAVERSE_DROP: {
                    handleDataverseDropStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case DATASET_DROP: {
                    handleDatasetDropStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case INDEX_DROP: {
                    handleIndexDropStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case TYPE_DROP: {
                    handleTypeDropStatement(metadataProvider, stmt);
                    break;
                }
                case NODEGROUP_DROP: {
                    handleNodegroupDropStatement(metadataProvider, stmt);
                    break;
                }

                case CREATE_FUNCTION: {
                    handleCreateFunctionStatement(metadataProvider, stmt);
                    break;
                }

                case FUNCTION_DROP: {
                    handleFunctionDropStatement(metadataProvider, stmt);
                    break;
                }

                case LOAD: {
                    handleLoadStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case INSERT: {
                    handleInsertStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case DELETE: {
                    handleDeleteStatement(metadataProvider, stmt, hcc);
                    break;
                }

                case CREATE_FEED: {
                    handleCreateFeedStatement(metadataProvider, stmt, hcc);
                    break;
                }

                case DROP_FEED: {
                    handleDropFeedStatement(metadataProvider, stmt, hcc);
                    break;
                }
                case CONNECT_FEED: {
                    handleConnectFeedStatement(metadataProvider, stmt, hcc);
                    break;
                }

                case DISCONNECT_FEED: {
                    handleDisconnectFeedStatement(metadataProvider, stmt, hcc);
                    break;
                }

                case QUERY: {
                    metadataProvider.setResultSetId(new ResultSetId(resultSetIdCounter++));
                    metadataProvider.setResultAsyncMode(resultDelivery == ResultDelivery.ASYNC
                            || resultDelivery == ResultDelivery.ASYNC_DEFERRED);
                    handleQuery(metadataProvider, (Query) stmt, hcc, hdc, resultDelivery);
                    break;
                }

                case COMPACT: {
                    handleCompactStatement(metadataProvider, stmt, hcc);
                    break;
                }

                case EXTERNAL_DATASET_REFRESH: {
                    handleExternalDatasetRefreshStatement(metadataProvider, stmt, hcc);
                    break;
                }

                case WRITE: {
                    Pair<IAWriterFactory, FileSplit> result = handleWriteStatement(metadataProvider, stmt);
                    if (result.first != null) {
                        writerFactory = result.first;
                    }
                    outputFile = result.second;
                    break;
                }
            }
        }
    }

    private void handleSetStatement(AqlMetadataProvider metadataProvider, Statement stmt, Map<String, String> config)
            throws RemoteException, ACIDException {
        SetStatement ss = (SetStatement) stmt;
        String pname = ss.getPropName();
        String pvalue = ss.getPropValue();
        config.put(pname, pvalue);
    }

    private Pair<IAWriterFactory, FileSplit> handleWriteStatement(AqlMetadataProvider metadataProvider, Statement stmt)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        WriteStatement ws = (WriteStatement) stmt;
        File f = new File(ws.getFileName());
        FileSplit outputFile = new FileSplit(ws.getNcName().getValue(), new FileReference(f));
        IAWriterFactory writerFactory = null;
        if (ws.getWriterClassName() != null) {
            writerFactory = (IAWriterFactory) Class.forName(ws.getWriterClassName()).newInstance();
        }
        return new Pair<IAWriterFactory, FileSplit>(writerFactory, outputFile);
    }

    private Dataverse handleUseDataverseStatement(AqlMetadataProvider metadataProvider, Statement stmt)
            throws Exception {
        DataverseDecl dvd = (DataverseDecl) stmt;
        String dvName = dvd.getDataverseName().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.acquireDataverseReadLock(dvName);
        try {
            Dataverse dv = MetadataManager.INSTANCE.getDataverse(metadataProvider.getMetadataTxnContext(), dvName);
            if (dv == null) {
                throw new MetadataException("Unknown dataverse " + dvName);
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            return dv;
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw new MetadataException(e);
        } finally {
            MetadataLockManager.INSTANCE.releaseDataverseReadLock(dvName);
        }
    }

    private void handleCreateDataverseStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {

        CreateDataverseStatement stmtCreateDataverse = (CreateDataverseStatement) stmt;
        String dvName = stmtCreateDataverse.getDataverseName().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);

        MetadataLockManager.INSTANCE.acquireDataverseReadLock(dvName);
        try {
            Dataverse dv = MetadataManager.INSTANCE.getDataverse(metadataProvider.getMetadataTxnContext(), dvName);
            if (dv != null) {
                if (stmtCreateDataverse.getIfNotExists()) {
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    return;
                } else {
                    throw new AlgebricksException("A dataverse with this name " + dvName + " already exists.");
                }
            }
            MetadataManager.INSTANCE.addDataverse(metadataProvider.getMetadataTxnContext(), new Dataverse(dvName,
                    stmtCreateDataverse.getFormat(), IMetadataEntity.PENDING_NO_OP));
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.releaseDataverseReadLock(dvName);
        }
    }

    private void validateCompactionPolicy(String compactionPolicy, Map<String, String> compactionPolicyProperties,
            MetadataTransactionContext mdTxnCtx, boolean isExternalDataset) throws AsterixException, Exception {
        CompactionPolicy compactionPolicyEntity = MetadataManager.INSTANCE.getCompactionPolicy(mdTxnCtx,
                MetadataConstants.METADATA_DATAVERSE_NAME, compactionPolicy);
        if (compactionPolicyEntity == null) {
            throw new AsterixException("Unknown compaction policy: " + compactionPolicy);
        }
        String compactionPolicyFactoryClassName = compactionPolicyEntity.getClassName();
        ILSMMergePolicyFactory mergePolicyFactory = (ILSMMergePolicyFactory) Class.forName(
                compactionPolicyFactoryClassName).newInstance();
        if (isExternalDataset && mergePolicyFactory.getName().compareTo("correlated-prefix") == 0) {
            throw new AsterixException("The correlated-prefix merge policy cannot be used with external dataset.");
        }
        if (compactionPolicyProperties == null) {
            if (mergePolicyFactory.getName().compareTo("no-merge") != 0) {
                throw new AsterixException("Compaction policy properties are missing.");
            }
        } else {
            for (Map.Entry<String, String> entry : compactionPolicyProperties.entrySet()) {
                if (!mergePolicyFactory.getPropertiesNames().contains(entry.getKey())) {
                    throw new AsterixException("Invalid compaction policy property: " + entry.getKey());
                }
            }
            for (String p : mergePolicyFactory.getPropertiesNames()) {
                if (!compactionPolicyProperties.containsKey(p)) {
                    throw new AsterixException("Missing compaction policy property: " + p);
                }
            }
        }
    }

    private void handleCreateDatasetStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws AsterixException, Exception {

        ProgressState progress = ProgressState.NO_PROGRESS;
        DatasetDecl dd = (DatasetDecl) stmt;
        String dataverseName = getActiveDataverseName(dd.getDataverse());
        String datasetName = dd.getName().getValue();
        DatasetType dsType = dd.getDatasetType();
        String itemTypeName = dd.getItemTypeName().getValue();
        Identifier ngNameId = dd.getDatasetDetailsDecl().getNodegroupName();
        String nodegroupName = getNodeGroupName(ngNameId, dd, dataverseName);
        String compactionPolicy = dd.getDatasetDetailsDecl().getCompactionPolicy();
        Map<String, String> compactionPolicyProperties = dd.getDatasetDetailsDecl().getCompactionPolicyProperties();
        boolean defaultCompactionPolicy = (compactionPolicy == null);

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);

        MetadataLockManager.INSTANCE.createDatasetBegin(dataverseName, dataverseName + "." + itemTypeName,
                nodegroupName, compactionPolicy, dataverseName + "." + datasetName, defaultCompactionPolicy);
        Dataset dataset = null;
        try {

            IDatasetDetails datasetDetails = null;
            Dataset ds = MetadataManager.INSTANCE.getDataset(metadataProvider.getMetadataTxnContext(), dataverseName,
                    datasetName);
            if (ds != null) {
                if (dd.getIfNotExists()) {
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    return;
                } else {
                    throw new AlgebricksException("A dataset with this name " + datasetName + " already exists.");
                }
            }
            Datatype dt = MetadataManager.INSTANCE.getDatatype(metadataProvider.getMetadataTxnContext(), dataverseName,
                    itemTypeName);
            if (dt == null) {
                throw new AlgebricksException(": type " + itemTypeName + " could not be found.");
            }
            switch (dd.getDatasetType()) {
                case INTERNAL: {
                    IAType itemType = dt.getDatatype();
                    if (itemType.getTypeTag() != ATypeTag.RECORD) {
                        throw new AlgebricksException("Can only partition ARecord's.");
                    }
                    List<String> partitioningExprs = ((InternalDetailsDecl) dd.getDatasetDetailsDecl())
                            .getPartitioningExprs();
                    boolean autogenerated = ((InternalDetailsDecl) dd.getDatasetDetailsDecl()).isAutogenerated();
                    ARecordType aRecordType = (ARecordType) itemType;
                    aRecordType.validatePartitioningExpressions(partitioningExprs, autogenerated);

                    String ngName = ngNameId != null ? ngNameId.getValue() : configureNodegroupForDataset(dd,
                            dataverseName, mdTxnCtx);
                    String filterField = ((InternalDetailsDecl) dd.getDatasetDetailsDecl()).getFilterField();
                    if (compactionPolicy == null) {
                        if (filterField != null) {
                            // If the dataset has a filter and the user didn't specify a merge policy, then we will pick the
                            // correlated-prefix as the default merge policy.
                            compactionPolicy = GlobalConfig.DEFAULT_FILTERED_DATASET_COMPACTION_POLICY_NAME;
                            compactionPolicyProperties = GlobalConfig.DEFAULT_COMPACTION_POLICY_PROPERTIES;
                        } else {
                            compactionPolicy = GlobalConfig.DEFAULT_COMPACTION_POLICY_NAME;
                            compactionPolicyProperties = GlobalConfig.DEFAULT_COMPACTION_POLICY_PROPERTIES;
                        }
                    } else {
                        validateCompactionPolicy(compactionPolicy,
                                                 compactionPolicyProperties, mdTxnCtx, false);
                    }
                    if (filterField != null) {
                        aRecordType.validateFilterField(filterField);
                    }
                    datasetDetails = new InternalDatasetDetails(InternalDatasetDetails.FileStructure.BTREE,
                            InternalDatasetDetails.PartitioningStrategy.HASH, partitioningExprs, partitioningExprs,
                            ngName, autogenerated, compactionPolicy, compactionPolicyProperties, filterField);
                    break;
                }
                case EXTERNAL: {
                    String adapter = ((ExternalDetailsDecl) dd.getDatasetDetailsDecl()).getAdapter();
                    Map<String, String> properties = ((ExternalDetailsDecl) dd.getDatasetDetailsDecl()).getProperties();

                    String ngName = ngNameId != null ? ngNameId.getValue() : configureNodegroupForDataset(dd,
                            dataverseName, mdTxnCtx);
                    if (compactionPolicy == null) {
                        compactionPolicy = GlobalConfig.DEFAULT_COMPACTION_POLICY_NAME;
                        compactionPolicyProperties = GlobalConfig.DEFAULT_COMPACTION_POLICY_PROPERTIES;
                    } else {
                        validateCompactionPolicy(compactionPolicy, compactionPolicyProperties, mdTxnCtx, true);
                    }
                    datasetDetails = new ExternalDatasetDetails(adapter, properties, ngName, new Date(),
                            ExternalDatasetTransactionState.COMMIT, compactionPolicy, compactionPolicyProperties);
                    break;
                }

            }

            //#. initialize DatasetIdFactory if it is not initialized.
            if (!DatasetIdFactory.isInitialized()) {
                DatasetIdFactory.initialize(MetadataManager.INSTANCE.getMostRecentDatasetId());
            }

            //#. add a new dataset with PendingAddOp
            dataset = new Dataset(dataverseName, datasetName, itemTypeName, datasetDetails, dd.getHints(), dsType,
                    DatasetIdFactory.generateDatasetId(), IMetadataEntity.PENDING_ADD_OP);
            MetadataManager.INSTANCE.addDataset(metadataProvider.getMetadataTxnContext(), dataset);

            if (dd.getDatasetType() == DatasetType.INTERNAL) {
                Dataverse dataverse = MetadataManager.INSTANCE.getDataverse(metadataProvider.getMetadataTxnContext(),
                        dataverseName);
                JobSpecification jobSpec = DatasetOperations.createDatasetJobSpec(dataverse, datasetName,
                        metadataProvider);

                //#. make metadataTxn commit before calling runJob.
                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                bActiveTxn = false;
                progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

                //#. runJob
                runJob(hcc, jobSpec, true);

                //#. begin new metadataTxn
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
            }

            //#. add a new dataset with PendingNoOp after deleting the dataset with PendingAddOp
            MetadataManager.INSTANCE.dropDataset(metadataProvider.getMetadataTxnContext(), dataverseName, datasetName);
            dataset.setPendingOp(IMetadataEntity.PENDING_NO_OP);
            MetadataManager.INSTANCE.addDataset(metadataProvider.getMetadataTxnContext(), dataset);
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }

            if (progress == ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA) {

                //#. execute compensation operations
                //   remove the index in NC
                //   [Notice]
                //   As long as we updated(and committed) metadata, we should remove any effect of the job
                //   because an exception occurs during runJob.
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                CompiledDatasetDropStatement cds = new CompiledDatasetDropStatement(dataverseName, datasetName);
                try {
                    JobSpecification jobSpec = DatasetOperations.createDropDatasetJobSpec(cds, metadataProvider);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    bActiveTxn = false;

                    runJob(hcc, jobSpec, true);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    if (bActiveTxn) {
                        abort(e, e2, mdTxnCtx);
                    }
                }

                //   remove the record from the metadata.
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                try {
                    MetadataManager.INSTANCE.dropDataset(metadataProvider.getMetadataTxnContext(), dataverseName,
                            datasetName);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    abort(e, e2, mdTxnCtx);
                    throw new IllegalStateException("System is inconsistent state: pending dataset(" + dataverseName
                            + "." + datasetName + ") couldn't be removed from the metadata", e);
                }
            }

            throw e;
        } finally {
            MetadataLockManager.INSTANCE.createDatasetEnd(dataverseName, dataverseName + "." + itemTypeName,
                    nodegroupName, compactionPolicy, dataverseName + "." + datasetName, defaultCompactionPolicy);
        }
    }

    private String getNodeGroupName(Identifier ngNameId, DatasetDecl dd, String dataverse) {
        if (ngNameId != null) {
            return ngNameId.getValue();
        }
        String hintValue = dd.getHints().get(DatasetNodegroupCardinalityHint.NAME);
        if (hintValue == null) {
            return MetadataConstants.METADATA_DEFAULT_NODEGROUP_NAME;
        } else {
            return (dataverse + ":" + dd.getName().getValue());
        }
    }

    private String configureNodegroupForDataset(DatasetDecl dd, String dataverse, MetadataTransactionContext mdTxnCtx)
            throws AsterixException {
        int nodegroupCardinality = -1;
        String nodegroupName;
        String hintValue = dd.getHints().get(DatasetNodegroupCardinalityHint.NAME);
        if (hintValue == null) {
            nodegroupName = MetadataConstants.METADATA_DEFAULT_NODEGROUP_NAME;
            return nodegroupName;
        } else {
            int numChosen = 0;
            boolean valid = DatasetHints.validate(DatasetNodegroupCardinalityHint.NAME,
                    dd.getHints().get(DatasetNodegroupCardinalityHint.NAME)).first;
            if (!valid) {
                throw new AsterixException("Incorrect use of hint:" + DatasetNodegroupCardinalityHint.NAME);
            } else {
                nodegroupCardinality = Integer.parseInt(dd.getHints().get(DatasetNodegroupCardinalityHint.NAME));
            }
            Set<String> nodeNames = AsterixAppContextInfo.getInstance().getMetadataProperties().getNodeNames();
            Set<String> nodeNamesClone = new HashSet<String>();
            for (String node : nodeNames) {
                nodeNamesClone.add(node);
            }
            String metadataNodeName = AsterixAppContextInfo.getInstance().getMetadataProperties().getMetadataNodeName();
            List<String> selectedNodes = new ArrayList<String>();
            selectedNodes.add(metadataNodeName);
            numChosen++;
            nodeNamesClone.remove(metadataNodeName);

            if (numChosen < nodegroupCardinality) {
                Random random = new Random();
                String[] nodes = nodeNamesClone.toArray(new String[] {});
                int[] b = new int[nodeNamesClone.size()];
                for (int i = 0; i < b.length; i++) {
                    b[i] = i;
                }

                for (int i = 0; i < nodegroupCardinality - numChosen; i++) {
                    int selected = i + random.nextInt(nodeNamesClone.size() - i);
                    int selNodeIndex = b[selected];
                    selectedNodes.add(nodes[selNodeIndex]);
                    int temp = b[0];
                    b[0] = b[selected];
                    b[selected] = temp;
                }
            }
            nodegroupName = dataverse + ":" + dd.getName().getValue();
            MetadataManager.INSTANCE.addNodegroup(mdTxnCtx, new NodeGroup(nodegroupName, selectedNodes));
            return nodegroupName;
        }

    }

    private void handleCreateIndexStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        ProgressState progress = ProgressState.NO_PROGRESS;
        CreateIndexStatement stmtCreateIndex = (CreateIndexStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtCreateIndex.getDataverseName());
        String datasetName = stmtCreateIndex.getDatasetName().getValue();

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);

        MetadataLockManager.INSTANCE.createIndexBegin(dataverseName, dataverseName + "." + datasetName);

        String indexName = null;
        JobSpecification spec = null;
        Dataset ds = null;
        // For external datasets
        ArrayList<ExternalFile> externalFilesSnapshot = null;
        boolean firstExternalDatasetIndex = false;
        boolean filesIndexReplicated = false;
        Index filesIndex = null;
        boolean datasetLocked = false;
        try {

            ds = MetadataManager.INSTANCE.getDataset(metadataProvider.getMetadataTxnContext(), dataverseName,
                    datasetName);
            if (ds == null) {
                throw new AlgebricksException("There is no dataset with this name " + datasetName + " in dataverse "
                        + dataverseName);
            }

            indexName = stmtCreateIndex.getIndexName().getValue();
            Index idx = MetadataManager.INSTANCE.getIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                    datasetName, indexName);

            String itemTypeName = ds.getItemTypeName();
            Datatype dt = MetadataManager.INSTANCE.getDatatype(metadataProvider.getMetadataTxnContext(), dataverseName,
                    itemTypeName);
            IAType itemType = dt.getDatatype();
            ARecordType aRecordType = (ARecordType) itemType;
            aRecordType.validateKeyFields(stmtCreateIndex.getFieldExprs(), stmtCreateIndex.getIndexType());

            if (idx != null) {
                if (stmtCreateIndex.getIfNotExists()) {
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    return;
                } else {
                    throw new AlgebricksException("An index with this name " + indexName + " already exists.");
                }
            }

            if (ds.getDatasetType() == DatasetType.INTERNAL) {
                List<FeedActivity> feedActivities = MetadataManager.INSTANCE.getActiveFeeds(mdTxnCtx, dataverseName,
                        datasetName);
                if (feedActivities != null && !feedActivities.isEmpty()) {
                    StringBuilder builder = new StringBuilder();

                    for (FeedActivity fa : feedActivities) {
                        builder.append(fa + "\n");
                    }
                    throw new AsterixException("Dataset" + datasetName
                            + " is currently being fed into by the following feeds " + "." + builder.toString()
                            + "\nOperation not supported.");
                }

            } else {
                // External dataset
                // Check if the dataset is indexible
                if (!ExternalIndexingOperations.isIndexible((ExternalDatasetDetails) ds.getDatasetDetails())) {
                    throw new AlgebricksException("dataset using "
                            + ((ExternalDatasetDetails) ds.getDatasetDetails()).getAdapter()
                            + " Adapter can't be indexed");
                }
                // check if the name of the index is valid
                if (!ExternalIndexingOperations.isValidIndexName(datasetName, indexName)) {
                    throw new AlgebricksException("external dataset index name is invalid");
                }

                // Check if the files index exist
                filesIndex = MetadataManager.INSTANCE.getIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                        datasetName, ExternalIndexingOperations.getFilesIndexName(datasetName));
                firstExternalDatasetIndex = (filesIndex == null);
                // lock external dataset
                ExternalDatasetsRegistry.INSTANCE.buildIndexBegin(ds, firstExternalDatasetIndex);
                datasetLocked = true;
                if (firstExternalDatasetIndex) {
                    // verify that no one has created an index before we acquire the lock
                    filesIndex = MetadataManager.INSTANCE.getIndex(metadataProvider.getMetadataTxnContext(),
                            dataverseName, datasetName, ExternalIndexingOperations.getFilesIndexName(datasetName));
                    if (filesIndex != null) {
                        ExternalDatasetsRegistry.INSTANCE.buildIndexEnd(ds, firstExternalDatasetIndex);
                        firstExternalDatasetIndex = false;
                        ExternalDatasetsRegistry.INSTANCE.buildIndexBegin(ds, firstExternalDatasetIndex);
                    }
                }
                if (firstExternalDatasetIndex) {
                    // Get snapshot from External File System
                    externalFilesSnapshot = ExternalIndexingOperations.getSnapshotFromExternalFileSystem(ds);
                    // Add an entry for the files index
                    filesIndex = new Index(dataverseName, datasetName,
                            ExternalIndexingOperations.getFilesIndexName(datasetName), IndexType.BTREE,
                            ExternalIndexingOperations.FILE_INDEX_FIELDS, false, IMetadataEntity.PENDING_ADD_OP);
                    MetadataManager.INSTANCE.addIndex(metadataProvider.getMetadataTxnContext(), filesIndex);
                    // Add files to the external files index
                    for (ExternalFile file : externalFilesSnapshot) {
                        MetadataManager.INSTANCE.addExternalFile(mdTxnCtx, file);
                    }
                    // This is the first index for the external dataset, replicate the files index
                    spec = ExternalIndexingOperations.buildFilesIndexReplicationJobSpec(ds, externalFilesSnapshot,
                            metadataProvider, true);
                    if (spec == null) {
                        throw new AsterixException(
                                "Failed to create job spec for replicating Files Index For external dataset");
                    }
                    filesIndexReplicated = true;
                    runJob(hcc, spec, true);
                }
            }

            //#. add a new index with PendingAddOp
            Index index = new Index(dataverseName, datasetName, indexName, stmtCreateIndex.getIndexType(), stmtCreateIndex.getIndexTypeProperty(),
                    stmtCreateIndex.getFieldExprs(), false,
                    IMetadataEntity.PENDING_ADD_OP 
                    
                    );
            MetadataManager.INSTANCE.addIndex(metadataProvider.getMetadataTxnContext(), index);

            //#. prepare to create the index artifact in NC.
            CompiledCreateIndexStatement cis = new CompiledCreateIndexStatement(index.getIndexName(), dataverseName,
                    index.getDatasetName(), index.getKeyFieldNames(), index.getIndexType(), index.getIndexTypeProperty());
            spec = IndexOperations.buildSecondaryIndexCreationJobSpec(cis, metadataProvider);
            if (spec == null) {
                throw new AsterixException("Failed to create job spec for creating index '"
                        + stmtCreateIndex.getDatasetName() + "." + stmtCreateIndex.getIndexName() + "'");
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

            //#. create the index artifact in NC.
            runJob(hcc, spec, true);

            mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
            bActiveTxn = true;
            metadataProvider.setMetadataTxnContext(mdTxnCtx);

            //#. load data into the index in NC.
            cis = new CompiledCreateIndexStatement(index.getIndexName(), dataverseName, index.getDatasetName(),
                    index.getKeyFieldNames(), index.getIndexType(), index.getIndexTypeProperty());
            spec = IndexOperations.buildSecondaryIndexLoadingJobSpec(cis, metadataProvider);
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;

            runJob(hcc, spec, true);

            //#. begin new metadataTxn
            mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
            bActiveTxn = true;
            metadataProvider.setMetadataTxnContext(mdTxnCtx);

            //#. add another new index with PendingNoOp after deleting the index with PendingAddOp
            MetadataManager.INSTANCE.dropIndex(metadataProvider.getMetadataTxnContext(), dataverseName, datasetName,
                    indexName);
            index.setPendingOp(IMetadataEntity.PENDING_NO_OP);
            MetadataManager.INSTANCE.addIndex(metadataProvider.getMetadataTxnContext(), index);
            // add another new files index with PendingNoOp after deleting the index with PendingAddOp
            if (firstExternalDatasetIndex) {
                MetadataManager.INSTANCE.dropIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                        datasetName, filesIndex.getIndexName());
                filesIndex.setPendingOp(IMetadataEntity.PENDING_NO_OP);
                MetadataManager.INSTANCE.addIndex(metadataProvider.getMetadataTxnContext(), filesIndex);
                // update transaction timestamp
                ((ExternalDatasetDetails) ds.getDatasetDetails()).setRefreshTimestamp(new Date());
                MetadataManager.INSTANCE.updateDataset(mdTxnCtx, ds);
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);

        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            // If files index was replicated for external dataset, it should be cleaned up on NC side
            if (filesIndexReplicated) {
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                        ExternalIndexingOperations.getFilesIndexName(datasetName));
                try {
                    JobSpecification jobSpec = ExternalIndexingOperations.buildDropFilesIndexJobSpec(cds,
                            metadataProvider, ds);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    bActiveTxn = false;
                    runJob(hcc, jobSpec, true);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    if (bActiveTxn) {
                        abort(e, e2, mdTxnCtx);
                    }
                }
            }

            if (progress == ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA) {
                //#. execute compensation operations
                //   remove the index in NC
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName, indexName);
                try {
                    JobSpecification jobSpec = IndexOperations
                            .buildDropSecondaryIndexJobSpec(cds, metadataProvider, ds);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    bActiveTxn = false;

                    runJob(hcc, jobSpec, true);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    if (bActiveTxn) {
                        abort(e, e2, mdTxnCtx);
                    }
                }

                if (firstExternalDatasetIndex) {
                    mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                    metadataProvider.setMetadataTxnContext(mdTxnCtx);
                    try {
                        // Drop External Files from metadata
                        MetadataManager.INSTANCE.dropDatasetExternalFiles(mdTxnCtx, ds);
                        MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                        abort(e, e2, mdTxnCtx);
                        throw new IllegalStateException("System is inconsistent state: pending files for("
                                + dataverseName + "." + datasetName + ") couldn't be removed from the metadata", e);
                    }
                    mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                    metadataProvider.setMetadataTxnContext(mdTxnCtx);
                    try {
                        // Drop the files index from metadata
                        MetadataManager.INSTANCE.dropIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                                datasetName, ExternalIndexingOperations.getFilesIndexName(datasetName));
                        MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                        abort(e, e2, mdTxnCtx);
                        throw new IllegalStateException("System is inconsistent state: pending index(" + dataverseName
                                + "." + datasetName + "." + ExternalIndexingOperations.getFilesIndexName(datasetName)
                                + ") couldn't be removed from the metadata", e);
                    }
                }
                // remove the record from the metadata.
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                try {
                    MetadataManager.INSTANCE.dropIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                            datasetName, indexName);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    abort(e, e2, mdTxnCtx);
                    throw new IllegalStateException("System is in inconsistent state: pending index(" + dataverseName
                            + "." + datasetName + "." + indexName + ") couldn't be removed from the metadata", e);
                }
            }
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.createIndexEnd(dataverseName, dataverseName + "." + datasetName);
            if (datasetLocked) {
                ExternalDatasetsRegistry.INSTANCE.buildIndexEnd(ds, firstExternalDatasetIndex);
            }
        }
    }

    private void handleCreateTypeStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {
        TypeDecl stmtCreateType = (TypeDecl) stmt;
        String dataverseName = getActiveDataverseName(stmtCreateType.getDataverseName());
        String typeName = stmtCreateType.getIdent().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.createTypeBegin(dataverseName, dataverseName + "." + typeName);
        try {

            Dataverse dv = MetadataManager.INSTANCE.getDataverse(mdTxnCtx, dataverseName);
            if (dv == null) {
                throw new AlgebricksException("Unknown dataverse " + dataverseName);
            }
            Datatype dt = MetadataManager.INSTANCE.getDatatype(mdTxnCtx, dataverseName, typeName);
            if (dt != null) {
                if (!stmtCreateType.getIfNotExists()) {
                    throw new AlgebricksException("A datatype with this name " + typeName + " already exists.");
                }
            } else {
                if (builtinTypeMap.get(typeName) != null) {
                    throw new AlgebricksException("Cannot redefine builtin type " + typeName + ".");
                } else {
                    Map<TypeSignature, IAType> typeMap = TypeTranslator.computeTypes(mdTxnCtx, (TypeDecl) stmt,
                            dataverseName);
                    TypeSignature typeSignature = new TypeSignature(dataverseName, typeName);
                    IAType type = typeMap.get(typeSignature);
                    MetadataManager.INSTANCE.addDatatype(mdTxnCtx, new Datatype(dataverseName, typeName, type, false));
                }
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.createTypeEnd(dataverseName, dataverseName + "." + typeName);
        }
    }

    private void handleDataverseDropStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        DataverseDropStatement stmtDelete = (DataverseDropStatement) stmt;
        String dataverseName = stmtDelete.getDataverseName().getValue();

        ProgressState progress = ProgressState.NO_PROGRESS;
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.acquireDataverseWriteLock(dataverseName);
        List<JobSpecification> jobsToExecute = new ArrayList<JobSpecification>();
        try {

            Dataverse dv = MetadataManager.INSTANCE.getDataverse(mdTxnCtx, dataverseName);
            if (dv == null) {
                if (stmtDelete.getIfExists()) {
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    return;
                } else {
                    throw new AlgebricksException("There is no dataverse with this name " + dataverseName + ".");
                }
            }

            //# disconnect all feeds from any datasets in the dataverse.
            List<FeedActivity> feedActivities = MetadataManager.INSTANCE.getActiveFeeds(mdTxnCtx, dataverseName, null);
            DisconnectFeedStatement disStmt = null;
            Identifier dvId = new Identifier(dataverseName);
            for (FeedActivity fa : feedActivities) {
                disStmt = new DisconnectFeedStatement(dvId, new Identifier(fa.getFeedName()), new Identifier(
                        fa.getDatasetName()));
                try {
                    handleDisconnectFeedStatement(metadataProvider, disStmt, hcc);
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Disconnected feed " + fa.getFeedName() + " from dataset " + fa.getDatasetName());
                    }
                } catch (Exception exception) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning("Unable to disconnect feed " + fa.getFeedName() + " from dataset "
                                + fa.getDatasetName() + ". Encountered exception " + exception);
                    }
                }
            }

            //#. prepare jobs which will drop corresponding datasets with indexes.
            List<Dataset> datasets = MetadataManager.INSTANCE.getDataverseDatasets(mdTxnCtx, dataverseName);
            for (int j = 0; j < datasets.size(); j++) {
                String datasetName = datasets.get(j).getDatasetName();
                DatasetType dsType = datasets.get(j).getDatasetType();
                if (dsType == DatasetType.INTERNAL) {
                    List<Index> indexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName,
                            datasetName);
                    for (int k = 0; k < indexes.size(); k++) {
                        if (indexes.get(k).isSecondaryIndex()) {
                            CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                    indexes.get(k).getIndexName());
                            jobsToExecute.add(IndexOperations.buildDropSecondaryIndexJobSpec(cds, metadataProvider,
                                    datasets.get(j)));
                        }
                    }

                    CompiledDatasetDropStatement cds = new CompiledDatasetDropStatement(dataverseName, datasetName);
                    jobsToExecute.add(DatasetOperations.createDropDatasetJobSpec(cds, metadataProvider));
                } else {
                    // External dataset
                    List<Index> indexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName,
                            datasetName);
                    for (int k = 0; k < indexes.size(); k++) {
                        if (ExternalIndexingOperations.isFileIndex(indexes.get(k))) {
                            CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                    indexes.get(k).getIndexName());
                            jobsToExecute.add(ExternalIndexingOperations.buildDropFilesIndexJobSpec(cds,
                                    metadataProvider, datasets.get(j)));
                        } else {
                            CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                    indexes.get(k).getIndexName());
                            jobsToExecute.add(IndexOperations.buildDropSecondaryIndexJobSpec(cds, metadataProvider,
                                    datasets.get(j)));
                        }
                    }
                    ExternalDatasetsRegistry.INSTANCE.removeDatasetInfo(datasets.get(j));
                }
            }
            jobsToExecute.add(DataverseOperations.createDropDataverseJobSpec(dv, metadataProvider));

            //#. mark PendingDropOp on the dataverse record by
            //   first, deleting the dataverse record from the DATAVERSE_DATASET
            //   second, inserting the dataverse record with the PendingDropOp value into the DATAVERSE_DATASET
            MetadataManager.INSTANCE.dropDataverse(mdTxnCtx, dataverseName);
            MetadataManager.INSTANCE.addDataverse(mdTxnCtx, new Dataverse(dataverseName, dv.getDataFormat(),
                    IMetadataEntity.PENDING_DROP_OP));

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

            for (JobSpecification jobSpec : jobsToExecute) {
                runJob(hcc, jobSpec, true);
            }

            mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
            bActiveTxn = true;
            metadataProvider.setMetadataTxnContext(mdTxnCtx);

            //#. finally, delete the dataverse.
            MetadataManager.INSTANCE.dropDataverse(mdTxnCtx, dataverseName);
            if (activeDefaultDataverse != null && activeDefaultDataverse.getDataverseName() == dataverseName) {
                activeDefaultDataverse = null;
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }

            if (progress == ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA) {
                if (activeDefaultDataverse != null && activeDefaultDataverse.getDataverseName() == dataverseName) {
                    activeDefaultDataverse = null;
                }

                //#. execute compensation operations
                //   remove the all indexes in NC
                try {
                    for (JobSpecification jobSpec : jobsToExecute) {
                        runJob(hcc, jobSpec, true);
                    }
                } catch (Exception e2) {
                    //do no throw exception since still the metadata needs to be compensated.
                    e.addSuppressed(e2);
                }

                //   remove the record from the metadata.
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                try {
                    MetadataManager.INSTANCE.dropDataverse(mdTxnCtx, dataverseName);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    abort(e, e2, mdTxnCtx);
                    throw new IllegalStateException("System is inconsistent state: pending dataverse(" + dataverseName
                            + ") couldn't be removed from the metadata", e);
                }
            }

            throw e;
        } finally {
            MetadataLockManager.INSTANCE.releaseDataverseWriteLock(dataverseName);
        }
    }

    private void handleDatasetDropStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        DropStatement stmtDelete = (DropStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtDelete.getDataverseName());
        String datasetName = stmtDelete.getDatasetName().getValue();

        ProgressState progress = ProgressState.NO_PROGRESS;
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);

        MetadataLockManager.INSTANCE.dropDatasetBegin(dataverseName, dataverseName + "." + datasetName);
        List<JobSpecification> jobsToExecute = new ArrayList<JobSpecification>();
        try {

            Dataset ds = MetadataManager.INSTANCE.getDataset(mdTxnCtx, dataverseName, datasetName);
            if (ds == null) {
                if (stmtDelete.getIfExists()) {
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    return;
                } else {
                    throw new AlgebricksException("There is no dataset with this name " + datasetName
                            + " in dataverse " + dataverseName + ".");
                }
            }

            if (ds.getDatasetType() == DatasetType.INTERNAL) {
                // prepare job spec(s) that would disconnect any active feeds involving the dataset.
                List<FeedActivity> feedActivities = MetadataManager.INSTANCE.getActiveFeeds(mdTxnCtx, dataverseName,
                        datasetName);
                List<JobSpecification> disconnectFeedJobSpecs = new ArrayList<JobSpecification>();
                if (feedActivities != null && !feedActivities.isEmpty()) {
                    for (FeedActivity fa : feedActivities) {
                        JobSpecification jobSpec = FeedOperations.buildDisconnectFeedJobSpec(dataverseName,
                                fa.getFeedName(), datasetName, metadataProvider, fa);
                        disconnectFeedJobSpecs.add(jobSpec);
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.info("Disconnected feed " + fa.getFeedName() + " from dataset " + datasetName
                                    + " as dataset is being dropped");
                        }
                    }
                }

                //#. prepare jobs to drop the datatset and the indexes in NC
                List<Index> indexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName, datasetName);
                for (int j = 0; j < indexes.size(); j++) {
                    if (indexes.get(j).isSecondaryIndex()) {
                        CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                indexes.get(j).getIndexName());
                        jobsToExecute.add(IndexOperations.buildDropSecondaryIndexJobSpec(cds, metadataProvider, ds));
                    }
                }
                CompiledDatasetDropStatement cds = new CompiledDatasetDropStatement(dataverseName, datasetName);
                jobsToExecute.add(DatasetOperations.createDropDatasetJobSpec(cds, metadataProvider));

                //#. mark the existing dataset as PendingDropOp
                MetadataManager.INSTANCE.dropDataset(mdTxnCtx, dataverseName, datasetName);
                MetadataManager.INSTANCE.addDataset(
                        mdTxnCtx,
                        new Dataset(dataverseName, datasetName, ds.getItemTypeName(), ds.getDatasetDetails(), ds
                                .getHints(), ds.getDatasetType(), ds.getDatasetId(), IMetadataEntity.PENDING_DROP_OP));

                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                bActiveTxn = false;
                progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

                //# disconnect the feeds
                for (JobSpecification jobSpec : disconnectFeedJobSpecs) {
                    runJob(hcc, jobSpec, true);
                }

                //#. run the jobs
                for (JobSpecification jobSpec : jobsToExecute) {
                    runJob(hcc, jobSpec, true);
                }

                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
            } else {
                // External dataset
                ExternalDatasetsRegistry.INSTANCE.removeDatasetInfo(ds);
                //#. prepare jobs to drop the datatset and the indexes in NC
                List<Index> indexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName, datasetName);
                for (int j = 0; j < indexes.size(); j++) {
                    if (ExternalIndexingOperations.isFileIndex(indexes.get(j))) {
                        CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                indexes.get(j).getIndexName());
                        jobsToExecute.add(IndexOperations.buildDropSecondaryIndexJobSpec(cds, metadataProvider, ds));
                    } else {
                        CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                indexes.get(j).getIndexName());
                        jobsToExecute.add(ExternalIndexingOperations.buildDropFilesIndexJobSpec(cds, metadataProvider,
                                ds));
                    }
                }

                //#. mark the existing dataset as PendingDropOp
                MetadataManager.INSTANCE.dropDataset(mdTxnCtx, dataverseName, datasetName);
                MetadataManager.INSTANCE.addDataset(
                        mdTxnCtx,
                        new Dataset(dataverseName, datasetName, ds.getItemTypeName(), ds.getDatasetDetails(), ds
                                .getHints(), ds.getDatasetType(), ds.getDatasetId(), IMetadataEntity.PENDING_DROP_OP));

                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                bActiveTxn = false;
                progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

                //#. run the jobs
                for (JobSpecification jobSpec : jobsToExecute) {
                    runJob(hcc, jobSpec, true);
                }
                if (indexes.size() > 0) {
                    ExternalDatasetsRegistry.INSTANCE.removeDatasetInfo(ds);
                }
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
            }

            //#. finally, delete the dataset.
            MetadataManager.INSTANCE.dropDataset(mdTxnCtx, dataverseName, datasetName);
            // Drop the associated nodegroup
            String nodegroup = ds.getDatasetDetails().getNodeGroupName();
            if (!nodegroup.equalsIgnoreCase(MetadataConstants.METADATA_DEFAULT_NODEGROUP_NAME)) {
                MetadataManager.INSTANCE.dropNodegroup(mdTxnCtx, dataverseName + ":" + datasetName);
            }

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }

            if (progress == ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA) {
                //#. execute compensation operations
                //   remove the all indexes in NC
                try {
                    for (JobSpecification jobSpec : jobsToExecute) {
                        runJob(hcc, jobSpec, true);
                    }
                } catch (Exception e2) {
                    //do no throw exception since still the metadata needs to be compensated.
                    e.addSuppressed(e2);
                }

                //   remove the record from the metadata.
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                try {
                    MetadataManager.INSTANCE.dropDataset(metadataProvider.getMetadataTxnContext(), dataverseName,
                            datasetName);
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    abort(e, e2, mdTxnCtx);
                    throw new IllegalStateException("System is inconsistent state: pending dataset(" + dataverseName
                            + "." + datasetName + ") couldn't be removed from the metadata", e);
                }
            }

            throw e;
        } finally {
            MetadataLockManager.INSTANCE.dropDatasetEnd(dataverseName, dataverseName + "." + datasetName);
        }
    }

    private void handleIndexDropStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {

        IndexDropStatement stmtIndexDrop = (IndexDropStatement) stmt;
        String datasetName = stmtIndexDrop.getDatasetName().getValue();
        String dataverseName = getActiveDataverseName(stmtIndexDrop.getDataverseName());
        ProgressState progress = ProgressState.NO_PROGRESS;
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);

        MetadataLockManager.INSTANCE.dropIndexBegin(dataverseName, dataverseName + "." + datasetName);

        String indexName = null;
        // For external index
        boolean dropFilesIndex = false;
        List<JobSpecification> jobsToExecute = new ArrayList<JobSpecification>();
        try {

            Dataset ds = MetadataManager.INSTANCE.getDataset(mdTxnCtx, dataverseName, datasetName);
            if (ds == null) {
                throw new AlgebricksException("There is no dataset with this name " + datasetName + " in dataverse "
                        + dataverseName);
            }

            List<FeedActivity> feedActivities = MetadataManager.INSTANCE.getActiveFeeds(mdTxnCtx, dataverseName,
                    datasetName);
            if (feedActivities != null && !feedActivities.isEmpty()) {
                StringBuilder builder = new StringBuilder();

                for (FeedActivity fa : feedActivities) {
                    builder.append(fa + "\n");
                }
                throw new AsterixException("Dataset" + datasetName
                        + " is currently being fed into by the following feeds " + "." + builder.toString()
                        + "\nOperation not supported.");
            }

            if (ds.getDatasetType() == DatasetType.INTERNAL) {
                indexName = stmtIndexDrop.getIndexName().getValue();
                Index index = MetadataManager.INSTANCE.getIndex(mdTxnCtx, dataverseName, datasetName, indexName);
                if (index == null) {
                    if (stmtIndexDrop.getIfExists()) {
                        MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                        return;
                    } else {
                        throw new AlgebricksException("There is no index with this name " + indexName + ".");
                    }
                }
                //#. prepare a job to drop the index in NC.
                CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName, indexName);
                jobsToExecute.add(IndexOperations.buildDropSecondaryIndexJobSpec(cds, metadataProvider, ds));

                //#. mark PendingDropOp on the existing index
                MetadataManager.INSTANCE.dropIndex(mdTxnCtx, dataverseName, datasetName, indexName);
                MetadataManager.INSTANCE.addIndex(mdTxnCtx,
                        new Index(dataverseName, datasetName, indexName, index.getIndexType(),
                                index.getKeyFieldNames(), index.isPrimaryIndex(), IMetadataEntity.PENDING_DROP_OP));

                //#. commit the existing transaction before calling runJob.
                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                bActiveTxn = false;
                progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

                for (JobSpecification jobSpec : jobsToExecute) {
                    runJob(hcc, jobSpec, true);
                }

                //#. begin a new transaction
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);

                //#. finally, delete the existing index
                MetadataManager.INSTANCE.dropIndex(mdTxnCtx, dataverseName, datasetName, indexName);
            } else {
                // External dataset
                indexName = stmtIndexDrop.getIndexName().getValue();
                Index index = MetadataManager.INSTANCE.getIndex(mdTxnCtx, dataverseName, datasetName, indexName);
                if (index == null) {
                    if (stmtIndexDrop.getIfExists()) {
                        MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                        return;
                    } else {
                        throw new AlgebricksException("There is no index with this name " + indexName + ".");
                    }
                } else if (ExternalIndexingOperations.isFileIndex(index)) {
                    throw new AlgebricksException("Dropping a dataset's files index is not allowed.");
                }
                //#. prepare a job to drop the index in NC.
                CompiledIndexDropStatement cds = new CompiledIndexDropStatement(dataverseName, datasetName, indexName);
                jobsToExecute.add(IndexOperations.buildDropSecondaryIndexJobSpec(cds, metadataProvider, ds));
                List<Index> datasetIndexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName,
                        datasetName);
                if (datasetIndexes.size() == 2) {
                    dropFilesIndex = true;
                    // only one index + the files index, we need to delete both of the indexes
                    for (Index externalIndex : datasetIndexes) {
                        if (ExternalIndexingOperations.isFileIndex(externalIndex)) {
                            cds = new CompiledIndexDropStatement(dataverseName, datasetName,
                                    externalIndex.getIndexName());
                            jobsToExecute.add(ExternalIndexingOperations.buildDropFilesIndexJobSpec(cds,
                                    metadataProvider, ds));
                            //#. mark PendingDropOp on the existing files index
                            MetadataManager.INSTANCE.dropIndex(mdTxnCtx, dataverseName, datasetName,
                                    externalIndex.getIndexName());
                            MetadataManager.INSTANCE.addIndex(
                                    mdTxnCtx,
                                    new Index(dataverseName, datasetName, externalIndex.getIndexName(), externalIndex
                                            .getIndexType(), externalIndex.getKeyFieldNames(), externalIndex
                                            .isPrimaryIndex(), IMetadataEntity.PENDING_DROP_OP));
                        }
                    }
                }

                //#. mark PendingDropOp on the existing index
                MetadataManager.INSTANCE.dropIndex(mdTxnCtx, dataverseName, datasetName, indexName);
                MetadataManager.INSTANCE.addIndex(mdTxnCtx,
                        new Index(dataverseName, datasetName, indexName, index.getIndexType(),
                                index.getKeyFieldNames(), index.isPrimaryIndex(), IMetadataEntity.PENDING_DROP_OP));

                //#. commit the existing transaction before calling runJob.
                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                bActiveTxn = false;
                progress = ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA;

                for (JobSpecification jobSpec : jobsToExecute) {
                    runJob(hcc, jobSpec, true);
                }

                //#. begin a new transaction
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);

                //#. finally, delete the existing index
                MetadataManager.INSTANCE.dropIndex(mdTxnCtx, dataverseName, datasetName, indexName);
                if (dropFilesIndex) {
                    // delete the files index too
                    MetadataManager.INSTANCE.dropIndex(mdTxnCtx, dataverseName, datasetName,
                            ExternalIndexingOperations.getFilesIndexName(datasetName));
                    MetadataManager.INSTANCE.dropDatasetExternalFiles(mdTxnCtx, ds);
                    ExternalDatasetsRegistry.INSTANCE.removeDatasetInfo(ds);
                }
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);

        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }

            if (progress == ProgressState.ADDED_PENDINGOP_RECORD_TO_METADATA) {
                //#. execute compensation operations
                //   remove the all indexes in NC
                try {
                    for (JobSpecification jobSpec : jobsToExecute) {
                        runJob(hcc, jobSpec, true);
                    }
                } catch (Exception e2) {
                    //do no throw exception since still the metadata needs to be compensated.
                    e.addSuppressed(e2);
                }

                //   remove the record from the metadata.
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                try {
                    MetadataManager.INSTANCE.dropIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                            datasetName, indexName);
                    if (dropFilesIndex) {
                        MetadataManager.INSTANCE.dropIndex(metadataProvider.getMetadataTxnContext(), dataverseName,
                                datasetName, ExternalIndexingOperations.getFilesIndexName(datasetName));
                    }
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                    abort(e, e2, mdTxnCtx);
                    throw new IllegalStateException("System is inconsistent state: pending index(" + dataverseName
                            + "." + datasetName + "." + indexName + ") couldn't be removed from the metadata", e);
                }
            }

            throw e;

        } finally {
            MetadataLockManager.INSTANCE.dropIndexEnd(dataverseName, dataverseName + "." + datasetName);
        }
    }

    private void handleTypeDropStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {

        TypeDropStatement stmtTypeDrop = (TypeDropStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtTypeDrop.getDataverseName());
        String typeName = stmtTypeDrop.getTypeName().getValue();

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.dropTypeBegin(dataverseName, dataverseName + "." + typeName);

        try {
            Datatype dt = MetadataManager.INSTANCE.getDatatype(mdTxnCtx, dataverseName, typeName);
            if (dt == null) {
                if (!stmtTypeDrop.getIfExists())
                    throw new AlgebricksException("There is no datatype with this name " + typeName + ".");
            } else {
                MetadataManager.INSTANCE.dropDatatype(mdTxnCtx, dataverseName, typeName);
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.dropTypeEnd(dataverseName, dataverseName + "." + typeName);
        }
    }

    private void handleNodegroupDropStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {

        NodeGroupDropStatement stmtDelete = (NodeGroupDropStatement) stmt;
        String nodegroupName = stmtDelete.getNodeGroupName().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.acquireNodeGroupWriteLock(nodegroupName);
        try {
            NodeGroup ng = MetadataManager.INSTANCE.getNodegroup(mdTxnCtx, nodegroupName);
            if (ng == null) {
                if (!stmtDelete.getIfExists())
                    throw new AlgebricksException("There is no nodegroup with this name " + nodegroupName + ".");
            } else {
                MetadataManager.INSTANCE.dropNodegroup(mdTxnCtx, nodegroupName);
            }

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.releaseNodeGroupWriteLock(nodegroupName);
        }
    }

    private void handleCreateFunctionStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {
        CreateFunctionStatement cfs = (CreateFunctionStatement) stmt;
        String dataverse = getActiveDataverseName(cfs.getSignature().getNamespace());
        String functionName = cfs.getaAterixFunction().getName();

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.functionStatementBegin(dataverse, dataverse + "." + functionName);
        try {

            Dataverse dv = MetadataManager.INSTANCE.getDataverse(mdTxnCtx, dataverse);
            if (dv == null) {
                throw new AlgebricksException("There is no dataverse with this name " + dataverse + ".");
            }
            Function function = new Function(dataverse, functionName, cfs.getaAterixFunction().getArity(),
                    cfs.getParamList(), Function.RETURNTYPE_VOID, cfs.getFunctionBody(), Function.LANGUAGE_AQL,
                    FunctionKind.SCALAR.toString());
            MetadataManager.INSTANCE.addFunction(mdTxnCtx, function);

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.functionStatementEnd(dataverse, dataverse + "." + functionName);
        }
    }

    private void handleFunctionDropStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {
        FunctionDropStatement stmtDropFunction = (FunctionDropStatement) stmt;
        FunctionSignature signature = stmtDropFunction.getFunctionSignature();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.functionStatementBegin(signature.getNamespace(), signature.getNamespace() + "."
                + signature.getName());
        try {
            Function function = MetadataManager.INSTANCE.getFunction(mdTxnCtx, signature);
            if (function == null) {
                if (!stmtDropFunction.getIfExists())
                    throw new AlgebricksException("Unknonw function " + signature);
            } else {
                MetadataManager.INSTANCE.dropFunction(mdTxnCtx, signature);
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.functionStatementEnd(signature.getNamespace(), signature.getNamespace() + "."
                    + signature.getName());
        }
    }

    private void handleLoadStatement(AqlMetadataProvider metadataProvider, Statement stmt, IHyracksClientConnection hcc)
            throws Exception {
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        acquireReadLatch();
        try {
            LoadStatement loadStmt = (LoadStatement) stmt;
            String dataverseName = getActiveDataverseName(loadStmt.getDataverseName());
            CompiledLoadFromFileStatement cls = new CompiledLoadFromFileStatement(dataverseName, loadStmt
                    .getDatasetName().getValue(), loadStmt.getAdapter(), loadStmt.getProperties(),
                    loadStmt.dataIsAlreadySorted());
            JobSpecification spec = APIFramework.compileQuery(null, metadataProvider, null, 0, null, sessionConfig,
                    out, pdf, cls);
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            if (spec != null) {
                runJob(hcc, spec, true);
            }
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            releaseReadLatch();
        }
    }

    private void handleInsertStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {

        InsertStatement stmtInsert = (InsertStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtInsert.getDataverseName());
        Query query = stmtInsert.getQuery();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.insertDeleteBegin(dataverseName,
                dataverseName + "." + stmtInsert.getDatasetName(), query.getDataverses(), query.getDatasets());

        try {
            metadataProvider.setWriteTransaction(true);
            CompiledInsertStatement clfrqs = new CompiledInsertStatement(dataverseName, stmtInsert.getDatasetName()
                    .getValue(), query, stmtInsert.getVarCounter());
            JobSpecification compiled = rewriteCompileQuery(metadataProvider, query, clfrqs);

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;

            if (compiled != null) {
                runJob(hcc, compiled, true);
            }

        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.insertDeleteEnd(dataverseName,
                    dataverseName + "." + stmtInsert.getDatasetName(), query.getDataverses(), query.getDatasets());
        }
    }

    private void handleDeleteStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {

        DeleteStatement stmtDelete = (DeleteStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtDelete.getDataverseName());
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE
                .insertDeleteBegin(dataverseName, dataverseName + "." + stmtDelete.getDatasetName(),
                        stmtDelete.getDataverses(), stmtDelete.getDatasets());

        try {
            metadataProvider.setWriteTransaction(true);
            CompiledDeleteStatement clfrqs = new CompiledDeleteStatement(stmtDelete.getVariableExpr(), dataverseName,
                    stmtDelete.getDatasetName().getValue(), stmtDelete.getCondition(), stmtDelete.getVarCounter(),
                    metadataProvider);
            JobSpecification compiled = rewriteCompileQuery(metadataProvider, clfrqs.getQuery(), clfrqs);

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;

            if (compiled != null) {
                runJob(hcc, compiled, true);
            }

        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.insertDeleteEnd(dataverseName,
                    dataverseName + "." + stmtDelete.getDatasetName(), stmtDelete.getDataverses(),
                    stmtDelete.getDatasets());
        }
    }

    private JobSpecification rewriteCompileQuery(AqlMetadataProvider metadataProvider, Query query,
            ICompiledDmlStatement stmt) throws AsterixException, RemoteException, AlgebricksException, JSONException,
            ACIDException {

        // Query Rewriting (happens under the same ongoing metadata transaction)
        Pair<Query, Integer> reWrittenQuery = APIFramework.reWriteQuery(declaredFunctions, metadataProvider, query,
                sessionConfig, out, pdf);

        // Query Compilation (happens under the same ongoing metadata
        // transaction)
        JobSpecification spec = APIFramework.compileQuery(declaredFunctions, metadataProvider, reWrittenQuery.first,
                reWrittenQuery.second, stmt == null ? null : stmt.getDatasetName(), sessionConfig, out, pdf, stmt);

        return spec;

    }

    private void handleCreateFeedStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {

        CreateFeedStatement cfs = (CreateFeedStatement) stmt;
        String dataverseName = getActiveDataverseName(cfs.getDataverseName());
        String feedName = cfs.getFeedName().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.createFeedBegin(dataverseName, dataverseName + "." + feedName);

        String adapterName = null;
        Feed feed = null;
        try {
            adapterName = cfs.getAdapterName();

            feed = MetadataManager.INSTANCE.getFeed(metadataProvider.getMetadataTxnContext(), dataverseName, feedName);
            if (feed != null) {
                if (cfs.getIfNotExists()) {
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                    return;
                } else {
                    throw new AlgebricksException("A feed with this name " + adapterName + " already exists.");
                }
            }

            feed = new Feed(dataverseName, feedName, adapterName, cfs.getAdapterConfiguration(),
                    cfs.getAppliedFunction());
            MetadataManager.INSTANCE.addFeed(metadataProvider.getMetadataTxnContext(), feed);
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.createFeedEnd(dataverseName, dataverseName + "." + feedName);
        }
    }

    private void handleDropFeedStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        FeedDropStatement stmtFeedDrop = (FeedDropStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtFeedDrop.getDataverseName());
        String feedName = stmtFeedDrop.getFeedName().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.dropFeedBegin(dataverseName, dataverseName + "." + feedName);

        try {
            Feed feed = MetadataManager.INSTANCE.getFeed(mdTxnCtx, dataverseName, feedName);
            if (feed == null) {
                if (!stmtFeedDrop.getIfExists()) {
                    throw new AlgebricksException("There is no feed with this name " + feedName + ".");
                }
            }

            List<FeedActivity> feedActivities;
            try {
                feedActivities = MetadataManager.INSTANCE.getConnectFeedActivitiesForFeed(mdTxnCtx, dataverseName,
                        feedName);
                MetadataManager.INSTANCE.dropFeed(mdTxnCtx, dataverseName, feedName);
                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            } catch (Exception e) {
                MetadataManager.INSTANCE.abortTransaction(mdTxnCtx);
                throw new MetadataException(e);
            }

            List<JobSpecification> jobSpecs = new ArrayList<JobSpecification>();
            for (FeedActivity feedActivity : feedActivities) {
                JobSpecification jobSpec = FeedOperations.buildDisconnectFeedJobSpec(dataverseName, feedName,
                        feedActivity.getDatasetName(), metadataProvider, feedActivity);
                jobSpecs.add(jobSpec);
            }

            for (JobSpecification spec : jobSpecs) {
                runJob(hcc, spec, true);
            }

        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.dropFeedEnd(dataverseName, dataverseName + "." + feedName);
        }
    }

    private void handleConnectFeedStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {

        ConnectFeedStatement cfs = (ConnectFeedStatement) stmt;
        String dataverseName = getActiveDataverseName(cfs.getDataverseName());
        String feedName = cfs.getFeedName();
        String datasetName = cfs.getDatasetName().getValue();

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.connectFeedBegin(dataverseName, dataverseName + "." + datasetName, dataverseName
                + "." + feedName);
        boolean readLatchAcquired = true;
        try {

            metadataProvider.setWriteTransaction(true);

            CompiledConnectFeedStatement cbfs = new CompiledConnectFeedStatement(dataverseName, cfs.getFeedName(), cfs
                    .getDatasetName().getValue(), cfs.getPolicy(), cfs.getQuery(), cfs.getVarCounter());

            Dataset dataset = MetadataManager.INSTANCE.getDataset(metadataProvider.getMetadataTxnContext(),
                    dataverseName, cfs.getDatasetName().getValue());
            if (dataset == null) {
                throw new AsterixException("Unknown target dataset :" + cfs.getDatasetName().getValue());
            }

            if (!dataset.getDatasetType().equals(DatasetType.INTERNAL)) {
                throw new AsterixException("Statement not applicable. Dataset " + cfs.getDatasetName().getValue()
                        + " is not of required type " + DatasetType.INTERNAL);
            }

            Feed feed = MetadataManager.INSTANCE.getFeed(metadataProvider.getMetadataTxnContext(), dataverseName,
                    cfs.getFeedName());
            if (feed == null) {
                throw new AsterixException("Unknown source feed: " + cfs.getFeedName());
            }

            FeedConnectionId feedConnId = new FeedConnectionId(dataverseName, cfs.getFeedName(), cfs.getDatasetName()
                    .getValue());
            FeedActivity recentActivity = MetadataManager.INSTANCE.getRecentActivityOnFeedConnection(mdTxnCtx,
                    feedConnId, null);
            boolean isFeedActive = FeedUtil.isFeedActive(recentActivity);
            if (isFeedActive && !cfs.forceConnect()) {
                throw new AsterixException("Feed " + cfs.getDatasetName().getValue()
                        + " is currently ACTIVE. Operation not supported");
            }

            FeedPolicy feedPolicy = MetadataManager.INSTANCE.getFeedPolicy(mdTxnCtx, dataverseName,
                    cbfs.getPolicyName());
            if (feedPolicy == null) {
                feedPolicy = MetadataManager.INSTANCE.getFeedPolicy(mdTxnCtx,
                        MetadataConstants.METADATA_DATAVERSE_NAME, cbfs.getPolicyName());
                if (feedPolicy == null) {
                    throw new AsterixException("Unknown feed policy" + cbfs.getPolicyName());
                }
            }

            cfs.initialize(metadataProvider.getMetadataTxnContext(), dataset, feed);
            cbfs.setQuery(cfs.getQuery());
            metadataProvider.getConfig().put(FunctionUtils.IMPORT_PRIVATE_FUNCTIONS, "" + Boolean.TRUE);
            metadataProvider.getConfig().put(BuiltinFeedPolicies.CONFIG_FEED_POLICY_KEY, cbfs.getPolicyName());
            JobSpecification compiled = rewriteCompileQuery(metadataProvider, cfs.getQuery(), cbfs);
            JobSpecification newJobSpec = FeedUtil.alterJobSpecificationForFeed(compiled, feedConnId, feedPolicy);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Altered feed ingestion spec to wrap operators");
            }

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            String waitForCompletionParam = metadataProvider.getConfig().get(ConnectFeedStatement.WAIT_FOR_COMPLETION);
            boolean waitForCompletion = waitForCompletionParam == null ? false : Boolean
                    .valueOf(waitForCompletionParam);
            if (waitForCompletion) {
                MetadataLockManager.INSTANCE.connectFeedEnd(dataverseName, dataverseName + "." + datasetName,
                        dataverseName + "." + feedName);
                readLatchAcquired = false;
            }
            runJob(hcc, newJobSpec, waitForCompletion);
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            if (readLatchAcquired) {
                MetadataLockManager.INSTANCE.connectFeedEnd(dataverseName, dataverseName + "." + datasetName,
                        dataverseName + "." + feedName);
            }
        }
    }

    private void handleDisconnectFeedStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        DisconnectFeedStatement cfs = (DisconnectFeedStatement) stmt;
        String dataverseName = getActiveDataverseName(cfs.getDataverseName());
        String datasetName = cfs.getDatasetName().getValue();

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.disconnectFeedBegin(dataverseName, dataverseName + "." + datasetName,
                dataverseName + "." + cfs.getFeedName());
        try {
            Dataset dataset = MetadataManager.INSTANCE.getDataset(metadataProvider.getMetadataTxnContext(),
                    dataverseName, cfs.getDatasetName().getValue());
            if (dataset == null) {
                throw new AsterixException("Unknown dataset :" + cfs.getDatasetName().getValue() + " in dataverse "
                        + dataverseName);
            }
            if (!dataset.getDatasetType().equals(DatasetType.INTERNAL)) {
                throw new AsterixException("Statement not applicable. Dataset " + cfs.getDatasetName().getValue()
                        + " is not of required type " + DatasetType.INTERNAL);
            }

            Feed feed = MetadataManager.INSTANCE.getFeed(metadataProvider.getMetadataTxnContext(), dataverseName, cfs
                    .getFeedName().getValue());
            if (feed == null) {
                throw new AsterixException("Unknown source feed :" + cfs.getFeedName());
            }

            FeedActivity feedActivity = MetadataManager.INSTANCE.getRecentActivityOnFeedConnection(mdTxnCtx,
                    new FeedConnectionId(dataverseName, feed.getFeedName(), datasetName), null);

            boolean isFeedActive = FeedUtil.isFeedActive(feedActivity);
            if (!isFeedActive) {
                throw new AsterixException("Feed " + cfs.getDatasetName().getValue()
                        + " is currently INACTIVE. Operation not supported");
            }

            JobSpecification jobSpec = FeedOperations.buildDisconnectFeedJobSpec(dataverseName, cfs.getFeedName()
                    .getValue(), cfs.getDatasetName().getValue(), metadataProvider, feedActivity);

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            runJob(hcc, jobSpec, true);
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.disconnectFeedEnd(dataverseName, dataverseName + "." + datasetName,
                    dataverseName + "." + cfs.getFeedName());
        }
    }

    private void handleCompactStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        CompactStatement compactStatement = (CompactStatement) stmt;
        String dataverseName = getActiveDataverseName(compactStatement.getDataverseName());
        String datasetName = compactStatement.getDatasetName().getValue();
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.compactBegin(dataverseName, dataverseName + "." + datasetName);

        List<JobSpecification> jobsToExecute = new ArrayList<JobSpecification>();
        try {
            Dataset ds = MetadataManager.INSTANCE.getDataset(mdTxnCtx, dataverseName, datasetName);
            if (ds == null) {
                throw new AlgebricksException("There is no dataset with this name " + datasetName + " in dataverse "
                        + dataverseName + ".");
            }
            List<Index> indexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName, datasetName);
            if (indexes.size() == 0) {
                throw new AlgebricksException("Cannot compact the extrenal dataset " + datasetName
                        + " because it has no indexes");
            }
            if (ds.getDatasetType() == DatasetType.INTERNAL) {
                for (int j = 0; j < indexes.size(); j++) {
                    if (indexes.get(j).isSecondaryIndex()) {
                        CompiledIndexCompactStatement cics = new CompiledIndexCompactStatement(dataverseName,
                                datasetName, indexes.get(j).getIndexName(), indexes.get(j).getKeyFieldNames(), indexes.get(j).getIndexType(), null);
                        jobsToExecute
                                .add(IndexOperations.buildSecondaryIndexCompactJobSpec(cics, metadataProvider, ds));
                    }
                }
                Dataverse dataverse = MetadataManager.INSTANCE.getDataverse(metadataProvider.getMetadataTxnContext(),
                        dataverseName);
                jobsToExecute.add(DatasetOperations.compactDatasetJobSpec(dataverse, datasetName, metadataProvider));
            } else {
                for (int j = 0; j < indexes.size(); j++) {
                    if (!ExternalIndexingOperations.isFileIndex(indexes.get(j))) {
                        CompiledIndexCompactStatement cics = new CompiledIndexCompactStatement(dataverseName,
                                datasetName, indexes.get(j).getIndexName(), indexes.get(j).getKeyFieldNames(), indexes.get(j).getIndexType(), null);
                        jobsToExecute
                                .add(IndexOperations.buildSecondaryIndexCompactJobSpec(cics, metadataProvider, ds));
                    }
                }
                jobsToExecute.add(ExternalIndexingOperations.compactFilesIndexJobSpec(ds, metadataProvider));
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;

            //#. run the jobs
            for (JobSpecification jobSpec : jobsToExecute) {
                runJob(hcc, jobSpec, true);
            }
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.compactEnd(dataverseName, dataverseName + "." + datasetName);
        }
    }

    private void handleQuery(AqlMetadataProvider metadataProvider, Query query, IHyracksClientConnection hcc,
            IHyracksDataset hdc, ResultDelivery resultDelivery) throws Exception {

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.queryBegin(activeDefaultDataverse, query.getDataverses(), query.getDatasets());
        JobSpecification compiled = null;
        try {
            compiled = rewriteCompileQuery(metadataProvider, query, null);

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;

            if (sessionConfig.isExecuteQuery() && compiled != null) {
                GlobalConfig.ASTERIX_LOGGER.info(compiled.toJSON().toString(1));
                JobId jobId = runJob(hcc, compiled, false);

                JSONObject response = new JSONObject();
                switch (resultDelivery) {
                    case ASYNC:
                        JSONArray handle = new JSONArray();
                        handle.put(jobId.getId());
                        handle.put(metadataProvider.getResultSetId().getId());
                        response.put("handle", handle);
                        out.print(response);
                        out.flush();
                        hcc.waitForCompletion(jobId);
                        break;
                    case SYNC:
                        ResultReader resultReader = new ResultReader(hcc, hdc);
                        resultReader.open(jobId, metadataProvider.getResultSetId());

                        // In this case (the normal case), we don't use the
                        // "response" JSONObject - just stream the results
                        // to the "out" PrintWriter
                        if (pdf == OutputFormat.CSV) {
                            ResultUtils.displayCSVHeader(metadataProvider.findOutputRecordType(), out);
                        }
                        ResultUtils.displayResults(resultReader, out, pdf);

                        hcc.waitForCompletion(jobId);
                        break;
                    case ASYNC_DEFERRED:
                        handle = new JSONArray();
                        handle.put(jobId.getId());
                        handle.put(metadataProvider.getResultSetId().getId());
                        response.put("handle", handle);
                        hcc.waitForCompletion(jobId);
                        out.print(response);
                        out.flush();
                        break;
                    default:
                        break;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.queryEnd(query.getDataverses(), query.getDatasets());
            // release external datasets' locks acquired during compilation of the query
            ExternalDatasetsRegistry.INSTANCE.releaseAcquiredLocks(metadataProvider);
        }
    }

    private void handleCreateNodeGroupStatement(AqlMetadataProvider metadataProvider, Statement stmt) throws Exception {

        NodegroupDecl stmtCreateNodegroup = (NodegroupDecl) stmt;
        String ngName = stmtCreateNodegroup.getNodegroupName().getValue();

        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        MetadataLockManager.INSTANCE.acquireNodeGroupWriteLock(ngName);

        try {
            NodeGroup ng = MetadataManager.INSTANCE.getNodegroup(mdTxnCtx, ngName);
            if (ng != null) {
                if (!stmtCreateNodegroup.getIfNotExists())
                    throw new AlgebricksException("A nodegroup with this name " + ngName + " already exists.");
            } else {
                List<Identifier> ncIdentifiers = stmtCreateNodegroup.getNodeControllerNames();
                List<String> ncNames = new ArrayList<String>(ncIdentifiers.size());
                for (Identifier id : ncIdentifiers) {
                    ncNames.add(id.getValue());
                }
                MetadataManager.INSTANCE.addNodegroup(mdTxnCtx, new NodeGroup(ngName, ncNames));
            }
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
        } catch (Exception e) {
            abort(e, e, mdTxnCtx);
            throw e;
        } finally {
            MetadataLockManager.INSTANCE.releaseNodeGroupWriteLock(ngName);
        }
    }

    private void handleExternalDatasetRefreshStatement(AqlMetadataProvider metadataProvider, Statement stmt,
            IHyracksClientConnection hcc) throws Exception {
        RefreshExternalDatasetStatement stmtRefresh = (RefreshExternalDatasetStatement) stmt;
        String dataverseName = getActiveDataverseName(stmtRefresh.getDataverseName());
        String datasetName = stmtRefresh.getDatasetName().getValue();
        ExternalDatasetTransactionState transactionState = ExternalDatasetTransactionState.COMMIT;
        MetadataTransactionContext mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
        MetadataLockManager.INSTANCE.refreshDatasetBegin(dataverseName, dataverseName + "." + datasetName);
        boolean bActiveTxn = true;
        metadataProvider.setMetadataTxnContext(mdTxnCtx);
        JobSpecification spec = null;
        Dataset ds = null;
        List<ExternalFile> metadataFiles = null;
        List<ExternalFile> deletedFiles = null;
        List<ExternalFile> addedFiles = null;
        List<ExternalFile> appendedFiles = null;
        List<Index> indexes = null;
        Dataset transactionDataset = null;
        boolean lockAquired = false;
        boolean success = false;
        try {
            ds = MetadataManager.INSTANCE.getDataset(metadataProvider.getMetadataTxnContext(), dataverseName,
                    datasetName);

            // Dataset exists ?
            if (ds == null) {
                throw new AlgebricksException("There is no dataset with this name " + datasetName + " in dataverse "
                        + dataverseName);
            }
            // Dataset external ?
            if (ds.getDatasetType() != DatasetType.EXTERNAL) {
                throw new AlgebricksException("dataset " + datasetName + " in dataverse " + dataverseName
                        + " is not an external dataset");
            }
            // Dataset has indexes ?
            indexes = MetadataManager.INSTANCE.getDatasetIndexes(mdTxnCtx, dataverseName, datasetName);
            if (indexes.size() == 0) {
                throw new AlgebricksException("External dataset " + datasetName + " in dataverse " + dataverseName
                        + " doesn't have any index");
            }

            // Record transaction time
            Date txnTime = new Date();

            // refresh lock here
            ExternalDatasetsRegistry.INSTANCE.refreshBegin(ds);
            lockAquired = true;

            // Get internal files
            metadataFiles = MetadataManager.INSTANCE.getDatasetExternalFiles(mdTxnCtx, ds);
            deletedFiles = new ArrayList<ExternalFile>();
            addedFiles = new ArrayList<ExternalFile>();
            appendedFiles = new ArrayList<ExternalFile>();

            // Compute delta
            // Now we compare snapshot with external file system
            if (ExternalIndexingOperations
                    .isDatasetUptodate(ds, metadataFiles, addedFiles, deletedFiles, appendedFiles)) {
                ((ExternalDatasetDetails) ds.getDatasetDetails()).setRefreshTimestamp(txnTime);
                MetadataManager.INSTANCE.updateDataset(mdTxnCtx, ds);
                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                // latch will be released in the finally clause
                return;
            }

            // At this point, we know data has changed in the external file system, record transaction in metadata and start
            transactionDataset = ExternalIndexingOperations.createTransactionDataset(ds);
            /*
             * Remove old dataset record and replace it with a new one
             */
            MetadataManager.INSTANCE.updateDataset(mdTxnCtx, transactionDataset);

            // Add delta files to the metadata
            for (ExternalFile file : addedFiles) {
                MetadataManager.INSTANCE.addExternalFile(mdTxnCtx, file);
            }
            for (ExternalFile file : appendedFiles) {
                MetadataManager.INSTANCE.addExternalFile(mdTxnCtx, file);
            }
            for (ExternalFile file : deletedFiles) {
                MetadataManager.INSTANCE.addExternalFile(mdTxnCtx, file);
            }

            // Create the files index update job
            spec = ExternalIndexingOperations.buildFilesIndexUpdateOp(ds, metadataFiles, deletedFiles, addedFiles,
                    appendedFiles, metadataProvider);

            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            transactionState = ExternalDatasetTransactionState.BEGIN;

            //run the files update job
            runJob(hcc, spec, true);

            for (Index index : indexes) {
                if (!ExternalIndexingOperations.isFileIndex(index)) {
                    spec = ExternalIndexingOperations.buildIndexUpdateOp(ds, index, metadataFiles, deletedFiles,
                            addedFiles, appendedFiles, metadataProvider);
                    //run the files update job
                    runJob(hcc, spec, true);
                }
            }

            // all index updates has completed successfully, record transaction state
            spec = ExternalIndexingOperations.buildCommitJob(ds, indexes, metadataProvider);

            // Aquire write latch again -> start a transaction and record the decision to commit
            mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
            metadataProvider.setMetadataTxnContext(mdTxnCtx);
            bActiveTxn = true;
            ((ExternalDatasetDetails) transactionDataset.getDatasetDetails())
                    .setState(ExternalDatasetTransactionState.READY_TO_COMMIT);
            ((ExternalDatasetDetails) transactionDataset.getDatasetDetails()).setRefreshTimestamp(txnTime);
            MetadataManager.INSTANCE.updateDataset(mdTxnCtx, transactionDataset);
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            bActiveTxn = false;
            transactionState = ExternalDatasetTransactionState.READY_TO_COMMIT;
            // We don't release the latch since this job is expected to be quick
            runJob(hcc, spec, true);
            // Start a new metadata transaction to record the final state of the transaction
            mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
            metadataProvider.setMetadataTxnContext(mdTxnCtx);
            bActiveTxn = true;

            for (ExternalFile file : metadataFiles) {
                if (file.getPendingOp() == ExternalFilePendingOp.PENDING_DROP_OP) {
                    MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
                } else if (file.getPendingOp() == ExternalFilePendingOp.PENDING_NO_OP) {
                    Iterator<ExternalFile> iterator = appendedFiles.iterator();
                    while (iterator.hasNext()) {
                        ExternalFile appendedFile = iterator.next();
                        if (file.getFileName().equals(appendedFile.getFileName())) {
                            // delete existing file
                            MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
                            // delete existing appended file
                            MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, appendedFile);
                            // add the original file with appended information
                            appendedFile.setFileNumber(file.getFileNumber());
                            appendedFile.setPendingOp(ExternalFilePendingOp.PENDING_NO_OP);
                            MetadataManager.INSTANCE.addExternalFile(mdTxnCtx, appendedFile);
                            iterator.remove();
                        }
                    }
                }
            }

            // remove the deleted files delta
            for (ExternalFile file : deletedFiles) {
                MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
            }

            // insert new files
            for (ExternalFile file : addedFiles) {
                MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
                file.setPendingOp(ExternalFilePendingOp.PENDING_NO_OP);
                MetadataManager.INSTANCE.addExternalFile(mdTxnCtx, file);
            }

            // mark the transaction as complete
            ((ExternalDatasetDetails) transactionDataset.getDatasetDetails())
                    .setState(ExternalDatasetTransactionState.COMMIT);
            MetadataManager.INSTANCE.updateDataset(mdTxnCtx, transactionDataset);

            // commit metadata transaction
            MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
            success = true;
        } catch (Exception e) {
            if (bActiveTxn) {
                abort(e, e, mdTxnCtx);
            }
            if (transactionState == ExternalDatasetTransactionState.READY_TO_COMMIT) {
                throw new IllegalStateException("System is inconsistent state: commit of (" + dataverseName + "."
                        + datasetName + ") refresh couldn't carry out the commit phase", e);
            }
            if (transactionState == ExternalDatasetTransactionState.COMMIT) {
                // Nothing to do , everything should be clean
                throw e;
            }
            if (transactionState == ExternalDatasetTransactionState.BEGIN) {
                // transaction failed, need to do the following
                // clean NCs removing transaction components
                mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                bActiveTxn = true;
                metadataProvider.setMetadataTxnContext(mdTxnCtx);
                spec = ExternalIndexingOperations.buildAbortOp(ds, indexes, metadataProvider);
                MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                bActiveTxn = false;
                try {
                    runJob(hcc, spec, true);
                } catch (Exception e2) {
                    // This should never happen -- fix throw illegal
                    e.addSuppressed(e2);
                    throw new IllegalStateException("System is in inconsistent state. Failed to abort refresh", e);
                }
                // remove the delta of files
                // return the state of the dataset to committed
                try {
                    mdTxnCtx = MetadataManager.INSTANCE.beginTransaction();
                    for (ExternalFile file : deletedFiles) {
                        MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
                    }
                    for (ExternalFile file : addedFiles) {
                        MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
                    }
                    for (ExternalFile file : appendedFiles) {
                        MetadataManager.INSTANCE.dropExternalFile(mdTxnCtx, file);
                    }
                    MetadataManager.INSTANCE.updateDataset(mdTxnCtx, ds);
                    // commit metadata transaction
                    MetadataManager.INSTANCE.commitTransaction(mdTxnCtx);
                } catch (Exception e2) {
                    abort(e, e2, mdTxnCtx);
                    e.addSuppressed(e2);
                    throw new IllegalStateException("System is in inconsistent state. Failed to drop delta files", e);
                }
            }
        } finally {
            if (lockAquired) {
                ExternalDatasetsRegistry.INSTANCE.refreshEnd(ds, success);
            }
            MetadataLockManager.INSTANCE.refreshDatasetEnd(dataverseName, dataverseName + "." + datasetName);
        }
    }

    private JobId runJob(IHyracksClientConnection hcc, JobSpecification spec, boolean waitForCompletion)
            throws Exception {
        JobId[] jobIds = executeJobArray(hcc, new Job[] { new Job(spec) }, out, waitForCompletion);
        return jobIds[0];
    }

    public JobId[] executeJobArray(IHyracksClientConnection hcc, Job[] jobs, PrintWriter out,
            boolean waitForCompletion) throws Exception {
        JobId[] startedJobIds = new JobId[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            JobSpecification spec = jobs[i].getJobSpec();
            spec.setMaxReattempts(0);
            JobId jobId = hcc.startJob(spec);
            startedJobIds[i] = jobId;
            if (waitForCompletion) {
                hcc.waitForCompletion(jobId);
            }
        }
        return startedJobIds;
    }

    private String getActiveDataverseName(String dataverse) throws AlgebricksException {
        if (dataverse != null) {
            return dataverse;
        }
        if (activeDefaultDataverse != null) {
            return activeDefaultDataverse.getDataverseName();
        }
        throw new AlgebricksException("dataverse not specified");
    }

    private String getActiveDataverseName(Identifier dataverse) throws AlgebricksException {
        return getActiveDataverseName(dataverse != null ? dataverse.getValue() : null);
    }

    private void acquireReadLatch() {
        MetadataManager.INSTANCE.acquireReadLatch();
    }

    private void releaseReadLatch() {
        MetadataManager.INSTANCE.releaseReadLatch();
    }

    private void abort(Exception rootE, Exception parentE, MetadataTransactionContext mdTxnCtx) {
        try {
            if (IS_DEBUG_MODE) {
                rootE.printStackTrace();
            }
            MetadataManager.INSTANCE.abortTransaction(mdTxnCtx);
        } catch (Exception e2) {
            parentE.addSuppressed(e2);
            throw new IllegalStateException(rootE);
        }
    }

}
