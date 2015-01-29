package edu.uci.ics.asterix.dataflow.data.nontagged.comparators;

public class HilbertValueConvertRunner {

    public static void main(String[] args) {
        HilbertValueConverter hvc = new HilbertValueConverter(2);
        int[][] cell = generateMatrix(hvc, 2);
        printMatrix(cell);
        cell = generateMatrix(hvc, 3);
        printMatrix(cell);
        cell = generateMatrix(hvc, 4);
        printMatrix(cell);
    }

    private static int[][] generateMatrix(HilbertValueConverter hvc, int order) {
        int cellNum = (int) Math.pow(2, order);
        int[][] cell = new int[cellNum][cellNum];
        for (int x = 0; x < cellNum; x++) {
            for (int y = 0; y < cellNum; y++) {
                cell[x][y] = hvc.convert(order, x, y);
            }
        }
        return cell;
    }
    
    private static void printMatrix(int[][] cell) {
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
