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

package org.apache.hyracks.storage.am.lsm.rtree.dataflow;

import java.util.Map;

import org.apache.hyracks.api.context.IHyracksTaskContext;
import org.apache.hyracks.api.dataflow.value.IBinaryComparatorFactory;
import org.apache.hyracks.api.dataflow.value.ILinearizeComparatorFactory;
import org.apache.hyracks.api.dataflow.value.ITypeTraits;
import org.apache.hyracks.storage.am.common.api.IPrimitiveValueProviderFactory;
import org.apache.hyracks.storage.am.common.dataflow.IIndexOperatorDescriptor;
import org.apache.hyracks.storage.am.common.dataflow.IndexDataflowHelper;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMIOOperationCallbackFactory;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMIOOperationSchedulerProvider;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMMergePolicyFactory;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMOperationTrackerProvider;
import org.apache.hyracks.storage.am.lsm.common.api.IVirtualBufferCacheProvider;
import org.apache.hyracks.storage.am.lsm.common.dataflow.AbstractLSMIndexDataflowHelperFactory;
import org.apache.hyracks.storage.am.rtree.frames.RTreePolicyType;

public class LSMRTreeWithAntiMatterTuplesDataflowHelperFactory extends AbstractLSMIndexDataflowHelperFactory {

    private static final long serialVersionUID = 1L;

    private final IBinaryComparatorFactory[] btreeComparatorFactories;
    private final IPrimitiveValueProviderFactory[] valueProviderFactories;
    private final RTreePolicyType rtreePolicyType;
    private final ILinearizeComparatorFactory linearizeCmpFactory;
    private final int[] rtreeFields;
    protected final boolean isPointMBR;

    public LSMRTreeWithAntiMatterTuplesDataflowHelperFactory(IPrimitiveValueProviderFactory[] valueProviderFactories,
            RTreePolicyType rtreePolicyType, IBinaryComparatorFactory[] btreeComparatorFactories,
            IVirtualBufferCacheProvider virtualBufferCacheProvider, ILSMMergePolicyFactory mergePolicyFactory,
            Map<String, String> mergePolicyProperties, ILSMOperationTrackerProvider opTrackerFactory,
            ILSMIOOperationSchedulerProvider ioSchedulerProvider, ILSMIOOperationCallbackFactory ioOpCallbackFactory,
            ILinearizeComparatorFactory linearizeCmpFactory, int[] rtreeFields, ITypeTraits[] filterTypeTraits,
            IBinaryComparatorFactory[] filterCmpFactories, int[] filterFields, boolean durable, boolean isPointMBR) {
        super(virtualBufferCacheProvider, mergePolicyFactory, mergePolicyProperties, opTrackerFactory,
                ioSchedulerProvider, ioOpCallbackFactory, 1.0, filterTypeTraits, filterCmpFactories, filterFields,
                durable);
        this.btreeComparatorFactories = btreeComparatorFactories;
        this.valueProviderFactories = valueProviderFactories;
        this.rtreePolicyType = rtreePolicyType;
        this.linearizeCmpFactory = linearizeCmpFactory;
        this.rtreeFields = rtreeFields;
        this.isPointMBR = isPointMBR;
    }

    @Override
    public IndexDataflowHelper createIndexDataflowHelper(IIndexOperatorDescriptor opDesc, IHyracksTaskContext ctx,
            int partition) {
        return new LSMRTreeWithAntiMatterTuplesDataflowHelper(opDesc, ctx, partition,
                virtualBufferCacheProvider.getVirtualBufferCaches(ctx, opDesc.getFileSplitProvider()),
                btreeComparatorFactories, valueProviderFactories, rtreePolicyType,
                mergePolicyFactory.createMergePolicy(mergePolicyProperties, ctx), opTrackerFactory,
                ioSchedulerProvider.getIOScheduler(ctx), ioOpCallbackFactory, linearizeCmpFactory, rtreeFields,
                filterTypeTraits, filterCmpFactories, filterFields, durable, isPointMBR);
    }
}
