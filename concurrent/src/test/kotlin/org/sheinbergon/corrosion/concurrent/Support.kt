package org.sheinbergon.corrosion.concurrent

import org.sheinbergon.corrosion.Corroded
import org.sheinbergon.corrosion.binaryTestMask

object TestMaskCorrodedFactory : CorrodedFactory {
    override fun newThread(r: Runnable) = Corroded(r, binaryTestMask)
}
