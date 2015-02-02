/*
 * Copyright 2014-2018 by The Regents of the University of California
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

package edu.uci.ics.asterix.dataflow.data.common;

import edu.uci.ics.hyracks.storage.am.common.api.IBinaryTokenizer;
import edu.uci.ics.hyracks.storage.am.common.api.IBinaryTokenizerFactory;
import edu.uci.ics.hyracks.storage.am.common.api.ITokenFactory;

public class SpatialCellBinaryTokenizerFactory implements IBinaryTokenizerFactory {
    private static final long serialVersionUID = 1L;
    private final ITokenFactory tokenFactory;
    private final double bottomLeftX;
    private final double bottomLeftY;
    private final double topRightX;
    private final double topRightY;
    private final short[] levelDensity;
    private final int cellsPerObject;
    private final int frameSize;
    private final boolean isQuery;

    public SpatialCellBinaryTokenizerFactory(double bottomLeftX, double bottomLeftY, double topRightX,
            double topRightY, short[] levelDensity, int cellsPerObject, ITokenFactory tokenFactory, int frameSize,
            boolean isQuery) {
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.levelDensity = levelDensity;
        this.cellsPerObject = cellsPerObject;
        this.tokenFactory = tokenFactory;
        this.frameSize = frameSize;
        this.isQuery = isQuery;
    }

    @Override
    public IBinaryTokenizer createTokenizer() {
        return new SpatialCellBinaryTokenizer(bottomLeftX, bottomLeftY, topRightX, topRightY, levelDensity,
                cellsPerObject, tokenFactory, frameSize, isQuery);
    }
}
