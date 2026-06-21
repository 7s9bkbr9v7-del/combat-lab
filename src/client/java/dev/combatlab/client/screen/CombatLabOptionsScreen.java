package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudModuleRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CombatLabOptionsScreen extends Screen {
	private static final Component TITLE = Component.literal("Combat Lab Options");
	private final Screen parent;
	private final CombatLabOptions options;
	private final HudModuleRegistry modules;
	private final DebugLogger debug;

	public CombatLabOptionsScreen(Screen parent, CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
		super(TITLE);
		this.parent = parent;
		this.options = options;
		this.modules = modules;
		this.debug = debug;
	}

	@Override
	protected void init() {
		int left = width / 2 - 100;
		int top = height / 2 - 40;
		addRenderableWidget(Button.builder(
				Component.literal("General"),
				button -> minecraft.setScreenAndShow(new GeneralOptionsScreen(this, options, debug))
		).bounds(left, top, 200, 20).build());

		addRenderableWidget(Button.builder(
				Component.literal("HUD"),
				button -> minecraft.setScreenAndShow(new HudOptionsScreen(this, modules))
		).bounds(left, top + 30, 200, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
				.bounds(left, top + 75, 200, 20)
				.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title, width / 2, 30, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreenAndShow(parent);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
