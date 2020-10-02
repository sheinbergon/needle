package org.sheinbergon.needle

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.amshove.kluent.shouldThrow
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.util.AffinityDescriptorException
import java.util.*

class AffinityDescriptorTest {

  @Test
  fun `Illegal affinity-descriptor specification - invalid range`() {
    { AffinityDescriptor.from("10-2") } shouldThrow AffinityDescriptorException::class
  }

  @Test
  fun `Illegal affinity-descriptor specification - invalid range, mixed specifications`() {
    { AffinityDescriptor.from("5,10-2") } shouldThrow AffinityDescriptorException::class
  }

  @Test
  fun `Illegal affinity-descriptor specification - negative value`() {
    { AffinityDescriptor.from(NumberUtils.LONG_MINUS_ONE) } shouldThrow AffinityDescriptorException::class
  }

  @Test
  fun `Illegal affinity-descriptor specification - out of bounds mask`() {
    val mask = AffinityDescriptor.MASK_UPPER_BOUND + `1L`
    { AffinityDescriptor.from(mask) } shouldThrow AffinityDescriptorException::class
  }

  @Test
  fun `Illegal affinity-descriptor specification - rubbish`() {
    { AffinityDescriptor.from("needle") } shouldThrow AffinityDescriptorException::class
  }

  @Test
  fun `Unsupported core set traits`() {
    AffinityDescriptor.UNSUPPORTED.mask() shouldBeEqualTo NumberUtils.LONG_MINUS_ONE
    AffinityDescriptor.UNSUPPORTED.toString() shouldBe null
  }

  @Test
  fun `Empty mask core set instantiation`() {
    AffinityDescriptor.from(StringUtils.EMPTY) shouldBeEqualTo AffinityDescriptor.process()
    AffinityDescriptor.from(NumberUtils.LONG_ZERO) shouldBeEqualTo AffinityDescriptor.process()
  }

  @Test
  fun `Core set equality`() {
    val ad1 = AffinityDescriptor.from(textTestMask)
    val ad2 = AffinityDescriptor.from(negatedBinaryTestMask)
    val ad3 = AffinityDescriptor.from(textTestMask)
    ad1 shouldNotBeEqualTo null
    ad1 shouldNotBeEqualTo ad2
    ad1 shouldNotBeEqualTo this
    ad1 shouldBeEqualTo ad3
    ad1 shouldBeEqualTo ad1
  }

  @Test
  fun `Core set hashCode`() {
    val ad1 = AffinityDescriptor.from(textTestMask)
    val ad2 = AffinityDescriptor.from(negatedBinaryTestMask)
    ad1.hashCode() shouldBeEqualTo Objects.hashCode(binaryTestMask)
    ad2.hashCode() shouldBeEqualTo Objects.hashCode(negatedBinaryTestMask)
    ad1.hashCode() shouldNotBeEqualTo ad2.hashCode()
  }
}
