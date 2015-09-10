package edu.uci.ics.asterix.experiment.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import edu.uci.ics.asterix.event.schema.cluster.Cluster;
import edu.uci.ics.asterix.experiment.action.base.IAction;
import edu.uci.ics.asterix.experiment.action.base.ParallelActionSet;
import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.AbstractRemoteExecutableAction;
import edu.uci.ics.asterix.experiment.action.derived.ManagixActions.LogAsterixManagixAction;
import edu.uci.ics.asterix.experiment.action.derived.ManagixActions.StartAsterixManagixAction;
import edu.uci.ics.asterix.experiment.action.derived.ManagixActions.StopAsterixManagixAction;
import edu.uci.ics.asterix.experiment.action.derived.RemoteAsterixDriverKill;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLFileAction;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLStringAction;
import edu.uci.ics.asterix.experiment.action.derived.SleepAction;
import edu.uci.ics.asterix.experiment.action.derived.TimedAction;
import edu.uci.ics.asterix.experiment.client.LSMExperimentConstants;
import edu.uci.ics.asterix.experiment.client.LSMExperimentSetRunner.LSMExperimentSetRunnerConfig;
import edu.uci.ics.hyracks.api.util.ExperimentProfiler;
import edu.uci.ics.hyracks.api.util.SpatialIndexProfiler;

/**
 * This class is used to create experiments for spatial index static data evaluation, that is, no ingestion is involved.
 * Also, there is no orchestration server involved in this experiment builder.
 */
public abstract class AbstractSpatialIndexExperiment3SIdxCreateAndQueryBuilder extends AbstractExperimentBuilder {

    private static final String ASTERIX_INSTANCE_NAME = "a1";
    private static final int SKIP_LINE_COUNT = 223;
    private static final int CACHE_WARM_UP_QUERY_COUNT = 500;
    private static final int SELECT_QUERY_COUNT = 5000;
    private static final int JOIN_QUERY_COUNT = 200;
    private static final int JOIN_CANDIDATE_COUNT = 100;
    private static final int MAX_QUERY_SEED = 10000;

    private int querySeed = 0;

    private int queryCount = 0;

    private final String logDirSuffix;

    protected final HttpClient httpClient;

    protected final String restHost;

    protected final int restPort;

    private final String managixHomePath;

    protected final String javaHomePath;

    protected final Path localExperimentRoot;

    protected final String username;

    protected final String sshKeyLocation;

    private final int duration;

    private final String clusterConfigFileName;

    private final String ingestFileName;

    protected final String dgenFileName;

    private final String countFileName;

    private final String statFile;

    protected final SequentialActionList lsAction;

    protected final String openStreetMapFilePath;

    protected final int locationSampleInterval;

    protected final String createAQLFilePath;

    protected final String querySeedFilePath;

    private final float[] radiusType = new float[] { 0.00001f, 0.0001f, 0.001f, 0.01f, 0.1f };
    private int radiusIter = 0;
    private final Random randGen;
    private BufferedReader br;

    public AbstractSpatialIndexExperiment3SIdxCreateAndQueryBuilder(String name, LSMExperimentSetRunnerConfig config,
            String clusterConfigFileName, String ingestFileName, String dgenFileName, String countFileName,
            String createAQLFileName) {
        super(name);
        this.logDirSuffix = config.getLogDirSuffix();
        this.httpClient = new DefaultHttpClient();
        this.restHost = config.getRESTHost();
        this.restPort = config.getRESTPort();
        this.managixHomePath = config.getManagixHome();
        this.javaHomePath = config.getJavaHome();
        this.localExperimentRoot = Paths.get(config.getLocalExperimentRoot());
        this.username = config.getUsername();
        this.sshKeyLocation = config.getSSHKeyLocation();
        this.duration = config.getDuration();
        this.clusterConfigFileName = clusterConfigFileName;
        this.ingestFileName = ingestFileName;
        this.dgenFileName = dgenFileName;
        this.countFileName = countFileName;
        this.statFile = config.getStatFile();
        this.lsAction = new SequentialActionList();
        this.openStreetMapFilePath = config.getOpenStreetMapFilePath();
        this.locationSampleInterval = config.getLocationSampleInterval();
        this.createAQLFilePath = createAQLFileName;
        this.querySeedFilePath = config.getQuerySeedFilePath();
        this.randGen = new Random();
    }

