package org.sheinbergon.corrosion

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AffinitiyResolverTest {

    @Test
    fun `Process affinity`() {
        AffinityResolver.NoOp.INSTANCE.process() shouldBeEqualTo CoreSet.UNSUPPORTED
    }
}