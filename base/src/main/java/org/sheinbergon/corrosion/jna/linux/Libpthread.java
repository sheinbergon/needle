package org.sheinbergon.corrosion.jna.linux;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.sheinbergon.corrosion.jna.linux.structure.CpuSet;

public class Libpthread {

    private final static String LIBRARY = "pthread";

    static {
        // Only register libpthread on Linux based platforms
        if (Platform.isLinux()) {
            Native.register(LIBRARY);
        }
    }

    static native long pthread_self();

    static native int pthread_getaffinity_np(long __th, int __cpusetsize, CpuSet __cpuset);

    static native int pthread_setaffinity_np(long __th, int __cpusetsize, CpuSet __cpuset);
}