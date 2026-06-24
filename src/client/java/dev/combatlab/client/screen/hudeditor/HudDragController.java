package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.AdaptiveLayoutHudModule;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSnapGuide;
import dev.combatlab.client.hud.HudSnapResult;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudSnapper;

import java.util.List;

public final class HudDragController {
	private static final int GRID_SIZE = 4;
	private final HudSelection selection;
	private final int snapThreshold;
	private HudModule draggedModule;
	private int dragOffsetX;
	private int dragOffsetY;
	private List<HudSnapGuide> snapGuides = List.of();

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

		draggedModule.detach(screenWidth, screenHeight);
		HudPosition position = draggedModule.position(screenWidth, screenHeight);
		dragOffsetX = (int) mouseX - position.x();
		dragOffsetY = (int) mouseY - position.y();
		return true;
	}

	public boolean drag(double mouseX, double mouseY, int screenWidth, int screenHeight, boolean snappingDisabled) {
		if (draggedModule == null) {
			return false;
		}

		HudSize size = draggedModule.size();
		int x = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, screenWidth - size.width()));
		int y = clamp((int) mouseY - dragOffsetY, 0, Math.max(0, screenHeight - size.height()));
		List<HudSelection.ModuleRectangle> others = selection.enabledModuleRectanglesExcept(
				draggedModule,
				screenWidth,
				screenHeight
		);
		HudRectangle moving = new HudRectangle(x, y, size.width(), size.height());
		HudSnapResult snapResult = snappingDisabled
				? new HudSnapResult(new HudPosition(x, y), List.of())
				: snap(moving, others, screenWidth, screenHeight);
		HudPosition snapped = snapResult.position();
		int finalX = clamp(snapped.x(), 0, Math.max(0, screenWidth - size.width()));
		int finalY = clamp(snapped.y(), 0, Math.max(0, screenHeight - size.height()));
		if (snappingDisabled) {
			draggedModule.updatePosition(
					finalX,
					finalY,
					screenWidth,
					screenHeight
			);
			draggedModule.clearAttachment();
			snapGuides = List.of();
		} else {
			HudRectangle snappedRectangle = new HudRectangle(finalX, finalY, size.width(), size.height());
			snapGuides = snapResult.guides();
			HudModuleAttachment.placeAndAttach(
					selection,
					draggedModule,
					snappedRectangle,
					others,
					snapThreshold,
					screenWidth,
					screenHeight
			);
		}
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
		snapGuides = List.of();
		return true;
	}

	public List<HudSnapGuide> snapGuides() {
		return snapGuides;
	}

	public HudModule activeModule() {
		return draggedModule;
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.clamp(value, minimum, maximum);
	}

	private HudSnapResult snap(
			HudRectangle moving,
			List<HudSelection.ModuleRectangle> others,
			int screenWidth,
			int screenHeight
	) {
		HudPosition grid = HudSnapper.snapToGrid(moving, GRID_SIZE, screenWidth, screenHeight);
		HudRectangle gridAligned = new HudRectangle(grid.x(), grid.y(), moving.width(), moving.height());
		return HudSnapper.snapWithGuides(
				gridAligned,
				others.stream().map(HudSelection.ModuleRectangle::rectangle).toList(),
				snapThreshold,
				screenWidth,
				screenHeight
		);
	}

}
