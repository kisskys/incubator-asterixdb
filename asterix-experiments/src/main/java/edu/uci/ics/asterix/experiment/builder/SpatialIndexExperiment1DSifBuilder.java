package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLFileAction;
import edu.uci.ics.asterix.experiment.client.LSMExperimentConstants;
import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment1DSifBuilder extends AbstractExperiment1Builder {

    public SpatialIndexExperiment1DSifBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment1DSif", config, "8node.xml", "base_8_ingest.aql", "8.dgen");
    }
    
    @Override
    protected void doBuildDDL(SequentialActionList seq) {
        seq.add(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                LSMExperimentConstants.AQL_DIR).resolve("spatial_1_sif.aql")));
    }
}