    protected void doPost(SequentialActionList seq) {
    }

    protected void doBuildDataGen(SequentialActionList seq, final Map<String, List<String>> dgenPairs) throws Exception {
    }

    @Override
    protected void doBuild(Experiment e) throws Exception {
        SequentialActionList execs = new SequentialActionList();

        String clusterConfigPath = localExperimentRoot.resolve(LSMExperimentConstants.CONFIG_DIR)
                .resolve(clusterConfigFileName).toString();
        String asterixConfigPath = localExperimentRoot.resolve(LSMExperimentConstants.CONFIG_DIR)
                .resolve(LSMExperimentConstants.ASTERIX_CONFIGURATION).toString();

        //start asterix instance
        execs.add(new StartAsterixManagixAction(managixHomePath, ASTERIX_INSTANCE_NAME));
        execs.add(new SleepAction(30000));

        //prepare io state action in NC node(s)
        Map<String, List<String>> dgenPairs = readDatagenPairs(localExperimentRoot.resolve(
                LSMExperimentConstants.DGEN_DIR).resolve(dgenFileName));
        final Set<String> ncHosts = new HashSet<>();
        for (List<String> ncHostList : dgenPairs.values()) {
            for (String ncHost : ncHostList) {
                ncHosts.add(ncHost.split(":")[0]);
            }
        }
        if (statFile != null) {
            ParallelActionSet ioCountActions = new ParallelActionSet();
            for (String ncHost : ncHosts) {
                ioCountActions.add(new AbstractRemoteExecutableAction(ncHost, username, sshKeyLocation) {

                    @Override
                    protected String getCommand() {
                        String cmd = "screen -d -m sh -c \"sar -b -u 1 > " + statFile + "\"";
                        return cmd;
                    }
                });
            }
            execs.add(ioCountActions);
        }

        //prepare post ls action
        SequentialActionList postLSAction = new SequentialActionList();
        File file = new File(clusterConfigPath);
        JAXBContext ctx = JAXBContext.newInstance(Cluster.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        final Cluster cluster = (Cluster) unmarshaller.unmarshal(file);
        String[] storageRoots = cluster.getIodevices().split(",");
        for (String ncHost : ncHosts) {
            for (final String sRoot : storageRoots) {
                lsAction.add(new AbstractRemoteExecutableAction(ncHost, username, sshKeyLocation) {
                    @Override
                    protected String getCommand() {
                        return "ls -Rl " + sRoot;
                    }
                });
                postLSAction.add(new AbstractRemoteExecutableAction(ncHost, username, sshKeyLocation) {
                    @Override
                    protected String getCommand() {
                        return "ls -Rl " + sRoot;
                    }
                });

            }
        }

        //---------- main experiment body begins -----------

        //delete all existing secondary indexes if any
        execs.add(new RunAQLStringAction(httpClient, restHost, restPort,
                "use dataverse experiments; drop index Tweets.dhbtreeLocation;"));
        execs.add(new RunAQLStringAction(httpClient, restHost, restPort,
                "use dataverse experiments; drop index Tweets.dhvbtreeLocation;"));
        execs.add(new RunAQLStringAction(httpClient, restHost, restPort,
                "use dataverse experiments; drop index Tweets.rtreeLocation;"));
        execs.add(new RunAQLStringAction(httpClient, restHost, restPort,
                "use dataverse experiments; drop index Tweets.shbtreeLocation;"));
        execs.add(new RunAQLStringAction(httpClient, restHost, restPort,
                "use dataverse experiments; drop index Tweets.sifLocation;"));

        //create secondary index 
        execs.add(new TimedAction(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                LSMExperimentConstants.AQL_DIR).resolve(createAQLFilePath))));

