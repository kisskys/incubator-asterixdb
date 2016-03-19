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

import java.io.UnsupportedEncodingException;

import org.apache.asterix.om.types.ATypeTag;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.storage.am.common.api.IToken;
import org.apache.hyracks.storage.am.common.api.ITokenFactory;

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
                if (hilbertValue[hOffset][MAX_LEVEL] == 0 && rangeOffset == 0) {
                    //special case: the query region covers a whole space 
                    //instead of inserting all points into the level 0 for this special case, 
                    //a level-0 single cell is divided into all cells in level 1.
                    rangeLevelNum = 0;
                    rangeSize = axisCellNum[0] * axisCellNum[0];
                    hilbertValue[hOffset][MAX_LEVEL] = 1;
                } else if (rangeOffset == 0 && curLevel == 0) {
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
        if (DEBUG) {
            System.out.println(cellId2String(tHilbertValue));
        }
    }

    @Override
    public void reset(byte[] data, int start, int length) throws HyracksDataException {
        highkeyFlag.clear();
        generateSortedCellIds(data, start, length);

        if (inputData[start] == ATypeTag.RECTANGLE.serialize()) {
            //merge cellIds into a range
            boolean merged = mergeCellIds();
            //promote cellIds into a cellId in an upper level
            boolean promoted = promoteCellIds();
            //repeat until there is no further optimization
            while (merged || promoted) {
                if (promoted) {
                    merged = mergeCellIds();
                } else {
                    merged = false;
                }
                if (merged) {
                    promoted = promoteCellIds();
                } else {
                    promoted = false;
                }
            }
        }

        curLevel = 0;
        rangeOffset = 0;
    }

    @Override
    public short getTokensCount() {
        return 0;
    }

    protected boolean isMergableForQuery(byte[] head, byte[] highkey) {
        return isMergableForNonQuery(head, highkey);
    }
}
