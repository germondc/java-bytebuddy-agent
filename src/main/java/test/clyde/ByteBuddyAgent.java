package test.clyde;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;

public class ByteBuddyAgent {
	public static void premain(final String agentArgs, final Instrumentation inst) throws Exception {
		System.out.printf("Starting %s\n", ByteBuddyAgent.class.getSimpleName());

		new AgentBuilder.Default().type(ElementMatchers.nameContains("alchemy")).transform(new MetricsTransformer())
				.installOn(inst);
	}

	private static class MetricsTransformer implements Transformer {
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) {
			final AsmVisitorWrapper methodsVisitor = Advice.to(EnterAdvice.class, ExitAdviceMethods.class)
					.on((ElementMatchers.isMethod()));

			final AsmVisitorWrapper constructorsVisitor = Advice
					.to(EnterAdviceConstructors.class, ExitAdviceConstructors.class)
					.on((ElementMatchers.isConstructor()));

			return builder.visit(methodsVisitor).visit(constructorsVisitor);
		}

		private static class EnterAdvice {
			@Advice.OnMethodEnter
			static long enter(@Advice.Origin final Method method) {
				System.out.println(String.format("enter m (%s) %s", method, Thread.currentThread().getId()));
				return System.nanoTime();
			}
		}

		private static class ExitAdviceMethods {
			@Advice.OnMethodExit(onThrowable = Throwable.class)
			static void exit(@Advice.Origin final Method method, @Advice.Enter final long startTime) {
				TimingOutput.addLine(startTime, System.nanoTime(), method.toString(), Thread.currentThread());
			}
		}

		private static class EnterAdviceConstructors {
			@Advice.OnMethodEnter
			static long enter(@Advice.Origin final Constructor<?> method) {
				System.out.println(String.format("enter c (%s)", method, Thread.currentThread().getId()));
				return System.nanoTime();
			}
		}

		private static class ExitAdviceConstructors {
			@Advice.OnMethodExit
			static void exit(@Advice.Origin final Constructor<?> method, @Advice.Enter final long startTime) {
				TimingOutput.addLine(startTime, System.nanoTime(), method.toString(), Thread.currentThread());
			}
		}
	}
}
