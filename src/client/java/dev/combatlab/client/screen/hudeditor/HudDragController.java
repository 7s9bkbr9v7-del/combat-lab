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
	private final HudModuleSelection moduleSelection;
	private final int snapThreshold;
	private HudModule draggedModule;
	private int dragOffsetX;
	private int dragOffsetY;
	private int draggedStartX;
	private int draggedStartY;
	private boolean dragging;
	private List<DragMember> dragMembers = List.of();
	private List<HudSnapGuide> snapGuides = List.of();

	public HudDragController(HudSelection selection, HudModuleSelection moduleSelection, int snapThreshold) {
		this.selection = selection;
		this.moduleSelection = moduleSelection;
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
		draggedStartX = position.x();
		draggedStartY = position.y();
		dragOffsetX = (int) mouseX - position.x();
		dragOffsetY = (int) mouseY - position.y();
		dragMembers = dragMembers(screenWidth, screenHeight);
		dragging = false;
		return true;
	}

	public boolean drag(double mouseX, double mouseY, int screenWidth, int screenHeight, boolean snappingDisabled) {
		if (draggedModule == null) {
			return false;
		}

		if (!dragging) {
			for (DragMember member : dragMembers) {
				member.module().detach(screenWidth, screenHeight);
			}
			dragging = true;
		}

		HudSize size = draggedModule.size();
		int x = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, screenWidth - size.width()));
		int y = clamp((int) mouseY - dragOffsetY, 0, Math.max(0, screenHeight - size.height()));
		List<HudSelection.ModuleRectangle> others = selection.enabledModuleRectanglesExcept(
				draggedModule,
				screenWidth,
				screenHeight
		);
		if (dragMembers.size() > 1) {
			others = others.stream()
					.filter(candidate -> !isDragMember(candidate.module()))
					.toList();
		}
		HudRectangle moving = new HudRectangle(x, y, size.width(), size.height());
		HudSnapResult snapResult = snappingDisabled
				? new HudSnapResult(new HudPosition(x, y), List.of())
				: snap(moving, others, screenWidth, screenHeight);
		HudPosition snapped = snapResult.position();
		int finalX = clamp(snapped.x(), 0, Math.max(0, screenWidth - size.width()));
		int finalY = clamp(snapped.y(), 0, Math.max(0, screenHeight - size.height()));
		if (dragMembers.size() > 1) {
			dragSelectedModules(finalX - draggedStartX, finalY - draggedStartY, screenWidth, screenHeight);
			snapGuides = snappingDisabled ? List.of() : snapResult.guides();
			return true;
		}
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
		for (DragMember member : dragMembers) {
			if (member.module() != draggedModule) {
				member.module().savePosition();
			}
		}
		draggedModule = null;
		draggedStartX = 0;
		draggedStartY = 0;
		dragging = false;
		dragMembers = List.of();
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

	private List<DragMember> dragMembers(int screenWidth, int screenHeight) {
		if (!moduleSelection.hasMultipleSelected() || !moduleSelection.selected(draggedModule)) {
			return List.of(new DragMember(draggedModule, selection.rectangle(draggedModule, screenWidth, screenHeight)));
		}
		return moduleSelection.selectedModules(selection.modules()).stream()
				.map(module -> new DragMember(module, selection.rectangle(module, screenWidth, screenHeight)))
				.toList();
	}

	private boolean isDragMember(HudModule module) {
		for (DragMember member : dragMembers) {
			if (member.module() == module) {
				return true;
			}
		}
		return false;
	}

	private void dragSelectedModules(int requestedDeltaX, int requestedDeltaY, int screenWidth, int screenHeight) {
		int deltaX = clampedGroupDeltaX(requestedDeltaX, screenWidth);
		int deltaY = clampedGroupDeltaY(requestedDeltaY, screenHeight);
		for (DragMember member : dragMembers) {
			member.module().clearAttachment();
			member.module().updatePosition(
					member.bounds().x() + deltaX,
					member.bounds().y() + deltaY,
					screenWidth,
					screenHeight
			);
		}
	}

	private int clampedGroupDeltaX(int requestedDeltaX, int screenWidth) {
		int minDelta = Integer.MIN_VALUE;
		int maxDelta = Integer.MAX_VALUE;
		for (DragMember member : dragMembers) {
			minDelta = Math.max(minDelta, -member.bounds().x());
			maxDelta = Math.min(maxDelta, screenWidth - member.bounds().right());
		}
		return clamp(requestedDeltaX, minDelta, maxDelta);
	}

	private int clampedGroupDeltaY(int requestedDeltaY, int screenHeight) {
		int minDelta = Integer.MIN_VALUE;
		int maxDelta = Integer.MAX_VALUE;
		for (DragMember member : dragMembers) {
			minDelta = Math.max(minDelta, -member.bounds().y());
			maxDelta = Math.min(maxDelta, screenHeight - member.bounds().bottom());
		}
		return clamp(requestedDeltaY, minDelta, maxDelta);
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

	private record DragMember(HudModule module, HudRectangle bounds) {
	}
}
