package dev.combatlab.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MinecraftCapabilities {
	public static final String VANILLA_HUD_STATUS_EFFECT_TARGET_LEGACY_GUI = MinecraftClasses.LEGACY_GUI;
	public static final String VANILLA_HUD_STATUS_EFFECT_TARGET_HUD = MinecraftClasses.HUD;

	private MinecraftCapabilities() {
	}

	public static CompatMethod guiSetScreen() {
		return CompatMethod.find(MinecraftClasses.LEGACY_GUI, "setScreen", Screen.class);
	}

	public static CompatMethod minecraftSetScreen() {
		return CompatMethod.find(Minecraft.class, "setScreen", Screen.class);
	}

	public static String[] vanillaHudStatusEffectTargets() {
		return new String[] {
				VANILLA_HUD_STATUS_EFFECT_TARGET_LEGACY_GUI,
				VANILLA_HUD_STATUS_EFFECT_TARGET_HUD
		};
	}
}
