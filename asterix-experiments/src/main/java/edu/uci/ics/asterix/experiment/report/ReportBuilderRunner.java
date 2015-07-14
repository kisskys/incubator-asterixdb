package edu.uci.ics.asterix.experiment.report;

public class ReportBuilderRunner {

    public static void main(String[] args) throws Exception {
        SIE1ReportBuilderRunner sie1 = new SIE1ReportBuilderRunner();
        sie1.generateSIE1IPS();
        sie1.generateInstantaneousInsertPS();
        sie1.generateIndexSize();
        sie1.generateGanttInstantaneousInsertPS();
        
        SIE2ReportBuilderRunner sie2 = new SIE2ReportBuilderRunner();
        sie2.generate20MinInsertPS();
        sie2.generateFirst20MinInsertPS();
        sie2.generateEvery20MinInsertPS();
        sie2.generate20MinQueryPS();
        sie2.generate20MinAverageQueryResultCount();
        sie2.generate20MinAverageQueryResponseTime();
        sie2.generateInstantaneousInsertPS();
        
        SIE3ReportBuilderRunner sie3 = new SIE3ReportBuilderRunner();
        sie3.generateIndexCreationTime();
        sie3.generateIndexSize();
        sie3.generateSelectQueryResponseTime();
        sie3.generateJoinQueryResponseTime();
        sie3.generateSelectQueryResultCount();
        sie3.generateJoinQueryResultCount();
    }

}
