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

import java.util.BitSet;

import edu.uci.ics.asterix.dataflow.data.nontagged.Coordinate;
import edu.uci.ics.asterix.dataflow.data.nontagged.comparators.HilbertCurve;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ADoubleSerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.APointSerializerDeserializer;
import edu.uci.ics.asterix.dataflow.data.nontagged.serde.ARectangleSerializerDeserializer;
import edu.uci.ics.asterix.om.types.ATypeTag;
import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.storage.am.common.api.IBinaryTokenizer;
import edu.uci.ics.hyracks.storage.am.common.api.IToken;
import edu.uci.ics.hyracks.storage.am.common.api.ITokenFactory;

public abstract class SpatialCellBinaryTokenizer implements IBinaryTokenizer {

    protected final static int MAX_LEVEL = 4; //Only 4-level grids are supported.
    protected final double bottomLeftX;
    protected final double bottomLeftY;
    protected final double topRightX;
    protected final double topRightY;
    protected final boolean isQuery;
    protected final int[][][] hilbertValueMatrix;
    protected final int[] axisCellNum;
    protected final int cellsPerObject;
    protected final double[] xCellSize;
    protected final double[] yCellSize;
    protected final int levelCount;
    protected final int tokenSize;

    protected final IToken token;
    protected final int[][] cellId;
    protected final byte[][] hilbertValue;
    protected int hilbertValueCount;
    protected int hOffset;
    protected final int[][][] candidateCellId;
    protected int tcOffset; //tail offset of candidateCellId
    protected int hcOffset; //head offset of candidateCellId
    protected final double[] cellBottomLeft;
    protected byte[] inputData;
    protected static byte[] OOPS_BYTE_ARRAY = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            MAX_LEVEL + 1 };
    protected static byte[] ALL_BYTE_ARRAY = new byte[] { 0, 0, 0, 0, 0 };
    protected boolean overflow;
    protected int nextCount;
    protected final BitSet highkeyFlag;
    protected final InMemorySpatialCellIdQuickSorter cellIdSorter;

    //temporary variables
    protected final double[] regionCoordinate = new double[4];
    protected final double[] cellCoordinate = new double[4];
    protected final int[] nextLevelOffset;
    protected final int[] tCellId1, tCellId2;

    public SpatialCellBinaryTokenizer(double bottomLeftX, double bottomLeftY, double topRightX, double topRightY,
            short[] levelDensity, int cellsPerObject, ITokenFactory tokenFactory, int frameSize, boolean isQuery) {
        assert levelDensity.length == MAX_LEVEL; //Only 4-level grids are supported.

        this.levelCount = levelDensity.length;
        this.tokenSize = levelCount + 1; // +1 for level indicator
        this.cellIdSorter = new InMemorySpatialCellIdQuickSorter(tokenSize);
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        int maxCellsPerObjectInFrame = frameSize / tokenSize;
        if (maxCellsPerObjectInFrame >= cellsPerObject)
            this.cellsPerObject = cellsPerObject;
        else
            this.cellsPerObject = maxCellsPerObjectInFrame;
        this.isQuery = isQuery;
        this.hilbertValueMatrix = new int[levelCount][][];
        this.axisCellNum = new int[levelCount];
        this.xCellSize = new double[levelCount]; //cell's x length
        this.yCellSize = new double[levelCount]; //cell's y length

        for (int i = 0; i < levelCount; i++) {
            switch (levelDensity[i]) {
                case HilbertCurve.DIMENSION2_ORDER2_CELL_NUM:
                    hilbertValueMatrix[i] = HilbertCurve.HILBERT_VALUE_DIMENSION2_ORDER2;
                    axisCellNum[i] = HilbertCurve.DIMENSION2_ORDER2_AXIS_CELL_NUM;
                    break;
                case HilbertCurve.DIMENSION2_ORDER3_CELL_NUM:
                    hilbertValueMatrix[i] = HilbertCurve.HILBERT_VALUE_DIMENSION2_ORDER3;
                    axisCellNum[i] = HilbertCurve.DIMENSION2_ORDER3_AXIS_CELL_NUM;
                    break;
                case HilbertCurve.DIMENSION2_ORDER4_CELL_NUM:
                    hilbertValueMatrix[i] = HilbertCurve.HILBERT_VALUE_DIMENSION2_ORDER4;
                    axisCellNum[i] = HilbertCurve.DIMENSION2_ORDER4_AXIS_CELL_NUM;
                    break;
                default:
                    break;
            }

            if (i == 0) {
                xCellSize[i] = (topRightX - bottomLeftX) / axisCellNum[i];
                yCellSize[i] = (topRightY - bottomLeftY) / axisCellNum[i];
            } else {
                xCellSize[i] = xCellSize[i - 1] / axisCellNum[i];
                yCellSize[i] = yCellSize[i - 1] / axisCellNum[i];
            }
        }

        this.cellId = new int[levelCount][2];
        this.candidateCellId = new int[cellsPerObject][levelCount][2];
        this.hilbertValue = new byte[cellsPerObject][tokenSize];
        this.highkeyFlag = new BitSet(cellsPerObject);
        this.cellBottomLeft = new double[2];
        token = tokenFactory.createToken();
        this.nextLevelOffset = new int[levelCount];
        this.tCellId1 = new int[2];
        this.tCellId2 = new int[2];
    }

    @Override
    public IToken getToken() {
        return token;
    }

    @Override
    public abstract boolean hasNext();

    @Override
    public abstract void next() throws HyracksDataException;

    //for debugging
    protected void printCellId(byte[] cId) {
        StringBuilder sb = new StringBuilder();
        sb.append("cellId: [");
        for (int i = 0; i < tokenSize; i++) {
            sb.append(cId[i]);
            if (i != tokenSize - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        System.out.println(sb.toString());
    }

    @Override
    public void reset(byte[] data, int start, int length) throws HyracksDataException {
        this.inputData = data;
        hilbertValueCount = 0;
        hOffset = 0;

        //check type tag
        if (inputData[start] == ATypeTag.POINT.serialize()) {
            double x = ADoubleSerializerDeserializer.getDouble(inputData,
                    start + APointSerializerDeserializer.getCoordinateOffset(Coordinate.X));
            double y = ADoubleSerializerDeserializer.getDouble(inputData,
                    start + APointSerializerDeserializer.getCoordinateOffset(Coordinate.Y));

            //handle OOPS case.
            if (isOOPS(x, y)) {
                handleOOPS();
                return;
            }

            //set entire space coordinate as a parent cell coordinate for top level - level 0
            for (int i = 0; i < levelCount; i++) {
                if (i == 0) {
                    cellBottomLeft[0] = bottomLeftX;
                    cellBottomLeft[1] = bottomLeftY;
                }
                computeCellId(x, y, xCellSize[i], yCellSize[i], cellBottomLeft[0], cellBottomLeft[1], cellId[i]);
                computeCellBottomeLeft(xCellSize[i], yCellSize[i], cellBottomLeft[0], cellBottomLeft[1], cellId[i],
                        cellBottomLeft);
            }

            //convert the computed cellId into a Hilbert value
            convertCellId2HilbertValue(cellId, levelCount, hilbertValue[hilbertValueCount]);
            hilbertValueCount++;

        } else if (inputData[start] == ATypeTag.RECTANGLE.serialize()) {
            double x1 = ADoubleSerializerDeserializer.getDouble(inputData,
                    start + ARectangleSerializerDeserializer.getBottomLeftCoordinateOffset(Coordinate.X));
            double y1 = ADoubleSerializerDeserializer.getDouble(inputData,
                    start + ARectangleSerializerDeserializer.getBottomLeftCoordinateOffset(Coordinate.Y));
            double x2 = ADoubleSerializerDeserializer.getDouble(inputData,
                    start + ARectangleSerializerDeserializer.getUpperRightCoordinateOffset(Coordinate.X));
            double y2 = ADoubleSerializerDeserializer.getDouble(inputData,
                    start + ARectangleSerializerDeserializer.getUpperRightCoordinateOffset(Coordinate.Y));

            //initialize variables
            int parentLevel = -1;
            overflow = false;
            tcOffset = -1;
            hcOffset = 0;
            cellCoordinate[0] = bottomLeftX;
            cellCoordinate[1] = bottomLeftY;
            cellCoordinate[2] = topRightX;
            cellCoordinate[3] = topRightY;

            //handle OOPS case.
            if (isOOPS(x1, y1) || isOOPS(x2, y2)) {
                handleOOPS();
            }

            //get candidate cells intersected with the given rectangle region
            do {
                //get the region coordinate intersected with a cell
                computeRegionCoordinateIntersectedWithCell(x1, y1, x2, y2, cellCoordinate[0], cellCoordinate[1],
                        cellCoordinate[2], cellCoordinate[3], regionCoordinate);

                //if regionCoordinate and cellCoordinate are equal, put the cell to the output list, i.e., hilbertValue.
                //special case: 
                //if the user defined space is contained in the given query rectangle, i.e., parentLevel == -1,
                //we still decompose the rectangle to handle it at level 0. The special case is also considered in *promote* optimization
                if (regionCoordinate[0] == cellCoordinate[0] && regionCoordinate[1] == cellCoordinate[1]
                        && regionCoordinate[2] == cellCoordinate[2] && regionCoordinate[3] == cellCoordinate[3]) {
                    if (parentLevel == -1) {
                        handleALL();
                    } else {
                        convertCellId2HilbertValue(candidateCellId[tcOffset], parentLevel + 1,
                                hilbertValue[hilbertValueCount]);
                        hilbertValueCount++;
                    }
                }
                //otherwise, decompose the region at lower level.
                else {
                    //get intersected children cells of a cell with a given region
                    computeChildrenCellsIntersectedWithRegion(regionCoordinate[0], regionCoordinate[1],
                            regionCoordinate[2], regionCoordinate[3], cellCoordinate[0], cellCoordinate[1], tcOffset,
                            parentLevel + 1);
                    if (overflow) {
                        break;
                    }
                }

                //update recurring variables - tcOffset, parentLevel, and nextLevelOffset
                ++tcOffset;
                if (tcOffset == candidateCellId.length) {
                    tcOffset = 0;
                }
                if (parentLevel == -1 || nextLevelOffset[parentLevel] == tcOffset) {
                    nextLevelOffset[++parentLevel] = hcOffset;
                }

                //stop decomposition if the parentLevel reached levelCount-1
                if (parentLevel == levelCount - 1) {
                    break;
                }

                //get a next cell from candidateCellIds and compute the cell's coordinate
                computeCellCoordinate(candidateCellId[tcOffset], parentLevel + 1, cellCoordinate);

            } while (tcOffset != hcOffset);

            //put the rest of the candidate cells into output list
            while (tcOffset != hcOffset) {
                convertCellId2HilbertValue(candidateCellId[tcOffset], parentLevel + 1, hilbertValue[hilbertValueCount]);
                hilbertValueCount++;
                tcOffset++;
                if (tcOffset == candidateCellId.length) {
                    tcOffset = 0;
                }
                if (parentLevel == -1 || (nextLevelOffset[parentLevel] == tcOffset && parentLevel != (levelCount - 1))) {
                    nextLevelOffset[++parentLevel] = hcOffset;
                }
            }

            //sort the hilbertValue array
            cellIdSorter.quicksort(hilbertValue, hilbertValueCount);

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
        } else {
            throw new HyracksDataException("SpatialCellBinaryTokenizer: unsupported type tag: " + inputData[start]);
        }
    }

    private boolean mergeCellIds() {
        boolean merged = false;
        int lowkey = 0;
        int highkey = 0;
        int head = 1;
        //forward head and highkey if the lowkey is in a range
        if (highkeyFlag.get(lowkey)) {
            ++highkey;
            ++head;
        }
        while (head < hilbertValueCount) {
            if (isMergable(hilbertValue[head], hilbertValue[highkey])) {
                if (lowkey == highkey) {
                    highkey = lowkey + 1;
                    highkeyFlag.set(lowkey);
                }
                if (highkeyFlag.get(head)) { /* head is in a range */
                    highkeyFlag.set(head, false);
                    System.arraycopy(hilbertValue[++head], 0, hilbertValue[highkey], 0, tokenSize);
                } else {
                    System.arraycopy(hilbertValue[head], 0, hilbertValue[highkey], 0, tokenSize);
                }
                merged = true;
            } else {
                ++highkey;
                lowkey = highkey;
                if (head != lowkey) {
                    System.arraycopy(hilbertValue[head], 0, hilbertValue[lowkey], 0, tokenSize);
                    if (highkeyFlag.get(head)) { /* head is in a range */
                        highkeyFlag.set(lowkey);
                        highkeyFlag.set(head, false);
                        System.arraycopy(hilbertValue[++head], 0, hilbertValue[++highkey], 0, tokenSize);
                    }
                } else {
                    if (highkeyFlag.get(head)) { /* head is in a range */
                        ++head;
                        ++highkey;
                    }
                }
            }
            ++head;
        }
        hilbertValueCount = highkey + 1;
        return merged;
    }

    private boolean isMergable(byte[] head, byte[] highkey) {
        int maxValidLevel = head[MAX_LEVEL] - 1;
        if (maxValidLevel < 0 /* entire space case */|| highkey[MAX_LEVEL] - 1 != maxValidLevel)
            return false;
        for (int i = 0; i < maxValidLevel; i++) {
            if (head[i] != highkey[i])
                return false;
        }

        if ((0xff & head[maxValidLevel]) - (0xff & highkey[maxValidLevel]) != 1)
            return false;

        return true;
    }

    private boolean promoteCellIds() {
        boolean promoted = false;
        int lowkey = 0;
        int tail = 0;

        while (lowkey < hilbertValueCount) {
            if (highkeyFlag.get(lowkey)) { /* range */
                if (isPromotableRange(lowkey)) {
                    promoteRange(lowkey, tail);

                    //flip the highkeyFlag
                    highkeyFlag.set(lowkey, false);

                    lowkey += 2;
                    ++tail;
                    promoted = true;
                } else {
                    if (lowkey != tail) {
                        System.arraycopy(hilbertValue[lowkey], 0, hilbertValue[tail], 0, tokenSize);
                        System.arraycopy(hilbertValue[lowkey + 1], 0, hilbertValue[tail + 1], 0, tokenSize);
                    }

                    //flip the src and dest highkeyFlag
                    highkeyFlag.set(lowkey, false);
                    highkeyFlag.set(tail);

                    lowkey += 2;
                    tail += 2;
                }
            } else { /* non-range */
                if (lowkey != tail) {
                    System.arraycopy(hilbertValue[lowkey], 0, hilbertValue[tail], 0, tokenSize);
                }
                ++lowkey;
                ++tail;
            }
        }

        hilbertValueCount = tail;

        return promoted;
    }

    private boolean isPromotableRange(int lowkey) {
        //Promotion examples:
        //There are 4 levels and each level has 2x2 cells.
        //Range(00003, 00303) is promoted into  00002.
        //Range(01003, 01303) is promoted into  01002.

        int validLevelNum = (0xff & (hilbertValue[lowkey][MAX_LEVEL])) - 1;
        int cellCount = axisCellNum[validLevelNum] * axisCellNum[validLevelNum];
        int highkeyCellNum = (0xff & (hilbertValue[lowkey + 1][validLevelNum]));
        int lowkeyCellNum = (0xff & (hilbertValue[lowkey][validLevelNum]));
        if (highkeyCellNum - lowkeyCellNum + 1 == cellCount) {
            return true;
        }
        return false;
    }

    private void promoteRange(int lowkey, int dest) {
        //Promotion examples:
        //There are 4 levels and each level has 2x2 cells.
        //Range(00003, 00303) is promoted into  00002.
        //Range(01003, 01303) is promoted into  01002.

        int newValidLevelCount = (0xff & (hilbertValue[lowkey][MAX_LEVEL])) - 1;
        if (lowkey != dest) {
            for (int i = 0; i < MAX_LEVEL; i++) {
                hilbertValue[dest][i] = hilbertValue[lowkey][i];
            }
        }
        hilbertValue[dest][MAX_LEVEL] = (byte) newValidLevelCount;
    }

    private void computeRegionCoordinateIntersectedWithCell(double rx1, double ry1, double rx2, double ry2, double cx1,
            double cy1, double cx2, double cy2, double[] rCoordinate) {
        rCoordinate[0] = rx1 < cx1 ? cx1 : rx1;
        rCoordinate[1] = ry1 < cy1 ? cy1 : ry1;
        rCoordinate[2] = rx2 > cx2 ? cx2 : rx2;
        rCoordinate[3] = ry2 > cy2 ? cy2 : ry2;
    }

    private void computeChildrenCellsIntersectedWithRegion(double rx1, double ry1, double rx2, double ry2,
            double pcx1 /*parent cell*/, double pcy1 /*parent cell*/, int pcId /*parentCellId*/, int curLevel) {
        int x, y, l;
        computeCellId(rx1, ry1, xCellSize[curLevel], yCellSize[curLevel], pcx1, pcy1, tCellId1);
        computeCellId(rx2, ry2, xCellSize[curLevel], yCellSize[curLevel], pcx1, pcy1, tCellId2);

        //handle a boundary case where region's TopRight point is on cell boundary line(s).
        //logic:
        //if (cellId != 0 && (region's BottomLeft point + cellId * cellSize == region's TopRight point))
        //then --cellId
        if (tCellId2[0] != 0 && (pcx1 + tCellId2[0] * xCellSize[curLevel] == rx2)) {
            --tCellId2[0];
        }
        if (tCellId2[1] != 0 && (pcy1 + tCellId2[1] * yCellSize[curLevel] == ry2)) {
            --tCellId2[1];
        }

        //check overflow
        int cellCount = (tCellId2[0] - tCellId1[0] + 1) * (tCellId2[1] - tCellId1[1] + 1);
        overflow = willOverflow(cellCount);
        if (overflow) {
            return;
        }

        //set computed cell Ids.
        for (y = tCellId1[1]; y <= tCellId2[1]; y++) {
            for (x = tCellId1[0]; x <= tCellId2[0]; x++) {
                //set upper level cell ids using the given parent cell.
                for (l = 0; l < curLevel; l++) {
                    candidateCellId[hcOffset][l][0] = candidateCellId[pcId][l][0];
                    candidateCellId[hcOffset][l][1] = candidateCellId[pcId][l][1];
                }
                candidateCellId[hcOffset][curLevel][0] = x;
                candidateCellId[hcOffset][curLevel][1] = y;
                if (++hcOffset == candidateCellId.length) {
                    hcOffset = 0;
                }
            }
        }
    }

    private void computeCellCoordinate(int[][] cId, int level, double[] cCoordinate) {
        cCoordinate[0] = bottomLeftX;
        cCoordinate[1] = bottomLeftY;
        for (int i = 0; i < level; i++) {
            cCoordinate[0] += cId[i][0] * xCellSize[i];
            cCoordinate[1] += cId[i][1] * yCellSize[i];
        }
        cCoordinate[2] = cCoordinate[0] + xCellSize[level - 1];
        cCoordinate[3] = cCoordinate[1] + yCellSize[level - 1];
    }

    private void convertCellId2HilbertValue(int[][] cId, int level, byte[] hVal) {
        for (int i = 0; i < level; i++) {
            hVal[i] = (byte) hilbertValueMatrix[i][cId[i][1]][cId[i][0]];
        }
        hVal[levelCount] = (byte) (level); //top level = level 0
    }

    private void handleOOPS() {
        for (int i = 0; i <= levelCount; i++) {
            hilbertValue[hilbertValueCount][i] = OOPS_BYTE_ARRAY[i];
        }
        ++hilbertValueCount;
    }

    private void handleALL() {
        for (int i = 0; i <= levelCount; i++) {
            hilbertValue[hilbertValueCount][i] = ALL_BYTE_ARRAY[i];
        }
        ++hilbertValueCount;
    }

    private void computeCellBottomeLeft(double xCellSize, double yCellSize, double bottomLeftX, double bottomLeftY,
            int[] cellId, double cellBottomLeft[]) {
        cellBottomLeft[0] = bottomLeftX + cellId[0] * xCellSize;
        cellBottomLeft[1] = bottomLeftY + cellId[1] * yCellSize;
    }

    private void computeCellId(double x, double y, double xCellSize, double yCellSize, double bottomLeftX,
            double bottomLeftY, int[] cellId) {
        cellId[0] = (int) Math.floor((x - bottomLeftX) / xCellSize);
        cellId[1] = (int) Math.floor((y - bottomLeftY) / yCellSize);
    }

    private boolean isOOPS(double x, double y) { //out of point space
        return !(x >= bottomLeftX && x < topRightX && y >= bottomLeftY && y < topRightY);
    }

    private boolean willOverflow(int increase) {
        if (hcOffset > tcOffset) {
            return cellsPerObject < hilbertValueCount + (hcOffset - tcOffset) + 1 + increase;
        } else {
            return cellsPerObject < hilbertValueCount + candidateCellId.length - tcOffset + 1 + hcOffset + increase;
        }
    }

}
