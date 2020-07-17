package org.sheinbergon.needle

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
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@RunWith(JUnitPlatform::class)
class NeedleTest {

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
            Needle.affinity() shouldBe AffinityDescriptor.UNSUPPORTED
            Needle.affinity(firstCoreAffinityDescriptor)
            Needle.affinity() shouldBe AffinityDescriptor.UNSUPPORTED
            Needle.self() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
        }
    }

    @Test
    fun `Set affinity for a JVM Thread using a binary mask`() {
        val textMask = textTestMask
        val binaryMask: Long = binaryTestMask
        val affinity = testAffinityDescriptor
        val thread = thread(start = false, block = setAffinityRunnable(testAffinityDescriptor))
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

    private fun setAffinityRunnable(affinity: AffinityDescriptor): () -> Unit = {
        Needle.affinity(affinity)
        val descriptor = Needle.affinity()
        binaryResult.value = descriptor.mask()
        textResult.value = descriptor.toString()
        latch.countDown()
    }
}