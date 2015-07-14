package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment3DhbtreeBuilder extends AbstractSpatialIndexExperiment3SIdxCreateAndQueryBuilder {

    public SpatialIndexExperiment3DhbtreeBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment3Dhbtree", config, "8node.xml", "base_8_ingest.aql", "8.dqgen", "count.aql",
                "spatial_3_create_dhbtree.aql");
    }
}
