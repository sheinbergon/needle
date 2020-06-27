package org.sheinbergon.corrosion

import com.sun.jna.Platform
import org.sheinbergon.corrosion.Corrosion.AffinityResolver
import org.sheinbergon.corrosion.jna.linux.LinuxAffinityResolver
import org.sheinbergon.corrosion.jna.win32.Win32AffinityResolver
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal fun unsupportedPlatform(action: () -> Unit) {
    setPlatform(AffinityResolver.NoOp.INSTANCE)
    try {
        action()
    } finally {
        resetPlatform()
    }
}

private fun resetPlatform() {
    val resolver = if (Platform.isWindows()) {
        Win32AffinityResolver.INSTANCE
    } else if (Platform.isLinux()) {
        LinuxAffinityResolver.INSTANCE
    } else {
        AffinityResolver.NoOp.INSTANCE
    }
    setPlatform(resolver)
}

private fun setPlatform(resolver: AffinityResolver<*>) {
    val affinityResolver = Corrosion::class.java.getDeclaredField("affinityResolver")
    val modifiers = Field::class.java.getDeclaredField("modifiers")
    affinityResolver.trySetAccessible()
    modifiers.trySetAccessible()
    modifiers.setInt(affinityResolver, affinityResolver.getModifiers() and Modifier.FINAL.inv())
    affinityResolver.set(null, resolver)
}