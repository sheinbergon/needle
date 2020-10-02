package org.sheinbergon.needle

import com.sun.jna.Platform
import org.sheinbergon.needle.jna.linux.LinuxAffinityResolver
import org.sheinbergon.needle.jna.win32.Win32AffinityResolver
import java.lang.reflect.Field
import java.lang.reflect.Modifier

const val `2L` = 2L
const val `1L` = 1L
const val `2` = 2
const val `1` = 1
const val `0` = 0
const val `-1` = -1
const val NEEDLE = "needle"

val availableCores = AffinityDescriptor.AVAILABLE_CORES

val maskUpperBound = AffinityDescriptor.MASK_UPPER_BOUND

val default = Needle.affinity()

val binaryTestMask by lazy {
  if (availableCores > `1`) maskUpperBound - `2L`
  else `1L`
}

val firstCoreAffinityDescriptor = AffinityDescriptor.from(`1L`)

val testAffinityDescriptor = AffinityDescriptor.from(binaryTestMask)

val negatedBinaryTestMask by lazy {
  binaryTestMask.xor(maskUpperBound - `1L`)
}

val negatedTestAffinityDescriptor = AffinityDescriptor.from(negatedBinaryTestMask)

val textTestMask by lazy {
  binaryMaskRanges(binaryTestMask).textMask()
}

val negatedTextTestMask by lazy {
  binaryMaskRanges(negatedBinaryTestMask).textMask()
}

private fun List<IntRange>.textMask() = this.map {
  with(it) {
    if (first == last) "$start"
    else "$first${AffinityDescriptor.RANGE_DELIMITER}$last"
  }
}.joinToString(separator = AffinityDescriptor.SPECIFICATION_DELIMITER)

private fun binaryMaskRanges(mask: Long): List<IntRange> {
  var start = `-1`
  var binaryMask = mask
  return mutableListOf<IntRange>().apply {
    for (index in `0` until availableCores) {
      if (binaryMask.and(`1L`) == `1L`) {
        if (start == `-1`) {
          start = index
        }
        if (index + `1` == availableCores) {
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
  val affinityResolver = Needle::class.java.getDeclaredField("affinityResolver")
  val modifiers = Field::class.java.getDeclaredField("modifiers")
  affinityResolver.trySetAccessible()
  modifiers.trySetAccessible()
  modifiers.setInt(affinityResolver, affinityResolver.getModifiers() and Modifier.FINAL.inv())
  affinityResolver.set(null, resolver)
}
