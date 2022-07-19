package org.sheinbergon.needle.agent;

import lombok.val;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.utility.JavaModule;
import org.sheinbergon.needle.Pinned;
import org.sheinbergon.needle.agent.util.AffinityGroupMatcher;
import org.sheinbergon.needle.agent.util.YamlCodec;
import org.sheinbergon.needle.util.NeedleAffinity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class NeedleAgent {

  private NeedleAgent() {
  }

  /**
   * Static agent loading endpoint.
   *
   * @param arguments       Agent configuration string, if specified, must be a valid JVM URL string pointing to the
   *                        agent configuration file path (i.e. file:///some/file.yml).
   * @param instrumentation JVM instrumentation interface
   * @throws Exception any execution error encountered during instrumentation setup
   */
  public static void premain(
      final String arguments,
      final Instrumentation instrumentation) throws Exception {
    val storage = Files.createTempDirectory("needle-agent-instrumentation").toFile();
    setupBootstrapInjection(storage, instrumentation);
    agentConfiguration(arguments);
    val builder = new AgentBuilder.Default()
        .disableClassFormatChanges()
        .ignore(nameStartsWith("net.bytebuddy."))
        .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
        .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
        .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
        .with(new AgentBuilder.InjectionStrategy.UsingInstrumentation(instrumentation, storage));
    val narrowable = matchers(builder);
    narrowable.transform(NeedleAgent::premainTransform)
        .installOn(instrumentation);
  }

  /**
   * Dynamic agent loading endpoint.
   *
   * @param arguments       Agent configuration string, if specified, must be a valid JVM URL string pointing to the
   *                        agent configuration file path (i.e. file:///some/file.yml).
   * @param instrumentation JVM instrumentation interface
   * @throws Exception any execution error encountered during instrumentation setup
   */
  public static void agentmain(
      final String arguments,
      final Instrumentation instrumentation) throws Exception {
    agentConfiguration(arguments);
    val builder = new AgentBuilder.Default()
        .disableClassFormatChanges()
        .ignore(nameStartsWith("net.bytebuddy."))
        .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
        .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
        .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE);
    val narrowable = matchers(builder);
    narrowable.transform(agentmainTransform())
        .installOn(instrumentation);

  }

  private static AgentBuilder.Identified.Narrowable matchers(
      final @Nonnull AgentBuilder builder) {
    return builder.type(not(isSubTypeOf(Pinned.class))
        .and(not(isAnnotatedWith(NeedleAffinity.class)))
        .and(isSubTypeOf(Thread.class).or(is(Thread.class))));
  }

  private static DynamicType.Builder<?> premainTransform(
      final @Nonnull DynamicType.Builder<?> builder,
      final @Nonnull TypeDescription typeDescription,
      final @Nullable ClassLoader classLoader,
      final @Nonnull JavaModule module) {
    return builder.visit(Advice.to(AffinityAdvice.class).on(named("run")));
  }

  private static AgentBuilder.Transformer agentmainTransform() {
    return new AgentBuilder.Transformer.ForAdvice()
        .include(NeedleAgent.class.getClassLoader())
        .advice(named("run"), AffinityAdvice.class.getName());
  }

  private static void setupBootstrapInjection(
      final @Nonnull File storage,
      final @Nonnull Instrumentation instrumentation) {
    ClassInjector.UsingInstrumentation
        .of(storage, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation)
        .inject(Map.of(
            new TypeDescription.ForLoadedType(AffinityAdvice.class),
            ClassFileLocator.ForClassLoader.read(AffinityAdvice.class)));
  }

  private static void agentConfiguration(final @Nullable String arguments) throws MalformedURLException {
    Supplier<NeedleAgentConfiguration> supplier;
    if (arguments != null) {
      val url = new URL(arguments);
      supplier = () -> YamlCodec.parseConfiguration(url);
    } else {
      supplier = () -> NeedleAgentConfiguration.DEFAULT;
    }
    AffinityGroupMatcher.setConfigurationSupplier(supplier);
  }
}
