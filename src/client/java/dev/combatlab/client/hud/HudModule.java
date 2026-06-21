package dev.combatlab.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A self-contained HUD module that can be registered, configured, and moved
 * without teaching the editor about its concrete type.
 */
public interface HudModule extends HudElement {
	Identifier id();

	Component displayName();

	boolean enabled();

	void setEnabled(boolean enabled);

	HudPosition position(int screenWidth, int screenHeight);

	HudSize size();

	void updatePosition(int x, int y, int screenWidth, int screenHeight);

	void savePosition();

	void renderEditorPreview(GuiGraphicsExtractor graphics, Font font);

	default void tick() {
	}

	default boolean contains(double mouseX, double mouseY, int screenWidth, int screenHeight) {
		HudPosition position = position(screenWidth, screenHeight);
		HudSize size = size();
		return mouseX >= position.x() && mouseX < position.x() + size.width()
				&& mouseY >= position.y() && mouseY < position.y() + size.height();
	}
}
