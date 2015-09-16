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
package edu.uci.ics.asterix.experiment.action.derived;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import edu.uci.ics.asterix.experiment.action.base.AbstractAction;

public class RunAQLFileAction extends AbstractAction {
    private final Logger LOGGER = Logger.getLogger(RunAQLFileAction.class.getName());
    private static final String REST_URI_TEMPLATE = "http://{0}:{1}/aql";

    private final HttpClient httpClient;

    private final Path aqlFilePath;

    private final String restHost;

    private final int restPort;

    public RunAQLFileAction(HttpClient httpClient, String restHost, int restPort, Path aqlFilePath) {
        this.httpClient = httpClient;
        this.aqlFilePath = aqlFilePath;
        this.restHost = restHost;
        this.restPort = restPort;
    }

    @Override
    public void doPerform() throws Exception {
        String aql = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Files.readAllBytes(aqlFilePath))).toString();
        String uri = MessageFormat.format(REST_URI_TEMPLATE, restHost, String.valueOf(restPort));
        HttpPost post = new HttpPost(uri);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(aql, StandardCharsets.UTF_8));
        HttpEntity entity = httpClient.execute(post).getEntity();
        if (entity != null && entity.isStreaming()) {
            printStream(entity.getContent());
        }
        if (aql.contains("compact")) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Compaction has been completed");
            }
        }
    }

    private void printStream(InputStream content) throws IOException {
        IOUtils.copy(content, System.out);
        System.out.flush();
    }
}
