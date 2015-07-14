package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment3ShbtreeBuilder extends AbstractSpatialIndexExperiment3SIdxCreateAndQueryBuilder {

    public SpatialIndexExperiment3ShbtreeBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment3Shbtree", config, "8node.xml", "base_8_ingest.aql", "8.dqgen", "count.aql",
                "spatial_3_create_shbtree.aql");
    }
}
