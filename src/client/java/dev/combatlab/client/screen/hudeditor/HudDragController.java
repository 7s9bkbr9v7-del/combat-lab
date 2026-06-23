package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.AdaptiveLayoutHudModule;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudAttachmentSide;
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
		draggedModule.updatePosition(
				finalX,
				finalY,
				screenWidth,
				screenHeight
		);
		if (snappingDisabled) {
			draggedModule.clearAttachment();
			snapGuides = List.of();
		} else {
			HudRectangle snappedRectangle = new HudRectangle(finalX, finalY, size.width(), size.height());
			snapGuides = snapResult.guides();
			updateAttachment(snappedRectangle, others);
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

	private boolean updateAttachment(
			HudRectangle moving,
			List<HudSelection.ModuleRectangle> others
	) {
		draggedModule.clearAttachment();
		for (HudSelection.ModuleRectangle candidate : others) {
			HudRectangle target = candidate.rectangle();
			if (!selection.canAttach(draggedModule, candidate.module())) {
				continue;
			}
			if (verticalRangesNear(moving, target)) {
				if (moving.right() == target.x()) {
					draggedModule.attachTo(candidate.module(), HudAttachmentSide.LEFT_OF, moving.y() - target.y());
					return true;
				}
				if (moving.x() == target.right()) {
					draggedModule.attachTo(candidate.module(), HudAttachmentSide.RIGHT_OF, moving.y() - target.y());
					return true;
				}
			}
			if (horizontalRangesNear(moving, target)) {
				if (moving.bottom() == target.y()) {
					draggedModule.attachTo(candidate.module(), HudAttachmentSide.ABOVE, moving.x() - target.x());
					return true;
				}
				if (moving.y() == target.bottom()) {
					draggedModule.attachTo(candidate.module(), HudAttachmentSide.BELOW, moving.x() - target.x());
					return true;
				}
			}
		}
		return false;
	}

	private boolean verticalRangesNear(HudRectangle first, HudRectangle second) {
		return first.bottom() + snapThreshold >= second.y() && second.bottom() + snapThreshold >= first.y();
	}

	private boolean horizontalRangesNear(HudRectangle first, HudRectangle second) {
		return first.right() + snapThreshold >= second.x() && second.right() + snapThreshold >= first.x();
	}
}
