package edu.uci.ics.asterix.experiment.client;

import org.kohsuke.args4j.Option;

public class SyntheticDataGeneratorConfig {

    @Option(name = "-psf", aliases = "--point-source-file", usage = "The point source file")
    private String pointSourceFile;

    public String getPointSourceFile() {
        return pointSourceFile;
    }
    
    @Option(name = "-psi", aliases = "--point-sampling-interval", usage = "The point sampling interval from the point source file", required = true)
    private int pointSamplingInterval;

    public int getpointSamplingInterval() {
        return pointSamplingInterval;
    }
    
    @Option(name = "-pid", aliases = "--parition-id", usage = "The partition id in order to avoid key duplication", required = true)
    private int partitionId;

    public int getPartitionId() {
        return partitionId;
    }
    
    @Option(name = "-of", aliases = "--output-filepath", usage = "The output file path", required = true)
    private String outputFilePath;

    public String getOutputFilePath() {
        return outputFilePath;
    }
    
    @Option(name = "-rc", aliases = "--record-count", usage = "The record count to generate", required = true)
    private long recordCount;

    public long getRecordCount() {
        return recordCount;
    }        
}
