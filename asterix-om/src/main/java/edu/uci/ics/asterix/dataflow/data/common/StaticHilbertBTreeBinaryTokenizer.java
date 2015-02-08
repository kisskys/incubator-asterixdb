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

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.storage.am.common.api.ITokenFactory;

public class StaticHilbertBTreeBinaryTokenizer extends SpatialCellBinaryTokenizer {
    
    private final byte[] hilbertValuePair; //a pair of cell Ids for range search 

    public StaticHilbertBTreeBinaryTokenizer(double bottomLeftX, double bottomLeftY, double topRightX,
            double topRightY, short[] levelDensity, int cellsPerObject, ITokenFactory tokenFactory, int frameSize,
            boolean isQuery) {
        super(bottomLeftX, bottomLeftY, topRightX, topRightY, levelDensity, cellsPerObject, tokenFactory, frameSize,
                isQuery);
        this.hilbertValuePair = new byte[tokenSize];
    }

    @Override
    public boolean hasNext() {
        nextCount = 0;
        return hOffset < hilbertValueCount;
    }

    @Override
    public void next() throws HyracksDataException {
        //reset token
        if (isQuery) {
            if (nextCount == 0) {
                token.reset(hilbertValue[hOffset], 0, tokenSize, tokenSize, 1);
                nextCount = 1;
            } else {
                if (highkeyFlag.get(hOffset)) {
                    //provide a highkey
                    computeCellIdRange(hilbertValue[hOffset + 1]);
                    //flip the flag
                    highkeyFlag.set(hOffset++, false);
                } else {
                    //provide the lowkey as a highkey
                    computeCellIdRange(hilbertValue[hOffset]);
                }
                token.reset(hilbertValuePair, 0, tokenSize, tokenSize, 1);
                hOffset++;
                nextCount = 0;
            }
        } else {
            token.reset(hilbertValue[hOffset++], 0, tokenSize, tokenSize, 1);
        }
    }

    private void computeCellIdRange(byte[] cellId) {
        int i;
        int replaceStartLevel = 0xff & cellId[levelCount];
        for (i = 0; i < replaceStartLevel; i++) {
            hilbertValuePair[i] = cellId[i];
        }
        for (; i < levelCount; i++) {
            hilbertValuePair[i] = (byte) (axisCellNum[i] * axisCellNum[i] - 1);
        }

        if (cellId[levelCount] <= levelCount) { //this deal with OOPS case
            hilbertValuePair[levelCount] = (byte) levelCount;
        }
    }
}
