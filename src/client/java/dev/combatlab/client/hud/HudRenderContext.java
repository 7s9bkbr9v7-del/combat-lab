package dev.combatlab.client.hud;

import dev.combatlab.client.state.ClientGameState;
import net.minecraft.client.gui.Font;

public record HudRenderContext(
		Font font,
		HudRectangle bounds,
		boolean editorPreview,
		ClientGameState gameState
) {
	public HudRenderContext(Font font, HudRectangle bounds, ClientGameState gameState) {
		this(font, bounds, false, gameState);
	}

	public HudGameState hud() {
		return gameState.hud();
	}
}
