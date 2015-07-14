package edu.uci.ics.asterix.experiment.report;

public abstract class AbstractStaticDataEvalReportBuilder implements IStaticDataEvalReportBuilder {

    protected final String expName;
    protected final String runLogFilePath;
    
    protected AbstractStaticDataEvalReportBuilder(String expName, String runLogFilePath) {
        this.expName = expName;
        this.runLogFilePath = runLogFilePath;
    }
}
