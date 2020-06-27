package org.sheinbergon.corrosion.concurrent.util;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.concurrent.CountDownLatch;

public final class ResettableOneOffLatch {

    private volatile CountDownLatch latch;

    public ResettableOneOffLatch() {
        latch = new CountDownLatch(NumberUtils.INTEGER_ONE);
    }

    public void reset() {
        latch = new CountDownLatch(NumberUtils.INTEGER_ONE);
    }

    public void fire() {
        latch.countDown();
    }

    public synchronized void await(final boolean reset) {
        try {
            latch.await();
            if (reset) reset();
        } catch (InterruptedException iex) {
            throw new RuntimeException(iex);
        }
    }
}