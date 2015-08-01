package edu.uci.ics.asterix.experiment.report;

import java.io.FileOutputStream;

public class SIE2ReportBuilderRunner {
    String filePath = "/Users/kisskys/workspace/asterix_experiment/run-log/result-report/";
    String runLogFilePath = "/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon-dgenbatchsize1/log-1436942229777/run.log";

    SIE2ReportBuilder sie2Dhbtree = new SIE2ReportBuilder("SpatialIndexExperiment2Dhbtree", runLogFilePath);
    SIE2ReportBuilder sie2Dhvbtree = new SIE2ReportBuilder("SpatialIndexExperiment2Dhvbtree", runLogFilePath);
    SIE2ReportBuilder sie2Rtree = new SIE2ReportBuilder("SpatialIndexExperiment2Rtree", runLogFilePath);
    SIE2ReportBuilder sie2Shbtree = new SIE2ReportBuilder("SpatialIndexExperiment2Shbtree", runLogFilePath);
    SIE2ReportBuilder sie2Sif = new SIE2ReportBuilder("SpatialIndexExperiment2Sif", runLogFilePath);

    StringBuilder sb = new StringBuilder();

    /**
     * generate sie2_20min_insert_ps.txt
     */
    public void generate20MinInsertPS() throws Exception {
        sb.setLength(0);
        sb.append("# sie2 20min inserts per second report\n");
        sb.append("index type, InsertPS\n");
        sb.append("dhbtree,").append(sie2Dhbtree.get20minInsertPS()).append("\n");
        sb.append("dhvbtree,").append(sie2Dhvbtree.get20minInsertPS()).append("\n");
        sb.append("rtree,").append(sie2Rtree.get20minInsertPS()).append("\n");
        sb.append("shbtree,").append(sie2Shbtree.get20minInsertPS()).append("\n");
        sb.append("sif,").append(sie2Sif.get20minInsertPS()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_20min_insert_ps.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateFirst20MinInsertPS() throws Exception {

        int dGenCount = 8;
        long dhbtreeIPS = 0, dhvbtreeIPS = 0, rtreeIPS = 0, shbtreeIPS = 0, sifIPS = 0;

        for (int i = 0; i < dGenCount; i++) {
            dhbtreeIPS += sie2Dhbtree.getFirstXminInsertPS(20, i);
            dhvbtreeIPS += sie2Dhvbtree.getFirstXminInsertPS(20, i);
            rtreeIPS += sie2Rtree.getFirstXminInsertPS(20, i);
            shbtreeIPS += sie2Shbtree.getFirstXminInsertPS(20, i);
            sifIPS += sie2Sif.getFirstXminInsertPS(20, i);
        }

        sb.setLength(0);
        sb.append("# sie2 first 20min inserts per second report\n");
        sb.append("index type, InsertPS\n");
        sb.append("dhbtree,").append(dhbtreeIPS).append("\n");
        sb.append("dhvbtree,").append(dhvbtreeIPS).append("\n");
        sb.append("rtree,").append(rtreeIPS).append("\n");
        sb.append("shbtree,").append(shbtreeIPS).append("\n");
        sb.append("sif,").append(sifIPS).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_first_20min_insert_ps.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateEvery20MinInsertPS() throws Exception {

        int dGenCount = 8;
        long dhbtreeIPS = 0, dhvbtreeIPS = 0, rtreeIPS = 0, shbtreeIPS = 0, sifIPS = 0;
        boolean getDhbtree = true, getDhvbtree = true, getRtree = true, getShbtree = true, getSif = true;

        sb.setLength(0);
        sb.append("# sie2 every 20min inserts per second report\n");
        sb.append("# time, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        
        for (int j = 1; j < 27; j++) {
            for (int i = 0; i < dGenCount; i++) {
                if (getDhbtree)
                    dhbtreeIPS += sie2Dhbtree.getFirstXminInsertPS(j*20, i);
                if (getDhvbtree)
                    dhvbtreeIPS += sie2Dhvbtree.getFirstXminInsertPS(j*20, i);
                if (getRtree)
                    rtreeIPS += sie2Rtree.getFirstXminInsertPS(j*20, i);
                if (getShbtree)
                    shbtreeIPS += sie2Shbtree.getFirstXminInsertPS(j*20, i);
                if (getSif)
                    sifIPS += sie2Sif.getFirstXminInsertPS(j*20, i);
            }
            if (dhbtreeIPS == 0) 
                getDhbtree = false;
            if (dhvbtreeIPS == 0) 
                getDhvbtree = false;
            if (rtreeIPS == 0) 
                getRtree = false;
            if (shbtreeIPS == 0) 
                getShbtree = false;
            if (sifIPS == 0) 
                getSif = false;

            sb.append(j*20).append(",").
            append(dhbtreeIPS == 0 ? "" : dhbtreeIPS).append(",").
            append(dhvbtreeIPS == 0 ? "" : dhvbtreeIPS).append(",").
            append(rtreeIPS == 0 ? "" : rtreeIPS).append(",").
            append(shbtreeIPS == 0 ? "" : shbtreeIPS).append(",").
            append(sifIPS == 0 ? "" : sifIPS).append("\n");
            
            dhbtreeIPS = 0;
            dhvbtreeIPS = 0;
            rtreeIPS = 0;
            shbtreeIPS = 0;
            sifIPS = 0;
            
        }

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_every_20min_insert_ps.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generate20MinQueryPS() throws Exception {
        sb.setLength(0);
        sb.append("# sie2 20min queries per second report\n");
        sb.append("index type, QueryPS\n");
        sb.append("dhbtree,").append(sie2Dhbtree.get20minQueryPS()).append("\n");
        sb.append("dhvbtree,").append(sie2Dhvbtree.get20minQueryPS()).append("\n");
        sb.append("rtree,").append(sie2Rtree.get20minQueryPS()).append("\n");
        sb.append("shbtree,").append(sie2Shbtree.get20minQueryPS()).append("\n");
        sb.append("sif,").append(sie2Sif.get20minQueryPS()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_20min_query_ps.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generate20MinAverageQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie2 20min query result count report\n");
        sb.append("index type, query result count\n");
        sb.append("dhbtree,").append(sie2Dhbtree.get20minAverageQueryResultCount()).append("\n");
        sb.append("dhvbtree,").append(sie2Dhvbtree.get20minAverageQueryResultCount()).append("\n");
        sb.append("rtree,").append(sie2Rtree.get20minAverageQueryResultCount()).append("\n");
        sb.append("shbtree,").append(sie2Shbtree.get20minAverageQueryResultCount()).append("\n");
        sb.append("sif,").append(sie2Sif.get20minAverageQueryResultCount()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_20min_average_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generate20MinAverageQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie2 20min query response time report\n");
        sb.append("index type, query response time\n");
        sb.append("dhbtree,").append(sie2Dhbtree.get20minAverageQueryResponseTime()).append("\n");
        sb.append("dhvbtree,").append(sie2Dhvbtree.get20minAverageQueryResponseTime()).append("\n");
        sb.append("rtree,").append(sie2Rtree.get20minAverageQueryResponseTime()).append("\n");
        sb.append("shbtree,").append(sie2Shbtree.get20minAverageQueryResponseTime()).append("\n");
        sb.append("sif,").append(sie2Sif.get20minAverageQueryResponseTime()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_20min_average_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

    public void generateInstantaneousInsertPS() throws Exception {
        for (int i = 0; i < 8; i++) {
            sb.setLength(0);
            sb.append("# sie2 instantaneous inserts per second report\n");
            sb.append(sie2Dhbtree.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_instantaneous_insert_ps_dhbtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
        for (int i = 0; i < 8; i++) {
            sb.setLength(0);
            sb.append("# sie2 instantaneous inserts per second report\n");
            sb.append(sie2Dhvbtree.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_instantaneous_insert_ps_dhvbtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
        for (int i = 0; i < 8; i++) {
            sb.setLength(0);
            sb.append("# sie2 instantaneous inserts per second report\n");
            sb.append(sie2Rtree.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_instantaneous_insert_ps_rtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
        for (int i = 0; i < 8; i++) {
            sb.setLength(0);
            sb.append("# sie2 instantaneous inserts per second report\n");
            sb.append(sie2Shbtree.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_instantaneous_insert_ps_shbtree_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
        for (int i = 0; i < 8; i++) {
            sb.setLength(0);
            sb.append("# sie2 instantaneous inserts per second report\n");
            sb.append(sie2Sif.getInstantaneousInsertPS(i, false));
            FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_instantaneous_insert_ps_sif_gen" + i + ".txt");
            fos.write(sb.toString().getBytes());
            ReportBuilderHelper.closeOutputFile(fos);
        }
    }
    
   public void generateGanttInstantaneousInsertPS() throws Exception {
       for (int i = 0; i < 1; i++) {
           sb.setLength(0);
           sb.append("# sie2 8nodes(8 dataGen) instantaneous inserts per second report\n");
           sb.append(sie2Dhbtree.getInstantaneousInsertPS(i, true));
           FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_instantaneous_insert_ps_dhbtree_gen"+i+".txt");
           fos.write(sb.toString().getBytes());
           ReportBuilderHelper.closeOutputFile(fos);
       }
       for (int i = 0; i < 1; i++) {
           sb.setLength(0);
           sb.append("# sie2 8nodes(8 dataGen) instantaneous inserts per second report\n");
           sb.append(sie2Dhvbtree.getInstantaneousInsertPS(i, true));
           FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_instantaneous_insert_ps_dhvbtree_gen"+i+".txt");
           fos.write(sb.toString().getBytes());
           ReportBuilderHelper.closeOutputFile(fos);
       }
       for (int i = 0; i < 1; i++) {
           sb.setLength(0);
           sb.append("# sie2 8nodes(8 dataGen) instantaneous inserts per second report\n");
           sb.append(sie2Rtree.getInstantaneousInsertPS(i, true));
           FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_instantaneous_insert_ps_rtree_gen"+i+".txt");
           fos.write(sb.toString().getBytes());
           ReportBuilderHelper.closeOutputFile(fos);
       }
       for (int i = 0; i < 1; i++) {
           sb.setLength(0);
           sb.append("# sie2 8nodes(8 dataGen) instantaneous inserts per second report\n");
           sb.append(sie2Shbtree.getInstantaneousInsertPS(i, true));
           FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_instantaneous_insert_ps_shbtree_gen"+i+".txt");
           fos.write(sb.toString().getBytes());
           ReportBuilderHelper.closeOutputFile(fos);
       }
       for (int i = 0; i < 1; i++) {
           sb.setLength(0);
           sb.append("# sie2 8nodes(8 dataGen) instantaneous inserts per second report\n");
           sb.append(sie2Sif.getInstantaneousInsertPS(i, true));
           FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_instantaneous_insert_ps_sif_gen"+i+".txt");
           fos.write(sb.toString().getBytes());
           ReportBuilderHelper.closeOutputFile(fos);
       }
        long dataGenStartTime = sie2Dhbtree.getDataGenStartTimeStamp();
        NCLogReportBuilder ncLogReportBuilder = new NCLogReportBuilder("/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon-dgenbatchsize1/log-1436942229777/SpatialIndexExperiment2Dhbtree/node1/logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_flush_merge_dhbtree.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
        
        dataGenStartTime = sie2Dhvbtree.getDataGenStartTimeStamp();
        ncLogReportBuilder = new NCLogReportBuilder("/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon-dgenbatchsize1/log-1436942229777/SpatialIndexExperiment2Dhvbtree/node1/logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_flush_merge_dhvbtree.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
        
        dataGenStartTime = sie2Rtree.getDataGenStartTimeStamp();
        ncLogReportBuilder = new NCLogReportBuilder("/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon-dgenbatchsize1/log-1436942229777/SpatialIndexExperiment2Rtree/node1/logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_flush_merge_rtree.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
        
        dataGenStartTime = sie2Shbtree.getDataGenStartTimeStamp();
        ncLogReportBuilder = new NCLogReportBuilder("/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon-dgenbatchsize1/log-1436942229777/SpatialIndexExperiment2Shbtree/node1/logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_flush_merge_shbtree.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
        
        dataGenStartTime = sie2Sif.getDataGenStartTimeStamp();
        ncLogReportBuilder = new NCLogReportBuilder("/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon-dgenbatchsize1/log-1436942229777/SpatialIndexExperiment2Sif/node1/logs/a1_node1.log");
        sb.setLength(0);
        sb.append(ncLogReportBuilder.getFlushMergeEventAsGanttChartFormat(dataGenStartTime));
        fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_gantt_1node_flush_merge_sif.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }
   public void generateSelectQueryResponseTime() throws Exception {
       sb.setLength(0);
       sb.append("# sie2 select query response time 1 report\n");
       
       sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
       sb.append("0.00001,").append(sie2Dhbtree.getSelectQueryResponseTime(0)).append(",").append(sie2Dhvbtree.getSelectQueryResponseTime(0))
               .append(",").append(sie2Rtree.getSelectQueryResponseTime(0)).append(",").append(sie2Shbtree.getSelectQueryResponseTime(0))
               .append(",").append(sie2Sif.getSelectQueryResponseTime(0)).append("\n");
       sb.append("0.0001,").append(sie2Dhbtree.getSelectQueryResponseTime(1)).append(",").append(sie2Dhvbtree.getSelectQueryResponseTime(1))
       .append(",").append(sie2Rtree.getSelectQueryResponseTime(1)).append(",").append(sie2Shbtree.getSelectQueryResponseTime(1))
       .append(",").append(sie2Sif.getSelectQueryResponseTime(1)).append("\n");
       sb.append("0.001,").append(sie2Dhbtree.getSelectQueryResponseTime(2)).append(",").append(sie2Dhvbtree.getSelectQueryResponseTime(2))
       .append(",").append(sie2Rtree.getSelectQueryResponseTime(2)).append(",").append(sie2Shbtree.getSelectQueryResponseTime(2))
       .append(",").append(sie2Sif.getSelectQueryResponseTime(2)).append("\n");
       
       FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_select_query_response_time1.txt");
       fos.write(sb.toString().getBytes());
       ReportBuilderHelper.closeOutputFile(fos);
       
       sb.setLength(0);
       sb.append("# sie2 select query response time 2 report\n");
       
       sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
       sb.append("0.01,").append(sie2Dhbtree.getSelectQueryResponseTime(3)).append(",").append(sie2Dhvbtree.getSelectQueryResponseTime(3))
       .append(",").append(sie2Rtree.getSelectQueryResponseTime(3)).append(",").append(sie2Shbtree.getSelectQueryResponseTime(3))
       .append(",").append(sie2Sif.getSelectQueryResponseTime(3)).append("\n");
       sb.append("0.1,").append(sie2Dhbtree.getSelectQueryResponseTime(4)).append(",").append(sie2Dhvbtree.getSelectQueryResponseTime(4))
       .append(",").append(sie2Rtree.getSelectQueryResponseTime(4)).append(",").append(sie2Shbtree.getSelectQueryResponseTime(4))
       .append(",").append(sie2Sif.getSelectQueryResponseTime(4)).append("\n");
       
       fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_select_query_response_time2.txt");
       fos.write(sb.toString().getBytes());
       ReportBuilderHelper.closeOutputFile(fos);
   }
   
   public void generateSelectQueryResultCount() throws Exception {
       sb.setLength(0);
       sb.append("# sie2 select query result count 1 report\n");
       
       sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
       sb.append("0.00001,").append(sie2Dhbtree.getSelectQueryResultCount(0)).append(",").append(sie2Dhvbtree.getSelectQueryResultCount(0))
               .append(",").append(sie2Rtree.getSelectQueryResultCount(0)).append(",").append(sie2Shbtree.getSelectQueryResultCount(0))
               .append(",").append(sie2Sif.getSelectQueryResultCount(0)).append("\n");
       sb.append("0.0001,").append(sie2Dhbtree.getSelectQueryResultCount(1)).append(",").append(sie2Dhvbtree.getSelectQueryResultCount(1))
       .append(",").append(sie2Rtree.getSelectQueryResultCount(1)).append(",").append(sie2Shbtree.getSelectQueryResultCount(1))
       .append(",").append(sie2Sif.getSelectQueryResultCount(1)).append("\n");
       sb.append("0.001,").append(sie2Dhbtree.getSelectQueryResultCount(2)).append(",").append(sie2Dhvbtree.getSelectQueryResultCount(2))
       .append(",").append(sie2Rtree.getSelectQueryResultCount(2)).append(",").append(sie2Shbtree.getSelectQueryResultCount(2))
       .append(",").append(sie2Sif.getSelectQueryResultCount(2)).append("\n");
       
       FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_select_query_result_count1.txt");
       fos.write(sb.toString().getBytes());
       ReportBuilderHelper.closeOutputFile(fos);
       
       sb.setLength(0);
       sb.append("# sie2 select query result count 2 report\n");
       
       sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
       sb.append("0.01,").append(sie2Dhbtree.getSelectQueryResultCount(3)).append(",").append(sie2Dhvbtree.getSelectQueryResultCount(3))
       .append(",").append(sie2Rtree.getSelectQueryResultCount(3)).append(",").append(sie2Shbtree.getSelectQueryResultCount(3))
       .append(",").append(sie2Sif.getSelectQueryResultCount(3)).append("\n");
       sb.append("0.1,").append(sie2Dhbtree.getSelectQueryResultCount(4)).append(",").append(sie2Dhvbtree.getSelectQueryResultCount(4))
       .append(",").append(sie2Rtree.getSelectQueryResultCount(4)).append(",").append(sie2Shbtree.getSelectQueryResultCount(4))
       .append(",").append(sie2Sif.getSelectQueryResultCount(4)).append("\n");
       
       fos = ReportBuilderHelper.openOutputFile(filePath + "sie2_select_query_result_count2.txt");
       fos.write(sb.toString().getBytes());
       ReportBuilderHelper.closeOutputFile(fos);
   }
}
