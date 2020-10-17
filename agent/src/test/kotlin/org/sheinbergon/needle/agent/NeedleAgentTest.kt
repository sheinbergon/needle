package org.sheinbergon.needle.agent

import com.sun.tools.attach.VirtualMachine
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.sheinbergon.needle.*
import org.sheinbergon.needle.util.NeedleAffinity
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

    private val defaultAffinity = default
  }

  @BeforeEach
  fun setup() {
    val agentUrl = NeedleAgent::class.java.getResource(AGENT_PATH)
    val agentFile = Paths.get(agentUrl.toURI()).toFile().absolutePath
    val pid = ProcessHandle.current().pid()
    val vm = VirtualMachine.attach(pid.toString())
    vm.loadAgent(agentFile, CONFIGURATION_PATH)
    vm.detach()
  }

  @Test
  fun `Verify prefix, thread-name based agent configuration`() {
    lateinit var affinity: AffinityDescriptor
    val thread = Thread { affinity = Needle.affinity() }
    thread.name = "$THREAD_NAME_PREFIX-0"
    thread.start()
    thread.join()
    affinity.mask() shouldBeEqualTo firstCoreAffinity.mask()
    affinity.toString() shouldBeEqualTo firstCoreAffinity.toString()
  }

  @Test
  fun `Verify regex, thread-class based agent configuration`() {
    lateinit var affinity: AffinityDescriptor
    val thread = NeedleAgentThread { affinity = Needle.affinity() }
    thread.start()
    thread.join()
    affinity.mask() shouldBeEqualTo secondCoreAffinity.mask()
    affinity.toString() shouldBeEqualTo secondCoreAffinity.toString()
  }

  @Test
  fun `Verify default agent configuration`() {
    lateinit var affinity: AffinityDescriptor
    val thread = Thread { affinity = Needle.affinity() }
    thread.start()
    thread.join()
    affinity.mask() shouldBeEqualTo defaultAffinity.mask()
    affinity.toString() shouldBeEqualTo defaultAffinity.toString()
  }

  @Test
  fun `Verify NeedleAffinity annotation agent exclusion`() {
    lateinit var affinity: AffinityDescriptor
    val thread = ExcludedAnnotatedNeedleAgentThread { affinity = Needle.affinity() }
    thread.start()
    thread.join()
    affinity.mask() shouldBeEqualTo defaultAffinity.mask()
    affinity.toString() shouldBeEqualTo defaultAffinity.toString()
  }

  @Test
  fun `Verify PinnedThread subclassing agent exclusion`() {
    lateinit var affinity: AffinityDescriptor
    val thread = ExcludedPinnedNeedleAgentThread { affinity = Needle.affinity() }
    thread.start()
    thread.join()
    affinity.mask() shouldBeEqualTo defaultAffinity.mask()
    affinity.toString() shouldBeEqualTo defaultAffinity.toString()
  }
}

// Included for affinity group due to class <-> regex matching heuristic
class NeedleAgentThread(runnable: () -> Unit) : Thread(runnable)

// Should have the same rules as above applied, but is excluded due parent class (PinnedThread, which implements Pinned)
class ExcludedPinnedNeedleAgentThread(runnable: () -> Unit) : PinnedThread(runnable)

// Should have the same rules as above applied, but is excluded due class annotation @NeedleAffinity
@NeedleAffinity
class ExcludedAnnotatedNeedleAgentThread(runnable: () -> Unit) : Thread(runnable)
