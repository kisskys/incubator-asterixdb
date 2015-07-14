package edu.uci.ics.asterix.experiment.client;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class SpatialQueryGeneratorDriver {
    public static void main(String[] args) throws Exception {
        SpatialQueryGeneratorConfig clientConfig = new SpatialQueryGeneratorConfig();
        CmdLineParser clp = new CmdLineParser(clientConfig);
        try {
            clp.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            clp.printUsage(System.err);
            System.exit(1);
        }

        SpatialQueryGenerator client = new SpatialQueryGenerator(clientConfig);
        client.start();
    }
}
