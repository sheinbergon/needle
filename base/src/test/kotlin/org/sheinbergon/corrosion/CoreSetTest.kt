package org.sheinbergon.corrosion

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.amshove.kluent.shouldThrow
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.util.CoreSetException
import java.util.*

class CoreSetTest {

    @Test
    fun `Illegal core-set specification - invalid range`() {
        { CoreSet.from("10-2") } shouldThrow CoreSetException::class
    }

    @Test
    fun `Illegal core-set specification - invalid range, mixed specifications`() {
        { CoreSet.from("5,10-2") } shouldThrow CoreSetException::class
    }

    @Test
    fun `Illegal core-set specification - negative value`() {
        { CoreSet.from(NumberUtils.LONG_MINUS_ONE) } shouldThrow CoreSetException::class
    }

    @Test
    fun `Illegal core-set specification - out of bounds mask`() {
        { CoreSet.from(CoreSet.MASK_UPPER_BOUND + `1L`) } shouldThrow CoreSetException::class
    }

    @Test
    fun `Illegal core-set specification - rubbish`() {
        { CoreSet.from("oxidise") } shouldThrow CoreSetException::class
    }

    @Test
    fun `Unsupported core set traits`() {
        CoreSet.UNSUPPORTED.mask() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
        CoreSet.UNSUPPORTED.toString() shouldBe null
    }

    @Test
    fun `Empty mask core set instantiation`() {
        CoreSet.from(StringUtils.EMPTY) shouldBe CoreSet.ALL
        CoreSet.from(NumberUtils.LONG_ZERO) shouldBe CoreSet.ALL
    }

    @Test
    fun `Core set equality`() {
        val cs1 = CoreSet.from(textTestMask)
        val cs2 = CoreSet.from(negatedBinaryTestMask)
        val cs3 = CoreSet.from(textTestMask)
        cs1 shouldNotBeEqualTo null
        cs1 shouldNotBeEqualTo cs2
        cs1 shouldNotBeEqualTo this
        cs1 shouldBeEqualTo cs3
        cs1 shouldBeEqualTo cs1
    }

    @Test
    fun `Core set hashCode`() {
        val cs1 = CoreSet.from(textTestMask)
        val cs2 = CoreSet.from(negatedBinaryTestMask)
        cs1.hashCode() shouldBeEqualTo Objects.hashCode(binaryTestMask)
        cs2.hashCode() shouldBeEqualTo Objects.hashCode(negatedBinaryTestMask)
        cs1.hashCode() shouldNotBeEqualTo cs2.hashCode()
    }
}