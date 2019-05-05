package test.clyde;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.lang.model.element.Element;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

public class TestAgent {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		new AgentBuilder.Default()
				// .with(AgentBuilder.Listener.StreamWriting.toSystemError())
				.ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
				.type(ElementMatchers.nameContains("alchemy."))
//				.transform((builder, td, cl) -> builder.visit(Advice.to(MyGeneralInterceptor.class).on(ElementMatchers.any())))
				.transform((builder, td, cl) -> builder.visit(Advice.to(MyGeneralInterceptor.class).on(ElementMatchers.not(ElementMatchers.isStatic()).and(ElementMatchers.not(ElementMatchers.isConstructor())))))
				.transform((builder, td, cl) -> builder.visit(Advice.to(MyStaticInterceptor.class).on(ElementMatchers.isStatic().and(ElementMatchers.not(ElementMatchers.isTypeInitializer())).and(ElementMatchers.not(ElementMatchers.isConstructor())))))
				.transform((builder, td, cl) -> builder.visit(Advice.to(MyInitializerInterceptor.class).on(ElementMatchers.isTypeInitializer())))
				.transform((builder, td, cl) -> builder.visit(Advice.to(MyConstructorInterceptor.class).on(ElementMatchers.isConstructor())))
				.installOn(inst);
	}

	public static class MyGeneralInterceptor {
		@Advice.OnMethodEnter
//		public static void enter(@Advice.Origin Method method, @Advice.This Object thiz) {
		public static void enter(@Advice.Origin Method method) {
			System.out.println("Intercepted Normal >> " + method);
		}
	}

	public static class MyStaticInterceptor {
		@Advice.OnMethodEnter
		public static void enter(@Advice.Origin Method method) {
			System.out.println("Intercepted Static >> " + method);
		}
	}

	public static class MyInitializerInterceptor {
		@Advice.OnMethodEnter
		public static void enter(@Advice.Origin("#t.#m") String method) {
			System.out.println("Intercepted Initia >> " + method);
		}
	}

	public static class MyConstructorInterceptor {
		@Advice.OnMethodEnter
		public static void enter(@Advice.Origin Constructor<?> method) {
			System.out.println("Intercepted Constr >> " + method);
		}
	}
}
