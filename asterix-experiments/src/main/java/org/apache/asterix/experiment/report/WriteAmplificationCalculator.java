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

package org.apache.asterix.experiment.report;

import java.util.Stack;

public class WriteAmplificationCalculator {
    private final long maxMergableComponentSize;
    private final long flushedComponentSize;
    private final int maxToleranceComponentCount;
    private final long stopIndexSize;
    private final Stack<Long> diskComponents;
    private final Stack<Long> tempDiskComponents;
    private long totalReadSize;
    private long totalWriteSize;
    private long totalIndexSize;
    private int totalFlushCount;
    private int totalMergeCount;
    private final String name;

    public WriteAmplificationCalculator(String name, long maxMergableComponentSize, long flushedComponentSize,
            int maxToleranceComponentCount, long stopIndexSize) {
        this.name = name;
        this.maxMergableComponentSize = maxMergableComponentSize;
        this.flushedComponentSize = flushedComponentSize;
        this.maxToleranceComponentCount = maxToleranceComponentCount;
        this.stopIndexSize = stopIndexSize;
        this.diskComponents = new Stack<Long>();
        this.tempDiskComponents = new Stack<Long>();
        this.totalReadSize = 0;
        this.totalWriteSize = 0;
        this.totalFlushCount = 0;
        this.totalMergeCount = 0;
    }

    public void run() {
        long indexSize = 0;
        while (indexSize <= stopIndexSize) {
            flush(diskComponents, flushedComponentSize);
            indexSize += flushedComponentSize;
            ++totalFlushCount;
        }
        totalIndexSize = indexSize;
        printWriteAmp(totalReadSize, totalWriteSize, totalIndexSize);
        System.out.println("diskComp count:" + diskComponents.size());
    }

    private void printWriteAmp(long totalReadSize, long totalWriteSize, long indexSize) {
        double read = ((double) totalReadSize) / 1024 / 1024 / 1024;
        double write = ((double) totalWriteSize) / 1024 / 1024 / 1024;
        double index = ((double) indexSize) / 1024 / 1024 / 1024;
        System.out.println("-------- " + name + " --------");
        System.out.println("read: " + read + "GB, write: " + write + "GB, index size: " + index + "GB");
        System.out.println("write amplification: " + ((write) / index));
        System.out.println("flush: " + totalFlushCount + ", merge: " + totalMergeCount);
    }

    public double getWriteAmp() {
        double read = ((double) totalReadSize) / 1024 / 1024 / 1024;
        double write = ((double) totalWriteSize) / 1024 / 1024 / 1024;
        double index = ((double) totalIndexSize) / 1024 / 1024 / 1024;
        return ((read + write) / index);
    }

    public String getResult() {
        double read = ((double) totalReadSize) / 1024 / 1024 / 1024;
        double write = ((double) totalWriteSize) / 1024 / 1024 / 1024;
        double index = ((double) totalIndexSize) / 1024 / 1024 / 1024;
        double writeAmp = write / index;
        return "" + read + "," + write + "," + index + "," + writeAmp + "," + totalFlushCount + "," + totalMergeCount;
        //return "" + diskComponents.size();
    }

    private void flush(Stack<Long> diskComponents, long flushedComponentSize) {
        diskComponents.push(new Long(flushedComponentSize));
        totalWriteSize += flushedComponentSize;
        prettyPrint(diskComponents);
        if (mergeIfNeeded(diskComponents)) {
            prettyPrint(diskComponents);
        }
    }

    private void prettyPrint(Stack<Long> diskComponents) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diskComponents.size(); i++) {
            sb.append("" + (diskComponents.get(i) / 1024 / 1024) + ",");
        }
        //        System.out.println(sb.toString());
    }

    private boolean mergeIfNeeded(Stack<Long> diskComponents) {
        long totalSize = 0;
        int count = 0;
        Long size = null;
        boolean needMerge = false;
        while (!diskComponents.empty()) {
            size = diskComponents.pop();
            if (size > maxMergableComponentSize) {
                diskComponents.push(size);
                break;
            }
            tempDiskComponents.push(size);
            ++count;
            totalSize += size;
            if (count >= maxToleranceComponentCount || totalSize > maxMergableComponentSize) {
                needMerge = true;
                break;
            }
        }
        if (needMerge) {
            diskComponents.push(merge(tempDiskComponents));
        } else {
            while (!tempDiskComponents.empty()) {
                diskComponents.push(tempDiskComponents.pop());
            }
        }
        return needMerge;
    }

    private long merge(Stack<Long> componentsToBeMerged) {
        long totalSize = 0;
        while (!componentsToBeMerged.isEmpty()) {
            totalSize += componentsToBeMerged.pop();
        }
        totalReadSize += totalSize;
        totalWriteSize += totalSize;
        ++totalMergeCount;
        return totalSize;
    }
}