        //run count query for cleaning up OS buffer cache
        if (countFileName != null) {
            execs.add(new RunAQLFileAction(httpClient, restHost, restPort, localExperimentRoot.resolve(
                    LSMExperimentConstants.AQL_DIR).resolve(countFileName)));
        }

        //run cache warm-up queries: run CACHE_WARM_UP_QUERY_COUNT select queries
        br = new BufferedReader(new FileReader(querySeedFilePath));
        radiusIter = 0;
        for (int i = 0; i < CACHE_WARM_UP_QUERY_COUNT; i++) {
            execs.add(getSelectQuery());
        }

        radiusIter = 0;
        //run queries for measurement: run SELECT_QUERY_COUNT select queries
        for (int i = 0; i < SELECT_QUERY_COUNT; i++) {
            execs.add(getSelectQuery());
        }

        radiusIter = 0;
        //run queries for measurement: run JOIN_QUERY_COUNT join queries
        for (int i = 0; i < JOIN_QUERY_COUNT; i++) {
            execs.add(getJoinQuery());
        }

        //---------- main experiment body ends -----------

        //kill io state action
//        if (statFile != null) {
//            ParallelActionSet ioCountKillActions = new ParallelActionSet();
//            for (String ncHost : ncHosts) {
//                ioCountKillActions.add(new AbstractRemoteExecutableAction(ncHost, username, sshKeyLocation) {
//
//                    @Override
//                    protected String getCommand() {
//                        String cmd = "screen -X -S `screen -list | grep Detached | awk '{print $1}'` quit";
//                        return cmd;
//                    }
//                });
//            }
//            execs.add(ioCountKillActions);
//        }

        //add ls action
        execs.add(postLSAction);

        //kill asterix cc and nc
        ParallelActionSet killCmds = new ParallelActionSet();
        for (String ncHost : ncHosts) {
            killCmds.add(new RemoteAsterixDriverKill(ncHost, username, sshKeyLocation));
        }
        killCmds.add(new RemoteAsterixDriverKill(restHost, username, sshKeyLocation));
        execs.add(killCmds);

        //stop asterix instance
        execs.add(new StopAsterixManagixAction(managixHomePath, ASTERIX_INSTANCE_NAME));

        //prepare to collect io state by putting the state file into asterix log dir
        if (statFile != null) {
            ParallelActionSet collectIOActions = new ParallelActionSet();
            for (String ncHost : ncHosts) {
                collectIOActions.add(new AbstractRemoteExecutableAction(ncHost, username, sshKeyLocation) {

                    @Override
                    protected String getCommand() {
                        String cmd = "cp " + statFile + " " + cluster.getLogDir();
                        return cmd;
                    }
                });
            }
            execs.add(collectIOActions);
        }

        //collect profile information
        if (ExperimentProfiler.PROFILE_MODE) {
            ParallelActionSet collectProfileInfo = new ParallelActionSet();
            for (String ncHost : ncHosts) {
                collectProfileInfo.add(new AbstractRemoteExecutableAction(ncHost, username, sshKeyLocation) {
                    @Override
                    protected String getCommand() {
                        String cmd = "mv " + SpatialIndexProfiler.PROFILE_HOME_DIR + "*.txt " + cluster.getLogDir();
                        return cmd;
                    }
                });
            }
            execs.add(collectProfileInfo);
        }

        //collect cc and nc logs
        execs.add(new LogAsterixManagixAction(managixHomePath, ASTERIX_INSTANCE_NAME, localExperimentRoot
                .resolve(LSMExperimentConstants.LOG_DIR + "-" + logDirSuffix).resolve(getName()).toString()));

