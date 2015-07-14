package edu.uci.ics.asterix.experiment.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import edu.uci.ics.asterix.tools.external.data.DataGenerator;
import edu.uci.ics.asterix.tools.external.data.DataGenerator.InitializationInfo;
import edu.uci.ics.asterix.tools.external.data.DataGenerator.TweetMessageIterator;
import edu.uci.ics.asterix.tools.external.data.GULongIDGenerator;

public class SyntheticDataGeneratorForSpatialIndexEvaluation {
    public static void main(String[] args) throws Exception {
        SyntheticDataGeneratorConfig config = new SyntheticDataGeneratorConfig();
        CmdLineParser clp = new CmdLineParser(config);
        try {
            clp.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            clp.printUsage(System.err);
            System.exit(1);
        }

        //prepare data generator
        GULongIDGenerator uidGenerator = new GULongIDGenerator(config.getPartitionId(), (byte) (0));
        String pointSourceFilePath = config.getPointSourceFile();
        int pointSampleInterval = config.getpointSamplingInterval();
        DataGenerator dataGenerator = new DataGenerator(new InitializationInfo(), pointSourceFilePath,
                pointSampleInterval);

        //get record count to be generated
        long maxRecordCount = config.getRecordCount();
        long recordCount = 0;

        //prepare timer
        long startTS = System.currentTimeMillis();

        //prepare tweetIterator which acutally generates tweet 
        TweetMessageIterator tweetIterator = dataGenerator.new TweetMessageIterator(0, uidGenerator);

        FileOutputStream fos = null;
        try {
            //prepare output file
            fos = openOutputFile(config.getOutputFilePath());

            while (recordCount < maxRecordCount) {
                //get a tweet and append newline at the end
                String tweet = tweetIterator.next().toString() + "\n";
                //write to file
                fos.write(tweet.getBytes());
                
                recordCount++;
                if (recordCount % 1000000 == 0) {
                    System.out.println("... generated " + recordCount + " records");
                }
            }
            System.out.println("Done: generated " + recordCount + " records in "
                    + ((System.currentTimeMillis() - startTS) / 1000) + " in seconds!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeOutputFile(fos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static FileOutputStream openOutputFile(String filepath) throws IOException {
        File file = new File(filepath);
        if (file.exists()) {
            throw new IOException(filepath + "already exists");
        }
        file.createNewFile();
        return new FileOutputStream(file);
    }

    public static void closeOutputFile(FileOutputStream fos) throws IOException {
        fos.flush();
        fos.close();
        fos = null;
    }
}
