@file:JvmName("PinnedThreadsKt")

package org.sheinbergon.needle.knitter

import org.sheinbergon.needle.AffinityDescriptor
import org.sheinbergon.needle.PinnedThread

private const val `0L` = 0L

fun pinnedThread(
    start: Boolean = true,
    isDaemon: Boolean = false,
    contextClassLoader: ClassLoader? = null,
    name: String? = null,
    affinity: AffinityDescriptor = AffinityDescriptor.from(`0L`),
    block: () -> Unit
): PinnedThread {
    val pinnedThread = PinnedThread(block::invoke, affinity)
    if (isDaemon) {
        pinnedThread.isDaemon = true
    }
    if (name != null) {
        pinnedThread.name = name
    }
    if (contextClassLoader != null) {
        pinnedThread.contextClassLoader = contextClassLoader
    }
    if (start) {
        pinnedThread.start()
    }
    return pinnedThread
}
