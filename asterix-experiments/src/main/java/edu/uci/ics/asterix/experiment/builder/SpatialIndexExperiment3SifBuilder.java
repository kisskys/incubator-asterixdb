package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment3SifBuilder extends AbstractSpatialIndexExperiment3SIdxCreateAndQueryBuilder {

    public SpatialIndexExperiment3SifBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment3Sif", config, "8node.xml", "base_8_ingest.aql", "8.dqgen", "count.aql",
                "spatial_3_create_sif.aql");
    }
}
