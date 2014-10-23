/*
 * Copyright 2009-2013 by The Regents of the University of California
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

package edu.uci.ics.asterix.runtime.linearizer;

import edu.uci.ics.hyracks.api.exceptions.HyracksDataException;
import edu.uci.ics.hyracks.dataflow.common.data.accessors.ITupleReference;
import edu.uci.ics.hyracks.storage.am.btree.impls.RangePredicate;
import edu.uci.ics.hyracks.storage.am.common.api.ILinearizerSearchHelper;
import edu.uci.ics.hyracks.storage.am.common.api.ILinearizerSearchPredicate;
import edu.uci.ics.hyracks.storage.am.common.ophelpers.MultiComparator;
import edu.uci.ics.hyracks.storage.am.common.tuples.PermutingFrameTupleReference;

public class HilbertBTreeRangePredicate extends RangePredicate implements ILinearizerSearchPredicate{
    private static final long serialVersionUID = 1L;

//    private double qBottomLeftX;
//    private double qBottomLeftY;
//    private double qUpperRightX;
//    private double qUpperRightY;
    private PermutingFrameTupleReference queryRegion;

    public HilbertBTreeRangePredicate(ITupleReference lowKey, ITupleReference highKey, boolean lowKeyInclusive,
            boolean highKeyInclusive, MultiComparator lowKeyCmp, MultiComparator highKeyCmp,
            PermutingFrameTupleReference minFilterKey, PermutingFrameTupleReference maxFilterKey, PermutingFrameTupleReference queryRegion) {
        super(lowKey, highKey, lowKeyInclusive, highKeyInclusive, lowKeyCmp, highKeyCmp, minFilterKey, maxFilterKey);
        this.queryRegion = queryRegion;
    }

    @Override
    public ILinearizerSearchHelper getLinearizerSearchModifier() throws HyracksDataException {
        return new HilbertBTreeSearchHelper(queryRegion);
    }
//
//    public void prepareRegionQuery(double x1, double y1, double x2, double y2) {
//        qBottomLeftX = x1;
//        qBottomLeftY = y1;
//        qUpperRightX = x2;
//        qUpperRightY = y2;
//    }
//
//    public double getQueryBottomLeftX() {
//        return qBottomLeftX;
//    }
//
//    public double getQueryBottomLeftY() {
//        return qBottomLeftY;
//    }
//
//    public double getQueryUpperRightX() {
//        return qUpperRightX;
//    }
//
//    public double getQueryUpperRightY() {
//        return qUpperRightY;
//    }
}
