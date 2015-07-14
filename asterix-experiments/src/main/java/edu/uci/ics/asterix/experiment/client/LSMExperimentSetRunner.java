package edu.uci.ics.asterix.experiment.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.builder.AbstractExperimentBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1ADhbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1ADhvbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1ARtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1AShbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1ASifBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1BDhbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1BDhvbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1BRtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1BShbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1BSifBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1CDhbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1CDhvbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1CRtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1CShbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1CSifBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1DDhbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1DDhvbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1DRtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1DShbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment1DSifBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment2DhbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment2DhvbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment2RtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment2ShbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment2SifBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment3DhbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment3DhvbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment3PIdxLoadBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment3RtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment3ShbtreeBuilder;
import edu.uci.ics.asterix.experiment.builder.SpatialIndexExperiment3SifBuilder;

public class LSMExperimentSetRunner {

    private static final Logger LOGGER = Logger.getLogger(LSMExperimentSetRunner.class.getName());

    public static class LSMExperimentSetRunnerConfig {

        private final String logDirSuffix;

        private final int nQueryRuns;

        public LSMExperimentSetRunnerConfig(String logDirSuffix, int nQueryRuns) {
            this.logDirSuffix = logDirSuffix;
            this.nQueryRuns = nQueryRuns;
        }

        public String getLogDirSuffix() {
            return logDirSuffix;
        }

        public int getNQueryRuns() {
            return nQueryRuns;
        }

        @Option(name = "-rh", aliases = "--rest-host", usage = "Asterix REST API host address", required = true, metaVar = "HOST")
        private String restHost;

        public String getRESTHost() {
            return restHost;
        }

        @Option(name = "-rp", aliases = "--rest-port", usage = "Asterix REST API port", required = true, metaVar = "PORT")
        private int restPort;

        public int getRESTPort() {
            return restPort;
        }

        @Option(name = "-mh", aliases = "--managix-home", usage = "Path to MANAGIX_HOME directory", required = true, metaVar = "MGXHOME")
        private String managixHome;

        public String getManagixHome() {
            return managixHome;
        }

        @Option(name = "-jh", aliases = "--java-home", usage = "Path to JAVA_HOME directory", required = true, metaVar = "JAVAHOME")
        private String javaHome;

        public String getJavaHome() {
            return javaHome;
        }

        @Option(name = "-ler", aliases = "--local-experiment-root", usage = "Path to the local LSM experiment root directory", required = true, metaVar = "LOCALEXPROOT")
        private String localExperimentRoot;

        public String getLocalExperimentRoot() {
            return localExperimentRoot;
        }

        @Option(name = "-u", aliases = "--username", usage = "Username to use for SSH/SCP", required = true, metaVar = "UNAME")
        private String username;

        public String getUsername() {
            return username;
        }

        @Option(name = "-k", aliases = "--key", usage = "SSH key location", metaVar = "SSHKEY")
        private String sshKeyLocation;

        public String getSSHKeyLocation() {
            return sshKeyLocation;
        }

        @Option(name = "-d", aliases = "--datagen-duartion", usage = "Data generation duration in seconds", metaVar = "DATAGENDURATION")
        private int duration;

        public int getDuration() {
            return duration;
        }

        @Option(name = "-qd", aliases = "--querygen-duartion", usage = "Query generation duration in seconds", metaVar = "QUERYGENDURATION")
        private int queryDuration;

        public int getQueryDuration() {
            return queryDuration;
        }

        @Option(name = "-regex", aliases = "--regex", usage = "Regular expression used to match experiment names", metaVar = "REGEXP")
        private String regex;

        public String getRegex() {
            return regex;
        }

        @Option(name = "-oh", aliases = "--orchestrator-host", usage = "The host address of THIS orchestrator")
        private String orchHost;

        public String getOrchestratorHost() {
            return orchHost;
        }

        @Option(name = "-op", aliases = "--orchestrator-port", usage = "The port to be used for the orchestrator server of THIS orchestrator")
        private int orchPort;

        public int getOrchestratorPort() {
            return orchPort;
        }

        @Option(name = "-qoh", aliases = "--query-orchestrator-host", usage = "The host address of query orchestrator")
        private String queryOrchHost;

        public String getQueryOrchestratorHost() {
            return queryOrchHost;
        }

        @Option(name = "-qop", aliases = "--query-orchestrator-port", usage = "The port to be used for the orchestrator server of query orchestrator")
        private int queryOrchPort;

        public int getQueryOrchestratorPort() {
            return queryOrchPort;
        }

        @Option(name = "-di", aliases = "--data-interval", usage = " Initial data interval to use when generating data for exp 7")
        private long dataInterval;

        public long getDataInterval() {
            return dataInterval;
        }

        @Option(name = "-ni", aliases = "--num-data-intervals", usage = "Number of data intervals to use when generating data for exp 7")
        private int numIntervals;

        public int getNIntervals() {
            return numIntervals;
        }

        @Option(name = "-sf", aliases = "--stat-file", usage = "Enable IO/CPU stats and place in specified file")
        private String statFile = null;

        public String getStatFile() {
            return statFile;
        }

