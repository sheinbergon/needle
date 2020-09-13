package org.sheinbergon.needle.shielding;

import net.bytebuddy.asm.Advice;
import org.sheinbergon.needle.AffinityDescriptor;
import org.sheinbergon.needle.Needle;

public final class ShieldingAdvice {

    /**
     *
     */

    private ShieldingAdvice() {
    }

    /**
     * @throws Exception
     */
    @Advice.OnMethodEnter
    public static void run() throws Exception {
        System.out.println("AAAAAAAAAAAAAAAA - " + Thread.currentThread().getName());
        Needle.affinity(AffinityDescriptor.from(5L));
    }
}
