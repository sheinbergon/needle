package org.sheinbergon.corrosion

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Test
import org.sheinbergon.corrosion.util.CoreSetException

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
    fun `Illegal core-set specification - rubbish`() {
        { CoreSet.from("oxidise") } shouldThrow CoreSetException::class
    }

    @Test
    fun `Empty core set behavior`() {
        CoreSet.EMPTY.mask() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
        CoreSet.EMPTY.toString() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE.toString()
    }
}