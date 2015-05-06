package edu.uci.ics.asterix.runtime.linearizer;

import edu.uci.ics.asterix.dataflow.data.nontagged.comparators.HilbertValueInt64Converter;
import edu.uci.ics.hyracks.storage.am.common.api.IHilbertValueComputer;

/*
 * A grid covering entire earth is created where a cell in the grid occupies is mapped to a region 10cm x 10cm, approximately.
 * See http://en.wikipedia.org/wiki/Decimal_degrees for details about decimal degree and its corresponding length.
 * Considering the range of coordinates (-180.0 ~ 180.0 for x and -90.0 ~ 90.0 for y) of earth and the cell size, the grid can 
 * have 360 million cells and 180 million cells along the x/y-axis, respectively. But, to make the space conform
 * Hilbert space (which is a square and the cell count in an axis is a number generated from 2 to the power of a certain number, 
 * where the certain number is called, "order".), the cell count along x/y axis is set to 2^29 which is a least number 
 * which is greater or equal to 360 million. So, the grid becomes a Hilbert space whose order is 29 and a cell size is 10cm x 10cm.
 * With the grid, when coordinates of a point is given, the cell index along x/y axis are computed and then corresponding Hilbert 
 * value can be computed effectively using HilbertValueInt64Converter class.   
 * 
 * The computation follows the following steps:
 * 1. check whether a given point is in the coordinates range of earth. If not, adjust the point to be in the space.
 * 2. convert the point into long-value x/y cell indexes considering the cell size of the grid.
 * 3. convert the x/y cell indexes into a Hilbert value using HilbertValueInt64Converter.
 * done!  
 */
public class GeoCoordinates2HilbertValueConverter implements IHilbertValueComputer {

    private final static int HILBERT_SPACE_ORDER = 29;
    private final HilbertValueInt64Converter hilbertConverter = new HilbertValueInt64Converter(2);
    private static final long CONVERTED_LONG_VALUE_CELL_SIZE = 1000000L; //1000000L considering a cell size = 10cm x 10cm approximately.
    private static final long LONG_VALUE_GRID_BEGIN_COORDINATE_ADJUSTMENT = 180L * CONVERTED_LONG_VALUE_CELL_SIZE;

    @Override
    public long computeInt64HilbertValue(double x, double y) {
        //1. check whether a given point is in the coordinates range of earth. If not, adjust the point to be in the space.
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

        //2. convert the point into long-value x/y cell indexes considering the cell size of the grid.
        x = x * CONVERTED_LONG_VALUE_CELL_SIZE;
        y = y * CONVERTED_LONG_VALUE_CELL_SIZE;
        long lx = (long) x;
        long ly = (long) y;
        long xCellIndex = lx + LONG_VALUE_GRID_BEGIN_COORDINATE_ADJUSTMENT;
        long yCellIndex = ly + LONG_VALUE_GRID_BEGIN_COORDINATE_ADJUSTMENT;

        //3. convert the x/y cell indexes into a Hilbert value using HilbertValueInt64Converter.
        long hilbertValue = hilbertConverter.convert(HILBERT_SPACE_ORDER, xCellIndex, yCellIndex);

        return hilbertValue;
    }

}
