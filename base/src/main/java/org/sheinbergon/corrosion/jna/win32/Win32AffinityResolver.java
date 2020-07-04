package org.sheinbergon.corrosion.jna.win32;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.sheinbergon.corrosion.AffinityResolver;
import org.sheinbergon.corrosion.CoreSet;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Win32AffinityResolver extends AffinityResolver<WinNT.HANDLE> {

    public static final AffinityResolver<?> INSTANCE = new Win32AffinityResolver();

    private static final WinDef.BOOL TRUE = new WinDef.BOOL(true);
    private static final WinDef.DWORD ALL_ACCESS = new WinDef.DWORD(WinNT.THREAD_ALL_ACCESS);

    private static BaseTSD.DWORD_PTR processAffinity() {
        val processHandle = Kernel32.GetCurrentProcess();
        val systemAffinityMask = new LongByReference();
        val processAffinityMask = new LongByReference();
        Kernel32.GetProcessAffinityMask(processHandle, processAffinityMask, systemAffinityMask);
        return new BaseTSD.DWORD_PTR(processAffinityMask.getValue());
    }

    @Nonnull
    @Override
    public WinNT.HANDLE self() {
        val id = Kernel32.GetCurrentThreadId();
        return Kernel32.OpenThread(ALL_ACCESS, TRUE, id);
    }

    @Override
    public synchronized void thread(final @Nonnull WinNT.HANDLE handle, final @Nonnull CoreSet cores) {
        val mask = cores.mask();
        val pointer = new BaseTSD.DWORD_PTR(mask);
        Kernel32.SetThreadAffinityMask(handle, pointer);
    }

    @Nonnull
    @Override
    public synchronized CoreSet thread(final @Nonnull WinNT.HANDLE handle) {
        val current = Kernel32.SetThreadAffinityMask(handle, processAffinity());
        Kernel32.SetThreadAffinityMask(handle, current);
        return CoreSet.from(current.longValue());
    }

    @Nonnull
    @Override
    protected CoreSet process() {
        return CoreSet.from(processAffinity().longValue());
    }
}