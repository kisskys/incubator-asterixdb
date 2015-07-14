package edu.uci.ics.asterix.experiment.report;

import java.io.FileOutputStream;

public class SIE2ReportBuilderRunner {
    String filePath = "/Users/kisskys/workspace/asterix_experiment/run-log/result-report/";
    String runLogFilePath = "/Users/kisskys/workspace/asterix_experiment/run-log/measure-with-balloon/log-1435991419079/run.log";

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

}
