package org.sheinbergon.needle.jna.linux;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sheinbergon.needle.jna.linux.structure.CpuSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Libc {

    private final static String LIBRARY = "c";

    static {
        // Only register the SO on Linux based platforms
        if (Platform.isLinux()) {
            Native.register(LIBRARY);
        }
    }

    static native int getpid();

    static native int sched_getaffinity(int pid, int cpusetsize, CpuSet mask);


}