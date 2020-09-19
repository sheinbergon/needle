package org.sheinbergon.needle.shielding;

import lombok.val;
import net.bytebuddy.asm.Advice;
import org.sheinbergon.needle.Needle;
import org.sheinbergon.needle.shielding.util.AffinityGroups;

import java.util.function.Consumer;

public final class ShieldingAdvice {

    /**
     * a.
     */
    public static final Consumer<ShieldingConfiguration.AffinityGroup> AFFINITY_SETTER =
            affinityGroup -> Needle.affinity(affinityGroup.affinity());

    /**
     *
     */
    private ShieldingAdvice() {
    }

    /**
     * @throws Exception
     */
    @Advice.OnMethodEnter
    public static void run() {
        val group = AffinityGroups.forThread(Thread.currentThread());
        group.ifPresent(AFFINITY_SETTER);
    }
}
