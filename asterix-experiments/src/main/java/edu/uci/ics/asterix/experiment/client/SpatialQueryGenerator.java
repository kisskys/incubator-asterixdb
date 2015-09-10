package edu.uci.ics.asterix.experiment.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import edu.uci.ics.asterix.experiment.action.base.IAction;
import edu.uci.ics.asterix.experiment.action.base.SequentialActionList;
import edu.uci.ics.asterix.experiment.action.derived.RunAQLStringAction;
import edu.uci.ics.asterix.experiment.action.derived.TimedAction;

public class SpatialQueryGenerator {

    private static final int SKIP_LINE_COUNT = 199;

    private final ExecutorService threadPool;

    private final int partitionRangeStart;

    private final int duration;

    private final String restHost;

    private final int restPort;

    private final String orchHost;

    private final int orchPort;

    private final String openStreetMapFilePath;

    public SpatialQueryGenerator(SpatialQueryGeneratorConfig config) {
        threadPool = Executors.newCachedThreadPool(new ThreadFactory() {

            private final AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                int tid = count.getAndIncrement();
                Thread t = new Thread(r, "DataGeneratorThread: " + tid);
                t.setDaemon(true);
                return t;
            }
        });

        partitionRangeStart = config.getPartitionRangeStart();
        duration = config.getDuration();
        restHost = config.getRESTHost();
        restPort = config.getRESTPort();
        orchHost = config.getQueryOrchestratorHost();
        orchPort = config.getQueryOrchestratorPort();
        openStreetMapFilePath = config.getOpenStreetMapFilePath();

    }

    public void start() throws Exception {
        final Semaphore sem = new Semaphore(0);
        threadPool.submit(new QueryGenerator(sem, restHost, restPort, orchHost, orchPort, partitionRangeStart,
                duration, openStreetMapFilePath));
        sem.acquire();
    }

    public static class QueryGenerator implements Runnable {

        private static final Logger LOGGER = Logger.getLogger(QueryGenerator.class.getName());

        private final HttpClient httpClient;
        private final Semaphore sem;
        private final String restHost;
        private final int restPort;
        private final String orchHost;
        private final int orchPort;
        private final int partition;
        private final int queryDuration;
        private final String openStreetMapFilePath;
        private final float[] radiusType = new float[] { 0.00001f, 0.0001f, 0.001f, 0.01f, 0.1f };
        private int radiusIter = 0;
        private final Random randGen;
        private BufferedReader br;
        private long queryCount;
        private Random random = new Random(211);

        public QueryGenerator(Semaphore sem, String restHost, int restPort, String orchHost, int orchPort,
                int partitionRangeStart, int queryDuration, String openStreetMapFilePath) {
            httpClient = new DefaultHttpClient();
            this.sem = sem;
            this.restHost = restHost;
            this.restPort = restPort;
            this.orchHost = orchHost;
            this.orchPort = orchPort;
            this.partition = partitionRangeStart;
            this.queryDuration = queryDuration * 1000;
            this.openStreetMapFilePath = openStreetMapFilePath;
            this.queryCount = 0;
            this.randGen = new Random(partitionRangeStart);
        }

        @Override
        public void run() {
            try {
                if (openStreetMapFilePath != null) {
                    this.br = new BufferedReader(new FileReader(openStreetMapFilePath));
                }
                Socket s = new Socket(restHost, restPort);
                try {
                    //connect to orchestrator socket
                    Socket orchSocket = null;
                    orchSocket = new Socket(orchHost, orchPort);
                    try {
                        //send reached message to orchestrator
                        sendReached(orchSocket);

                        //wait until receiving resume message from the orchestrator server
                        receiveResume(orchSocket);

                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.info("QueryGen[" + partition + "] starts sending queries...");
                        }
                        //send queries during query duration
                        long startTS = System.currentTimeMillis();
                        long prevTS = startTS;
                        long curTS = startTS;
                        while (curTS - startTS < queryDuration) {
                            sendQuery();
                            queryCount++;
                            curTS = System.currentTimeMillis();
                            if (LOGGER.isLoggable(Level.INFO) && queryCount % 100 == 0) {
                                LOGGER.info("QueryGen[" + partition + "][TimeToQuery100] " + (curTS - prevTS)
                                        + " in milliseconds");
                                prevTS = curTS;
                            }
                        }
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.info("QueryGen[" + partition + "][QueryCount] " + queryCount + " in "
                                    + (queryDuration / 1000) + " seconds");
                        }

                        //send reqched message to orchestrator
                        sendReached(orchSocket);
                    } finally {
                        if (orchSocket != null) {
                            orchSocket.close();
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    if (openStreetMapFilePath != null) {
                        br.close();
                    }
                    s.close();
                }
            } catch (Throwable t) {
                System.err.println("Error connecting to rest API server " + restHost + ":" + restPort);
                t.printStackTrace();
            } finally {
                sem.release();
            }
        }

        private void sendQuery() throws IOException {
            //prepare radius and center point
            int skipLineCount = SKIP_LINE_COUNT;
            int lineCount = 0;
            String line = null;;
            String strPoints[] = null;
            float x = 0, y = 0;
            int beginX = -180, endX = 180, beginY = -90, endY = 90;
            if (openStreetMapFilePath != null) {
                while (lineCount < skipLineCount) {
                    if ((line = br.readLine()) == null) {
                        //reopen file
                        br.close();
                        br = new BufferedReader(new FileReader(openStreetMapFilePath));
                        line = br.readLine();
                    }
                    strPoints = line.split(",");
                    if (strPoints.length != 2) {
                        continue;
                    }
                    lineCount++;
                }
                y = Float.parseFloat(strPoints[0]) / 10000000; //latitude (y value)
                x = Float.parseFloat(strPoints[1]) / 10000000; //longitude (x value)
            } else {
                int xMajor = beginX + random.nextInt(endX - beginX);
                int xMinor = random.nextInt(100);
                x = xMajor + ((float) xMinor) / 100;

                int yMajor = beginY + random.nextInt(endY - beginY);
                int yMinor = random.nextInt(100);
                y = yMajor + ((float) yMinor) / 100;
            }

            //create action
            SequentialActionList sAction = new SequentialActionList();
            IAction rangeQueryAction = new TimedAction(new RunAQLStringAction(httpClient, restHost, restPort,
                    getRangeQueryAQL(radiusType[radiusIter++ % radiusType.length], x, y)));
            sAction.add(rangeQueryAction);

            //perform
            sAction.perform();
        }

        private String getRangeQueryAQL(float radius, float x, float y) {
            StringBuilder sb = new StringBuilder();
            sb.append("use dataverse experiments; ");
            sb.append("count( ");
            sb.append("for $x in dataset Tweets").append(" ");
            sb.append("let $n :=  create-circle( ");
            sb.append("point(\"").append(x).append(", ").append(y).append("\") ");
            sb.append(", ");
            sb.append(String.format("%f", radius));
            sb.append(" )");
            sb.append("where spatial-intersect($x.sender-location, $n)");
            sb.append("return $x ");
            sb.append(");");
            return sb.toString();
        }

        private void sendReached(Socket s) throws IOException {
            new DataOutputStream(s.getOutputStream()).writeInt(OrchestratorDGProtocol.REACHED.ordinal());
            s.getOutputStream().flush();
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Sent " + OrchestratorDGProtocol.REACHED + " to " + s.getRemoteSocketAddress());
            }
        }

        private void receiveResume(Socket s) throws IOException {
            int msg = new DataInputStream(s.getInputStream()).readInt();
            OrchestratorDGProtocol msgType = OrchestratorDGProtocol.values()[msg];
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Received " + msgType + " from " + s.getRemoteSocketAddress());
            }
            if (msgType != OrchestratorDGProtocol.RESUME) {
                throw new IllegalStateException("Unknown protocol message received: " + msgType);
            }
        }

        private void sendStopped(Socket s) throws IOException {
            new DataOutputStream(s.getOutputStream()).writeInt(OrchestratorDGProtocol.STOPPED.ordinal());
            s.getOutputStream().flush();
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Sent " + OrchestratorDGProtocol.STOPPED + " to " + s.getRemoteSocketAddress());
            }
        }

    }
}
