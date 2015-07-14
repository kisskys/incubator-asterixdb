package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLFileAction;
import edu.uci.ics.asterix.experiment.client.LSMExperimentConstants;
import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment2RtreeBuilder extends AbstractSpatialIndexExperiment2Builder {

    public SpatialIndexExperiment2RtreeBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment2Rtree", config, "8node.xml", "base_8_ingest_query.aql", "8.dqgen");
    }

    @Override
    protected void doBuildDDL(SequentialActionList seq) {
        seq.add(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                LSMExperimentConstants.AQL_DIR).resolve("spatial_1_rtree.aql")));
    }
}
