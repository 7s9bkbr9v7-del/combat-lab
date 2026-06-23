package dev.combatlab.client.screen;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudOptionsScreen extends Screen {
	private static final Component TITLE = Component.literal("HUD Options");
	private final Screen parent;
	private final HudModuleRegistry modules;

	public HudOptionsScreen(Screen parent, HudModuleRegistry modules) {
		super(TITLE);
		this.parent = parent;
		this.modules = modules;
	}

	@Override
	protected void init() {
		int left = width / 2 - 100;
		int y = height / 2 - (modules.modules().size() * 30) / 2;
		for (HudModule module : modules.modules()) {
			addRenderableWidget(OptionTooltip.describe(Checkbox.builder(module.displayName(), font)
					.pos(left, y)
					.maxWidth(200)
					.selected(module.enabled())
					.onValueChange((checkbox, selected) -> module.setEnabled(selected))
					.build(), Component.literal("Show or hide this module in game and in the HUD editor.")));
			y += 30;
		}

		addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
				.bounds(left, y + 15, 200, 20)
				.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title, width / 2, 30, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		ScreenNavigator.open(minecraft, parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
