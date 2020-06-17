package org.sheinbergon.corrosion.concurrent;

import org.sheinbergon.corrosion.Corroded;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public interface CorrodedFactory extends ThreadFactory {

    Corroded newThread(@Nonnull Runnable r);
}
