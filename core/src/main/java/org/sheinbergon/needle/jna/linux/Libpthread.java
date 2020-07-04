package org.sheinbergon.needle.jna.linux;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sheinbergon.needle.jna.linux.structure.CpuSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Libpthread {

    private final static String LIBRARY = "pthread";

    static {
        // Only register the SO on Linux based platforms
        if (Platform.isLinux()) {
            Native.register(LIBRARY);
        }
    }

    static native long pthread_self();

    static native int pthread_getaffinity_np(long __th, int __cpusetsize, CpuSet __cpuset);

    static native int pthread_setaffinity_np(long __th, int __cpusetsize, CpuSet __cpuset);
}