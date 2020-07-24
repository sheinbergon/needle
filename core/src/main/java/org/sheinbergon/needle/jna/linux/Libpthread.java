package org.sheinbergon.needle.jna.linux;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.sheinbergon.needle.jna.linux.structure.CpuSet;

public final class Libpthread {

    /**
     * Libpthread SO name.
     */
    private static final String LIBRARY = "pthread";

    static {
        // Only register the SO on Linux based platforms
        if (Platform.isLinux()) {
            Native.register(LIBRARY);
        }
    }

    static native long pthread_self();

    static native int pthread_getaffinity_np(long th, int cpusetsize, CpuSet cpuset);

    static native int pthread_setaffinity_np(long th, int cpusetsize, CpuSet cpuset);

    private Libpthread() {
    }
}
