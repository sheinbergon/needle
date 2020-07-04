package org.sheinbergon.needle.jna.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.W32APIOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Kernel32 {

    private final static String LIBRARY = "kernel32";

    static {
        // Only register DLL on Windows based platforms
        if (Platform.isWindows()) {
            val library = NativeLibrary.getInstance(LIBRARY, W32APIOptions.DEFAULT_OPTIONS);
            Native.register(library);
        }
    }

    static native WinNT.HANDLE GetCurrentProcess();

    static native WinDef.DWORD GetCurrentThreadId();

    static native WinNT.HANDLE OpenThread(
            WinDef.DWORD dwDesiredAccess,
            WinDef.BOOL bInheritHandle,
            WinDef.DWORD dwThreadId);

    static native boolean GetProcessAffinityMask(
            WinNT.HANDLE hProcess,
            LongByReference lpProcessAffinityMask,
            LongByReference lpSystemAffinityMask);

    static native BaseTSD.DWORD_PTR SetThreadAffinityMask(
            WinNT.HANDLE hThread,
            BaseTSD.DWORD_PTR dwThreadAffinityMask);
}