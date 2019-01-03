package org.deep.rogs.model;


import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {
    private long totalSize;

    @Override
    public void write(int b) {
        ++totalSize;
    }

    @Override
    public void write(byte[] b) {
        totalSize += b.length;
    }

    @Override
    public void write(byte[] b, int offset, int len) {

        totalSize += len;
    }

    public long getTotalSize() {
        return totalSize;
    }
}

