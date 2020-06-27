package org.sheinbergon.corrosion

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@RunWith(JUnitPlatform::class)
@ExtendWith(MockitoExtension::class)
class CorrosionTest {

    private lateinit var latch: CountDownLatch
    private lateinit var binaryResult: AtomicLong
    private lateinit var stringResult: AtomicRef<String>

    @BeforeEach
    fun setup() {
        stringResult = atomic(StringUtils.EMPTY)
        binaryResult = atomic(0x1111111111111111)
        latch = CountDownLatch(1)
    }

    @Test
    fun `Unsupported platform behavior - Thread access`() {
        unsupportedPlatform {
            Corrosion.get() shouldBe CoreSet.EMPTY
            Corrosion.set(NumberUtils.LONG_ONE)
            Corrosion.get() shouldBe CoreSet.EMPTY
            Corrosion.self() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
        }
    }

    @Test
    fun `Set affinity for a JVM Thread using a binary mask`() {
        val stringMask = "0-1"
        val binaryMask: Long = 0x0000000000000003
        val thread = thread(start = false, block = setAffinityRunnable(binaryMask))
        launchThread(thread, binaryMask, stringMask)
    }

    @Test
    fun `Set affinity for a JVM Thread using a string mask`() {
        val stringMask = "3"
        val binaryMask: Long = 0x0000000000000008
        val thread = thread(start = false, block = setAffinityRunnable(stringMask))
        launchThread(thread, binaryMask, stringMask)
    }

    private fun launchThread(thread: Thread, binaryMask: Long, stringMask: String) {
        try {
            thread.start()
            latch.await()
            binaryResult.value `should be equal to` binaryMask
            stringResult.value `should be equal to` stringMask
        } finally {
            thread.interrupt()
        }
    }

    private fun setAffinityRunnable(affinity: String): () -> Unit = {
        Corrosion.set(affinity)
        val coreSet = Corrosion.get()
        binaryResult.value = coreSet.mask()
        stringResult.value = coreSet.toString()
        latch.countDown()
    }

    private fun setAffinityRunnable(affinity: Long): () -> Unit = {
        Corrosion.set(affinity)
        val coreSet = Corrosion.get()
        binaryResult.value = coreSet.mask()
        stringResult.value = coreSet.toString()
        latch.countDown()
    }
}