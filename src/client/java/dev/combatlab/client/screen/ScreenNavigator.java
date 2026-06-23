package dev.combatlab.client.screen;

import dev.combatlab.client.compat.CompatMethod;
import dev.combatlab.client.compat.MinecraftCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Opens screens through the non-nested screen transition path across nearby Minecraft versions.
 */
public final class ScreenNavigator {
	private static final CompatMethod GUI_SET_SCREEN = MinecraftCapabilities.guiSetScreen();
	private static final CompatMethod MINECRAFT_SET_SCREEN = MinecraftCapabilities.minecraftSetScreen();

	private ScreenNavigator() {
	}

	public static void open(Minecraft minecraft, Screen screen) {
		if (minecraft == null) {
			return;
		}
		if (GUI_SET_SCREEN.invoke(minecraft.gui, screen)) {
			return;
		}
		if (MINECRAFT_SET_SCREEN.invoke(minecraft, screen)) {
			return;
		}
		throw new IllegalStateException("No compatible screen transition method was found.");
	}
}
