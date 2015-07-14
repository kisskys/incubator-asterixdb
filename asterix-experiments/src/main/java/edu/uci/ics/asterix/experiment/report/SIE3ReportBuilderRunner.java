package edu.uci.ics.asterix.experiment.report;

import java.io.FileOutputStream;

public class SIE3ReportBuilderRunner {
    String filePath = "/Users/kisskys/workspace/asterix_experiment/run-log/result-report/";
    String runLogFilePath = "/Users/kisskys/workspace/asterix_experiment/run-log/8dgen-with-balloon/log-1436596748752/run.log";

    SIE3ReportBuilder sie3Dhbtree = new SIE3ReportBuilder("SpatialIndexExperiment3Dhbtree", runLogFilePath);
    SIE3ReportBuilder sie3Dhvbtree = new SIE3ReportBuilder("SpatialIndexExperiment3Dhvbtree", runLogFilePath);
    SIE3ReportBuilder sie3Rtree = new SIE3ReportBuilder("SpatialIndexExperiment3Rtree", runLogFilePath);
    SIE3ReportBuilder sie3Shbtree = new SIE3ReportBuilder("SpatialIndexExperiment3Shbtree", runLogFilePath);
    SIE3ReportBuilder sie3Sif = new SIE3ReportBuilder("SpatialIndexExperiment3Sif", runLogFilePath);

    StringBuilder sb = new StringBuilder();

