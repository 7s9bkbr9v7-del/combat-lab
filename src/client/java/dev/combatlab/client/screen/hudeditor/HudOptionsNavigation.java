package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.screen.CombatLabOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HudOptionsNavigation {
	private final CombatLabOptions options;
	private final HudModuleRegistry modules;
	private final DebugLogger debug;

	public HudOptionsNavigation(CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
		this.options = options;
		this.modules = modules;
		this.debug = debug;
	}

	public List<Button> createButtons(
			Screen editor,
			Minecraft minecraft,
			int screenWidth,
			int screenHeight,
			Runnable closeEditor
	) {
		Button optionsButton = Button.builder(
				Component.literal("HUD Options"),
				button -> {
					debug.info("Opening HUD options screen");
					minecraft.gui.setScreen(new CombatLabOptionsScreen(editor, options, modules, debug));
				}
		).bounds(screenWidth / 2 - 75, screenHeight / 2 - 10, 150, 20).build();

		Button doneButton = Button.builder(Component.literal("Done"), button -> closeEditor.run())
				.bounds(screenWidth / 2 - 75, screenHeight - 35, 150, 20)
				.build();
		return List.of(optionsButton, doneButton);
	}
}
