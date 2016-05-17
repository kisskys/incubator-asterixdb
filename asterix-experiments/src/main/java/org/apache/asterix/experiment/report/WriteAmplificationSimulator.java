package org.apache.asterix.experiment.report;

public class WriteAmplificationSimulator {

    public static final long GB = 1024l * 1024l * 1024l;

    public static void main(String[] args) {

        //dhbtree pidx
        long maxMergableComponentSize = GB;
        int maxToleranceComponentCount = 5;
        long flushedComponentSize;
        long stopIndexSize;
        String[] indexNameList = new String[] { "dhbtree", "dhvbtree", "rtree", "shbtree", "sif", "pidx-dhbtree",
                "pidx-dhvbtree", "pidx-rtree", "pidx-shbtree", "pidx-sif" };
        int indexTypeCount = 5;
        int partitionCount = 32;
        String namePrefix;
        String name;
        WriteAmplificationCalculator wmc;
        int idx;

        long[] m1d3FlushedComponentSizeList = new long[] { 3502080l, 4363185l, 3738282l, 4675584l, 21346011l,
                66420736l, 65433600l, 70511386l, 65920477l, 50751522l };
        double[] m1d3StopIndexSizeList = new double[] { 15.57885742, 24.19519043, 17.62231445, 24.46606445,
                61.83081055, 337.8023682, 406.7111816, 370.0313721, 391.5908203, 154.8637695 };

        long[] m2d3FlushedComponentSizeList = new long[] { 6651904l, 8577024l, 6873088l, 8451413l, 41626659l,
                139288966l, 138115395l, 139254637l, 133355189l, 100652668l };
        double[] m2d3StopIndexSizeList = new double[] { 18.09814453, 27.52111816, 19.21984863, 28.21691895, 91.359375,
                392.5539551, 462.84375, 403.7957764, 451.8635254, 230.3604736 };

        StringBuilder sbResult = new StringBuilder();
        sbResult.append("name,read,write,indexSize,writeAmp,flush,merge\n");

        //m1d3
        namePrefix = "m1d3-";
        for (int i = 0; i < indexTypeCount; i++) {
            //pidx
            idx = i + indexTypeCount;
            name = namePrefix + indexNameList[idx];
            flushedComponentSize = m1d3FlushedComponentSizeList[idx];
            stopIndexSize = (long) m1d3StopIndexSizeList[idx] * GB / 32;
            wmc = new WriteAmplificationCalculator(name, maxMergableComponentSize, flushedComponentSize,
                    maxToleranceComponentCount, stopIndexSize);
            wmc.run();
            sbResult.append(name).append(",").append(wmc.getResult()).append("\n");

            //sidx
            idx = i;
            name = namePrefix + indexNameList[idx];
            flushedComponentSize = m1d3FlushedComponentSizeList[idx];
            stopIndexSize = (long) m1d3StopIndexSizeList[idx] * GB / 32;
            wmc = new WriteAmplificationCalculator(name, maxMergableComponentSize, flushedComponentSize,
                    maxToleranceComponentCount, stopIndexSize);
            wmc.run();
            sbResult.append(name).append(",").append(wmc.getResult()).append("\n");
        }

        //m2d3
        namePrefix = "m2d3-";
        for (int i = 0; i < indexTypeCount; i++) {
            //pidx
            idx = i + indexTypeCount;
            name = namePrefix + indexNameList[idx];
            flushedComponentSize = m2d3FlushedComponentSizeList[idx];
            stopIndexSize = (long) m2d3StopIndexSizeList[idx] * GB / 32;
            wmc = new WriteAmplificationCalculator(name, maxMergableComponentSize, flushedComponentSize,
                    maxToleranceComponentCount, stopIndexSize);
            wmc.run();
            sbResult.append(name).append(",").append(wmc.getResult()).append("\n");

            //sidx
            idx = i;
            name = namePrefix + indexNameList[idx];
            flushedComponentSize = m2d3FlushedComponentSizeList[idx];
            stopIndexSize = (long) m2d3StopIndexSizeList[idx] * GB / 32;
            wmc = new WriteAmplificationCalculator(name, maxMergableComponentSize, flushedComponentSize,
                    maxToleranceComponentCount, stopIndexSize);
            wmc.run();
            sbResult.append(name).append(",").append(wmc.getResult()).append("\n");
        }

        System.out.println(sbResult.toString());

        //        //m1d3 pidx
        //        flushedComponentSize = 66420736L;
        //        stopIndexSize = (long) 337.8023682d * 1024 * 1024 * 1024 / 32;
        //        WriteAmplificationCalculator m1d3PidxW = new WriteAmplificationCalculator("m1d3Pidx", maxMergableComponentSize,
        //                flushedComponentSize, maxToleranceComponentCount, stopIndexSize);
        //        m1d3PidxW.run();
        //
        //        //m1d3 sidx
        //        flushedComponentSize = 3502080L;
        //        stopIndexSize = (long) 15.57885742d * 1024 * 1024 * 1024 / 32;
        //        WriteAmplificationCalculator m1d3SidxW = new WriteAmplificationCalculator("m1d3Sidx", maxMergableComponentSize,
        //                flushedComponentSize, maxToleranceComponentCount, stopIndexSize);
        //        m1d3SidxW.run();
        //
        //        //m2d3 pidx
        //        flushedComponentSize = 143357021L;
        //        stopIndexSize = (long) 392.5539551d * 1024 * 1024 * 1024 / 32;
        //        WriteAmplificationCalculator m2d3PidxW = new WriteAmplificationCalculator("m2d3Pidx", maxMergableComponentSize,
        //                flushedComponentSize, maxToleranceComponentCount, stopIndexSize);
        //        m2d3PidxW.run();
        //
        //        //m2d3 sidx        
        //        flushedComponentSize = 6782976L;
        //        stopIndexSize = (long) 18.09814453d * 1024 * 1024 * 1024 / 32;
        //        WriteAmplificationCalculator m2d3SidxW = new WriteAmplificationCalculator("m2d3Sidx", maxMergableComponentSize,
        //                flushedComponentSize, maxToleranceComponentCount, stopIndexSize);
        //        m2d3SidxW.run();
    }
}
