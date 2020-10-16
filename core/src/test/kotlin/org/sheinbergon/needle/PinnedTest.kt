package org.sheinbergon.needle;

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class PinnedTest {

  private lateinit var atomic: AtomicRef<Pinned?>

  @BeforeEach
  fun setup() {
    atomic = atomic(null)
  }

  @Test
  fun `Calling Pinned#current() from within a PinnedThread`() {
    val runnable = { Pinned.current().ifPresent { atomic.value = it } }
    val pinned = PinnedThread(runnable)
    pinned.start()
    pinned.join()
    atomic.value shouldBeEqualTo pinned
  }

  @Test
  fun `Calling Pinned#current() from within a standard Thread`() {
    val runnable = { Pinned.current().ifPresent { atomic.value = it } }
    val thread = thread(start = true, block = runnable)
    thread.join()
    atomic.value shouldBe null
  }
}