        @Option(name = "-of", aliases = "--openstreetmap-filepath", usage = "The open street map gps point data file path")
        private String openStreetMapFilePath;

        public String getOpenStreetMapFilePath() {
            return openStreetMapFilePath;
        }

        @Option(name = "-si", aliases = "--location-sample-interval", usage = "Location sample interval from open street map point data")
        private int locationSampleInterval;

        public int getLocationSampleInterval() {
            return locationSampleInterval;
        }

        @Option(name = "-qsf", aliases = "--query-seed-filepath", usage = "The query seed file path")
        private String querySeedFilePath;

        public String getQuerySeedFilePath() {
            return querySeedFilePath;
        }
    }

    public static void main(String[] args) throws Exception {
        //        LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
        LSMExperimentSetRunnerConfig config = new LSMExperimentSetRunnerConfig(String.valueOf(System
                .currentTimeMillis()), 3);
        CmdLineParser clp = new CmdLineParser(config);
        try {
            clp.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            clp.printUsage(System.err);
            System.exit(1);
        }

        Collection<AbstractExperimentBuilder> suite = new ArrayList<>();

        suite.add(new SpatialIndexExperiment1ADhbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1ADhvbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1ARtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1AShbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1ASifBuilder(config));
        suite.add(new SpatialIndexExperiment1BDhbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1BDhvbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1BRtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1BShbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1BSifBuilder(config));
        suite.add(new SpatialIndexExperiment1CDhbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1CDhvbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1CRtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1CShbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1CSifBuilder(config));
        suite.add(new SpatialIndexExperiment1DDhbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1DDhvbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1DRtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1DShbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment1DSifBuilder(config));
        suite.add(new SpatialIndexExperiment2DhbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment2DhvbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment2RtreeBuilder(config));
        suite.add(new SpatialIndexExperiment2ShbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment2SifBuilder(config));
        suite.add(new SpatialIndexExperiment3PIdxLoadBuilder(config));
        suite.add(new SpatialIndexExperiment3DhbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment3DhvbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment3RtreeBuilder(config));
        suite.add(new SpatialIndexExperiment3ShbtreeBuilder(config));
        suite.add(new SpatialIndexExperiment3SifBuilder(config));
        //        suite.add(new Experiment7BBuilder(config));
        //        suite.add(new Experiment7DBuilder(config));
        //        suite.add(new Experiment7ABuilder(config));
        //        suite.add(new Experiment8DBuilder(config));
        //        suite.add(new Experiment8ABuilder(config));
        //        suite.add(new Experiment8BBuilder(config));
        //        suite.add(new Experiment9ABuilder(config));
        //        suite.add(new Experiment9DBuilder(config));
        //        suite.add(new Experiment9BBuilder(config));
        //        suite.add(new Experiment6ABuilder(config));
        //        suite.add(new Experiment6BBuilder(config));
        //        suite.add(new Experiment6CBuilder(config));
        //        suite.add(new Experiment2D1Builder(config));
        //        suite.add(new Experiment2D2Builder(config));
        //        suite.add(new Experiment2D4Builder(config));
        //        suite.add(new Experiment2D8Builder(config));
        //        suite.add(new Experiment2C1Builder(config));
        //        suite.add(new Experiment2C2Builder(config));
        //        suite.add(new Experiment2C4Builder(config));
        //        suite.add(new Experiment2C8Builder(config));
        //        suite.add(new Experiment2A1Builder(config));
        //        suite.add(new Experiment2A2Builder(config));
        //        suite.add(new Experiment2A4Builder(config));
        //        suite.add(new Experiment2A8Builder(config));
        //        suite.add(new Experiment2B1Builder(config));
        //        suite.add(new Experiment2B2Builder(config));
        //        suite.add(new Experiment2B4Builder(config));
        //        suite.add(new Experiment2B8Builder(config));
        //        suite.add(new Experiment1ABuilder(config));
        //        suite.add(new Experiment1BBuilder(config));
        //        suite.add(new Experiment1CBuilder(config));
        //        suite.add(new Experiment1DBuilder(config));
        //        suite.add(new Experiment4ABuilder(config));
        //        suite.add(new Experiment4BBuilder(config));
        //        suite.add(new Experiment4CBuilder(config));
        //        suite.add(new Experiment4DBuilder(config));
        //        suite.add(new Experiment3ABuilder(config));
        //        suite.add(new Experiment3BBuilder(config));
        //        suite.add(new Experiment3CBuilder(config));
        //        suite.add(new Experiment3DBuilder(config));
        //        suite.add(new Experiment5ABuilder(config));
        //        suite.add(new Experiment5BBuilder(config));
        //        suite.add(new Experiment5CBuilder(config));
        //        suite.add(new Experiment5DBuilder(config));

        Pattern p = config.getRegex() == null ? null : Pattern.compile(config.getRegex());

        SequentialActionList exps = new SequentialActionList();
        for (AbstractExperimentBuilder eb : suite) {
            if (p == null || p.matcher(eb.getName()).matches()) {
                exps.add(eb.build());
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Added " + eb.getName() + " to run list...");
                }
            }
        }
        exps.perform();
    }
}
