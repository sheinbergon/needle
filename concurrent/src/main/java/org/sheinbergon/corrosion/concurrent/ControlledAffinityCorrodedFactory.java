package org.sheinbergon.corrosion.concurrent;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sheinbergon.corrosion.Corroded;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

@RequiredArgsConstructor
public final class ControlledAffinityCorrodedFactory implements ThreadFactory {

    @FunctionalInterface
    public interface Controller {
        Long affinity(Runnable target);
    }

    private final Controller controller;

    @Override
    public final Corroded newThread(final @Nonnull Runnable r) {
        val mask = controller.affinity(r);
        return new Corroded(r, mask);
    }
}