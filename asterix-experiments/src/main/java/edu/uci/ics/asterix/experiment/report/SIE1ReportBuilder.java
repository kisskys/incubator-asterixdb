package edu.uci.ics.asterix.experiment.report;

public class SIE1ReportBuilder extends AbstractDynamicDataEvalReportBuilder {
    public SIE1ReportBuilder(String expName, String runLogFilePath) {
        super(expName, runLogFilePath);
    }

    @Override
    public String getOverallInsertPS() throws Exception {
        return null;
    }

    @Override
    public String get20minInsertPS(int minutes) throws Exception {
        renewStringBuilder();
        openRunLog();
        try {
            if (!moveToExperimentBegin()) {
                //The experiment run log doesn't exist in this run log file
                return null;
            }
            
            String line;
            while((line = br.readLine()) != null) {
                if (line.contains("i64")) {
                    rsb.append(ReportBuilderHelper.getLong(line, "[ ", "i64") / (minutes * 60));
                    break;
                }
            }

            return rsb.toString();
        } finally {
            closeRunLog();
        }
    }

    @Override
    public String getInstantaneousQueryPS() throws Exception {
        return null;
    }

    @Override
    public String get20minQueryPS(int minutes) throws Exception {
        return null;
//        renewStringBuilder();
//        openRunLog();
//        try {
//
//            return getResult();
//        } finally {
//            closeRunLog();
//        }
    }
}
