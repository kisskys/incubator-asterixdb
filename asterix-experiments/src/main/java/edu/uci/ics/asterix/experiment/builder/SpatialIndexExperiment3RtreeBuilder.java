package edu.uci.ics.asterix.experiment.builder;

import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;

public class SpatialIndexExperiment3RtreeBuilder extends AbstractSpatialIndexExperiment3SIdxCreateAndQueryBuilder {

    public SpatialIndexExperiment3RtreeBuilder(LSMExperimentSetRunnerConfig config) {
        super("SpatialIndexExperiment3Rtree", config, "8node.xml", "base_8_ingest.aql", "8.dqgen", "count.aql",
                "spatial_3_create_rtree.aql");
    }
}
