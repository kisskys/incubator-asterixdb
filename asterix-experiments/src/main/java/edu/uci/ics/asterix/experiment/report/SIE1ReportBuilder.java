/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
