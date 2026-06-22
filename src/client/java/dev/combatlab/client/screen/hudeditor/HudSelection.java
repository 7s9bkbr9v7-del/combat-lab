package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudCorner;
import dev.combatlab.client.hud.HudHorizontalSide;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
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

	public ResizeSelection topResizeHandleAt(
			double mouseX,
			double mouseY,
			int screenWidth,
			int screenHeight,
			int handleSize
	) {
		for (HudModule module : modules.modules().reversed()) {
			if (module.enabled() && module instanceof ResizableHudModule resizable) {
				HudCorner corner = cornerFacingCenter(module, screenWidth, screenHeight);
				if (resizeHandle(module, corner, screenWidth, screenHeight, handleSize).contains(mouseX, mouseY)) {
					return new ResizeSelection(resizable, corner);
				}
			}
		}
		return null;
	}

	public List<HudRectangle> enabledRectangles(int screenWidth, int screenHeight) {
		return modules.modules().stream()
				.filter(HudModule::enabled)
				.map(module -> rectangle(module, screenWidth, screenHeight))
				.toList();
	}

	public List<HudRectangle> enabledRectanglesExcept(HudModule excluded, int screenWidth, int screenHeight) {
		return modules.modules().stream()
				.filter(module -> module != excluded && module.enabled())
				.map(module -> rectangle(module, screenWidth, screenHeight))
				.toList();
	}

	public HudRectangle rectangle(HudModule module, int screenWidth, int screenHeight) {
		HudPosition position = module.position(screenWidth, screenHeight);
		HudSize size = module.size();
		return new HudRectangle(position.x(), position.y(), size.width(), size.height());
	}

	public HudCorner cornerFacingCenter(HudModule module, int screenWidth, int screenHeight) {
		return module.orientation(screenWidth, screenHeight).cornerFacingCenter();
	}

	public HudRectangle resizeHandle(HudModule module, int screenWidth, int screenHeight, int handleSize) {
		return resizeHandle(
				module,
				cornerFacingCenter(module, screenWidth, screenHeight),
				screenWidth,
				screenHeight,
				handleSize
		);
	}

	private HudRectangle resizeHandle(
			HudModule module,
			HudCorner corner,
			int screenWidth,
			int screenHeight,
			int handleSize
	) {
		HudRectangle rectangle = rectangle(module, screenWidth, screenHeight);
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
}
