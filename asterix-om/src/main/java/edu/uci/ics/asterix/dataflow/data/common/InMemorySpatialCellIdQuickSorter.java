package edu.uci.ics.asterix.dataflow.data.common;

public class InMemorySpatialCellIdQuickSorter {
    
    private final int cellIdLen;
    
    public InMemorySpatialCellIdQuickSorter(int cellIdLen) {
        this.cellIdLen = cellIdLen;
    }
    
    private void swap(byte[][] a, int i, int j) {
        for (int k = 0; k < cellIdLen; k++) {
            byte temp = a[i][k];
            a[i][k] = a[j][k];
            a[j][k] = temp;
        }
    }
    
    private void quicksort(byte[][] list, int from, int to) {
        // If the indexes cross, then we've sorted the whole array.
        if (from >= to) {
            return;
        }
        
        // Choose a pivot value and then partition the array so that every value
        // less than the pivot is positioned before the pivot in the array and
        // every value greater than the pivot is positioned after the pivot in
        // the array.
        byte[] pivot = list[from];
        int i = from - 1;
        int j = to + 1;
        while (i < j) {
            // Keep incrementing from the start of the range so long as the
            // values are less than the pivot.
            i++;
            //while (list[i] < pivot) { i++; }
            while (compare(list[i], pivot) < 0) { i++; }
            // Keep decrementing from the end of the range so long as the values
            // are greater than the pivot.
            j--;
            //while (list[j] > pivot) { j--; }
            while (compare(list[j], pivot) > 0) { j--; }
            // So long at the indexes have not crossed, swap the pivot with the
            // value that was out of place.
            if (i < j) {
                swap(list, i, j);
            }
        }
        
        // Recursively sort the two portions of the array
        quicksort(list, from, j);
        quicksort(list, j + 1, to);
    }
    
    private int compare(byte[] cId1, byte[] cId2) {
        int diff;
        for (int i = 0; i < cellIdLen; i++) {
            diff = (0xff & cId1[i]) - (0xff & cId2[i]);
            if (diff != 0) return diff;
        }
        return 0;
    }
    
    // Helper method that kicks off the recursive quicksort method
    public void quicksort(byte [][] list, int len) {
        quicksort(list, 0, len-1);
    }

}
