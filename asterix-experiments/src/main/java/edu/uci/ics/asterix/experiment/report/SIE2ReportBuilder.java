package edu.uci.ics.asterix.experiment.report;

public class SIE2ReportBuilder extends AbstractDynamicDataEvalReportBuilder {
    public SIE2ReportBuilder(String expName, String runLogFilePath) {
        super(expName, runLogFilePath);
    }

    @Override
    public String getOverallInsertPS() throws Exception {
        return null;
    }

    @Override
    public String get20minInsertPS() throws Exception {
        renewStringBuilder();
        openRunLog();
        try {
            if (!moveToExperimentBegin()) {
                //The experiment run log doesn't exist in this run log file
                return null;
            }
            
            String line;
            long insertCount = 0;
            while((line = br.readLine()) != null) {
                if (line.contains("[During ingestion + queries][InsertCount]")) {
                    insertCount += ReportBuilderHelper.getLong(line, "=", "in");
                }
                if (line.contains("Running")) {
                    break;
                }
            }
            rsb.append(insertCount/1200);
            return rsb.toString();
        } finally {
            closeRunLog();
        }
    }
    
    public long getFirstXminInsertPS(int minutes, int genId) throws Exception {
        renewStringBuilder();
        openRunLog();
        try {
            if (!moveToExperimentBegin()) {
                //The experiment run log doesn't exist in this run log file
                return 0;
            }

            String line;
            int dGenId;
            int count = 0;
            long timeToInsert = 0;
            long totalTimeToInsert = 0;
            boolean haveResult = false;
            while ((line = br.readLine()) != null) {
                if (line.contains("[During ingestion only][TimeToInsert100000]")) {
                    dGenId = ReportBuilderHelper.getInt(line, "DataGen[", "]");
                    if (dGenId == genId) {
                        count++;
                        timeToInsert = ReportBuilderHelper.getLong(line, INSTANTANEOUS_INSERT_STRING, "in");
                        totalTimeToInsert += timeToInsert;
                        if (totalTimeToInsert > minutes*60000) {
                            haveResult = true;
                            break;
                        }
                    }
                }
                if (line.contains("Running")) {
                    break;
                }
            }
            if (haveResult || totalTimeToInsert > (minutes*60000 - 1200000)) {
                return  (count * INSTANTAEOUS_INSERT_COUNT) / (totalTimeToInsert/1000);
            } else {
                return 0;
            }
        } finally {
            closeRunLog();
        }
    }

    @Override
    public String getInstantaneousQueryPS() throws Exception {
        return null;
    }

    @Override
    public String get20minQueryPS() throws Exception {
        renewStringBuilder();
        openRunLog();
        try {
            if (!moveToExperimentBegin()) {
                //The experiment run log doesn't exist in this run log file
                return null;
            }
            
            String line;
            long queryCount = 0;
            while((line = br.readLine()) != null) {
                if (line.contains("[QueryCount]")) {
                    queryCount += ReportBuilderHelper.getLong(line, "[QueryCount]", "in");
                }
                if (line.contains("Running")) {
                    break;
                }
            }
            rsb.append(queryCount/(float)1200);
            return rsb.toString();
        } finally {
            closeRunLog();
        }
    }
    
    public String get20minAverageQueryResultCount() throws Exception {
        renewStringBuilder();
        openRunLog();
        try {
            if (!moveToExperimentBegin()) {
                //The experiment run log doesn't exist in this run log file
                return null;
            }
            
            String line;
            long resultCount = 0;
            long queryCount = 0;
            while((line = br.readLine()) != null) {
                if (line.contains("i64")) {
                    resultCount += ReportBuilderHelper.getLong(line, "[", "i64");
                    ++queryCount;
                }
                if (line.contains("Running")) {
                    break;
                }
            }
            rsb.append(resultCount/queryCount);
            return rsb.toString();
        } finally {
            closeRunLog();
        }
    }
    
    public String get20minAverageQueryResponseTime() throws Exception {
        renewStringBuilder();
        openRunLog();
        try {
            if (!moveToExperimentBegin()) {
                //The experiment run log doesn't exist in this run log file
                return null;
            }
            
            String line;
            long responseTime = 0;
            long queryCount = 0;
            while((line = br.readLine()) != null) {
                if (line.contains("Elapsed time = ")) {
                    responseTime += ReportBuilderHelper.getLong(line, "=", "for");
                    ++queryCount;
                }
                if (line.contains("Running")) {
                    break;
                }
            }
            rsb.append(responseTime/queryCount);
            return rsb.toString();
        } finally {
            closeRunLog();
        }
    }
}
