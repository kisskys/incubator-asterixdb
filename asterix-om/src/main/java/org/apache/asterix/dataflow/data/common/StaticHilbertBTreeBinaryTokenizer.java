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

import org.apache.asterix.om.types.ATypeTag;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.storage.am.common.api.ITokenFactory;

public class StaticHilbertBTreeBinaryTokenizer extends SpatialCellBinaryTokenizer {

    private final byte[] tHilbertValue;
    private final long[] cellCountsInBottomLevel;

    //variables to support non-point object 
    private boolean isInputRectangleType;
    private int curLevel;
    private int rangeOffset;
    private int rangeSize;
    private int rangeLevelNum;

    public StaticHilbertBTreeBinaryTokenizer(double bottomLeftX, double bottomLeftY, double topRightX,
            double topRightY, short[] levelDensity, int cellsPerObject, ITokenFactory tokenFactory, int frameSize,
            boolean isQuery) {
        super(bottomLeftX, bottomLeftY, topRightX, topRightY, levelDensity, cellsPerObject, tokenFactory, frameSize,
                isQuery);
        this.tHilbertValue = new byte[tokenSize];
        this.cellCountsInBottomLevel = new long[MAX_LEVEL];
        for (int i = 0; i < MAX_LEVEL; i++) {
            cellCountsInBottomLevel[i] = 1;
            for (int j = 1; j < MAX_LEVEL - i; j++) {
                cellCountsInBottomLevel[i] *= axisCellNum[j] * axisCellNum[j];
            }
        }
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
                token.reset(tHilbertValue, 0, tokenSize, tokenSize, 1);
                hOffset++;
                nextCount = 0;
            }
        } else {
            if (isInputRectangleType) {
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
                token.reset(tHilbertValue, 0, tokenSize, tokenSize, 1);
            } else {
                token.reset(hilbertValue[hOffset++], 0, tokenSize, tokenSize, 1);
            }
        }
    }

    private void computeCellIdRange(byte[] cellId) {
        int i;
        int replaceStartLevel = 0xff & cellId[levelCount];
        for (i = 0; i < replaceStartLevel; i++) {
            tHilbertValue[i] = cellId[i];
        }
        for (; i < levelCount; i++) {
            tHilbertValue[i] = (byte) (axisCellNum[i] * axisCellNum[i] - 1);
        }

        if (cellId[levelCount] <= levelCount) { //this deal with OOPS case
            tHilbertValue[levelCount] = (byte) levelCount;
        }
    }

    @Override
    public void reset(byte[] data, int start, int length) throws HyracksDataException {
        highkeyFlag.clear();
        generateSortedCellIds(data, start, length);

        isInputRectangleType = inputData[start] == ATypeTag.RECTANGLE.serialize();
        if (isInputRectangleType) {
            if (isQuery) {
                mergeCellIds();
            } else {
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

                curLevel = 0;
                rangeOffset = 0;
            }
        }
    }

    protected boolean isMergableForQuery(byte[] head, byte[] highkey) {
        int maxValidLevel = head[MAX_LEVEL] - 1;
        if (maxValidLevel < 0 /* entire space case */|| maxValidLevel == MAX_LEVEL /* OOPS case */)
            return false;

        //consider highkey hilbert value as a cell Id in non-bottom level. 
        //That is, treat the non-bottom level cell as a range and find the high key of the range
        //the found high key is stored in tHilbertValue variable. 
        computeCellIdRange(highkey);

        //if (the high key hilbert value in tHilbertValue - head hilbertValue = 1), then return true;  
        if (getOrdinalInBottomLevel(head) - getOrdinalInBottomLevel(tHilbertValue) == 1)
            return true;
        else
            return false;
    }

    private long getOrdinalInBottomLevel(byte[] cId) {
        long v = 0;
        int maxValidLevel = cId[MAX_LEVEL];
        for (int i = 0; i < maxValidLevel; i++) {
            v += (cId[i] & 0xff) * cellCountsInBottomLevel[i];
        }
        return v;
    }

    @Override
    public short getTokensCount() {
        return 0;
    }
}