    public void generateIndexCreationTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 index creation time report\n");
        sb.append("index type, index creation time\n");
        sb.append("dhbtree,").append(sie3Dhbtree.getIndexCreationTime()).append("\n");
        sb.append("dhvbtree,").append(sie3Dhvbtree.getIndexCreationTime()).append("\n");
        sb.append("rtree,").append(sie3Rtree.getIndexCreationTime()).append("\n");
        sb.append("shbtree,").append(sie3Shbtree.getIndexCreationTime()).append("\n");
        sb.append("sif,").append(sie3Sif.getIndexCreationTime()).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie3_index_creation_time.txt");
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
        
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie3_sidx_size.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }
    
    public void generateSelectQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query response time report\n");
        
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getSelectQueryResponseTime(0)).append(",").append(sie3Dhvbtree.getSelectQueryResponseTime(0))
                .append(",").append(sie3Rtree.getSelectQueryResponseTime(0)).append(",").append(sie3Shbtree.getSelectQueryResponseTime(0))
                .append(",").append(sie3Sif.getSelectQueryResponseTime(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getSelectQueryResponseTime(1)).append(",").append(sie3Dhvbtree.getSelectQueryResponseTime(1))
        .append(",").append(sie3Rtree.getSelectQueryResponseTime(1)).append(",").append(sie3Shbtree.getSelectQueryResponseTime(1))
        .append(",").append(sie3Sif.getSelectQueryResponseTime(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getSelectQueryResponseTime(2)).append(",").append(sie3Dhvbtree.getSelectQueryResponseTime(2))
        .append(",").append(sie3Rtree.getSelectQueryResponseTime(2)).append(",").append(sie3Shbtree.getSelectQueryResponseTime(2))
        .append(",").append(sie3Sif.getSelectQueryResponseTime(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getSelectQueryResponseTime(3)).append(",").append(sie3Dhvbtree.getSelectQueryResponseTime(3))
        .append(",").append(sie3Rtree.getSelectQueryResponseTime(3)).append(",").append(sie3Shbtree.getSelectQueryResponseTime(3))
        .append(",").append(sie3Sif.getSelectQueryResponseTime(3)).append("\n");
        sb.append("0.1,").append(sie3Dhbtree.getSelectQueryResponseTime(4)).append(",").append(sie3Dhvbtree.getSelectQueryResponseTime(4))
        .append(",").append(sie3Rtree.getSelectQueryResponseTime(4)).append(",").append(sie3Shbtree.getSelectQueryResponseTime(4))
        .append(",").append(sie3Sif.getSelectQueryResponseTime(4)).append("\n");
        
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie3_select_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }
    
    public void generateSelectQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 select query result count report\n");
        
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getSelectQueryResultCount(0)).append(",").append(sie3Dhvbtree.getSelectQueryResultCount(0))
                .append(",").append(sie3Rtree.getSelectQueryResultCount(0)).append(",").append(sie3Shbtree.getSelectQueryResultCount(0))
                .append(",").append(sie3Sif.getSelectQueryResultCount(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getSelectQueryResultCount(1)).append(",").append(sie3Dhvbtree.getSelectQueryResultCount(1))
        .append(",").append(sie3Rtree.getSelectQueryResultCount(1)).append(",").append(sie3Shbtree.getSelectQueryResultCount(1))
        .append(",").append(sie3Sif.getSelectQueryResultCount(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getSelectQueryResultCount(2)).append(",").append(sie3Dhvbtree.getSelectQueryResultCount(2))
        .append(",").append(sie3Rtree.getSelectQueryResultCount(2)).append(",").append(sie3Shbtree.getSelectQueryResultCount(2))
        .append(",").append(sie3Sif.getSelectQueryResultCount(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getSelectQueryResultCount(3)).append(",").append(sie3Dhvbtree.getSelectQueryResultCount(3))
        .append(",").append(sie3Rtree.getSelectQueryResultCount(3)).append(",").append(sie3Shbtree.getSelectQueryResultCount(3))
        .append(",").append(sie3Sif.getSelectQueryResultCount(3)).append("\n");
        sb.append("0.1,").append(sie3Dhbtree.getSelectQueryResultCount(4)).append(",").append(sie3Dhvbtree.getSelectQueryResultCount(4))
        .append(",").append(sie3Rtree.getSelectQueryResultCount(4)).append(",").append(sie3Shbtree.getSelectQueryResultCount(4))
        .append(",").append(sie3Sif.getSelectQueryResultCount(4)).append("\n");
        
        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie3_select_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }
    
    public void generateJoinQueryResponseTime() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query response time report\n");
        
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getJoinQueryResponseTime(0)).append(",").append(sie3Dhvbtree.getJoinQueryResponseTime(0))
                .append(",").append(sie3Rtree.getJoinQueryResponseTime(0)).append(",").append(sie3Shbtree.getJoinQueryResponseTime(0))
                .append(",").append(sie3Sif.getJoinQueryResponseTime(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getJoinQueryResponseTime(1)).append(",").append(sie3Dhvbtree.getJoinQueryResponseTime(1))
        .append(",").append(sie3Rtree.getJoinQueryResponseTime(1)).append(",").append(sie3Shbtree.getJoinQueryResponseTime(1))
        .append(",").append(sie3Sif.getJoinQueryResponseTime(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getJoinQueryResponseTime(2)).append(",").append(sie3Dhvbtree.getJoinQueryResponseTime(2))
        .append(",").append(sie3Rtree.getJoinQueryResponseTime(2)).append(",").append(sie3Shbtree.getJoinQueryResponseTime(2))
        .append(",").append(sie3Sif.getJoinQueryResponseTime(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getJoinQueryResponseTime(3)).append(",").append(sie3Dhvbtree.getJoinQueryResponseTime(3))
        .append(",").append(sie3Rtree.getJoinQueryResponseTime(3)).append(",").append(sie3Shbtree.getJoinQueryResponseTime(3))
        .append(",").append(sie3Sif.getJoinQueryResponseTime(3)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie3_join_query_response_time.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }
    
    public void generateJoinQueryResultCount() throws Exception {
        sb.setLength(0);
        sb.append("# sie3 join query result count report\n");
        
        sb.append("radius, dhbtree, dhvbtree, rtree, shbtree, sif\n");
        sb.append("0.00001,").append(sie3Dhbtree.getJoinQueryResultCount(0)).append(",").append(sie3Dhvbtree.getJoinQueryResultCount(0))
                .append(",").append(sie3Rtree.getJoinQueryResultCount(0)).append(",").append(sie3Shbtree.getJoinQueryResultCount(0))
                .append(",").append(sie3Sif.getJoinQueryResultCount(0)).append("\n");
        sb.append("0.0001,").append(sie3Dhbtree.getJoinQueryResultCount(1)).append(",").append(sie3Dhvbtree.getJoinQueryResultCount(1))
        .append(",").append(sie3Rtree.getJoinQueryResultCount(1)).append(",").append(sie3Shbtree.getJoinQueryResultCount(1))
        .append(",").append(sie3Sif.getJoinQueryResultCount(1)).append("\n");
        sb.append("0.001,").append(sie3Dhbtree.getJoinQueryResultCount(2)).append(",").append(sie3Dhvbtree.getJoinQueryResultCount(2))
        .append(",").append(sie3Rtree.getJoinQueryResultCount(2)).append(",").append(sie3Shbtree.getJoinQueryResultCount(2))
        .append(",").append(sie3Sif.getJoinQueryResultCount(2)).append("\n");
        sb.append("0.01,").append(sie3Dhbtree.getJoinQueryResultCount(3)).append(",").append(sie3Dhvbtree.getJoinQueryResultCount(3))
        .append(",").append(sie3Rtree.getJoinQueryResultCount(3)).append(",").append(sie3Shbtree.getJoinQueryResultCount(3))
        .append(",").append(sie3Sif.getJoinQueryResultCount(3)).append("\n");

        FileOutputStream fos = ReportBuilderHelper.openOutputFile(filePath + "sie3_join_query_result_count.txt");
        fos.write(sb.toString().getBytes());
        ReportBuilderHelper.closeOutputFile(fos);
    }

}
