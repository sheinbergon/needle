package org.sheinbergon.needle.jna.linux;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.sheinbergon.needle.jna.linux.structure.CpuSet;

public final class Libc {

    /**
     * Libc SO name.
     */
    private static final String LIBRARY = "c";

    static {
        // Only register the SO on Linux based platforms
        if (Platform.isLinux()) {
            Native.register(LIBRARY);
        }
    }

    static native int getpid();

    static native int sched_getaffinity(int pid, int cpusetsize, CpuSet mask);

    private Libc() {
    }
}
