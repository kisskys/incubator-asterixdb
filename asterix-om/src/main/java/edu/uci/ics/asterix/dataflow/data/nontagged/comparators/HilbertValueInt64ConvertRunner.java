package edu.uci.ics.asterix.dataflow.data.nontagged.comparators;

public class HilbertValueInt64ConvertRunner {

    public static void main(String[] args) {
        HilbertValueInt64Converter hvc = new HilbertValueInt64Converter(2);
        long[][] cell = generateMatrix(hvc, 2);
        printMatrix(cell);
        cell = generateMatrix(hvc, 3);
        printMatrix(cell);
        cell = generateMatrix(hvc, 4);
        printMatrix(cell);
    }

    private static long[][] generateMatrix(HilbertValueInt64Converter hvc, long order) {
        int cellNum = (int) Math.pow(2, order);
        long[][] cell = new long[cellNum][cellNum];
        for (int x = 0; x < cellNum; x++) {
            for (int y = 0; y < cellNum; y++) {
                cell[x][y] = hvc.convert(order, x, y);
            }
        }
        return cell;
    }
    
    private static void printMatrix(long[][] cell) {
        for (int y = 0; y < cell.length; y++) {
            System.out.print("{\t");
            for (int x = 0; x < cell[y].length; x++) {
                if (x == cell[y].length-1)
                    System.out.print(cell[x][y]);
                else
                    System.out.print(cell[x][y] + ",\t");
            }
            if (y == cell.length-1)
                System.out.println("\t}");
            else 
                System.out.println("\t},");
        }
    }
}
