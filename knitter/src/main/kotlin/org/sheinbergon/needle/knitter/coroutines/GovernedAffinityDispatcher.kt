package org.sheinbergon.needle.knitter.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import org.sheinbergon.needle.AffinityDescriptor

abstract class GovernedAffinityDispatcher : CoroutineDispatcher() {

    abstract fun alter(affinity: AffinityDescriptor)
}
