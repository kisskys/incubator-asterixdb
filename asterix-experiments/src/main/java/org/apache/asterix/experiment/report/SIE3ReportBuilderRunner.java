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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SIE3ReportBuilderRunner {
    static boolean IS_PROFILE = false;
    String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/result-report/";
    SIE3ReportBuilder sie3Dhbtree;
    SIE3ReportBuilder sie3Dhvbtree;
    SIE3ReportBuilder sie3Rtree;
    SIE3ReportBuilder sie3Shbtree;
    SIE3ReportBuilder sie3Sif;

    //for profiling report -------------------------------------
    String profileFileHomeDir;
    String profileLogFileName;
    String indexSearchTimeFilePath;
    String falsePositiveFilePath;
    String cacheMissFilePath;
    ProfilerReportBuilder dhbtreeProfiler;
    ProfilerReportBuilder dhvbtreeProfiler;
    ProfilerReportBuilder rtreeProfiler;
    ProfilerReportBuilder shbtreeProfiler;
    ProfilerReportBuilder sifProfiler;
    //for profiling report -------------------------------------

    StringBuilder sb = new StringBuilder();

    public SIE3ReportBuilderRunner() {

        String expHomePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf5g-DiskBuf5g-Lsev-Jvm11g-Lock0g/exp3-rg128k/";
        profileFileHomeDir = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/profile-exp3-rg128k-large-radius/";
        String runLogFileName = "run-exp3.log";
        profileLogFileName = "run-exp3.log"; //"profile-exp3.log";
        String queryLogFileNamePrefix = "QueryGenResult-";
        String queryLogFileNameSuffix = "-130.149.249.51.txt";

        sie3Dhbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Dhbtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment3Dhbtree" + queryLogFileNameSuffix);
        sie3Dhvbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Dhvbtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment3Dhvbtree" + queryLogFileNameSuffix);
        sie3Rtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Rtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment3Rtree" + queryLogFileNameSuffix);
        sie3Shbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Shbtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment3Shbtree" + queryLogFileNameSuffix);
        sie3Sif = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Sif", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment3Sif" + queryLogFileNameSuffix);

        //        sie3Dhbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Dhbtree", runLogFileName,
        //                queryLogFileNamePrefix + "SpatialIndexExperiment3Dhbtree" + queryLogFileNameSuffix);
        //        sie3Dhvbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Dhvbtree", runLogFileName,
        //                queryLogFileNamePrefix + "SpatialIndexExperiment3Dhvbtree" + queryLogFileNameSuffix);
        //        sie3Rtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Rtree", runLogFileName,
        //                queryLogFileNamePrefix + "SpatialIndexExperiment3Rtree" + queryLogFileNameSuffix);
        //        sie3Shbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Shbtree", runLogFileName,
        //                queryLogFileNamePrefix + "SpatialIndexExperiment3Shbtree" + queryLogFileNameSuffix);
        //        sie3Sif = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment3Sif", runLogFileName,
        //                queryLogFileNamePrefix + "SpatialIndexExperiment3Sif" + queryLogFileNameSuffix);
    }

    public void generateIndexCreationTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 index creation time report\n");
        sb.append("index type, index creation time\n");
        sb.append("dhbtree,").append(sie3Dhbtree.getIndexCreationTime()).append("\n");
        sb.append("dhvbtree,").append(sie3Dhvbtree.getIndexCreationTime()).append("\n");
        sb.append("rtree,").append(sie3Rtree.getIndexCreationTime()).append("\n");
        sb.append("shbtree,").append(sie3Shbtree.getIndexCreationTime()).append("\n");
        sb.append("sif,").append(sie3Sif.getIndexCreationTime()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_index_creation_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateIndexSize() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 index size report\n");

        sb.append("index type, index size\n");
        sb.append("dhbtree,").append(sie3Dhbtree.getIndexSize("Tweets_idx_dhbtreeLocation/device_id")).append("\n");
        sb.append("dhvbtree,").append(sie3Dhvbtree.getIndexSize("Tweets_idx_dhvbtreeLocation/device_id")).append("\n");
        sb.append("rtree,").append(sie3Rtree.getIndexSize("Tweets_idx_rtreeLocation/device_id")).append("\n");
        sb.append("shbtree,").append(sie3Shbtree.getIndexSize("Tweets_idx_shbtreeLocation/device_id")).append("\n");
        sb.append("sif,").append(sie3Sif.getIndexSize("Tweets_idx_sifLocation/device_id")).append("\n");
        sb.append("# pidx,").append(sie3Sif.getIndexSize("Tweets_idx_Tweets/device_id")).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_sidx_size.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query response time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie3Rtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie3Shbtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie3Sif.getSelectQueryResponseTime(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie3Rtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie3Shbtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie3Sif.getSelectQueryResponseTime(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie3Rtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie3Shbtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie3Sif.getSelectQueryResponseTime(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie3Rtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie3Shbtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie3Sif.getSelectQueryResponseTime(3)).append("\n");
        sb.append("0.1,").append(sie3Dhbtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie3Rtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie3Shbtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie3Sif.getSelectQueryResponseTime(4)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query result count report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getSelectQueryResultCount(0)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResultCount(0)).append(",")
                .append(sie3Rtree.getSelectQueryResultCount(0)).append(",")
                .append(sie3Shbtree.getSelectQueryResultCount(0)).append(",")
                .append(sie3Sif.getSelectQueryResultCount(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getSelectQueryResultCount(1)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResultCount(1)).append(",")
                .append(sie3Rtree.getSelectQueryResultCount(1)).append(",")
                .append(sie3Shbtree.getSelectQueryResultCount(1)).append(",")
                .append(sie3Sif.getSelectQueryResultCount(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getSelectQueryResultCount(2)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResultCount(2)).append(",")
                .append(sie3Rtree.getSelectQueryResultCount(2)).append(",")
                .append(sie3Shbtree.getSelectQueryResultCount(2)).append(",")
                .append(sie3Sif.getSelectQueryResultCount(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getSelectQueryResultCount(3)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResultCount(3)).append(",")
                .append(sie3Rtree.getSelectQueryResultCount(3)).append(",")
                .append(sie3Shbtree.getSelectQueryResultCount(3)).append(",")
                .append(sie3Sif.getSelectQueryResultCount(3)).append("\n");
        sb.append("0.1,").append(sie3Dhbtree.getSelectQueryResultCount(4)).append(",")
                .append(sie3Dhvbtree.getSelectQueryResultCount(4)).append(",")
                .append(sie3Rtree.getSelectQueryResultCount(4)).append(",")
                .append(sie3Shbtree.getSelectQueryResultCount(4)).append(",")
                .append(sie3Sif.getSelectQueryResultCount(4)).append("\n");

        FileOutputStream fos = ReportBuilderHelper
                .openOutputFile(outputFilePath + "sie3_select_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query response time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie3Rtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie3Shbtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie3Sif.getJoinQueryResponseTime(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie3Rtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie3Shbtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie3Sif.getJoinQueryResponseTime(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie3Rtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie3Shbtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie3Sif.getJoinQueryResponseTime(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie3Rtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie3Shbtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie3Sif.getJoinQueryResponseTime(3)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_join_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query result count report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getJoinQueryResultCount(0)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResultCount(0)).append(",")
                .append(sie3Rtree.getJoinQueryResultCount(0)).append(",")
                .append(sie3Shbtree.getJoinQueryResultCount(0)).append(",").append(sie3Sif.getJoinQueryResultCount(0))
                .append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getJoinQueryResultCount(1)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResultCount(1)).append(",")
                .append(sie3Rtree.getJoinQueryResultCount(1)).append(",")
                .append(sie3Shbtree.getJoinQueryResultCount(1)).append(",").append(sie3Sif.getJoinQueryResultCount(1))
                .append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getJoinQueryResultCount(2)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResultCount(2)).append(",")
                .append(sie3Rtree.getJoinQueryResultCount(2)).append(",")
                .append(sie3Shbtree.getJoinQueryResultCount(2)).append(",").append(sie3Sif.getJoinQueryResultCount(2))
                .append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getJoinQueryResultCount(3)).append(",")
                .append(sie3Dhvbtree.getJoinQueryResultCount(3)).append(",")
                .append(sie3Rtree.getJoinQueryResultCount(3)).append(",")
                .append(sie3Shbtree.getJoinQueryResultCount(3)).append(",").append(sie3Sif.getJoinQueryResultCount(3))
                .append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_join_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledSidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query profiled sidx search time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(true, false, 0, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 0, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 0, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 0, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 0, 0)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(true, false, 1, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 1, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 1, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 1, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 1, 0)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(true, false, 2, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 2, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 2, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 2, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 2, 0)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(true, false, 3, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 3, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 3, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 3, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 3, 0)).append("\n");
        sb.append("0.1,").append(dhbtreeProfiler.getIdxNumber(true, false, 4, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 4, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 4, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 4, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 4, 0)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_profiled_sidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledPidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query profiled pidx search time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(true, false, 0, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 0, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 0, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 0, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 0, 1)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(true, false, 1, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 1, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 1, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 1, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 1, 1)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(true, false, 2, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 2, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 2, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 2, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 2, 1)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(true, false, 3, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 3, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 3, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 3, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 3, 1)).append("\n");
        sb.append("0.1,").append(dhbtreeProfiler.getIdxNumber(true, false, 4, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, false, 4, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, false, 4, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, false, 4, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, false, 4, 1)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_profiled_pidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled sidx search time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(true, true, 0, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 0, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 0, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 0, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 0, 1)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(true, true, 1, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 1, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 1, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 1, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 1, 1)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(true, true, 2, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 2, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 2, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 2, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 2, 1)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(true, true, 3, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 3, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 3, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 3, 1)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 3, 1)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_sidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledPidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled pidx search time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(true, true, 0, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 0, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 0, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 0, 2)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 0, 2)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(true, true, 1, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 1, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 1, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 1, 2)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 1, 2)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(true, true, 2, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 2, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 2, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 2, 2)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 2, 2)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(true, true, 3, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 3, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 3, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 3, 2)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 3, 2)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_pidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSeedPidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled query seed pidx search time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(true, true, 0, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 0, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 0, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 0, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 0, 0)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(true, true, 1, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 1, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 1, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 1, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 1, 0)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(true, true, 2, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 2, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 2, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 2, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 2, 0)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(true, true, 3, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(true, true, 3, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(true, true, 3, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(true, true, 3, 0)).append(",")
                .append(sifProfiler.getIdxNumber(true, true, 3, 0)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_seed_pidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledSidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query profiled sidx cache miss report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(false, false, 0, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 0, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 0, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 0, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 0, 0)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(false, false, 1, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 1, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 1, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 1, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 1, 0)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(false, false, 2, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 2, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 2, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 2, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 2, 0)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(false, false, 3, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 3, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 3, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 3, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 3, 0)).append("\n");
        sb.append("0.1,").append(dhbtreeProfiler.getIdxNumber(false, false, 4, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 4, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 4, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 4, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 4, 0)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_profiled_sidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledPidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query profiled pidx cache miss report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(false, false, 0, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 0, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 0, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 0, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 0, 1)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(false, false, 1, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 1, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 1, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 1, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 1, 1)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(false, false, 2, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 2, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 2, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 2, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 2, 1)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(false, false, 3, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 3, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 3, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 3, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 3, 1)).append("\n");
        sb.append("0.1,").append(dhbtreeProfiler.getIdxNumber(false, false, 4, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, false, 4, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, false, 4, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, false, 4, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, false, 4, 1)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_profiled_pidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled sidx cache miss report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(false, true, 0, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 0, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 0, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 0, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 0, 1)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(false, true, 1, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 1, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 1, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 1, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 1, 1)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(false, true, 2, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 2, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 2, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 2, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 2, 1)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(false, true, 3, 1)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 3, 1)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 3, 1)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 3, 1)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 3, 1)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_sidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledPidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled pidx cache miss report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(false, true, 0, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 0, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 0, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 0, 2)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 0, 2)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(false, true, 1, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 1, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 1, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 1, 2)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 1, 2)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(false, true, 2, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 2, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 2, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 2, 2)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 2, 2)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(false, true, 3, 2)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 3, 2)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 3, 2)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 3, 2)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 3, 2)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_pidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSeedPidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled query seed pidx search time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getIdxNumber(false, true, 0, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 0, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 0, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 0, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 0, 0)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getIdxNumber(false, true, 1, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 1, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 1, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 1, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 1, 0)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getIdxNumber(false, true, 2, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 2, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 2, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 2, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 2, 0)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getIdxNumber(false, true, 3, 0)).append(",")
                .append(dhvbtreeProfiler.getIdxNumber(false, true, 3, 0)).append(",")
                .append(rtreeProfiler.getIdxNumber(false, true, 3, 0)).append(",")
                .append(shbtreeProfiler.getIdxNumber(false, true, 3, 0)).append(",")
                .append(sifProfiler.getIdxNumber(false, true, 3, 0)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_seed_pidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledFalsePositive() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query profiled false positive raw report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getFalsePositives(false, 0)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(false, 0)).append(",")
                .append(rtreeProfiler.getFalsePositives(false, 0)).append(",")
                .append(shbtreeProfiler.getFalsePositives(false, 0)).append(",")
                .append(sifProfiler.getFalsePositives(false, 0)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getFalsePositives(false, 1)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(false, 1)).append(",")
                .append(rtreeProfiler.getFalsePositives(false, 1)).append(",")
                .append(shbtreeProfiler.getFalsePositives(false, 1)).append(",")
                .append(sifProfiler.getFalsePositives(false, 1)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getFalsePositives(false, 2)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(false, 2)).append(",")
                .append(rtreeProfiler.getFalsePositives(false, 2)).append(",")
                .append(shbtreeProfiler.getFalsePositives(false, 2)).append(",")
                .append(sifProfiler.getFalsePositives(false, 2)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getFalsePositives(false, 3)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(false, 3)).append(",")
                .append(rtreeProfiler.getFalsePositives(false, 3)).append(",")
                .append(shbtreeProfiler.getFalsePositives(false, 3)).append(",")
                .append(sifProfiler.getFalsePositives(false, 3)).append("\n");
        sb.append("0.1,").append(dhbtreeProfiler.getFalsePositives(false, 4)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(false, 4)).append(",")
                .append(rtreeProfiler.getFalsePositives(false, 4)).append(",")
                .append(shbtreeProfiler.getFalsePositives(false, 4)).append(",")
                .append(sifProfiler.getFalsePositives(false, 4)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_profiled_false_positive_raw.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
        generateFalsePositive(outputFilePath + "sie3_select_query_profiled_false_positive_raw.txt", outputFilePath
                + "sie3_select_query_result_count.txt", outputFilePath
                + "sie3_select_query_profiled_false_positive.txt", false);
    }

    private void generateFalsePositive(String falsePositveFile, String queryResultCountFile, String outputFile,
            boolean isJoin) throws IOException {

        String[] fps, rcs;
        sb.setLength(0);

        BufferedReader brFalsePositive = new BufferedReader(new FileReader(falsePositveFile));
        BufferedReader brQueryResultCount = new BufferedReader(new FileReader(queryResultCountFile));

        //discard two head lines
        brFalsePositive.readLine();
        brFalsePositive.readLine();
        brQueryResultCount.readLine();
        brQueryResultCount.readLine();

        int radiusCount = isJoin ? 4 : 5;
        int partitionCount = 24;
        String[] radius = { "0.00001", "0.0001", "0.001", "0.01", "0.1" };

        if (isJoin) {
            sb.append("# sie3 join query profiled false positive report\n");
        } else {
            sb.append("# sie3 select query profiled false positive report\n");
        }
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");

        for (int i = 0; i < radiusCount; i++) {
            fps = brFalsePositive.readLine().split(",");
            rcs = brQueryResultCount.readLine().split(",");
            //false positive count
            sb.append(radius[i])
                    .append(",")
                    .append(((Double.parseDouble(fps[1]) * partitionCount) - Double.parseDouble(rcs[1]))
                            / partitionCount)
                    .append(",")
                    .append(((Double.parseDouble(fps[2]) * partitionCount) - Double.parseDouble(rcs[2]))
                            / partitionCount)
                    .append(",")
                    .append(((Double.parseDouble(fps[3]) * partitionCount) - Double.parseDouble(rcs[3]))
                            / partitionCount)
                    .append(",")
                    .append(((Double.parseDouble(fps[4]) * partitionCount) - Double.parseDouble(rcs[4]))
                            / partitionCount)
                    .append(",")
                    .append(((Double.parseDouble(fps[5]) * partitionCount) - Double.parseDouble(rcs[5]))
                            / partitionCount).append("\n");
            //false positive rate
            //            sb.append(radius[i])
            //            .append(",").append(((Double.parseDouble(fps[1]) * partitionCount) - Double.parseDouble(rcs[1]))/(Double.parseDouble(fps[1]) * partitionCount))
            //            .append(",").append(((Double.parseDouble(fps[2]) * partitionCount) - Double.parseDouble(rcs[2]))/(Double.parseDouble(fps[2]) * partitionCount))
            //            .append(",").append(((Double.parseDouble(fps[3]) * partitionCount) - Double.parseDouble(rcs[3]))/(Double.parseDouble(fps[3]) * partitionCount))
            //            .append(",").append(((Double.parseDouble(fps[4]) * partitionCount) - Double.parseDouble(rcs[4]))/(Double.parseDouble(fps[4]) * partitionCount))
            //            .append(",").append(((Double.parseDouble(fps[5]) * partitionCount) - Double.parseDouble(rcs[5]))/(Double.parseDouble(fps[5]) * partitionCount))
            //            .append("\n");
        }
        brFalsePositive.close();
        brQueryResultCount.close();

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFile);
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledFalsePositive() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query profiled false positive raw report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(dhbtreeProfiler.getFalsePositives(true, 0)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(true, 0)).append(",")
                .append(rtreeProfiler.getFalsePositives(true, 0)).append(",")
                .append(shbtreeProfiler.getFalsePositives(true, 0)).append(",")
                .append(sifProfiler.getFalsePositives(true, 0)).append("\n");
        sb.append("0.0001,").append(dhbtreeProfiler.getFalsePositives(true, 1)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(true, 1)).append(",")
                .append(rtreeProfiler.getFalsePositives(true, 1)).append(",")
                .append(shbtreeProfiler.getFalsePositives(true, 1)).append(",")
                .append(sifProfiler.getFalsePositives(true, 1)).append("\n");
        sb.append("0.001,").append(dhbtreeProfiler.getFalsePositives(true, 2)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(true, 2)).append(",")
                .append(rtreeProfiler.getFalsePositives(true, 2)).append(",")
                .append(shbtreeProfiler.getFalsePositives(true, 2)).append(",")
                .append(sifProfiler.getFalsePositives(true, 2)).append("\n");
        sb.append("0.01,").append(dhbtreeProfiler.getFalsePositives(true, 3)).append(",")
                .append(dhvbtreeProfiler.getFalsePositives(true, 3)).append(",")
                .append(rtreeProfiler.getFalsePositives(true, 3)).append(",")
                .append(shbtreeProfiler.getFalsePositives(true, 3)).append(",")
                .append(sifProfiler.getFalsePositives(true, 3)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_join_query_profiled_false_positive_raw.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);

        generateFalsePositive(outputFilePath + "sie3_join_query_profiled_false_positive_raw.txt", outputFilePath
                + "sie3_join_query_result_count.txt", outputFilePath + "sie3_join_query_profiled_false_positive.txt",
                true);
    }

    public void generateQueryProfiledOperatorTime() throws Exception {
        String parentPath = profileFileHomeDir;
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment3Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment3Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment3Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment3Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment3Sif/logs/";
        ArrayList<String> fileList = new ArrayList<String>(8);
        //        fileList.add("executionTime-192.168.0.11.txt");
        fileList.add("executionTime-130.149.249.52.txt");
        fileList.add("executionTime-130.149.249.53.txt");
        fileList.add("executionTime-130.149.249.54.txt");
        fileList.add("executionTime-130.149.249.55.txt");
        fileList.add("executionTime-130.149.249.56.txt");
        fileList.add("executionTime-130.149.249.57.txt");
        fileList.add("executionTime-130.149.249.58.txt");
        fileList.add("executionTime-130.149.249.59.txt");
        boolean isIndexOnlyPlan = true;

        //            for (int i = 0; i < 5; i++) {
        //                String fileParentPath = executionTimeFileParentPath[i];
        //                OperatorProfilerReportBuilder oprb = new OperatorProfilerReportBuilder(fileParentPath, fileList);
        //                System.out.println("--------  " + i + " ----------\n");
        //                System.out.println(oprb.getIdxNumber(false, 0, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(false, 1, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(false, 2, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(false, 3, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(false, 4, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(true, 0, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(true, 1, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(true, 2, isIndexOnlyPlan, false));
        //                System.out.println(oprb.getIdxNumber(true, 3, isIndexOnlyPlan, false));
        //            }
        //            

        OperatorProfilerReportBuilder dhbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[0],
                fileList);
        OperatorProfilerReportBuilder dhvbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[1],
                fileList);
        OperatorProfilerReportBuilder rtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[2],
                fileList);
        OperatorProfilerReportBuilder shbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[3],
                fileList);
        OperatorProfilerReportBuilder sifOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[4],
                fileList);

        StringBuilder sb = new StringBuilder();

        //for select query
        for (int i = 0; i < 5; i++) {
            sb.setLength(0);
            sb.append("# sie3 select query profiled operator time report\n");
            String[] st = null;
            sb.append("operator, STREAM_SELECT, PIDX_SEARCH, STREAM_PROJECT, SPLIT, SIDX_SEARCH, ASSIGN, UNION_ALL, TXN_JOB_COMMIT, DISTRIBUTE_RESULT,\n");
            st = dhbtreeOprb.getOperatorTime(false, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("dhbtree,").append(st[1]);
            st = dhvbtreeOprb.getOperatorTime(false, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("dhvbtree,").append(st[1]);
            st = rtreeOprb.getOperatorTime(false, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("rtree,").append(st[1]);
            st = shbtreeOprb.getOperatorTime(false, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("shbtree,").append(st[1]);
            st = sifOprb.getOperatorTime(false, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("sif,").append(st[1]);

            String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                    + "sie3_select_query_operator_profile_time_r" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }

        //for join query
        for (int i = 0; i < 4; i++) {
            sb.setLength(0);
            sb.append("# sie3 join query profiled operator time report\n");
            String[] st = null;
            sb.append("operator, STREAM_SELECT,INNER_PIDX_SEARCH,OUTER_PIDX_SEARCH,SPLIT,STREAM_PROJECT,INNER_SIDX_SEARCH,UNION_ALL,ASSIGN,TXN_JOB_COMMIT,DISTRIBUTE_RESULT,\n");
            st = dhbtreeOprb.getOperatorTime(true, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("dhbtree,").append(st[1]);
            st = dhvbtreeOprb.getOperatorTime(true, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("dhvbtree,").append(st[1]);
            st = rtreeOprb.getOperatorTime(true, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("rtree,").append(st[1]);
            st = shbtreeOprb.getOperatorTime(true, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("shbtree,").append(st[1]);
            st = sifOprb.getOperatorTime(true, i, isIndexOnlyPlan, false).split("!");
            sb.append(st[0]);
            sb.append("sif,").append(st[1]);

            String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                    + "sie3_join_query_operator_profile_time_r" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
    }

    public void generateQueryProfiledCacheMiss() throws Exception {
        String parentPath = profileFileHomeDir;
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment3Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment3Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment3Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment3Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment3Sif/logs/";
        ArrayList<String> fileList = new ArrayList<String>(8);
        //        fileList.add("executionTime-192.168.0.11.txt");
        fileList.add("cacheMissPerQuery-130.149.249.52.txt");
        fileList.add("cacheMissPerQuery-130.149.249.53.txt");
        fileList.add("cacheMissPerQuery-130.149.249.54.txt");
        fileList.add("cacheMissPerQuery-130.149.249.55.txt");
        fileList.add("cacheMissPerQuery-130.149.249.56.txt");
        fileList.add("cacheMissPerQuery-130.149.249.57.txt");
        fileList.add("cacheMissPerQuery-130.149.249.58.txt");
        fileList.add("cacheMissPerQuery-130.149.249.59.txt");
        boolean isIndexOnlyPlan = true;

        OperatorProfilerReportBuilder dhbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[0],
                fileList);
        OperatorProfilerReportBuilder dhvbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[1],
                fileList);
        OperatorProfilerReportBuilder rtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[2],
                fileList);
        OperatorProfilerReportBuilder shbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[3],
                fileList);
        OperatorProfilerReportBuilder sifOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[4],
                fileList);

        StringBuilder sb = new StringBuilder();

        //for select query
        for (int i = 0; i < 5; i++) {
            sb.setLength(0);
            sb.append("# sie3 select query profiled cache miss report\n");
            sb.append("operator, PIDX_SEARCH, SIDX_SEARCH \n");
            sb.append("dhbtree,").append(dhbtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("dhvbtree,").append(dhvbtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("rtree,").append(rtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("shbtree,").append(shbtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("sif,").append(sifOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");

            String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_select_query_cache_miss_r"
                    + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }

        //for join query
        for (int i = 0; i < 4; i++) {
            sb.setLength(0);
            sb.append("# sie3 join query profiled cache miss report\n");
            sb.append("operator, INNER_PIDX_SEARCH, OUTER_PIDX_SEARCH,INNER_SIDX_SEARCH \n");
            sb.append("dhbtree,").append(dhbtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("dhvbtree,").append(dhvbtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("rtree,").append(rtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("shbtree,").append(shbtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("sif,").append(sifOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");

            String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_join_query_cache_miss_r"
                    + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
    }

    public void generateQueryProfiledFalsePositive() throws Exception {
        String parentPath = profileFileHomeDir;
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment3Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment3Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment3Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment3Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment3Sif/logs/";
        ArrayList<String> fileList = new ArrayList<String>(8);
        //        fileList.add("executionTime-192.168.0.11.txt");
        fileList.add("falsePositivePerQuery-130.149.249.52.txt");
        fileList.add("falsePositivePerQuery-130.149.249.53.txt");
        fileList.add("falsePositivePerQuery-130.149.249.54.txt");
        fileList.add("falsePositivePerQuery-130.149.249.55.txt");
        fileList.add("falsePositivePerQuery-130.149.249.56.txt");
        fileList.add("falsePositivePerQuery-130.149.249.57.txt");
        fileList.add("falsePositivePerQuery-130.149.249.58.txt");
        fileList.add("falsePositivePerQuery-130.149.249.59.txt");
        boolean isIndexOnlyPlan = true;

        OperatorProfilerReportBuilder dhbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[0],
                fileList);
        OperatorProfilerReportBuilder dhvbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[1],
                fileList);
        OperatorProfilerReportBuilder rtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[2],
                fileList);
        OperatorProfilerReportBuilder shbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[3],
                fileList);
        OperatorProfilerReportBuilder sifOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[4],
                fileList);

        StringBuilder sb = new StringBuilder();

        //for select query
        sb.setLength(0);
        sb.append("# sie3 select query profiled false positive report\n");
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        for (int i = 0; i < 5; i++) {
            String radius = getRadius(i);
            sb.append(radius).append(dhbtreeOprb.getFalsePositive(false, i, isIndexOnlyPlan)).append(",");
            sb.append(dhvbtreeOprb.getFalsePositive(false, i, isIndexOnlyPlan)).append(",");
            sb.append(rtreeOprb.getFalsePositive(false, i, isIndexOnlyPlan)).append(",");
            sb.append(shbtreeOprb.getFalsePositive(false, i, isIndexOnlyPlan)).append(",");
            sb.append(sifOprb.getFalsePositive(false, i, isIndexOnlyPlan)).append("\n");
        }
        String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_select_query_false_positive.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);

        //for join query
        sb.setLength(0);
        sb.append("# sie3 join query profiled false positive report\n");
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        for (int i = 0; i < 4; i++) {
            String radius = getRadius(i);
            sb.append(radius).append(dhbtreeOprb.getFalsePositive(true, i, isIndexOnlyPlan)).append(",");
            sb.append(dhvbtreeOprb.getFalsePositive(true, i, isIndexOnlyPlan)).append(",");
            sb.append(rtreeOprb.getFalsePositive(true, i, isIndexOnlyPlan)).append(",");
            sb.append(shbtreeOprb.getFalsePositive(true, i, isIndexOnlyPlan)).append(",");
            sb.append(sifOprb.getFalsePositive(true, i, isIndexOnlyPlan)).append("\n");
        }
        outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
        fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie3_join_query_false_positive.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    private String getRadius(int i) {
        String radius = null;
        if (i == 0) {
            radius = "0.00001,";
        } else if (i == 1) {
            radius = "0.0001,";
        } else if (i == 2) {
            radius = "0.001,";
        } else if (i == 3) {
            radius = "0.01,";
        } else if (i == 4) {
            radius = "0.1,";
        }
        return radius;
    }

    public void generateIndexBuildProfiledCacheMiss() throws Exception {
        String parentPath = profileFileHomeDir;
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment3Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment3Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment3Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment3Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment3Sif/logs/";
        ArrayList<String> fileList = new ArrayList<String>(8);
        //        fileList.add("executionTime-192.168.0.11.txt");
        fileList.add("cacheMissPerQuery-130.149.249.52.txt");
        fileList.add("cacheMissPerQuery-130.149.249.53.txt");
        fileList.add("cacheMissPerQuery-130.149.249.54.txt");
        fileList.add("cacheMissPerQuery-130.149.249.55.txt");
        fileList.add("cacheMissPerQuery-130.149.249.56.txt");
        fileList.add("cacheMissPerQuery-130.149.249.57.txt");
        fileList.add("cacheMissPerQuery-130.149.249.58.txt");
        fileList.add("cacheMissPerQuery-130.149.249.59.txt");

        OperatorProfilerReportBuilder dhbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[0],
                fileList);
        OperatorProfilerReportBuilder dhvbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[1],
                fileList);
        OperatorProfilerReportBuilder rtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[2],
                fileList);
        OperatorProfilerReportBuilder shbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[3],
                fileList);
        OperatorProfilerReportBuilder sifOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[4],
                fileList);

        StringBuilder sb = new StringBuilder();

        sb.setLength(0);
        sb.append("# sie3 index creation cache miss report\n");
        sb.append("index type, index creation time\n");
        sb.append("dhbtree,").append(dhbtreeOprb.getCacheMissForIndexBuild()).append("\n");
        sb.append("dhvbtree,").append(dhvbtreeOprb.getCacheMissForIndexBuild()).append("\n");
        sb.append("rtree,").append(rtreeOprb.getCacheMissForIndexBuild()).append("\n");
        sb.append("shbtree,").append(shbtreeOprb.getCacheMissForIndexBuild()).append("\n");
        sb.append("sif,").append(sifOprb.getCacheMissForIndexBuild()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_index_creation_cache_miss_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);

    }

    public void generateIndexBuildProfiledOperatorTime() throws Exception {
        String parentPath = profileFileHomeDir;
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment3Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment3Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment3Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment3Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment3Sif/logs/";
        ArrayList<String> fileList = new ArrayList<String>(8);
        //        fileList.add("executionTime-192.168.0.11.txt");
        fileList.add("executionTime-130.149.249.52.txt");
        fileList.add("executionTime-130.149.249.53.txt");
        fileList.add("executionTime-130.149.249.54.txt");
        fileList.add("executionTime-130.149.249.55.txt");
        fileList.add("executionTime-130.149.249.56.txt");
        fileList.add("executionTime-130.149.249.57.txt");
        fileList.add("executionTime-130.149.249.58.txt");
        fileList.add("executionTime-130.149.249.59.txt");

        OperatorProfilerReportBuilder dhbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[0],
                fileList);
        OperatorProfilerReportBuilder dhvbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[1],
                fileList);
        OperatorProfilerReportBuilder rtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[2],
                fileList);
        OperatorProfilerReportBuilder shbtreeOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[3],
                fileList);
        OperatorProfilerReportBuilder sifOprb = new OperatorProfilerReportBuilder(executionTimeFileParentPath[4],
                fileList);

        StringBuilder sb = new StringBuilder();

        //for select query
        sb.setLength(0);
        sb.append("# sie3 index build profiled operator time report\n");
        String[] st = null;
        sb.append("operator,SORT_RUN_GEN,PIDX_SEARCH,ASSIGN,SORT_RUN_MERGER,TXN_JOB_COMMIT,DISTRIBUTE_RESULT,\n");
        st = dhbtreeOprb.getOperatorTimeForIndexBuild(false).split("!");
        sb.append(st[0]);
        sb.append("dhbtree,").append(st[1]);
        st = dhvbtreeOprb.getOperatorTimeForIndexBuild(false).split("!");
        sb.append(st[0]);
        sb.append("dhvbtree,").append(st[1]);
        st = rtreeOprb.getOperatorTimeForIndexBuild(false).split("!");
        sb.append(st[0]);
        sb.append("rtree,").append(st[1]);
        st = shbtreeOprb.getOperatorTimeForIndexBuild(false).split("!");
        sb.append(st[0]);
        sb.append("shbtree,").append(st[1]);
        st = sifOprb.getOperatorTimeForIndexBuild(false).split("!");
        sb.append(st[0]);
        sb.append("sif,").append(st[1]);

        String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie3_index_build_operator_profile_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

}
