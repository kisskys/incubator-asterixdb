/*
 * Copyright 2009-2010 by The Regents of the University of California
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
package edu.uci.ics.hyracks.control.cc.job.manager.events;

import org.json.JSONObject;

import edu.uci.ics.hyracks.api.job.JobId;
import edu.uci.ics.hyracks.control.cc.ClusterControllerService;
import edu.uci.ics.hyracks.control.cc.job.JobRun;
import edu.uci.ics.hyracks.control.cc.jobqueue.SynchronizableEvent;

public class GetJobProfileJSONEvent extends SynchronizableEvent {
    private final ClusterControllerService ccs;
    private final JobId jobId;
    private JSONObject profile;

    public GetJobProfileJSONEvent(ClusterControllerService ccs, JobId jobId) {
        this.ccs = ccs;
        this.jobId = jobId;
    }

    @Override
    protected void doRun() throws Exception {
        profile = new JSONObject();
        JobRun jobRun = ccs.getRunMap().get(jobId);
        if (jobRun == null) {
            profile = new JSONObject();
            return;
        }
        profile = jobRun.getJobProfile().toJSON();
    }

    public JSONObject getProfile() {
        return profile;
    }
}