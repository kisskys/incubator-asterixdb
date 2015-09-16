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
package edu.uci.ics.asterix.experiment.client;

import java.net.Socket;
import java.util.Collections;

import edu.uci.ics.asterix.experiment.action.base.AbstractAction;
import edu.uci.ics.asterix.tools.external.data.TweetGeneratorForSpatialIndexEvaluation;

public class SocketDataGeneratorExecutable extends AbstractAction {

    private final String adapterHost;

    private final int adapterPort;

    public SocketDataGeneratorExecutable(String adapterHost, int adapterPort) {
        this.adapterHost = adapterHost;
        this.adapterPort = adapterPort;
    }

    @Override
    protected void doPerform() throws Exception {
        Thread.sleep(4000);
        Socket s = new Socket(adapterHost, adapterPort);
        try {
            TweetGeneratorForSpatialIndexEvaluation tg = new TweetGeneratorForSpatialIndexEvaluation(Collections.<String, String> emptyMap(), 0,
                    TweetGeneratorForSpatialIndexEvaluation.OUTPUT_FORMAT_ADM_STRING, s.getOutputStream());
            long start = System.currentTimeMillis();
            while (tg.setNextRecordBatch(1000)) {
            }
            long end = System.currentTimeMillis();
            long total = end - start;
            System.out.println("Generation finished: " + tg.getNumFlushedTweets() + " in " + total / 1000 + " seconds");
        } finally {
            s.close();
        }
    }

}
