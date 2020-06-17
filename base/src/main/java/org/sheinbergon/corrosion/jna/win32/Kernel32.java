package org.sheinbergon.corrosion.jna.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.W32APIOptions;
import lombok.val;

public class Kernel32 {

    private final static String LIBRARY = "kernel32";

    static {
        // Only register Kernel32 DLL on Windows based platforms
        if (Platform.isWindows()) {
            val library = NativeLibrary.getInstance(LIBRARY, W32APIOptions.DEFAULT_OPTIONS);
            Native.register(library);
        }
    }

    static native WinNT.HANDLE GetCurrentProcess();

    static native WinNT.HANDLE GetCurrentThread();

    static native boolean GetProcessAffinityMask(
            WinNT.HANDLE hProcess,
            LongByReference lpProcessAffinityMask,
            LongByReference lpSystemAffinityMask);

    static native BaseTSD.DWORD_PTR SetThreadAffinityMask(
            WinNT.HANDLE hThread,
            BaseTSD.DWORD_PTR dwThreadAffinityMask);
}