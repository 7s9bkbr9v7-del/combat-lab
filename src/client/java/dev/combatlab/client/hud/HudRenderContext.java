package dev.combatlab.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public record HudRenderContext(
		Minecraft client,
		Font font,
		HudRectangle bounds,
		boolean editorPreview
) {
	public HudRenderContext(Minecraft client, Font font, HudRectangle bounds) {
		this(client, font, bounds, false);
	}
}
