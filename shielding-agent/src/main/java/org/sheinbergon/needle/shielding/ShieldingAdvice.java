package org.sheinbergon.needle.shielding;

import lombok.val;
import net.bytebuddy.asm.Advice;
import org.sheinbergon.needle.Needle;
import org.sheinbergon.needle.shielding.util.AffinityGroups;

public final class ShieldingAdvice {

    /**
     *
     */
    private ShieldingAdvice() {
    }

    /**
     *
     */
    @Advice.OnMethodEnter
    public static void run() {
        try {
            val group = AffinityGroups.forThread(Thread.currentThread());
            Needle.affinity(group.affinity());
        } catch (Throwable throwable) {
            //
        }
    }
}
