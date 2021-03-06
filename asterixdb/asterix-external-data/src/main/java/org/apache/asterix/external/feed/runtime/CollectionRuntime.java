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
package org.apache.asterix.external.feed.runtime;

import java.util.Map;

import org.apache.asterix.external.feed.api.ISubscribableRuntime;
import org.apache.asterix.external.feed.api.ISubscriberRuntime;
import org.apache.asterix.external.feed.dataflow.FeedFrameCollector;
import org.apache.asterix.external.feed.dataflow.FeedFrameCollector.State;
import org.apache.asterix.external.feed.dataflow.FeedRuntimeInputHandler;
import org.apache.asterix.external.feed.management.FeedConnectionId;
import org.apache.hyracks.api.comm.IFrameWriter;
import org.apache.hyracks.api.context.IHyracksTaskContext;

/**
 * Represents the feed runtime that collects feed tuples from another feed.
 * In case of a primary feed, the CollectionRuntime collects tuples from the feed
 * intake job. For a secondary feed, tuples are collected from the intake/compute
 * runtime associated with the source feed.
 */
public class CollectionRuntime extends FeedRuntime implements ISubscriberRuntime {

    private final FeedConnectionId connectionId;            // [Dataverse - Feed - Dataset]
    private final ISubscribableRuntime sourceRuntime;       // Runtime that provides the data
    private final Map<String, String> feedPolicy;           // Policy associated with the feed
    private FeedFrameCollector frameCollector;              // Collector that can be plugged into a frame distributor
    private final IHyracksTaskContext ctx;

    public CollectionRuntime(FeedConnectionId connectionId, FeedRuntimeId runtimeId,
            FeedRuntimeInputHandler inputSideHandler, IFrameWriter outputSideWriter, ISubscribableRuntime sourceRuntime,
            Map<String, String> feedPolicy, IHyracksTaskContext ctx) {
        super(runtimeId, inputSideHandler, outputSideWriter);
        this.connectionId = connectionId;
        this.sourceRuntime = sourceRuntime;
        this.feedPolicy = feedPolicy;
        this.ctx = ctx;
    }

    public State waitTillCollectionOver() throws InterruptedException {
        if (!(isCollectionOver())) {
            synchronized (frameCollector) {
                while (!isCollectionOver()) {
                    frameCollector.wait();
                }
            }
        }
        return frameCollector.getState();
    }

    private boolean isCollectionOver() {
        return frameCollector.getState().equals(FeedFrameCollector.State.FINISHED)
                || frameCollector.getState().equals(FeedFrameCollector.State.HANDOVER);
    }

    @Override
    public void setMode(Mode mode) {
        getInputHandler().setMode(mode);
    }

    @Override
    public Map<String, String> getFeedPolicy() {
        return feedPolicy;
    }

    public FeedConnectionId getConnectionId() {
        return connectionId;
    }

    public ISubscribableRuntime getSourceRuntime() {
        return sourceRuntime;
    }

    public void setFrameCollector(FeedFrameCollector frameCollector) {
        this.frameCollector = frameCollector;
    }

    @Override
    public FeedFrameCollector getFrameCollector() {
        return frameCollector;
    }

    public IHyracksTaskContext getCtx() {
        return ctx;
    }
}
