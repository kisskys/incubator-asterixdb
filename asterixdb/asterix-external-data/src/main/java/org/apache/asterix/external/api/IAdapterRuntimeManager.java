/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.external.api;

import org.apache.asterix.external.dataset.adapter.FeedAdapter;
import org.apache.asterix.external.feed.api.IIntakeProgressTracker;
import org.apache.asterix.external.feed.management.FeedId;

public interface IAdapterRuntimeManager {

    public enum State {
        /**
         * Indicates that AsterixDB is maintaining the flow of data from external source into its storage.
         */
        ACTIVE_INGESTION,

        /**
         * Indicates that data from external source is being buffered and not
         * pushed downstream
         */

        INACTIVE_INGESTION,
        /**
         * Indicates that feed ingestion activity has finished.
         */
        FINISHED_INGESTION,

        /** Indicates the occurrence of a failure during the intake stage of a data ingestion pipeline **/
        FAILED_INGESTION
    }

    /**
     * Start feed ingestion
     * @throws Exception
     */
    public void start() throws Exception;

    /**
     * Stop feed ingestion.
     * @throws Exception
     */
    public void stop() throws Exception;

    /**
     * @return feedId associated with the feed that is being ingested.
     */
    public FeedId getFeedId();

    /**
     * @return an instance of the {@code FeedAdapter} in use.
     */
    public FeedAdapter getFeedAdapter();

    /**
     * @return state associated with the AdapterRuntimeManager. See {@code State}.
     */
    public State getState();

    /**
     * @param state
     */
    public void setState(State state);

    public IIntakeProgressTracker getProgressTracker();

    public int getPartition();

}
