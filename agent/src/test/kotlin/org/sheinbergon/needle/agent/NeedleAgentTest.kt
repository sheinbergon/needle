package org.sheinbergon.needle.agent

import com.sun.tools.attach.VirtualMachine
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.AffinityDescriptor
import org.sheinbergon.needle.Needle
import org.sheinbergon.needle.`1L`
import org.sheinbergon.needle.`2L`
import java.nio.file.Paths

class AffinityAgentTest {

    companion object {
        private val AGENT_PATH = System.getProperty("test.agent.jar.path")!!

        private val CONFIGURATION_PATH = AffinityAgentTest::class.java
                .getResource("/test-configuration.yml")
                .toString()

        private const val THREAD_NAME_PREFIX = "needle-agent-thread"

        private val firstCoreAffinity = AffinityDescriptor.from(`1L`)

        private val secondCoreAffinity = AffinityDescriptor.from(`2L`)

        private val defaultAffinity = org.sheinbergon.needle.default
    }

    @BeforeEach
    fun setup() {
        val agentUrl = NeedleAgent::class.java.getResource(AGENT_PATH)
        val agentFile = Paths.get(agentUrl.toURI()).toFile().absolutePath
        val pid = ProcessHandle.current().pid()
        val vm = VirtualMachine.attach(pid.toString())
        vm.loadAgent(agentFile, CONFIGURATION_PATH);
        vm.detach()
    }

    @Test
    fun `Verify prefix, thread-name based shielding agent configuration`() {
        var affinity: AffinityDescriptor? = null
        val thread = Thread { affinity = Needle.affinity() }
        thread.name = "$THREAD_NAME_PREFIX-0"
        thread.start()
        thread.join()
        affinity!!.mask() shouldBeEqualTo firstCoreAffinity.mask()
        affinity!!.toString() shouldBeEqualTo firstCoreAffinity.toString()
    }

    @Test
    fun `Verify regex, thread-class based shielding agent configuration`() {
        var affinity: AffinityDescriptor? = null
        val thread = NeedleAgentThread { affinity = Needle.affinity() }
        thread.start()
        thread.join()
        affinity!!.mask() shouldBeEqualTo secondCoreAffinity.mask()
        affinity!!.toString() shouldBeEqualTo secondCoreAffinity.toString()
    }

    @Test
    fun `Verify default affinity agent configuration`() {
        var affinity: AffinityDescriptor? = null
        val thread = Thread { affinity = Needle.affinity() }
        thread.start()
        thread.join()
        affinity!!.mask() shouldBeEqualTo defaultAffinity.mask()
        affinity!!.toString() shouldBeEqualTo defaultAffinity.toString()
    }
}

class NeedleAgentThread(runnable: () -> Unit) : Thread(runnable)
