package org.sheinbergon.needle.concurrent.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.concurrent.CountDownLatch;

public final class ResettableOneOffLatch {

  /**
   * Underlying mutable {@code CountDownLatch} used to provide latch synchronization facility.
   */
  private volatile CountDownLatch latch;

  /**
   * Initializes this latch in an 'unlatched' mode.
   */
  public ResettableOneOffLatch() {
    this(false);
  }

  /**
   * Initializes this latch in either 'latched' or 'unlatched' mode.
   *
   * @param latched Setting this to {@code true} instantiates this class in a 'latched' mode,
   *                requiring an initial call to {@link ResettableOneOffLatch#fire()} in order to unlatch
   */
  public ResettableOneOffLatch(final boolean latched) {
    latch = new CountDownLatch(latched ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO);
  }

  /**
   * Reset this latch back to a 'latched' state, irregardless of its current state.
   */
  public void reset() {
    latch = new CountDownLatch(NumberUtils.INTEGER_ONE);
  }

  /**
   * Unlatches this latch, moving it to an unlatched mode.
   */
  public void fire() {
    latch.countDown();
  }

  /**
   * Wait for this latch to fire, optionally resetting it fires.
   *
   * @param reset whether or not to reset this latch after it fires
   */
  public synchronized void await(final boolean reset) {
    try {
      latch.await();
      if (reset) {
        reset();
      }
    } catch (InterruptedException iex) {
      throw new RuntimeException(iex);
    }
  }
}
