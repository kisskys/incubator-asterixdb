package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLFileAction;
import edu.uci.ics.asterix.experiment.client.LSMExperimentConstants;
import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment1BRtreeBuilder extends AbstractExperiment1Builder {

    public SpatialIndexExperiment1BRtreeBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment1BRtree", config, "2node.xml", "base_2_ingest.aql", "2.dgen");
    }

    @Override
    protected void doBuildDDL(SequentialActionList seq) {
        seq.add(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                LSMExperimentConstants.AQL_DIR).resolve("spatial_1_rtree.aql")));
    }
}
