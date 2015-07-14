package edu.uci.ics.asterix.experiment.report;

public interface IDynamicDataEvalReportBuilder {
    public String getInstantaneousInsertPS(int genId, boolean useTimeForX) throws Exception;

    public String getOverallInsertPS() throws Exception;

    public String get20minInsertPS() throws Exception;

    public String getInstantaneousQueryPS() throws Exception;

    public String get20minQueryPS() throws Exception;
}
