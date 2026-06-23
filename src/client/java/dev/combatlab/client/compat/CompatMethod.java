package dev.combatlab.client.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CompatMethod {
	private final Method method;

	private CompatMethod(Method method) {
		this.method = method;
	}

	public static CompatMethod find(String className, String methodName, Class<?>... parameterTypes) {
		try {
			return find(Class.forName(className), methodName, parameterTypes);
		} catch (ClassNotFoundException exception) {
			return missing();
		}
	}

	public static CompatMethod find(Class<?> targetClass, String methodName, Class<?>... parameterTypes) {
		try {
			return new CompatMethod(targetClass.getMethod(methodName, parameterTypes));
		} catch (NoSuchMethodException exception) {
			return missing();
		}
	}

	public static CompatMethod missing() {
		return new CompatMethod(null);
	}

	public boolean present() {
		return method != null;
	}

	public boolean invoke(Object target, Object... arguments) {
		if (method == null || target == null) {
			return false;
		}

		try {
			method.invoke(target, arguments);
			return true;
		} catch (IllegalAccessException exception) {
			throw new IllegalStateException("Unable to access compatible Minecraft method " + method, exception);
		} catch (InvocationTargetException exception) {
			Throwable cause = exception.getCause();
			if (cause instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException("Compatible Minecraft method failed " + method, cause);
		}
	}
}
