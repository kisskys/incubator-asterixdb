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

package org.apache.asterix.dataflow.data.common;

import org.apache.hyracks.storage.am.common.api.IBinaryTokenizer;
import org.apache.hyracks.storage.am.common.api.IBinaryTokenizerFactory;
import org.apache.hyracks.storage.am.common.api.ITokenFactory;

public class SIFBinaryTokenizerFactory implements IBinaryTokenizerFactory {
    private static final long serialVersionUID = 1L;
    private final ITokenFactory tokenFactory;
    private final double bottomLeftX;
    private final double bottomLeftY;
    private final double topRightX;
    private final double topRightY;
    private final short[] levelDensity;
    private final int cellsPerObject;
    private final int frameSize;

    public SIFBinaryTokenizerFactory(double bottomLeftX, double bottomLeftY, double topRightX, double topRightY,
            short[] levelDensity, int cellsPerObject, ITokenFactory tokenFactory, int frameSize) {
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.levelDensity = levelDensity;
        this.cellsPerObject = cellsPerObject;
        this.tokenFactory = tokenFactory;
        this.frameSize = frameSize;
    }

    @Override
    public IBinaryTokenizer createTokenizer() {
        return new SIFBinaryTokenizer(bottomLeftX, bottomLeftY, topRightX, topRightY, levelDensity, cellsPerObject,
                tokenFactory, frameSize);
    }
}