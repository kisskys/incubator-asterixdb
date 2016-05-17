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

public class SIE4ReportBuilderRunner {
    static boolean IS_PROFILE = false;
    String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/result-report/";
    SIE3ReportBuilder sie4Dhbtree;
    SIE3ReportBuilder sie4Dhvbtree;
    SIE3ReportBuilder sie4Rtree;
    SIE3ReportBuilder sie4Shbtree;
    SIE3ReportBuilder sie4Sif;

    //for profiling report -------------------------------------
    String profileFileHomeDir;
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

    public SIE4ReportBuilderRunner() {
        String expHomePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf1g-DiskBuf9p5g-Lsev-Jvm13p5g-Lock0g/exp4/";
        profileFileHomeDir = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/profile-exp4/";
        String runLogFileName = "run-exp4.log";
        String queryLogFileNamePrefix = "QueryGenResult-";
        String queryLogFileNameSuffix = "-130.149.249.51.txt";

        sie4Dhbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment4Dhbtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment4Dhbtree" + queryLogFileNameSuffix);
        sie4Dhvbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment4Dhvbtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment4Dhvbtree" + queryLogFileNameSuffix);
        sie4Rtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment4Rtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment4Rtree" + queryLogFileNameSuffix);
        sie4Shbtree = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment4Shbtree", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment4Shbtree" + queryLogFileNameSuffix);
        sie4Sif = new SIE3ReportBuilder(expHomePath, "SpatialIndexExperiment4Sif", runLogFileName,
                queryLogFileNamePrefix + "SpatialIndexExperiment4Sif" + queryLogFileNameSuffix);
    }

    public void generateIndexCreationTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 index creation time report\n");
        sb.append("index type, index creation time\n");
        sb.append("dhbtree,").append(sie4Dhbtree.getIndexCreationTime()).append("\n");
        sb.append("dhvbtree,").append(sie4Dhvbtree.getIndexCreationTime()).append("\n");
        sb.append("rtree,").append(sie4Rtree.getIndexCreationTime()).append("\n");
        sb.append("shbtree,").append(sie4Shbtree.getIndexCreationTime()).append("\n");
        sb.append("sif,").append(sie4Sif.getIndexCreationTime()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie4_index_creation_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateIndexSize() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 index size report\n");

        sb.append("index type, index size\n");
        sb.append("dhbtree,").append(sie4Dhbtree.getIndexSize("Tweets_idx_dhbtreeLocation/device_id")).append("\n");
        sb.append("dhvbtree,").append(sie4Dhvbtree.getIndexSize("Tweets_idx_dhvbtreeLocation/device_id")).append("\n");
        sb.append("rtree,").append(sie4Rtree.getIndexSize("Tweets_idx_rtreeLocation/device_id")).append("\n");
        sb.append("shbtree,").append(sie4Shbtree.getIndexSize("Tweets_idx_shbtreeLocation/device_id")).append("\n");
        sb.append("sif,").append(sie4Sif.getIndexSize("Tweets_idx_sifLocation/device_id")).append("\n");
        sb.append("# pidx,").append(sie4Sif.getIndexSize("Tweets_idx_Tweets/device_id")).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie4_sidx_size.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query response time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie4Dhbtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie4Rtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie4Shbtree.getSelectQueryResponseTime(0)).append(",")
                .append(sie4Sif.getSelectQueryResponseTime(0)).append("\n");
        sb.append("0.0001,").append(sie4Dhbtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie4Rtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie4Shbtree.getSelectQueryResponseTime(1)).append(",")
                .append(sie4Sif.getSelectQueryResponseTime(1)).append("\n");
        sb.append("0.001,").append(sie4Dhbtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie4Rtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie4Shbtree.getSelectQueryResponseTime(2)).append(",")
                .append(sie4Sif.getSelectQueryResponseTime(2)).append("\n");
        sb.append("0.01,").append(sie4Dhbtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie4Rtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie4Shbtree.getSelectQueryResponseTime(3)).append(",")
                .append(sie4Sif.getSelectQueryResponseTime(3)).append("\n");
        sb.append("0.1,").append(sie4Dhbtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie4Rtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie4Shbtree.getSelectQueryResponseTime(4)).append(",")
                .append(sie4Sif.getSelectQueryResponseTime(4)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                + "sie4_select_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query result count report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie4Dhbtree.getSelectQueryResultCount(0)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResultCount(0)).append(",")
                .append(sie4Rtree.getSelectQueryResultCount(0)).append(",")
                .append(sie4Shbtree.getSelectQueryResultCount(0)).append(",")
                .append(sie4Sif.getSelectQueryResultCount(0)).append("\n");
        sb.append("0.0001,").append(sie4Dhbtree.getSelectQueryResultCount(1)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResultCount(1)).append(",")
                .append(sie4Rtree.getSelectQueryResultCount(1)).append(",")
                .append(sie4Shbtree.getSelectQueryResultCount(1)).append(",")
                .append(sie4Sif.getSelectQueryResultCount(1)).append("\n");
        sb.append("0.001,").append(sie4Dhbtree.getSelectQueryResultCount(2)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResultCount(2)).append(",")
                .append(sie4Rtree.getSelectQueryResultCount(2)).append(",")
                .append(sie4Shbtree.getSelectQueryResultCount(2)).append(",")
                .append(sie4Sif.getSelectQueryResultCount(2)).append("\n");
        sb.append("0.01,").append(sie4Dhbtree.getSelectQueryResultCount(3)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResultCount(3)).append(",")
                .append(sie4Rtree.getSelectQueryResultCount(3)).append(",")
                .append(sie4Shbtree.getSelectQueryResultCount(3)).append(",")
                .append(sie4Sif.getSelectQueryResultCount(3)).append("\n");
        sb.append("0.1,").append(sie4Dhbtree.getSelectQueryResultCount(4)).append(",")
                .append(sie4Dhvbtree.getSelectQueryResultCount(4)).append(",")
                .append(sie4Rtree.getSelectQueryResultCount(4)).append(",")
                .append(sie4Shbtree.getSelectQueryResultCount(4)).append(",")
                .append(sie4Sif.getSelectQueryResultCount(4)).append("\n");

        FileOutputStream fos = ReportBuilderHelper
                .openOutputFile(outputFilePath + "sie4_select_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query response time report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie4Dhbtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie4Rtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie4Shbtree.getJoinQueryResponseTime(0)).append(",")
                .append(sie4Sif.getJoinQueryResponseTime(0)).append("\n");
        sb.append("0.0001,").append(sie4Dhbtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie4Rtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie4Shbtree.getJoinQueryResponseTime(1)).append(",")
                .append(sie4Sif.getJoinQueryResponseTime(1)).append("\n");
        sb.append("0.001,").append(sie4Dhbtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie4Rtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie4Shbtree.getJoinQueryResponseTime(2)).append(",")
                .append(sie4Sif.getJoinQueryResponseTime(2)).append("\n");
        sb.append("0.01,").append(sie4Dhbtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie4Rtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie4Shbtree.getJoinQueryResponseTime(3)).append(",")
                .append(sie4Sif.getJoinQueryResponseTime(3)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie4_join_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query result count report\n");

        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie4Dhbtree.getJoinQueryResultCount(0)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResultCount(0)).append(",")
                .append(sie4Rtree.getJoinQueryResultCount(0)).append(",")
                .append(sie4Shbtree.getJoinQueryResultCount(0)).append(",").append(sie4Sif.getJoinQueryResultCount(0))
                .append("\n");
        sb.append("0.0001,").append(sie4Dhbtree.getJoinQueryResultCount(1)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResultCount(1)).append(",")
                .append(sie4Rtree.getJoinQueryResultCount(1)).append(",")
                .append(sie4Shbtree.getJoinQueryResultCount(1)).append(",").append(sie4Sif.getJoinQueryResultCount(1))
                .append("\n");
        sb.append("0.001,").append(sie4Dhbtree.getJoinQueryResultCount(2)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResultCount(2)).append(",")
                .append(sie4Rtree.getJoinQueryResultCount(2)).append(",")
                .append(sie4Shbtree.getJoinQueryResultCount(2)).append(",").append(sie4Sif.getJoinQueryResultCount(2))
                .append("\n");
        sb.append("0.01,").append(sie4Dhbtree.getJoinQueryResultCount(3)).append(",")
                .append(sie4Dhvbtree.getJoinQueryResultCount(3)).append(",")
                .append(sie4Rtree.getJoinQueryResultCount(3)).append(",")
                .append(sie4Shbtree.getJoinQueryResultCount(3)).append(",").append(sie4Sif.getJoinQueryResultCount(3))
                .append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie4_join_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledSidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query profiled sidx search time report\n");

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
                + "sie4_select_query_profiled_sidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledPidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query profiled pidx search time report\n");

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
                + "sie4_select_query_profiled_pidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query profiled sidx search time report\n");

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
                + "sie4_join_query_profiled_sidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledPidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query profiled pidx search time report\n");

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
                + "sie4_join_query_profiled_pidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSeedPidxSearchTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query profiled query seed pidx search time report\n");

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
                + "sie4_join_query_profiled_seed_pidx_search_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledSidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query profiled sidx cache miss report\n");

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
                + "sie4_select_query_profiled_sidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledPidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query profiled pidx cache miss report\n");

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
                + "sie4_select_query_profiled_pidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query profiled sidx cache miss report\n");

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
                + "sie4_join_query_profiled_sidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledPidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query profiled pidx cache miss report\n");

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
                + "sie4_join_query_profiled_pidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateJoinQueryProfiledSeedPidxCacheMiss() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 join query profiled query seed pidx search time report\n");

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
                + "sie4_join_query_profiled_seed_pidx_cache_miss.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSelectQueryProfiledFalsePositive() throws Exception {
        sb.setLength(0);
        sb.append("# sie4 select query profiled false positive raw report\n");

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
                + "sie4_select_query_profiled_false_positive_raw.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
        generateFalsePositive(outputFilePath + "sie4_select_query_profiled_false_positive_raw.txt", outputFilePath
                + "sie4_select_query_result_count.txt", outputFilePath
                + "sie4_select_query_profiled_false_positive.txt", false);
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
            sb.append("# sie4 join query profiled false positive report\n");
        } else {
            sb.append("# sie4 select query profiled false positive report\n");
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
        sb.append("# sie4 join query profiled false positive raw report\n");

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
                + "sie4_join_query_profiled_false_positive_raw.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);

        generateFalsePositive(outputFilePath + "sie4_join_query_profiled_false_positive_raw.txt", outputFilePath
                + "sie4_join_query_result_count.txt", outputFilePath + "sie4_join_query_profiled_false_positive.txt",
                true);
    }

    public void generateQueryProfiledOperatorTime() throws Exception {
        String parentPath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/profile-exp4/";
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment4Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment4Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment4Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment4Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment4Sif/logs/";
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
        boolean isIndexOnlyPlan = false;

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
            sb.append("# sie4 select query profiled operator time report\n");
            String[] st = null;
            sb.append("operator,STREAM_SELECT,SORT_RUN_GEN,PIDX_SEARCH,STREAM_PROJECT,SIDX_SEARCH,ASSIGN,SORT_RUN_MERGER,TXN_JOB_COMMIT,DISTRIBUTE_RESULT,\n");
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

            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath
                    + "sie4_select_query_operator_profile_time_r" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }

        //for join query
        for (int i = 0; i < 4; i++) {
            sb.setLength(0);
            sb.append("# sie4 join query profiled operator time report\n");
            String[] st = null;
            sb.append("operator,STREAM_SELECT,SORT_RUN_GEN,INNER_PIDX_SEARCH,OUTER_PIDX_SEARCH,STREAM_PROJECT,INNER_SIDX_SEARCH,ASSIGN,SORT_RUN_MERGER,TXN_JOB_COMMIT,DISTRIBUTE_RESULT,\n");
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
                    + "sie4_join_query_operator_profile_time_r" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
    }

    public void generateQueryProfiledCacheMiss() throws Exception {
        String parentPath = profileFileHomeDir;
        String executionTimeFileParentPath[] = new String[5];

        executionTimeFileParentPath[0] = parentPath + "SpatialIndexExperiment4Dhbtree/logs/";
        executionTimeFileParentPath[1] = parentPath + "SpatialIndexExperiment4Dhvbtree/logs/";
        executionTimeFileParentPath[2] = parentPath + "SpatialIndexExperiment4Rtree/logs/";
        executionTimeFileParentPath[3] = parentPath + "SpatialIndexExperiment4Shbtree/logs/";
        executionTimeFileParentPath[4] = parentPath + "SpatialIndexExperiment4Sif/logs/";
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
        boolean isIndexOnlyPlan = false;

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
            sb.append("# sie4 select query profiled cache miss report\n");
            sb.append("operator, PIDX_SEARCH, SIDX_SEARCH \n");
            sb.append("dhbtree,").append(dhbtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("dhvbtree,").append(dhvbtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("rtree,").append(rtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("shbtree,").append(shbtreeOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");
            sb.append("sif,").append(sifOprb.getCacheMiss(false, i, isIndexOnlyPlan)).append("\n");

            String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie4_select_query_cache_miss_r"
                    + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }

        //for join query
        for (int i = 0; i < 4; i++) {
            sb.setLength(0);
            sb.append("# sie4 join query profiled cache miss report\n");
            sb.append("operator, INNER_PIDX_SEARCH, OUTER_PIDX_SEARCH,INNER_SIDX_SEARCH \n");
            sb.append("dhbtree,").append(dhbtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("dhvbtree,").append(dhvbtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("rtree,").append(rtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("shbtree,").append(shbtreeOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");
            sb.append("sif,").append(sifOprb.getCacheMiss(true, i, isIndexOnlyPlan)).append("\n");

            String outputFilePath = "/Users/kisskys/workspace/asterix_master/resultLog/MemBuf3g-DiskBuf3g-Lsev-Jvm7g-Lock0g/result-report/";
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie4_join_query_cache_miss_r"
                    + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
    }
}
