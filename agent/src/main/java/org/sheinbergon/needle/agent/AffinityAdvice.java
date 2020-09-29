package org.sheinbergon.needle.agent;

import lombok.val;
import net.bytebuddy.asm.Advice;
import org.sheinbergon.needle.Needle;
import org.sheinbergon.needle.agent.util.AffinityGroupMatcher;

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
            val group = AffinityGroupMatcher.forThread(Thread.currentThread());
            Needle.affinity(group.affinity());
        } catch (Throwable throwable) {
            // Do nothing if any exception were thrown, for now.
            // TODO - Expose these via JUL logging (meant to bridged to other loggers)
        }
    }
}
