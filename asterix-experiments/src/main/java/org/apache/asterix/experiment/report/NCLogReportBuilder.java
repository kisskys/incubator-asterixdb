/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.asterix.experiment.report;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class NCLogReportBuilder {

    private String ncLogFilePath = "/Users/kisskys/workspace/asterix_experiment/run-log/measure-with-balloon/sie1-8dgen/log-1436511417368/SpatialIndexExperiment1ADhbtree/logs/a1_node1.log";
    private BufferedReader br;
    private String timeLine;
    private String msgLine;
    private int pid0count;
    private int pid1count;
    private int pid2count;
    private int pid3count;
    private int sid0count;
    private int sid1count;
    private int sid2count;
    private int sid3count;

    public NCLogReportBuilder(String filePath) {
        if (filePath != null) {
            this.ncLogFilePath = filePath;
        }
    }

    public String getFlushMergeEventAsGanttChartFormat(long testBeginTimeStamp) throws Exception {
        openNCLog();
        StringBuilder sb = new StringBuilder();
        long flushStartTimeStamp, flushFinishTimeStamp, mergeStartTimeStamp = 0, mergeFinishTimeStamp;
        String indexName;
        String threadId;
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
        HashMap<String, Long> flushMap = new HashMap<String, Long>();
        HashMap<String, Long> mergeMap = new HashMap<String, Long>();
        long sTime, fTime;
        int flushCount = 0;
        int mergeCount = 0;
        long pidxTotalFlushTime = 0;
        long pidxTotalMergeTime = 0;
        long sidxTotalFlushTime = 0;
        long sidxTotalMergeTime = 0;
        int pidxFlushCount = 0;
        int pidxMergeCount = 0;
        int sidxFlushCount = 0;
        int sidxMergeCount = 0;
        try {
            while ((timeLine = br.readLine()) != null) {
                if ((msgLine = br.readLine()) == null) {
                    break;
                }
                while (!msgLine.contains("SEVERE:")) {
                    timeLine = msgLine;
                    msgLine = br.readLine();
                    if (msgLine == null) {
                        break;
                    }
                }
                if (msgLine == null) {
                    break;
                }

                //flush start
                if (msgLine.contains("Started a flush operation for index")) {
                    flushStartTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (flushStartTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "SEVERE: [", "]")
                            + ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    //                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    flushMap.put(indexName, flushStartTimeStamp);
                }

                //flush finish
                if (msgLine.contains("Finished the flush operation for index")) {
                    flushFinishTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (flushFinishTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "SEVERE: [", "]")
                            + ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    //                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");

                    if (flushMap.containsKey(indexName)) {
                        flushStartTimeStamp = flushMap.remove(indexName);
                        sTime = (flushStartTimeStamp - testBeginTimeStamp) / 1000;
                        fTime = (flushFinishTimeStamp - testBeginTimeStamp) / 1000;
                        if (fTime == sTime) {
                            ++fTime;
                        }
                        //only for sie1
                        //                        if (fTime > 1200) {
                        //                            fTime = 1200;
                        //                        }
                        sb.append("f-" + getPrintName(indexName)).append("\t").append(sTime).append("\t").append(fTime)
                                .append("\t").append(indexName.contains("Tweets") ? "flushPidx" : "flushSidx")
                                .append("\n");
                    } else {
                        System.out.println("Flush start of " + indexName + " doesn't exist!!");
                    }
                    ++flushCount;
                    if (getPrintName(indexName).contains("pidx")) {
                        pidxTotalFlushTime += flushFinishTimeStamp - flushFinishTimeStamp;
                        ++pidxFlushCount;
                    } else {
                        sidxTotalFlushTime += flushFinishTimeStamp - flushFinishTimeStamp;
                        ++sidxFlushCount;
                    }
                }

                //merge start
                if (msgLine.contains("Started a merge operation for index")) {
                    mergeStartTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (mergeStartTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "SEVERE: [", "]")
                            + ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    //                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    mergeMap.put(indexName, mergeStartTimeStamp);
                }

                //merge finish
                if (msgLine.contains("Finished the merge operation for index")) {
                    mergeFinishTimeStamp = ReportBuilderHelper.getTimeStampAsLong(timeLine, format);

                    //ignore flush op which happened before the data gen started.
                    if (mergeFinishTimeStamp < testBeginTimeStamp) {
                        continue;
                    }

                    indexName = ReportBuilderHelper.getString(msgLine, "SEVERE: [", "]")
                            + ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");
                    //                    indexName = ReportBuilderHelper.getString(msgLine, "experiments/Tweets_idx_", "/]");

                    if (mergeMap.containsKey(indexName)) {
                        mergeStartTimeStamp = mergeMap.remove(indexName);
                        sTime = (mergeStartTimeStamp - testBeginTimeStamp) / 1000;
                        fTime = (mergeFinishTimeStamp - testBeginTimeStamp) / 1000;
                        if (fTime == sTime) {
                            ++fTime;
                        }
                        //only for sie1
                        //                        if (fTime > 1200) {
                        //                            fTime = 1200;
                        //                        }
                        sb.append("m-" + getPrintName(indexName)).append("\t").append(sTime).append("\t").append(fTime)
                                .append("\t").append(indexName.contains("Tweets") ? "mergePidx" : "mergeSidx")
                                .append("\n");
                    }
                    ++mergeCount;
                    if (getPrintName(indexName).contains("pidx")) {
                        pidxTotalMergeTime += mergeFinishTimeStamp - mergeStartTimeStamp;
                        ++pidxMergeCount;
                    } else {
                        sidxTotalMergeTime += mergeFinishTimeStamp - mergeStartTimeStamp;
                        ++sidxMergeCount;
                    }
                }
            }

            Iterator<Entry<String, Long>> mergeMapIter = mergeMap.entrySet().iterator();
            Entry<String, Long> entry = null;
            while (mergeMapIter.hasNext()) {
                entry = mergeMapIter.next();
                sb.append("m-" + getPrintName(entry.getKey())).append("\t")
                        .append((entry.getValue() - testBeginTimeStamp) / 1000).append("\t").append(60 * 20)
                        .append("\t").append(entry.getKey().contains("Tweets") ? "mergePidx" : "mergeSidx")
                        .append("\n");
            }

            Iterator<Entry<String, Long>> flushMapIter = mergeMap.entrySet().iterator();
            while (mergeMapIter.hasNext()) {
                entry = flushMapIter.next();
                sb.append("f-" + getPrintName(entry.getKey())).append("\t")
                        .append((entry.getValue() - testBeginTimeStamp) / 1000).append("\t").append(60 * 20)
                        .append("\t").append(entry.getKey().contains("Tweets") ? "flushPidx" : "flushSidx")
                        .append("\n");
            }

            return sb.toString();
        } finally {
            closeNCLog();
            //            System.out.println("exp: " + ncLogFilePath);
            //            System.out.println("flushCount: " + flushCount);
            //            System.out.println("mergeCount: " + mergeCount);
            //            System.out.println("count: " + pid0count + "," + pid1count + "," + pid2count + "," + pid3count + ","
            //                    + sid0count + "," + sid1count + "," + sid2count + "," + sid3count);
            //            System.out.println("" + pidxTotalFlushTime + "," + pidxTotalFlushTime / pidxFlushCount + ","
            //                    + pidxTotalMergeTime + "," + pidxTotalMergeTime / pidxMergeCount + "," + sidxTotalFlushTime + ","
            //                    + sidxTotalFlushTime / sidxFlushCount + "," + sidxTotalMergeTime + "," + sidxTotalMergeTime
            //                    / sidxMergeCount);
            //            System.out.println("" + (pidxTotalMergeTime / 1000) + "," + (pidxTotalMergeTime / 1000) / pidxMergeCount
            //                    + "," + (sidxTotalMergeTime / 1000) + "," + (sidxTotalMergeTime / 1000) / sidxMergeCount);
            System.out
                    .println("" + pidxFlushCount + "," + pidxMergeCount + "," + sidxFlushCount + "," + sidxMergeCount);
        }
    }

    private String getPrintName(String indexName) {
        String name = null;
        if (indexName.contains("Tweets")) {
            if (indexName.contains("id_0")) {
                name = "pidx0";
                pid0count++;
            } else if (indexName.contains("id_1")) {
                name = "pidx1";
                pid1count++;
            } else if (indexName.contains("id_2")) {
                name = "pidx2";
                pid2count++;
            } else if (indexName.contains("id_3")) {
                name = "pidx3";
                pid3count++;
            }
        } else if (indexName.contains("Location")) {
            if (indexName.contains("id_0")) {
                name = "sidx0"; //ReportBuilderHelper.getString(indexName, "Location") + "0";
                sid0count++;
            } else if (indexName.contains("id_1")) {
                name = "sidx1"; //ReportBuilderHelper.getString(indexName, "Location") + "1";
                sid1count++;
            } else if (indexName.contains("id_2")) {
                name = "sidx2"; //ReportBuilderHelper.getString(indexName, "Location") + "2";
                sid2count++;
            } else if (indexName.contains("id_3")) {
                name = "sidx3"; //ReportBuilderHelper.getString(indexName, "Location") + "2";
                sid3count++;
            }
        }
        return name;
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