        e.addBody(execs);
    }

    protected Map<String, List<String>> readDatagenPairs(Path p) throws IOException {
        Map<String, List<String>> dgenPairs = new HashMap<>();
        Scanner s = new Scanner(p, StandardCharsets.UTF_8.name());
        try {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] pair = line.split("\\s+");
                List<String> vals = dgenPairs.get(pair[0]);
                if (vals == null) {
                    vals = new ArrayList<>();
                    dgenPairs.put(pair[0], vals);
                }
                vals.add(pair[1]);
            }
        } finally {
            s.close();
        }
        return dgenPairs;
    }

    private SequentialActionList getSelectQuery() throws IOException {
        //prepare radius and center point
        int skipLineCount = SKIP_LINE_COUNT;
        int lineCount = 0;
        String line = null;;

        querySeed += SKIP_LINE_COUNT;
        if (querySeed > MAX_QUERY_SEED) {
            querySeed -= MAX_QUERY_SEED;
        }

        while (lineCount < skipLineCount) {
            if ((line = br.readLine()) == null) {
                //reopen file
                br.close();
                br = new BufferedReader(new FileReader(querySeedFilePath));
                line = br.readLine();
            }
            lineCount++;
        }

        int beginIdx = line.indexOf("(", line.indexOf("point"));
        int endIdx = line.indexOf(")", line.indexOf("point")) + 1;
        String point = line.substring(beginIdx, endIdx);

        //create action
        SequentialActionList sAction = new SequentialActionList();
        IAction queryAction = new TimedAction(new RunAQLStringAction(httpClient, restHost, restPort, getSelectQueryAQL(
                radiusType[radiusIter++ % radiusType.length], point)));
        sAction.add(queryAction);

        return sAction;
    }

    private String getSelectQueryAQL(float radius, String point) {
        StringBuilder sb = new StringBuilder();
        sb.append("use dataverse experiments; ");
        sb.append("count( ");
        sb.append("for $x in dataset Tweets").append(" ");
        sb.append("let $n :=  create-circle( ");
        sb.append("point").append(point).append(" ");
        sb.append(", ");
        sb.append(String.format("%f", radius));
        sb.append(" ) ");
        sb.append("where spatial-intersect($x.sender-location, $n) ");
        sb.append("return $x ");
        sb.append(");");

        System.out.println("[squery" + (queryCount++) + "]" + sb.toString());

        return sb.toString();
    }

    private SequentialActionList getJoinQuery() {
        querySeed += SKIP_LINE_COUNT;
        if (querySeed > MAX_QUERY_SEED) {
            querySeed -= MAX_QUERY_SEED;
        }

        int lowId = querySeed * 10000 + 1;
        int highId = (querySeed + JOIN_CANDIDATE_COUNT) * 10000 + 1;

        //create action
        SequentialActionList sAction = new SequentialActionList();
        IAction queryAction = new TimedAction(new RunAQLStringAction(httpClient, restHost, restPort, getJoinQueryAQL(
                radiusType[radiusIter++ % (radiusType.length - 1)], lowId, highId)));
        sAction.add(queryAction);

        return sAction;
    }

    private String getJoinQueryAQL(float radius, int lowId, int highId) {
        StringBuilder sb = new StringBuilder();
        sb.append(" use dataverse experiments; \n");
        sb.append(" count( \n");
        sb.append(" for $x in dataset JoinSeedTweets").append(" \n");
        sb.append(" let $area := create-circle($x.sender-location, ").append(String.format("%f", radius))
                .append(" ) \n");
        sb.append(" for $y in dataset Tweets \n");
        sb.append(" where $x.tweetid >= int64(\"" + lowId + "\") ").append(
                "and $x.tweetid < int64(\"" + highId + "\") and ");
        sb.append(" spatial-intersect($y.sender-location, $area) \n");
        sb.append(" return $y \n");
        sb.append(" );\n");

        System.out.println("[jquery" + (queryCount++) + "]" + sb.toString());

        return sb.toString();
    }
}
