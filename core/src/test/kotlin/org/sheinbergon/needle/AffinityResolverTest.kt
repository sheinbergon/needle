package org.sheinbergon.needle

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AffinityResolverTest {

  @Test
  fun `Process affinity`() {
    AffinityResolver.NoOp.INSTANCE.process() shouldBeEqualTo AffinityDescriptor.UNSUPPORTED
  }
}
