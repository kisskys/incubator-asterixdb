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
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import edu.uci.ics.asterix.experiment.action.base.AbstractAction;

public class RunAQLStringAction extends AbstractAction {
    private static final String REST_URI_TEMPLATE = "http://{0}:{1}/aql";

    private final HttpClient httpClient;

    private final String aql;

    private final String restHost;

    private final int restPort;

    public RunAQLStringAction(HttpClient httpClient, String restHost, int restPort, String aql) {
        this.httpClient = httpClient;
        this.aql = aql;
        this.restHost = restHost;
        this.restPort = restPort;
    }

    @Override
    public void doPerform() throws Exception {
        String uri = MessageFormat.format(REST_URI_TEMPLATE, restHost, String.valueOf(restPort));
        HttpPost post = new HttpPost(uri);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(aql, StandardCharsets.UTF_8));
        HttpEntity entity = httpClient.execute(post).getEntity();
        if (entity != null && entity.isStreaming()) {
            printStream(entity.getContent());
        }
    }

    private void printStream(InputStream content) throws IOException {
        IOUtils.copy(content, System.out);
        System.out.flush();
    }
}
