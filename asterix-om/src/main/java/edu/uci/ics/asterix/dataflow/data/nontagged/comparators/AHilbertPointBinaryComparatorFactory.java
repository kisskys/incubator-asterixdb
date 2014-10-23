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
package edu.uci.ics.asterix.dataflow.data.nontagged.comparators;

import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparator;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryComparatorFactory;

/*
 * This compares two points based on the hilbert curve. Currently, it only supports
 * doubles (this can be changed by changing all doubles to ints as there are no
 * number generics in Java) in the two-dimensional space. For more dimensions, the
 * state machine has to be automatically generated. The idea of the fractal generation
 * of the curve is described e.g. in http://dl.acm.org/ft_gateway.cfm?id=383528&type=pdf
 * 
 * Unlike the described approach, this comparator does not compute the hilbert value at 
 * any point. Instead, it only evaluates how the two inputs compare to each other. This
 * is done by starting at the lowest hilbert resolution and zooming in on the fractal until
 * the two points are in different quadrants.
 * 
 * As a performance optimization, the state of the state machine is saved in a stack and 
 * maintained over comparisons. The idea behind this is that comparisons are usually in a
 * similar area (e.g. geo coordinates). Zooming in from [-MAX_VALUE, MAX_VALUE] would take
 * ~300 steps every time. Instead, the comparator start from the previous state and zooms out
 * if necessary
 */
public class AHilbertPointBinaryComparatorFactory implements IBinaryComparatorFactory {

    private static final long serialVersionUID = 1L;
    public static final AHilbertPointBinaryComparatorFactory INSTANCE = new AHilbertPointBinaryComparatorFactory();

    private AHilbertPointBinaryComparatorFactory() {

    }
    
    @Override
    public IBinaryComparator createBinaryComparator() {
        return new AHilbertPointBinaryComparator();
    }

}
