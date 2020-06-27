package org.sheinbergon.corrosion

import com.sun.jna.Platform
import org.sheinbergon.corrosion.Corrosion.AffinityResolver
import org.sheinbergon.corrosion.jna.linux.LinuxAffinityResolver
import org.sheinbergon.corrosion.jna.win32.Win32AffinityResolver
import java.lang.reflect.Field
import java.lang.reflect.Modifier

const val `1L` = 1L
const val `2L` = 2L
const val `1` = 1
const val `0` = 0
const val `-1` = -1

val binaryTestMask by lazy {
    if (CoreSet.AVAILABLE_CORES > `1L`) CoreSet.MASK_UPPER_BOUND - `2L`
    else `1L`
}

val negatedTestMask by lazy {
    binaryTestMask.xor(CoreSet.MASK_UPPER_BOUND - `1L`)
}

val textTestMask by lazy {
    binaryTestMaskRanges.map {
        with(it) {
            if (first == last) "$start"
            else "$first${CoreSet.RANGE_DELIMITER}$last"
        }
    }.joinToString(separator = CoreSet.SPECIFICATION_DELIMITER)
}

private val binaryTestMaskRanges: List<IntRange> by lazy {
    var start = `-1`
    var binaryMask = binaryTestMask
    mutableListOf<IntRange>().apply {
        for (index in `0` until CoreSet.AVAILABLE_CORES) {
            if (binaryMask.and(`1L`) == `1L`) {
                if (start == `-1`) {
                    start = index
                } else if (index + `1` == CoreSet.AVAILABLE_CORES) {
                    add(start..index)
                }
            } else if (start != `-1`) {
                add(start until index)
                start = `-1`
            }
            binaryMask = binaryMask.shr(`1`)
        }
    }
}

fun unsupportedPlatform(action: () -> Unit) {
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