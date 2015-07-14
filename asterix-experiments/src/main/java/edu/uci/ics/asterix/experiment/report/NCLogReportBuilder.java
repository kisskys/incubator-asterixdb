package edu.uci.ics.asterix.experiment.report;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class NCLogReportBuilder {

    private String ncLogFilePath = "/Users/kisskys/workspace/asterix_experiment/run-log/measure-with-balloon/sie1-8dgen/log-1436511417368/SpatialIndexExperiment1ADhbtree/logs/a1_node1.log";
    private BufferedReader br;
    private String timeLine;
    private String msgLine;

    public NCLogReportBuilder(String filePath) {
        if (filePath != null) {
            this.ncLogFilePath = filePath;
        }
    }

    public String getFlushMergeEventAsGanttChartFormat(long testBeginTimeStamp) throws Exception {
        openNCLog();
        StringBuilder sb = new StringBuilder();
        long flushStartTimeStamp, flushFinishTimeStamp, mergeStartTimeStamp, mergeFinishTimeStamp;
        String indexName;
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
        HashMap<String, Long> flushMap = new HashMap<String, Long>();
        HashMap<String, Long> mergeMap = new HashMap<String, Long>();
        try {
            while ((timeLine = br.readLine()) != null) {
                if ((msgLine = br.readLine()) == null) {
                    break;
                }

                //flush start
                if (msgLine.contains("Started a flush operation for index")) {
                    flushStartTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (flushStartTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    flushMap.put(indexName, flushStartTimeStamp);
                }

                //flush finish
                if (msgLine.contains("Finished the flush operation for index")) {
                    flushFinishTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (flushFinishTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    flushStartTimeStamp = flushMap.remove(indexName);

                    sb.append("f-"+indexName).append("\t").append((flushStartTimeStamp - testBeginTimeStamp) / 1000)
                            .append("\t").append((flushFinishTimeStamp - testBeginTimeStamp) / 1000).append("\t")
                            .append("flush").append("\n");
                }

                //merge start
                if (msgLine.contains("Started a merge operation for index")) {
                    mergeStartTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (mergeStartTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    mergeMap.put(indexName, mergeStartTimeStamp);
                }

                //merge finish
                if (msgLine.contains("Finished the merge operation for index")) {
                    mergeFinishTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (mergeFinishTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    mergeStartTimeStamp = mergeMap.remove(indexName);

                    sb.append("m-"+indexName).append("\t")
                            .append((mergeStartTimeStamp - testBeginTimeStamp) / 1000).append("\t")
                            .append((mergeFinishTimeStamp - testBeginTimeStamp) / 1000).append("\t")
                            .append("merge").append("\n");
                }
            }

            return sb.toString();
        } finally {
            closeNCLog();
        }
    }

    protected void openNCLog() throws IOException {
        br = new BufferedReader(new FileReader(ncLogFilePath));
    }

    protected void closeNCLog() throws IOException {
        if (br != null) {
            br.close();
        }
    }

}
