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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class OperatorProfilerReportBuilder {

    private static final int INDEX_BUILD_OP_COUNT = 1;
    private static final int PIDX_SCAN_OP_COUNT = 1;
    private static final int WARM_UP_SELECT_QUERY_COUNT = 500;
    private static final int SELECT_QUERY_COUNT = 5000;
    private static final int JOIN_QUERY_COUNT = 200;
    private static final int JOIN_RADIUS_TYPE_COUNT = 4;
    private static final int SELECT_RADIUS_TYPE_COUNT = 5;
    private static final int IDX_JOIN_RADIUS_SKIP = JOIN_RADIUS_TYPE_COUNT - 1;
    private static final int IDX_SELECT_RADIUS_SKIP = SELECT_RADIUS_TYPE_COUNT - 1;
    private static final int IDX_INITIAL_JOIN_SKIP = INDEX_BUILD_OP_COUNT + PIDX_SCAN_OP_COUNT
            + WARM_UP_SELECT_QUERY_COUNT + SELECT_QUERY_COUNT;
    private static final int IDX_INITIAL_SELECT_SKIP = INDEX_BUILD_OP_COUNT + PIDX_SCAN_OP_COUNT
            + WARM_UP_SELECT_QUERY_COUNT;

    private static final int HYRACK_JOB_ELAPSED_TIME_FIELD = 2;
    private static final int OP_ELAPSED_TIME_FIELD = 4;
    private static final int OP_TASK_ID_FIELD = 2;
    private static final int OP_NAME_FIELD = 1;
    private static final int OP_JOB_COMMIT_TIME_FIELD = 2;

    private static final int PARTITION_COUNT_PER_NODE = 4;

    //constant for cache miss
    private static final int CM_LINE_COUNT_PER_INDEX_BUILD_OP = 1 * PARTITION_COUNT_PER_NODE;
    private static final int CM_LINE_COUNT_PER_PIDX_SCAN_OP = 1 * PARTITION_COUNT_PER_NODE;
    private static final int CM_LINE_COUNT_PER_SELECT_QUERY = 2 * PARTITION_COUNT_PER_NODE;
    private static final int CM_LINE_COUNT_PER_JOIN_QUERY = 3 * PARTITION_COUNT_PER_NODE;
    private static final int CM_INITIAL_SELECT_SKIP = CM_LINE_COUNT_PER_INDEX_BUILD_OP + CM_LINE_COUNT_PER_PIDX_SCAN_OP
            + WARM_UP_SELECT_QUERY_COUNT * CM_LINE_COUNT_PER_SELECT_QUERY;
    private static final int CM_INITIAL_JOIN_SKIP = CM_LINE_COUNT_PER_INDEX_BUILD_OP + CM_LINE_COUNT_PER_PIDX_SCAN_OP
            + (WARM_UP_SELECT_QUERY_COUNT + SELECT_QUERY_COUNT) * CM_LINE_COUNT_PER_SELECT_QUERY;
    private static final int CM_SELECT_RADIUS_SKIP = (SELECT_RADIUS_TYPE_COUNT - 1) * CM_LINE_COUNT_PER_SELECT_QUERY;
    private static final int CM_JOIN_RADIUS_SKIP = (JOIN_RADIUS_TYPE_COUNT - 1) * CM_LINE_COUNT_PER_JOIN_QUERY;

    //constant for false positive
    private static final int FP_LINE_COUNT_PER_INDEX_BUILD_OP = 0;
    private static final int FP_LINE_COUNT_PER_PIDX_SCAN_OP = 0;
    private static final int FP_LINE_COUNT_PER_SELECT_QUERY = 2 * PARTITION_COUNT_PER_NODE;
    private static final int FP_LINE_COUNT_PER_JOIN_QUERY = 2 * PARTITION_COUNT_PER_NODE;
    private static final int FP_INITIAL_SELECT_SKIP = FP_LINE_COUNT_PER_INDEX_BUILD_OP + FP_LINE_COUNT_PER_PIDX_SCAN_OP
            + WARM_UP_SELECT_QUERY_COUNT * FP_LINE_COUNT_PER_SELECT_QUERY;
    private static final int FP_INITIAL_JOIN_SKIP = FP_LINE_COUNT_PER_INDEX_BUILD_OP + FP_LINE_COUNT_PER_PIDX_SCAN_OP
            + (WARM_UP_SELECT_QUERY_COUNT + SELECT_QUERY_COUNT) * FP_LINE_COUNT_PER_SELECT_QUERY;
    private static final int FP_SELECT_RADIUS_SKIP = (SELECT_RADIUS_TYPE_COUNT - 1) * FP_LINE_COUNT_PER_SELECT_QUERY;
    private static final int FP_JOIN_RADIUS_SKIP = (JOIN_RADIUS_TYPE_COUNT - 1) * FP_LINE_COUNT_PER_JOIN_QUERY;

    private String profiledLogFileParentPath = null;
    private BufferedReader[] brProfiledLogFiles;
    private String line;
    private int lineNum;
    private ArrayList<String> fileList;
    private int fileCount;
    private final String EXTERNAL_SORT_RUN_GEN_NAME = "EXTERNAL_SORT_RUN_GENERATOR";
    private final String EXTERNAL_SORT_RUN_MERGER_NAME = "EXTERNAL_SORT_RUN_MERGER";
    private long externalSortOpTime = 0;
    private int externalSortOpTimeCount = 0;

    public OperatorProfilerReportBuilder(String profiledLogFileParentPath, ArrayList<String> fileList) {
        this.profiledLogFileParentPath = profiledLogFileParentPath;
        this.fileList = fileList;
        fileCount = fileList.size();
    }

    public String getOperatorTime(boolean isJoin, int radiusIdx, boolean isIndexOnlyPlan, boolean humanReadable)
            throws Exception {
        openProfiledLogFile();

        StringBuilder sb = new StringBuilder();
        int initialSkip = (isJoin ? IDX_INITIAL_JOIN_SKIP : IDX_INITIAL_SELECT_SKIP) + radiusIdx;
        int radiusSkip = isJoin ? IDX_JOIN_RADIUS_SKIP : IDX_SELECT_RADIUS_SKIP;
        int queryCount = isJoin ? JOIN_QUERY_COUNT / JOIN_RADIUS_TYPE_COUNT
                : SELECT_QUERY_COUNT / SELECT_RADIUS_TYPE_COUNT;
        lineNum = 0;
        JobStat jobStat = new JobStat();

        try {

            for (int i = 0; i < fileCount; i++) {
                //initial skip for each file
                BufferedReader br = brProfiledLogFiles[i];
                int jobCount = 0;
                while ((line = br.readLine()) != null) {
                    if (i == 0)
                        lineNum++;
                    if (line.contains("TOTAL_HYRACKS_JOB")) {
                        jobCount++;
                        if (jobCount == initialSkip) {
                            break;
                        }
                    }
                }
            }

            for (int j = 0; j < queryCount; j++) {
                for (int i = 0; i < fileCount; i++) {
                    BufferedReader br = brProfiledLogFiles[i];
                    while ((line = br.readLine()) != null) {
                        if (i == 0)
                            lineNum++;
                        if (line.contains("TOTAL_HYRACKS_JOB")) {
                            //Reaching Here, line variable contains the job to be counted
                            analyzeOperatorExecutionTime(jobStat, br, i, isJoin, isIndexOnlyPlan);
                            break;
                        }
                    }
                }
                jobStat.updateTaskForAvgWithSlowestTask();

                for (int i = 0; i < fileCount; i++) {
                    BufferedReader br = brProfiledLogFiles[i];
                    //radius skip
                    int jobCount = 0;
                    while ((line = br.readLine()) != null) {
                        if (i == 0)
                            lineNum++;
                        if (line.contains("TOTAL_HYRACKS_JOB")) {
                            jobCount++;
                            if (jobCount == radiusSkip) {
                                break;
                            }
                        }
                    }
                }
            }

            //System.out.println("lineNum: " + lineNum);
            if (humanReadable) {
                sb.append("TOTAL_HYRACKS_JOB,"
                        + (((double) jobStat.getHyracksJobTimeSum()) / jobStat.getHyracksJobCount()) + ","
                        + jobStat.getHyracksJobTimeSum() + "," + jobStat.getHyracksJobCount() + "\n");
                sb.append(jobStat.getOperatorsElapsedTimeAsString(humanReadable));
            } else {
                sb.append(jobStat.getOperatorsElapsedTimeAsString(humanReadable));
            }
            return sb.toString();
        } finally {
            closeProfiledLogFile();
        }
    }

    public String getOperatorTimeForIndexBuild(boolean humanReadable) throws Exception {
        openProfiledLogFile();

        StringBuilder sb = new StringBuilder();
        lineNum = 0;
        JobStat jobStat = new JobStat();

        try {
            for (int i = 0; i < fileCount; i++) {
                BufferedReader br = brProfiledLogFiles[i];
                while ((line = br.readLine()) != null) {
                    if (i == 0)
                        lineNum++;
                    if (line.contains("TOTAL_HYRACKS_JOB")) {
                        //Reaching Here, line variable contains the job to be counted
                        analyzeOperatorExecutionTime(jobStat, br, i, false, false);
                        break;
                    }
                }
            }
            jobStat.updateTaskForAvgWithSlowestTask();

            //System.out.println("lineNum: " + lineNum);
            if (humanReadable) {
                sb.append("TOTAL_HYRACKS_JOB,"
                        + (((double) jobStat.getHyracksJobTimeSum()) / jobStat.getHyracksJobCount()) + ","
                        + jobStat.getHyracksJobTimeSum() + "," + jobStat.getHyracksJobCount() + "\n");
                sb.append(jobStat.getOperatorsElapsedTimeAsString(humanReadable));
            } else {
                sb.append(jobStat.getOperatorsElapsedTimeAsString(humanReadable));
            }
            return sb.toString();
        } finally {
            closeProfiledLogFile();
        }
    }

    private void analyzeOperatorExecutionTime(JobStat jobStat, BufferedReader br, int brIdx, boolean isJoin,
            boolean isIndexOnlyPlan) throws IOException {
        int partitionCountPerNode = 4;
        int[] indexSearchCounts = new int[4];

        //the line argument contains TOTAL_HYRACKS_JOB string. eg.:
        //2015-11-04 19:13:08,003   TOTAL_HYRACKS_JOB a1_node1_JID:3_26202768 TOTAL_HYRACKS_JOB1446660788003    1066    1.066   1066    1.066
        String tokens[] = line.split("\t");

        //        if (Long.parseLong(tokens[HYRACK_JOB_ELAPSED_TIME_FIELD]) > 10000) {
        //            System.out.println("[" + lineNum + "] " + line);
        //        }

        //add hyracks job time: this time doesn't pick the slowest one but the first one. 
        if (brIdx == 0) {
            jobStat.addHyracksJobTime(Long.parseLong(tokens[HYRACK_JOB_ELAPSED_TIME_FIELD]));
        }

        while ((line = br.readLine()) != null) {
            if (brIdx == 0)
                lineNum++;

            if (line.isEmpty()) {
                break;
            }

            tokens = line.split("\t");
            if (line.contains("DISTRIBUTE_RESULT")) {
                jobStat.addDistributeResultTime(Long.parseLong(tokens[OP_ELAPSED_TIME_FIELD]));
                continue;
            }
            if (line.contains("EMPTY_TUPLE_SOURCE")) {
                continue; //ignore empty-tuple-source op.
            }

            if (line.contains("TXN_JOB_COMMIT")) {
                //add job commit time: this time doesn't pick the slowest one but the first one. 
                if (brIdx == 0) {
                    jobStat.addJobCommitTime(Long.parseLong(tokens[OP_JOB_COMMIT_TIME_FIELD]));
                }
                continue;
            }

            String subTokens[] = tokens[OP_TASK_ID_FIELD].split(":");

            String opName = tokens[OP_NAME_FIELD];
            long elapsedTime = Long.parseLong(tokens[OP_ELAPSED_TIME_FIELD]);

            if (tokens[OP_NAME_FIELD].contains("_INDEX@")) {
                if (tokens[OP_NAME_FIELD].equals("BTREE_INDEX@Tweets_SEARCH")) {
                    opName = "INNER_PIDX_SEARCH";
                } else if (tokens[OP_NAME_FIELD].equals("BTREE_INDEX@JoinSeedTweets_SEARCH")) {
                    opName = "OUTER_PIDX_SEARCH";
                } else {
                    opName = "INNER_SIDX_SEARCH";
                }
            }

            jobStat.updateOperatorTime(subTokens[6] + ":" + subTokens[7], opName, elapsedTime);
        }
    }

    protected void openProfiledLogFile() throws IOException {
        brProfiledLogFiles = new BufferedReader[fileCount];
        for (int i = 0; i < fileCount; i++) {
            brProfiledLogFiles[i] = new BufferedReader(new FileReader(profiledLogFileParentPath + fileList.get(i)));
        }
    }

    protected void closeProfiledLogFile() throws IOException {
        for (int i = 0; i < fileCount; i++) {
            if (brProfiledLogFiles[i] != null) {
                brProfiledLogFiles[i].close();
            }
        }
    }

    public String getCacheMiss(boolean isJoin, int radiusIdx, boolean isIndexOnlyPlan) throws Exception {

        openProfiledLogFile();

        StringBuilder sb = new StringBuilder();
        int initialSkip = (isJoin ? CM_INITIAL_JOIN_SKIP : CM_INITIAL_SELECT_SKIP)
                + (radiusIdx * (isJoin ? CM_LINE_COUNT_PER_JOIN_QUERY : CM_LINE_COUNT_PER_SELECT_QUERY));
        int radiusSkip = isJoin ? CM_JOIN_RADIUS_SKIP : CM_SELECT_RADIUS_SKIP;
        int queryCount = isJoin ? JOIN_QUERY_COUNT / JOIN_RADIUS_TYPE_COUNT
                : SELECT_QUERY_COUNT / SELECT_RADIUS_TYPE_COUNT;

        CacheMissCount cmc = new CacheMissCount();
        try {

            for (int i = 0; i < fileCount; i++) {
                //initial skip for each file
                BufferedReader br = brProfiledLogFiles[i];
                int lineCount = 0;
                while ((line = br.readLine()) != null) {
                    if (i == 0)
                        lineNum++;
                    lineCount++;
                    if (lineCount == initialSkip) {
                        break;
                    }
                }
            }

            for (int j = 0; j < queryCount; j++) {
                for (int i = 0; i < fileCount; i++) {
                    BufferedReader br = brProfiledLogFiles[i];
                    analyzeCacheMiss(cmc, br, i, isJoin, isIndexOnlyPlan);
                }

                for (int i = 0; i < fileCount; i++) {
                    BufferedReader br = brProfiledLogFiles[i];
                    //radius skip
                    int lineCount = 0;
                    while ((line = br.readLine()) != null) {
                        if (i == 0)
                            lineNum++;
                        lineCount++;
                        if (lineCount == radiusSkip) {
                            break;
                        }
                    }
                }
            }

            //System.out.println("lineNum: " + lineNum);
            if (isJoin) {
                sb.append(((double) cmc.innerPidx) / (JOIN_QUERY_COUNT / JOIN_RADIUS_TYPE_COUNT) / fileCount)
                        .append(",")
                        .append(((double) cmc.outerPidx) / (JOIN_QUERY_COUNT / JOIN_RADIUS_TYPE_COUNT) / fileCount)
                        .append(",")
                        .append(((double) cmc.innerSidx) / (JOIN_QUERY_COUNT / JOIN_RADIUS_TYPE_COUNT) / fileCount);
            } else {
                sb.append(((double) cmc.innerPidx) / (SELECT_QUERY_COUNT / SELECT_RADIUS_TYPE_COUNT) / fileCount)
                        .append(",")
                        .append(((double) cmc.innerSidx) / (SELECT_QUERY_COUNT / SELECT_RADIUS_TYPE_COUNT) / fileCount);
            }
            return sb.toString();
        } finally {
            closeProfiledLogFile();
        }
    }

    private void analyzeCacheMiss(CacheMissCount cmc, BufferedReader br, int brIdx, boolean isJoin,
            boolean isIndexOnlyPlan) throws IOException {
        int lineCountToRead = isJoin ? CM_LINE_COUNT_PER_JOIN_QUERY : CM_LINE_COUNT_PER_SELECT_QUERY;
        int innerPidxCacheMissMax = 0;
        int innerSidxCacheMissMax = 0;
        int outerPidxCacheMissMax = 0;
        int count;

        for (int i = 0; i < lineCountToRead; i++) {
            line = br.readLine();
            count = Integer.parseInt(line.split(",")[1]);
            if (brIdx == 0)
                ++lineNum;
            if (line.contains("Location")) {
                if (innerSidxCacheMissMax < count) {
                    innerSidxCacheMissMax = count;
                }
            } else if (line.contains("@Tweets")) {
                if (innerPidxCacheMissMax < count) {
                    innerPidxCacheMissMax = count;
                }
            } else if (line.contains("JoinSeed")) {
                if (outerPidxCacheMissMax < count) {
                    outerPidxCacheMissMax = count;
                }
            } else {
                throw new IllegalStateException("unknown index:" + line + "in line [" + lineNum + "]");
            }
        }

        cmc.innerPidx += innerPidxCacheMissMax;
        cmc.innerSidx += innerSidxCacheMissMax;
        if (isJoin) {
            cmc.outerPidx += outerPidxCacheMissMax;
        }

    }

    class CacheMissCount {
        public int innerPidx;
        public int innerSidx;
        public int outerPidx;
    }

    public String getCacheMissForIndexBuild() throws Exception {

        openProfiledLogFile();
        try {
            StringBuilder sb = new StringBuilder();
            int cacheMiss = 0;

            for (int i = 0; i < fileCount; i++) {
                BufferedReader br = brProfiledLogFiles[i];
                for (int j = 0; j < PARTITION_COUNT_PER_NODE; j++) {
                    line = br.readLine();
                    cacheMiss += Integer.parseInt(line.split(",")[1]);
                }
            }
            sb.append(cacheMiss);
            return sb.toString();
        } finally {
            closeProfiledLogFile();
        }
    }

    public String getFalsePositive(boolean isJoin, int radiusIdx, boolean isIndexOnlyPlan) throws Exception {

        openProfiledLogFile();

        StringBuilder sb = new StringBuilder();
        int initialSkip = (isJoin ? FP_INITIAL_JOIN_SKIP : FP_INITIAL_SELECT_SKIP)
                + (radiusIdx * (isJoin ? FP_LINE_COUNT_PER_JOIN_QUERY : FP_LINE_COUNT_PER_SELECT_QUERY));
        int radiusSkip = isJoin ? FP_JOIN_RADIUS_SKIP : FP_SELECT_RADIUS_SKIP;
        int queryCount = isJoin ? JOIN_QUERY_COUNT / JOIN_RADIUS_TYPE_COUNT
                : SELECT_QUERY_COUNT / SELECT_RADIUS_TYPE_COUNT;

        FalsePositiveCount fpc = new FalsePositiveCount();
        try {

            for (int i = 0; i < fileCount; i++) {
                //initial skip for each file
                BufferedReader br = brProfiledLogFiles[i];
                int lineCount = 0;
                while ((line = br.readLine()) != null) {
                    if (i == 0)
                        lineNum++;
                    lineCount++;
                    if (lineCount == initialSkip) {
                        break;
                    }
                }
            }

            for (int j = 0; j < queryCount; j++) {
                for (int i = 0; i < fileCount; i++) {
                    BufferedReader br = brProfiledLogFiles[i];
                    analyzeFalsePositive(fpc, br, i, isJoin, isIndexOnlyPlan);
                }

                for (int i = 0; i < fileCount; i++) {
                    BufferedReader br = brProfiledLogFiles[i];
                    //radius skip
                    int lineCount = 0;
                    while ((line = br.readLine()) != null) {
                        if (i == 0)
                            lineNum++;
                        lineCount++;
                        if (lineCount == radiusSkip) {
                            break;
                        }
                    }
                }
            }

            //System.out.println("lineNum: " + lineNum);
            sb.append(((double) fpc.falsePositive) / ((double) fpc.input));
            return sb.toString();
        } finally {
            closeProfiledLogFile();
        }
    }

    private void analyzeFalsePositive(FalsePositiveCount fpc, BufferedReader br, int brIdx, boolean isJoin,
            boolean isIndexOnlyPlan) throws IOException {
        int lineCountToRead = isJoin ? FP_LINE_COUNT_PER_JOIN_QUERY : FP_LINE_COUNT_PER_SELECT_QUERY;
        String stCounts[] = null;

        for (int i = 0; i < lineCountToRead; i++) {
            line = br.readLine();
            if (brIdx == 0)
                ++lineNum;
            stCounts = line.split(",");

            fpc.falsePositive += Long.parseLong(stCounts[0]);
            fpc.input += Long.parseLong(stCounts[1]);
            fpc.output += Long.parseLong(stCounts[2]);
        }
    }

    class FalsePositiveCount {
        public long falsePositive;
        public long input;
        public long output;
    }

    class JobStat {
        private long hyracksJobElapsedTimeSum;
        private int hyracksJobCount;
        private long distributeResultTimeSum;
        private long jobCommitTimeSum;
        private Task taskForAvg;
        private HashMap<String, Task> taskId2TaskMap;

        public JobStat() {
            hyracksJobElapsedTimeSum = 0;
            hyracksJobCount = 0;
            distributeResultTimeSum = 0;
            jobCommitTimeSum = 0;
            taskForAvg = new Task("TaskForAvg");
            taskId2TaskMap = new HashMap<String, Task>();
        }

        public void reset() {
            hyracksJobElapsedTimeSum = 0;
            hyracksJobCount = 0;
            distributeResultTimeSum = 0;
            jobCommitTimeSum = 0;
            taskForAvg.reset();;
            taskId2TaskMap.clear();
        }

        public void addHyracksJobTime(long elapsedTime) {
            hyracksJobElapsedTimeSum += elapsedTime;
            hyracksJobCount++;
        }

        public void addDistributeResultTime(long elapsedTime) {
            distributeResultTimeSum += elapsedTime;
        }

        public void addJobCommitTime(long elapsedTime) {
            jobCommitTimeSum += elapsedTime;
        }

        public long getDistributeResultTime() {
            return distributeResultTimeSum;
        }

        public long getJobCommitTime() {
            return jobCommitTimeSum;
        }

        public long getHyracksJobTimeSum() {
            return hyracksJobElapsedTimeSum;
        }

        public int getHyracksJobCount() {
            return hyracksJobCount;
        }

        public void updateOperatorTime(String taskId, String operatorName, long elapsedTime) {
            Task task = taskId2TaskMap.get(taskId);
            if (task == null) {
                task = new Task(taskId);
                taskId2TaskMap.put(new String(taskId), task);
            }
            task.updateOperatorTime(operatorName, elapsedTime);
        }

        public void updateTaskForAvgWithSlowestTask() {
            Iterator<Entry<String, Task>> taskIter = taskId2TaskMap.entrySet().iterator();
            Task slowestTask = null;
            Task curTask;

            //get the slowest task
            while (taskIter.hasNext()) {
                curTask = taskIter.next().getValue();
                if (slowestTask == null) {
                    slowestTask = curTask;
                } else {
                    if (slowestTask.getElapsedTime() < curTask.getElapsedTime()) {
                        slowestTask = curTask;
                    }
                }
            }

            //update the TaskForAvg with the slowest one
            HashMap<String, SumCount> operator2SumCountMap = slowestTask.getOperator2SumCountMap();
            Iterator<Entry<String, SumCount>> operatorIter = operator2SumCountMap.entrySet().iterator();
            while (operatorIter.hasNext()) {
                Entry<String, SumCount> entry = operatorIter.next();
                SumCount sc = entry.getValue();
                taskForAvg.updateOperatorTime(entry.getKey(), sc.sum);
            }
            taskId2TaskMap.clear();
        }

        public String getOperatorsElapsedTimeAsString(boolean humanReadable) {
            //            return "SUM_OF_OPERATORS,"
            //                    + (((double) (taskForAvg.getElapsedTime() + distributeResultTimeSum)) / hyracksJobCount) + ","
            //                    + taskForAvg.getElapsedTime() + "," + hyracksJobCount + "\n"
            //                    + taskForAvg.getOperatorsElapsedTimeAsString() + "DISTRIBUTE_RESULT,"
            //                    + (((double) distributeResultTimeSum) / hyracksJobCount) + "," + distributeResultTimeSum + ","
            //                    + hyracksJobCount + "\n";
            if (humanReadable) {
                return "SUM_OF_OPERATORS,"
                        + (((double) (taskForAvg.getElapsedTime() + jobCommitTimeSum + distributeResultTimeSum))
                                / hyracksJobCount)
                        + "," + taskForAvg.getElapsedTime() + "," + hyracksJobCount + "\n"
                        + taskForAvg.getOperatorsElapsedTimeAsStringHumanReadable() + "TXN_JOB_COMMIT,"
                        + (((double) jobCommitTimeSum) / hyracksJobCount) + "," + jobCommitTimeSum + ","
                        + hyracksJobCount + "\n" + "DISTRIBUTE_RESULT,"
                        + (((double) distributeResultTimeSum) / hyracksJobCount) + "," + distributeResultTimeSum + ","
                        + hyracksJobCount + "\n";
            } else {
                return "#" + taskForAvg.getOperatorsName() + "TXN_JOB_COMMIT,DISTRIBUTE_RESULT,\n!"
                        + taskForAvg.getOperatorsElapsedTime() + (((double) jobCommitTimeSum) / hyracksJobCount) + ","
                        + (((double) distributeResultTimeSum) / hyracksJobCount) + ",\n";
            }
        }
    }

    class Task {
        private String taskId;
        private long elapsedTime;
        private HashMap<String, SumCount> operator2SumCountMap;

        public Task(String taskId) {
            this.taskId = new String(taskId);
            elapsedTime = 0;
            operator2SumCountMap = new HashMap<String, SumCount>();
        }

        @Override
        public int hashCode() {
            return taskId.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Task)) {
                return false;
            }
            return ((Task) o).taskId == taskId;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public void updateOperatorTime(String operatorName, long elapsedTime) {
            SumCount sc = operator2SumCountMap.get(operatorName);
            if (sc == null) {
                sc = new SumCount();
                sc.sum = 0;
                sc.count = 0;
                operator2SumCountMap.put(new String(operatorName), sc);
            }
            sc.sum += elapsedTime;
            sc.count++;
            this.elapsedTime += elapsedTime;
        }

        public void reset() {
            elapsedTime = 0;
            operator2SumCountMap.clear();
        }

        public String getOperatorsElapsedTimeAsStringHumanReadable() {
            StringBuilder sb = new StringBuilder();
            Iterator<Entry<String, SumCount>> iter = operator2SumCountMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, SumCount> entry = iter.next();
                SumCount sc = entry.getValue();
                sb.append(entry.getKey()).append(",").append(((double) sc.sum) / sc.count).append(",").append(sc.sum)
                        .append(",").append(sc.count).append("\n");
            }
            return sb.toString();
        }

        public String getOperatorsName() {
            StringBuilder sb = new StringBuilder();
            Iterator<Entry<String, SumCount>> iter = operator2SumCountMap.entrySet().iterator();
            //add operator names into a row
            while (iter.hasNext()) {
                Entry<String, SumCount> entry = iter.next();
                sb.append(entry.getKey()).append(",");
            }
            return sb.toString();
        }

        public String getOperatorsElapsedTime() {
            StringBuilder sb = new StringBuilder();
            externalSortOpTime = 0;
            externalSortOpTimeCount = 0;
            boolean hasSortOp = false;
            //add operator times into the next row
            Iterator<Entry<String, SumCount>> iter = operator2SumCountMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, SumCount> entry = iter.next();
                //                if (entry.getKey().equalsIgnoreCase(EXTERNAL_SORT_RUN_GEN_NAME)) {
                //                    externalSortOpTime = entry.getValue().sum;
                //                } else if (entry.getKey().equalsIgnoreCase(EXTERNAL_SORT_RUN_MERGER_NAME)) {
                //                    externalSortOpTime += entry.getValue().sum;
                //                    externalSortOpTimeCount += entry.getValue().count;
                //                } else {
                //                    SumCount sc = entry.getValue();
                //                    sb.append(((double) sc.sum) / sc.count).append(",");
                //                }
                if (entry.getKey().equalsIgnoreCase(EXTERNAL_SORT_RUN_GEN_NAME)) {
                    hasSortOp = true;
                }
                if (entry.getKey().equalsIgnoreCase("INNER_PIDX_SEARCH") && !hasSortOp) {
                    sb.append("0.0,");
                }
                SumCount sc = entry.getValue();
                sb.append(((double) sc.sum) / sc.count).append(",");
            }
            if (!hasSortOp) {
                sb.append("0.0,");
            }
            return sb.toString();
        }

        public HashMap<String, SumCount> getOperator2SumCountMap() {
            return operator2SumCountMap;
        }
    }

    class SumCount {
        public long sum;
        public int count;
    }

}
