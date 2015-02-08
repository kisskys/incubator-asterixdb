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

import java.io.UnsupportedEncodingException;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.storage.am.common.api.IToken;
import edu.uci.ics.hyracks.storage.am.common.api.ITokenFactory;

public class MultiLevelSIFBinaryTokenizer extends SpatialCellBinaryTokenizer {

    private int curLevel;
    private int rangeOffset;
    private int rangeSize;
    private int rangeLevelNum;
    private final StringBuilder sb;
    //temporary variables
    private final byte[] tHilbertValue; //a pair of cell Ids for range search 

    public MultiLevelSIFBinaryTokenizer(double bottomLeftX, double bottomLeftY, double topRightX, double topRightY,
            short[] levelDensity, int cellsPerObject, ITokenFactory tokenFactory, int frameSize, boolean isQuery) {
        super(bottomLeftX, bottomLeftY, topRightX, topRightY, levelDensity, cellsPerObject, tokenFactory, frameSize,
                isQuery);
        this.tHilbertValue = new byte[tokenSize];
        this.sb = new StringBuilder();
    }

    @Override
    public IToken getToken() {
        return token;
    }

    @Override
    public boolean hasNext() {
        return hOffset < hilbertValueCount;
    }

    @Override
    public void next() throws HyracksDataException {
        //reset token
        if (isQuery) {
            if (highkeyFlag.get(hOffset)) {
                if (rangeOffset == 0 && curLevel == 0) {
                    rangeLevelNum = hilbertValue[hOffset][MAX_LEVEL] - 1;
                    rangeSize = ((hilbertValue[hOffset + 1][rangeLevelNum]) & 0xff)
                            - ((hilbertValue[hOffset][rangeLevelNum]) & 0xff) + 1;
                }
            } else {
                if (rangeOffset == 0 && curLevel == 0) {
                    rangeSize = 1;
                }
            }

            if (rangeOffset < rangeSize) {
                System.arraycopy(hilbertValue[hOffset], 0, tHilbertValue, 0, tokenSize);
                tHilbertValue[rangeLevelNum] = (byte) (((hilbertValue[hOffset][rangeLevelNum]) & 0xff) + rangeOffset);
                resetToken();
                ++rangeOffset;
            }

            if (rangeOffset == rangeSize) {
                rangeOffset = 0;
                if (rangeSize == 1) {
                    ++hOffset;
                } else {
                    hOffset += 2;
                }
            }
        } else {
            //for a point object, generate a cell Id for each level of grids  
            //TODO deal with non-point spatial object
            int i = 0;
            for (; i <= curLevel; i++) {
                tHilbertValue[i] = hilbertValue[hOffset][i];
            }
            for (; i < MAX_LEVEL; i++) {
                tHilbertValue[i] = 0;
            }
            tHilbertValue[MAX_LEVEL] = (byte) (curLevel + 1);

            resetToken();
            ++curLevel;

            if (curLevel == MAX_LEVEL) {
                ++hOffset;
                curLevel = 0;
            }
        }
    }

    private void resetToken() throws HyracksDataException {
        sb.setLength(0);
        for (int i = 0; i < tokenSize; i++) {
            sb.append(tHilbertValue[i] & 0xff);
            if (i != tokenSize - 1) {
                sb.append(".");
            }
        }
        String strCellId = sb.toString();
        byte[] bytearr;
        try {
            bytearr = strCellId.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new HyracksDataException(e);
        }
        token.reset(bytearr, 0, bytearr.length, strCellId.length(), 1);
    }

    @Override
    public void reset(byte[] data, int start, int length) throws HyracksDataException {
        super.reset(data, start, length);
        curLevel = 0;
        rangeOffset = 0;
    }
}
