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

public class ReportBuilderRunner {
    public static final boolean REPORT_SIE1 = false;
    public static final boolean REPORT_SIE2 = false;
    public static final boolean REPORT_SIE3 = false;
    public static final boolean REPORT_SIE4 = false;
    public static final boolean REPORT_SIE5 = false;
    public static final boolean REPORT_SIE1_RECT = false;
    public static final boolean REPORT_SIE2_RECT = false;
    public static final boolean REPORT_SIE3_RECT = true;
    public static final boolean REPORT_SIE4_RECT = false;
    public static final boolean REPORT_SIE5_RECT = false;

    public static void main(String[] args) throws Exception {

        if (REPORT_SIE1) {
            SIE1ReportBuilderRunner sie1 = new SIE1ReportBuilderRunner();
            sie1.generateSIE1IPS();
            sie1.generateInstantaneousInsertPS();
            sie1.generatePrimaryIndexSize();
            sie1.generateSecondaryIndexSize();
            sie1.generateGanttInstantaneousInsertPS();
            sie1.generateAccumulatedInsertPS();
            sie1.generateAverageFlushedComponentSize();
        }

        if (REPORT_SIE1_RECT) {
            SIE1RectReportBuilderRunner sie1 = new SIE1RectReportBuilderRunner();
            sie1.generateSIE1IPS();
            sie1.generateInstantaneousInsertPS();
            sie1.generatePrimaryIndexSize();
            sie1.generateSecondaryIndexSize();
            sie1.generateGanttInstantaneousInsertPS();
            sie1.generateAccumulatedInsertPS();
            sie1.generateAverageFlushedComponentSize();
        }

        if (REPORT_SIE2) {
            SIE2ReportBuilderRunner sie2 = new SIE2ReportBuilderRunner();
            sie2.generateOverallInsertPS();
            sie2.generateAccumulatedInsertPS();
            sie2.generateQueryPS();
            sie2.generateAverageQueryResultCount();
            sie2.generateAverageQueryResponseTime();
            sie2.generateInstantaneousInsertPS();
            sie2.generateGanttInstantaneousInsertPS();
            sie2.generateSelectQueryResponseTime();
            sie2.generateSelectQueryResultCount();
        }

        if (REPORT_SIE2_RECT) {
            SIE2RectReportBuilderRunner sie2 = new SIE2RectReportBuilderRunner();
            sie2.generateOverallInsertPS();
            sie2.generateAccumulatedInsertPS();
            sie2.generateQueryPS();
            sie2.generateAverageQueryResultCount();
            sie2.generateAverageQueryResponseTime();
            sie2.generateInstantaneousInsertPS();
            sie2.generateGanttInstantaneousInsertPS();
            sie2.generateSelectQueryResponseTime();
            sie2.generateSelectQueryResultCount();
        }

        if (REPORT_SIE3) {
            SIE3ReportBuilderRunner sie3 = new SIE3ReportBuilderRunner();
            sie3.generateIndexCreationTime();
            sie3.generateIndexSize();
            sie3.generateSelectQueryResponseTime();
            sie3.generateJoinQueryResponseTime();
            sie3.generateSelectQueryResultCount();
            sie3.generateJoinQueryResultCount();

            // profile info
            //            sie3.generateQueryProfiledOperatorTime();
            //            sie3.generateQueryProfiledCacheMiss();
            //            sie3.generateQueryProfiledFalsePositive();
            //            sie3.generateIndexBuildProfiledCacheMiss();
            //            sie3.generateIndexBuildProfiledOperatorTime();

            //            sie3.generateSelectQueryProfiledSidxSearchTime();
            //            sie3.generateSelectQueryProfiledPidxSearchTime();
            //            sie3.generateJoinQueryProfiledSidxSearchTime();
            //            sie3.generateJoinQueryProfiledPidxSearchTime();
            //            sie3.generateJoinQueryProfiledSeedPidxSearchTime();
            //            sie3.generateSelectQueryProfiledSidxCacheMiss();
            //            sie3.generateSelectQueryProfiledPidxCacheMiss();
            //            sie3.generateJoinQueryProfiledSidxCacheMiss();
            //            sie3.generateJoinQueryProfiledPidxCacheMiss();
            //            sie3.generateJoinQueryProfiledSeedPidxCacheMiss();
            //            sie3.generateSelectQueryProfiledFalsePositive();
            //            sie3.generateJoinQueryProfiledFalsePositive();
        }

        if (REPORT_SIE3_RECT) {
            SIE3RectReportBuilderRunner sie3 = new SIE3RectReportBuilderRunner();
            sie3.generateIndexCreationTime();
            sie3.generateIndexSize();
            sie3.generateSelectQueryResponseTime();
            sie3.generateJoinQueryResponseTime();
            sie3.generateSelectQueryResultCount();
            sie3.generateJoinQueryResultCount();

            // profile info
            //            sie3.generateQueryProfiledOperatorTime();
            //            sie3.generateQueryProfiledCacheMiss();
            //            sie3.generateQueryProfiledFalsePositive();
            //            sie3.generateIndexBuildProfiledCacheMiss();
            //            sie3.generateIndexBuildProfiledOperatorTime();

            //            sie3.generateSelectQueryProfiledSidxSearchTime();
            //            sie3.generateSelectQueryProfiledPidxSearchTime();
            //            sie3.generateJoinQueryProfiledSidxSearchTime();
            //            sie3.generateJoinQueryProfiledPidxSearchTime();
            //            sie3.generateJoinQueryProfiledSeedPidxSearchTime();
            //            sie3.generateSelectQueryProfiledSidxCacheMiss();
            //            sie3.generateSelectQueryProfiledPidxCacheMiss();
            //            sie3.generateJoinQueryProfiledSidxCacheMiss();
            //            sie3.generateJoinQueryProfiledPidxCacheMiss();
            //            sie3.generateJoinQueryProfiledSeedPidxCacheMiss();
            //            sie3.generateSelectQueryProfiledFalsePositive();
            //            sie3.generateJoinQueryProfiledFalsePositive();
        }

        if (REPORT_SIE4) {
            SIE4ReportBuilderRunner sie4 = new SIE4ReportBuilderRunner();
            sie4.generateIndexCreationTime();
            sie4.generateIndexSize();
            sie4.generateSelectQueryResponseTime();
            sie4.generateJoinQueryResponseTime();
            sie4.generateSelectQueryResultCount();
            sie4.generateJoinQueryResultCount();

            //profile info
            //            sie4.generateQueryProfiledOperatorTime();
            //            sie4.generateQueryProfiledCacheMiss();

        }

        if (REPORT_SIE4_RECT) {
            SIE4RectReportBuilderRunner sie4 = new SIE4RectReportBuilderRunner();
            sie4.generateIndexCreationTime();
            sie4.generateIndexSize();
            sie4.generateSelectQueryResponseTime();
            sie4.generateJoinQueryResponseTime();
            sie4.generateSelectQueryResultCount();
            sie4.generateJoinQueryResultCount();

            //profile info
            //            sie4.generateQueryProfiledOperatorTime();
            //            sie4.generateQueryProfiledCacheMiss();

        }

        if (REPORT_SIE5) {
            SIE5ReportBuilderRunner sie5 = new SIE5ReportBuilderRunner();
            sie5.generateOverallInsertPS();
            sie5.generateAccumulatedInsertPS();
            sie5.generateQueryPS();
            sie5.generateAverageQueryResultCount();
            sie5.generateAverageQueryResponseTime();
            sie5.generateInstantaneousInsertPS();
            sie5.generateGanttInstantaneousInsertPS();
            sie5.generateSelectQueryResponseTime();
            sie5.generateSelectQueryResultCount();
        }

    }
}
