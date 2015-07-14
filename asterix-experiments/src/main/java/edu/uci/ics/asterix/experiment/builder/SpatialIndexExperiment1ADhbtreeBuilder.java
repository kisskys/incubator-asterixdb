package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLFileAction;
import edu.uci.ics.asterix.experiment.client.LSMExperimentConstants;
import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment1ADhbtreeBuilder extends AbstractExperiment1Builder {

    public SpatialIndexExperiment1ADhbtreeBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment1ADhbtree", config, "1node.xml", "base_1_ingest.aql", "1.dgen");
    }

    @Override
    protected void doBuildDDL(SequentialActionList seq) {
        seq.add(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                LSMExperimentConstants.AQL_DIR).resolve("spatial_1_dhbtree.aql")));
    }

}
