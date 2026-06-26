package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudCorner;
import dev.combatlab.client.hud.HudHorizontalSide;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudOrientationResolver;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudVerticalSide;
import dev.combatlab.client.hud.ResizableHudModule;

import java.util.List;

public final class HudSelection {
	private final HudModuleRegistry modules;

	public HudSelection(HudModuleRegistry modules) {
		this.modules = modules;
	}

	public HudModule topModuleAt(double mouseX, double mouseY, int screenWidth, int screenHeight) {
		for (HudModule module : modules.modules().reversed()) {
			if (module.enabled() && module.contains(mouseX, mouseY, screenWidth, screenHeight)) {
				return module;
			}
		}
		return null;
	}

	public List<HudModule> modules() {
		return modules.modules();
	}

	public ResizeSelection topResizeHandleAt(
			double mouseX,
			double mouseY,
			int screenWidth,
			int screenHeight,
			int handleSize
	) {
		for (HudModule module : modules.modules().reversed()) {
			if (module.enabled() && module instanceof ResizableHudModule resizable) {
				HudRectangle rectangle = rectangle(module, screenWidth, screenHeight);
				HudCorner corner = HudOrientationResolver.resolve(
						rectangle,
						screenWidth,
						screenHeight
				).cornerFacingCenter();
				if (resizeHandle(rectangle, corner, handleSize).contains(mouseX, mouseY)) {
					return new ResizeSelection(resizable, corner);
				}
			}
		}
		return null;
	}

	public List<ModuleRectangle> enabledModuleRectanglesExcept(
			HudModule excluded,
			int screenWidth,
			int screenHeight
	) {
		return modules.modules().stream()
				.filter(module -> module != excluded && module.enabled())
				.map(module -> new ModuleRectangle(module, rectangle(module, screenWidth, screenHeight)))
				.toList();
	}

	public boolean canAttach(HudModule moving, HudModule target) {
		HudModule current = target;
		for (int depth = 0; depth <= modules.modules().size(); depth++) {
			if (current == moving) {
				return false;
			}
			String parentId = current.attachmentTargetId();
			if (parentId == null) {
				return true;
			}
			current = modules.module(parentId);
			if (current == null) {
				return true;
			}
		}
		return false;
	}

	public HudRectangle rectangle(HudModule module, int screenWidth, int screenHeight) {
		return module.bounds(screenWidth, screenHeight);
	}

	public HudCorner cornerFacingCenter(HudModule module, int screenWidth, int screenHeight) {
		return module.orientation(screenWidth, screenHeight).cornerFacingCenter();
	}

	public HudRectangle resizeHandle(HudModule module, int screenWidth, int screenHeight, int handleSize) {
		return resizeHandle(
				rectangle(module, screenWidth, screenHeight),
				cornerFacingCenter(module, screenWidth, screenHeight),
				handleSize
		);
	}

	public HudRectangle resizeHandle(HudRectangle rectangle, HudCorner corner, int handleSize) {
		return new HudRectangle(
				corner.x(rectangle) - handleOffsetX(corner, handleSize),
				corner.y(rectangle) - handleOffsetY(corner, handleSize),
				handleSize,
				handleSize
		);
	}

	private static int handleOffsetX(HudCorner corner, int handleSize) {
		return corner.horizontalSide() == HudHorizontalSide.RIGHT ? handleSize : 0;
	}

	private static int handleOffsetY(HudCorner corner, int handleSize) {
		return corner.verticalSide() == HudVerticalSide.BOTTOM ? handleSize : 0;
	}

	public record ResizeSelection(ResizableHudModule module, HudCorner corner) {
	}

	public record ModuleRectangle(HudModule module, HudRectangle rectangle) {
	}
}
