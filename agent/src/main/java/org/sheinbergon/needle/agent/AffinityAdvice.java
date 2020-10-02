package org.sheinbergon.needle.agent;

import lombok.val;
import net.bytebuddy.asm.Advice;
import org.sheinbergon.needle.Needle;
import org.sheinbergon.needle.Pinned;
import org.sheinbergon.needle.agent.util.AffinityGroupMatcher;
import org.sheinbergon.needle.util.NeedleAffinity;

import javax.annotation.Nonnull;

public final class AffinityAdvice {

    /**
     * Empty utility class private constructor.
     */
    private AffinityAdvice() {
    }

    /**
     * Byte-Buddy advice method, this is latched on to {@link Thread#run()}, running before the actual method executes.
     * <p>
     * Simply put, it search for a matching affinity group, and sets the thread's affintiy according to the group's
     * {@link org.sheinbergon.needle.AffinityDescriptor} specified values.
     *
     * @see NeedleAgentConfiguration
     * @see org.sheinbergon.needle.AffinityDescriptor
     */
    @Advice.OnMethodEnter
    public static void run() {
        try {
            val thread = Thread.currentThread();
            if (!excluded(thread)) {
                val group = AffinityGroupMatcher.forThread(thread);
                Needle.affinity(group.affinity());
            }
        } catch (Throwable throwable) {
            // Do nothing if any exception were thrown, for now.
            // TODO - Expose these via JUL logging (meant to bridged to other loggers)
        }
    }

    /**
     * Indicates whether this given thread should be exempt from affinity-group matching and setting.
     * This method is public to satisfy byte-buddy class-loading requirements.
     *
     * @param thread The thread to inspect for exclusion from affinity-group matching.
     * @return Boolean value, indicating if this thread is to be applied affinity-group settings
     */
    public static boolean excluded(final @Nonnull Thread thread) {
        val type = thread.getClass();
        return Pinned.class.isAssignableFrom(type) || type.isAnnotationPresent(NeedleAffinity.class);
    }
}
