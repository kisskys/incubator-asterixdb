package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLFileAction;
import edu.uci.ics.asterix.experiment.client.LSMExperimentConstants;
import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment3PIdxLoadBuilder extends AbstractSpatialIndexExperiment3PIdxLoadBuilder {

    public SpatialIndexExperiment3PIdxLoadBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment3PIdxLoad", config, "8node.xml", "base_8_ingest.aql", "8.dqgen", "count.aql", "spatial_3_pidx_load.aql");
    }

    @Override
    protected void doBuildDDL(SequentialActionList seq) {
        seq.add(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                LSMExperimentConstants.AQL_DIR).resolve("spatial_3.aql")));
    }

}
