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
    private lateinit var textResult: AtomicRef<String>

    @BeforeEach
    fun setup() {
        textResult = atomic(StringUtils.EMPTY)
        binaryResult = atomic(0x1111111111111111)
        latch = CountDownLatch(1)
    }

    @Test
    fun `Unsupported platform behavior - Thread access`() {
        unsupportedPlatform {
            Corrosion.get() shouldBe CoreSet.UNSUPPORTED
            Corrosion.set(NumberUtils.LONG_ONE)
            Corrosion.get() shouldBe CoreSet.UNSUPPORTED
            Corrosion.self() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
        }
    }

    @Test
    fun `Set affinity for a JVM Thread using a binary mask`() {
        val textMask = textTestMask
        val binaryMask: Long = binaryTestMask
        val thread = thread(start = false, block = setAffinityRunnable(binaryMask))
        launchAndVerify(thread, binaryMask, textMask)
    }

    @Test
    fun `Set affinity for a JVM Thread using a text mask`() {
        val textMask = textTestMask
        val binaryMask: Long = binaryTestMask
        val thread = thread(start = false, block = setAffinityRunnable(textMask))
        launchAndVerify(thread, binaryMask, textMask)
    }

    private fun launchAndVerify(thread: Thread, binaryMask: Long, textMask: String) {
        try {
            thread.start()
            latch.await()
            binaryResult.value `should be equal to` binaryMask
            textResult.value `should be equal to` textMask
        } finally {
            thread.interrupt()
        }
    }

    private fun setAffinityRunnable(affinity: String): () -> Unit = {
        Corrosion.set(affinity)
        val coreSet = Corrosion.get()
        binaryResult.value = coreSet.mask()
        textResult.value = coreSet.toString()
        latch.countDown()
    }

    private fun setAffinityRunnable(affinity: Long): () -> Unit = {
        Corrosion.set(affinity)
        val coreSet = Corrosion.get()
        binaryResult.value = coreSet.mask()
        textResult.value = coreSet.toString()
        latch.countDown()
    }
}