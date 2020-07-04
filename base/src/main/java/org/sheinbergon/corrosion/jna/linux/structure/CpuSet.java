package org.sheinbergon.corrosion.jna.linux.structure;

import com.sun.jna.Structure;

import java.util.List;

public class CpuSet extends Structure {

    private final static int CPU_SETSIZE = 1024;
    private final static int NCPUBITS = Long.SIZE;
    public long[] __bits = new long[CPU_SETSIZE / NCPUBITS];

    public CpuSet() {
        setAlignType(ALIGN_NONE);
        setAutoSynch(true);
    }

    @Override
    protected List<String> getFieldOrder() {
        return List.of("__bits");
    }

    public int bytes() {
        return Long.BYTES * __bits.length;
    }

    public void zero() {
        this.clear();
    }
}