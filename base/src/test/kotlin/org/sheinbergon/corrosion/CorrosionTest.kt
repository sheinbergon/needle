package org.sheinbergon.corrosion

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@RunWith(JUnitPlatform::class)
@ExtendWith(MockitoExtension::class)
class CorrosionTest {

    @Test
    fun `Unsupported platform behavior`() {
        val affinityResolver = Corrosion::class.java.getDeclaredField("affinityResolver")
        val modifiers = Field::class.java.getDeclaredField("modifiers")
        affinityResolver.trySetAccessible()
        modifiers.trySetAccessible()
        modifiers.setInt(affinityResolver, affinityResolver.getModifiers() and Modifier.FINAL.inv())
        affinityResolver.set(null, Corrosion.AffinityResolver.NoOp.INSTANCE);
        Corrosion.get() shouldBe CoreSet.EMPTY
        Corrosion.set(NumberUtils.LONG_ONE)
        Corrosion.get() shouldBe CoreSet.EMPTY
        Corrosion.self() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
    }

    @Test
    fun `Set affinity for a JVM Thread using a mask`() {
        val expectedString = "0-1"
        val expectedMask: Long = 0x0000000000000003
        val maskResult: AtomicLong = atomic(0x1111111111111111)
        val stringResult: AtomicRef<String> = atomic(StringUtils.EMPTY)
        val latch = CountDownLatch(1)
        val thread = thread(start = false, block = setAndGetAffinity(latch, maskResult, stringResult, expectedMask))
        try {
            thread.start()
            latch.await()
            maskResult.value `should be equal to` expectedMask
            stringResult.value `should be equal to` expectedString
        } finally {
            thread.interrupt()
        }
    }

    @Test
    fun `Set affinity for a JVM Thread using a core list`() {
        val expectedString = "3"
        val expectedMask: Long = 0x0000000000000008
        val stringResult = atomic(StringUtils.EMPTY)
        val maskResult: AtomicLong = atomic(0x1111111111111111)
        val latch = CountDownLatch(1)
        val thread = thread(start = false, block = setAndGetAffinity(latch, maskResult, stringResult, expectedString))
        try {
            thread.start()
            latch.await()
            maskResult.value `should be equal to` expectedMask
            stringResult.value `should be equal to` expectedString
        } finally {
            thread.interrupt()
        }
    }

    private fun setAndGetAffinity(
        latch: CountDownLatch,
        maskResult: AtomicLong,
        stringResult: AtomicRef<String>,
        expected: String
    ): () -> Unit = {
        Corrosion.set(expected)
        val coreSet = Corrosion.get()
        maskResult.value = coreSet.mask()
        stringResult.value = coreSet.toString()
        latch.countDown()
    }

    private fun setAndGetAffinity(
        latch: CountDownLatch,
        maskResult: AtomicLong,
        stringResult: AtomicRef<String>,
        expectedMask: Long
    ): () -> Unit = {
        Corrosion.set(expectedMask)
        val coreSet = Corrosion.get()
        maskResult.value = coreSet.mask()
        stringResult.value = coreSet.toString()
        latch.countDown()
    }
}