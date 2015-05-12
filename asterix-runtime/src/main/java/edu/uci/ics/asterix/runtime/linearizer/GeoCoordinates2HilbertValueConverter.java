package edu.uci.ics.asterix.runtime.linearizer;

import edu.uci.ics.asterix.dataflow.data.nontagged.comparators.HilbertValueInt64Converter;
import edu.uci.ics.hyracks.storage.am.common.api.IHilbertValueComputer;

/* 
 * This class converts a given point represented as x/y coordinates into a 64-bit Hilbert value. 
 * The computation follows the following steps:
 * 1. check whether a given point is in geo-space(-180.0 ~ 180.0 for x and -90.0 ~ 90.0 for y). 
 *    If not, adjust the point to be in the space.
 * 2. convert the point into integer x/y coordinates where both x and y double values are mapped to integer values lies in from 0 to 2^31.
 *    where ,for example, a distance between 1 and 2 along x-axis represents 2cm approximately in geo-space.
 * 3. convert the integer x/y coordinates into a Hilbert value using HilbertValueInt64Converter.
 * 
 * done!  
 */
public class GeoCoordinates2HilbertValueConverter implements IHilbertValueComputer {

    private final static int HILBERT_SPACE_ORDER = 31;
    private static final int MAX_COORDINATE = 180;
    private static final long COORDINATE_EXTENSION_FACTOR = (long) (((double) (1L << HILBERT_SPACE_ORDER)) / (2 * MAX_COORDINATE));
    private final HilbertValueInt64Converter hilbertConverter = new HilbertValueInt64Converter(2);

    @Override
    public long computeInt64HilbertValue(double x, double y) {
        //1. check whether a given point is in geo-space(-180.0 ~ 180.0 for x and -90.0 ~ 90.0 for y).
        //   If not, adjust the point to be in the space.
        if (x < -180.0) {
            x = -180.0;
        } else if (x >= 180.0) {
            x = 179.0;
        }
        if (y < -90.0) {
            y = -90.0;
        } else if (y >= 90.0) {
            y = 89.0;
        }

        //2. convert the point into integer x/y coordinates where both x and y double values are mapped to integer values lies in from 0 to 2^31.
        //   where ,for example, a distance between 1 and 2 along x-axis represents 2cm approximately in geo-space.
        long lx = getLongCoordinate(x);
        long ly = getLongCoordinate(y);

        //3. convert the integer x/y coordinates into a Hilbert value using HilbertValueInt64Converter.
        long hilbertValue = hilbertConverter.convert(HILBERT_SPACE_ORDER, lx, ly);

        return hilbertValue;
    }

    private long getLongCoordinate(double c) {
        return ((long) ((c + (double) MAX_COORDINATE) * (double) COORDINATE_EXTENSION_FACTOR));
    }

}
