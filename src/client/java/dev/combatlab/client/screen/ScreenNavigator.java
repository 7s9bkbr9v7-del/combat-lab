package dev.combatlab.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Opens screens through the non-nested screen transition path across nearby Minecraft versions.
 */
public final class ScreenNavigator {
	private static final Method GUI_SET_SCREEN = findGuiSetScreen();
	private static final Method MINECRAFT_SET_SCREEN = findMinecraftSetScreen();

	private ScreenNavigator() {
	}

	public static void open(Minecraft minecraft, Screen screen) {
		if (minecraft == null) {
			return;
		}
		if (invoke(GUI_SET_SCREEN, minecraft.gui, screen)) {
			return;
		}
		if (invoke(MINECRAFT_SET_SCREEN, minecraft, screen)) {
			return;
		}
		throw new IllegalStateException("No compatible screen transition method was found.");
	}

	private static Method findGuiSetScreen() {
		try {
			return Class.forName("net.minecraft.client.gui.Gui").getMethod("setScreen", Screen.class);
		} catch (ClassNotFoundException | NoSuchMethodException exception) {
			return null;
		}
	}

	private static Method findMinecraftSetScreen() {
		try {
			return Minecraft.class.getMethod("setScreen", Screen.class);
		} catch (NoSuchMethodException exception) {
			return null;
		}
	}

	private static boolean invoke(Method method, Object target, Screen screen) {
		if (method == null || target == null) {
			return false;
		}
		try {
			method.invoke(target, screen);
			return true;
		} catch (IllegalAccessException exception) {
			throw new IllegalStateException("Unable to access screen transition method.", exception);
		} catch (InvocationTargetException exception) {
			Throwable cause = exception.getCause();
			if (cause instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException("Screen transition failed.", cause);
		}
	}
}
