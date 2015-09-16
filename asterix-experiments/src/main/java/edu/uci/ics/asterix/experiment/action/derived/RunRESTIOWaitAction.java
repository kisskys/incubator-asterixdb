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

import java.text.MessageFormat;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import edu.uci.ics.asterix.experiment.action.base.AbstractAction;

public class RunRESTIOWaitAction extends AbstractAction {
    private static final String REST_URI_TEMPLATE = "http://{0}:{1}/iowait";

    private final HttpClient httpClient;

    private final String restHost;

    private final int restPort;

    public RunRESTIOWaitAction(HttpClient httpClient, String restHost, int restPort) {
        this.httpClient = httpClient;
        this.restHost = restHost;
        this.restPort = restPort;
    }

    @Override
    public void doPerform() throws Exception {
        String uri = MessageFormat.format(REST_URI_TEMPLATE, restHost, String.valueOf(restPort));
        HttpGet get = new HttpGet(uri);
        HttpEntity entity = httpClient.execute(get).getEntity();
        EntityUtils.consume(entity);
    }

}
