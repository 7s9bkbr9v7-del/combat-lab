package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.AdaptiveLayoutHudModule;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudSnapper;

public final class HudDragController {
	private final HudSelection selection;
	private final int snapThreshold;
	private HudModule draggedModule;
	private int dragOffsetX;
	private int dragOffsetY;

	public HudDragController(HudSelection selection, int snapThreshold) {
		this.selection = selection;
		this.snapThreshold = snapThreshold;
	}

	public boolean begin(double mouseX, double mouseY, int screenWidth, int screenHeight) {
		draggedModule = selection.topModuleAt(mouseX, mouseY, screenWidth, screenHeight);
		if (draggedModule == null) {
			return false;
		}
		if (draggedModule instanceof AdaptiveLayoutHudModule adaptive) {
			adaptive.lockLayout();
		}

		HudPosition position = draggedModule.position(screenWidth, screenHeight);
		dragOffsetX = (int) mouseX - position.x();
		dragOffsetY = (int) mouseY - position.y();
		return true;
	}

	public boolean drag(double mouseX, double mouseY, int screenWidth, int screenHeight) {
		if (draggedModule == null) {
			return false;
		}

		HudSize size = draggedModule.size();
		int x = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, screenWidth - size.width()));
		int y = clamp((int) mouseY - dragOffsetY, 0, Math.max(0, screenHeight - size.height()));
		HudPosition snapped = HudSnapper.snap(
				new HudRectangle(x, y, size.width(), size.height()),
				selection.enabledRectanglesExcept(draggedModule, screenWidth, screenHeight),
				snapThreshold,
				screenWidth,
				screenHeight
		);
		draggedModule.updatePosition(
				clamp(snapped.x(), 0, Math.max(0, screenWidth - size.width())),
				clamp(snapped.y(), 0, Math.max(0, screenHeight - size.height())),
				screenWidth,
				screenHeight
		);
		return true;
	}

	public boolean release() {
		if (draggedModule == null) {
			return false;
		}
		if (draggedModule instanceof AdaptiveLayoutHudModule adaptive) {
			adaptive.unlockLayout();
		}
		draggedModule.savePosition();
		draggedModule = null;
		return true;
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.clamp(value, minimum, maximum);
	}
}
