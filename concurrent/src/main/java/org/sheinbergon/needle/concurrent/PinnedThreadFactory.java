package org.sheinbergon.needle.concurrent;

import org.sheinbergon.needle.PinnedThread;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public interface PinnedThreadFactory extends ThreadFactory {

    PinnedThread newThread(@Nonnull Runnable r);
}
