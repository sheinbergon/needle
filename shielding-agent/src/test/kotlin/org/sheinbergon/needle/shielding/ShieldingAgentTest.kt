package org.sheinbergon.needle.shielding

import com.sun.tools.attach.VirtualMachine
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.Needle
import org.sheinbergon.needle.default
import java.nio.file.Paths

class ShieldingAgentTest {

    companion object {
        private val AGENT_PATH = System.getProperty("test.agent.jar.path")!!

        private val DEF = default
    }

    @BeforeEach
    fun setup() {
        val agentUrl = ShieldingAgent::class.java.getResource(AGENT_PATH)
        val agentFile = Paths.get(agentUrl.toURI()).toFile().absolutePath
        val pid = ProcessHandle.current().pid()
        val vm = VirtualMachine.attach(pid.toString())
        vm.loadAgent(agentFile);
        vm.detach()
    }

    @Test
    fun test() {
        val thread = Thread {
            println("AAAAAAAAAA")
            Needle.affinity() shouldBeEqualTo DEF
        }
        thread.name = "Testi"
        thread.start()
        thread.join()
    }
}
