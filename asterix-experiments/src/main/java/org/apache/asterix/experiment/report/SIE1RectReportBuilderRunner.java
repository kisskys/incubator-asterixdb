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

import java.io.FileOutputStream;
import java.util.ArrayList;

public class SIE1RectReportBuilderRunner {
    String expHomePath = "/Users/kisskys/workspace/asterix_master/resultLog/Rect-InstantLock-MemBuf1g-DiskBuf3g-Lsev-Jvm7g-Lock6p5g-cell30/run-exp1-lake/";
    String runLogFileName = "run-exp1-lake.log";
    String outputFilePath = "/Users/kisskys/workspace/asterix_master/plotScript/rect/";

    SIE1ReportBuilder sie1ARtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1ARtree", runLogFileName);
    SIE1ReportBuilder sie1AShbtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1AShbtree",
            runLogFileName);

    SIE1ReportBuilder sie1BRtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1BRtree", runLogFileName);
    SIE1ReportBuilder sie1BShbtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1BShbtree",
            runLogFileName);

    SIE1ReportBuilder sie1CRtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1CRtree", runLogFileName);
    SIE1ReportBuilder sie1CShbtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1CShbtree",
            runLogFileName);

    SIE1ReportBuilder sie1DRtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1DRtree", runLogFileName);
    SIE1ReportBuilder sie1DShbtree = new SIE1ReportBuilder(expHomePath, "SpatialIndexExperiment1DShbtree",
            runLogFileName);

    StringBuilder sb = new StringBuilder();

    /**
     * generate sie1_ips.txt
     */
    public void generateSIE1IPS() throws Exception {
        int minutes = 60;
        sb.setLength(0);
        sb.append("# sie1 ips(inserts per second) report\n");
        sb.append("# number of nodes, dhbtree, dhvbtree, rtree, shbtree\n");
        sb.append("1,").append(",").append(",").append(sie1ARtree.getOverallInsertPS(minutes)).append(",")
                .append(sie1AShbtree.getOverallInsertPS(minutes)).append("\n");

        sb.append("2,").append(",").append(",").append(sie1BRtree.getOverallInsertPS(minutes)).append(",")
                .append(sie1BShbtree.getOverallInsertPS(minutes)).append("\n");

        sb.append("4,").append(",").append(",").append(sie1CRtree.getOverallInsertPS(minutes)).append(",")
                .append(sie1CShbtree.getOverallInsertPS(minutes)).append("\n");

        sb.append("8,").append(",").append(",").append(sie1DRtree.getOverallInsertPS(minutes)).append(",")
                .append(sie1DShbtree.getOverallInsertPS(minutes)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie1_ips.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    /**
     * generate sie1_accumulated_insert_ps.txt
     */
    public void generateAccumulatedInsertPS() throws Exception {
        int targetRound = 721; //(3600 seconds / 5seconds) + 1
        int roundInterval = 5;

        ArrayList<Long> ipsListRtree = new ArrayList<Long>();
        ArrayList<Long> ipsListShbtree = new ArrayList<Long>();
        sie1ARtree.getAllNodesAccumulatedInsertPS(targetRound, ipsListRtree);
        sie1AShbtree.getAllNodesAccumulatedInsertPS(targetRound, ipsListShbtree);

        sb.setLength(0);
        sb.append("# sie1 accumulated inserts per second report\n");
        sb.append("# time, rtree, shbtree\n");

        for (int i = 0; i < targetRound; i++) {
            sb.append("" + (i * roundInterval) + "," + "," + "," + ipsListRtree.get(i) + "," + ipsListShbtree.get(i)
                    + "\n");
        }
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie1_accumulated_insert_ps.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);

        ipsListRtree.clear();
        ipsListShbtree.clear();
    }

    public void generateInstantaneousInsertPS() throws Exception {
        int nodeCount = 8;
        for (int i = 0; i < nodeCount; i++) {
            sb.setLength(0);
            sb.append("# sie1 8nodes(8 dataGen) instantaneous inserts per second report\n");
            sb.append(sie1DRtree.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper
                    .openOutputFile(outputFilePath + "sie1_8nodes_instantaneous_insert_ps_rtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
        for (int i = 0; i < nodeCount; i++) {
            sb.setLength(0);
            sb.append("# sie1 8nodes(8 dataGen) instantaneous inserts per second report\n");
            sb.append(sie1DShbtree.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper
                    .openOutputFile(outputFilePath + "sie1_8nodes_instantaneous_insert_ps_shbtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
    }

    public void generatePrimaryIndexSize() throws Exception {
        sb.setLength(0);
        sb.append("# sie1 primary index size report\n");

        sb.append("# number of nodes, dhbtree, dhvbtree, rtree, shbtree\n");
        sb.append("1,").append(",").append(",").append(sie1ARtree.getIndexSize("Tweets_idx_Tweets/device_id"))
                .append(",").append(sie1AShbtree.getIndexSize("Tweets_idx_Tweets/device_id")).append("\n");
        sb.append("2,").append(",").append(",").append(sie1BRtree.getIndexSize("Tweets_idx_Tweets/device_id"))
                .append(",").append(sie1BShbtree.getIndexSize("Tweets_idx_Tweets/device_id")).append("\n");
        sb.append("4,").append(",").append(",").append(sie1CRtree.getIndexSize("Tweets_idx_Tweets/device_id"))
                .append(",").append(sie1CShbtree.getIndexSize("Tweets_idx_Tweets/device_id")).append("\n");
        sb.append("8,").append(",").append(",").append(sie1DRtree.getIndexSize("Tweets_idx_Tweets/device_id"))
                .append(",").append(sie1DShbtree.getIndexSize("Tweets_idx_Tweets/device_id")).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie1_primary_index_size.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateSecondaryIndexSize() throws Exception {
        sb.setLength(0);
        sb.append("# sie1 secondary index size report\n");

        sb.append("# number of nodes, dhbtree, dhvbtree, rtree, shbtree \n");
        sb.append("1,").append(",").append(",").append(sie1ARtree.getIndexSize("Tweets_idx_rtreeLocation/device_id"))
                .append(",").append(sie1AShbtree.getIndexSize("Tweets_idx_shbtreeLocation/device_id")).append("\n");
        sb.append("2,").append(",").append(",").append(sie1BRtree.getIndexSize("Tweets_idx_rtreeLocation/device_id"))
                .append(",").append(sie1BShbtree.getIndexSize("Tweets_idx_shbtreeLocation/device_id")).append("\n");
        sb.append("4,").append(",").append(",").append(sie1CRtree.getIndexSize("Tweets_idx_rtreeLocation/device_id"))
                .append(",").append(sie1CShbtree.getIndexSize("Tweets_idx_shbtreeLocation/device_id")).append("\n");
        sb.append("8,").append(",").append(",").append(sie1DRtree.getIndexSize("Tweets_idx_rtreeLocation/device_id"))
                .append(",").append(sie1DShbtree.getIndexSize("Tweets_idx_shbtreeLocation/device_id")).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie1_secondary_index_size.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateAverageFlushedComponentSize() throws Exception {
        sb.setLength(0);
        sb.append("# sie1 average flushed component size report\n");
        sb.append(
                "# number of nodes, dhbtree, dhvbtree, rtree, shbtree, dhbtree-pidx, dhvbtree-pidx, rtree-pidx, shbtree-pidx\n");
        sb.append("1,").append(",").append(",").append(",")
                .append(sie1ARtree.getAverageFlushedComponentSize("Tweets_idx_rtreeLocation/device_id")).append(",")
                .append(sie1DShbtree.getAverageFlushedComponentSize("Tweets_idx_shbtreeLocation/device_id")).append(",")
                .append(",").append(",")
                .append(sie1ARtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append(",")
                .append(sie1AShbtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append("\n");
        sb.append("2,").append(",").append(",").append(",")
                .append(sie1BRtree.getAverageFlushedComponentSize("Tweets_idx_rtreeLocation/device_id")).append(",")
                .append(sie1DShbtree.getAverageFlushedComponentSize("Tweets_idx_shbtreeLocation/device_id")).append(",")
                .append(",").append(",")
                .append(sie1BRtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append(",")
                .append(sie1BShbtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append("\n");
        sb.append("4,").append(",").append(",").append(",")
                .append(sie1CRtree.getAverageFlushedComponentSize("Tweets_idx_rtreeLocation/device_id")).append(",")
                .append(sie1DShbtree.getAverageFlushedComponentSize("Tweets_idx_shbtreeLocation/device_id")).append(",")
                .append(",").append(",")
                .append(sie1CRtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append(",")
                .append(sie1CShbtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append("\n");
        sb.append("8,").append(",").append(",").append(",")
                .append(sie1DRtree.getAverageFlushedComponentSize("Tweets_idx_rtreeLocation/device_id")).append(",")
                .append(sie1DShbtree.getAverageFlushedComponentSize("Tweets_idx_shbtreeLocation/device_id")).append(",")
                .append(",").append(",")
                .append(sie1DRtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append(",")
                .append(sie1DShbtree.getAverageFlushedComponentSize("Tweets_idx_Tweets/device_id")).append("\n");

        FileOutputStream fos = ReportBuilderHelper
                .openOutputFile(outputFilePath + "sie1_average_flushed_component_size.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateGanttInstantaneousInsertPS() throws Exception {

        SIE1ReportBuilder rtree = sie1DRtree;
        SIE1ReportBuilder shbtree = sie1DShbtree;
        String sie1Type = "D";
        String logDirPrefix = "";

        for (int i = 0; i < 1; i++) {
            sb.setLength(0);
            sb.append("# sie1 1node(1 dataGen) instantaneous inserts per second report\n");
            sb.append(rtree.getInstantaneousInsertPS(i, true));
            FileOutputStream fos = ReportBuilderHelper
                    .openOutputFile(outputFilePath + "sie1_gantt_1node_instantaneous_insert_ps_rtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
        for (int i = 0; i < 1; i++) {
            sb.setLength(0);
            sb.append("# sie1 1node(1 dataGen) instantaneous inserts per second report\n");
            sb.append(shbtree.getInstantaneousInsertPS(i, true));
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(
                    outputFilePath + "sie1_gantt_1node_instantaneous_insert_ps_shbtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }

        long dataGenStartTime;
        NCLogReportBuilder ncLogReportBuilder;
        FileOutputStream fos;

        dataGenStartTime = rtree.getDataGenStartTimeStamp();
        ncLogReportBuilder = new NCLogReportBuilder(
                expHomePath + "SpatialIndexExperiment1" + sie1Type + "Rtree/" + logDirPrefix + "logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie1_gantt_1node_flush_merge_rtree.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);

        dataGenStartTime = shbtree.getDataGenStartTimeStamp();
        ncLogReportBuilder = new NCLogReportBuilder(
                expHomePath + "SpatialIndexExperiment1" + sie1Type + "Shbtree/" + logDirPrefix + "logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        fos = ReportBuilderHelper.openOutputFile(outputFilePath + "sie1_gantt_1node_flush_merge_shbtree.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

}
