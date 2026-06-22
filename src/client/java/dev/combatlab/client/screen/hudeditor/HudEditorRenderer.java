package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudOrientation;
import dev.combatlab.client.hud.HudOrientationResolver;
import dev.combatlab.client.hud.HudOutlineResolver;
import dev.combatlab.client.hud.HudOutlineSegment;
import dev.combatlab.client.hud.HudOutlineSegments;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.ResizableHudModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class HudEditorRenderer {
	private static final int OUTLINE_COLOR = 0xFF60A5FA;
	private static final int RESIZE_HANDLE_COLOR = 0xFF22C55E;

	private final HudModuleRegistry modules;
	private final HudSelection selection;
	private final HudResizeController resizeController;
	private final int handleSize;

	public HudEditorRenderer(
			HudModuleRegistry modules,
			HudSelection selection,
			HudResizeController resizeController,
			int handleSize
	) {
		this.modules = modules;
		this.selection = selection;
		this.resizeController = resizeController;
		this.handleSize = handleSize;
	}

	public boolean renderEditorLayer(
			GuiGraphicsExtractor graphics,
			Font font,
			int screenWidth,
			int screenHeight,
			int mouseX,
			int mouseY
	) {
		List<ModuleLayout> layouts = new ArrayList<>();
		List<HudRectangle> rectangles = new ArrayList<>();
		for (HudModule module : modules.modules()) {
			if (module.enabled()) {
				HudRectangle bounds = selection.rectangle(module, screenWidth, screenHeight);
				layouts.add(new ModuleLayout(
						module,
						bounds,
						HudOrientationResolver.resolve(bounds, screenWidth, screenHeight)
				));
				rectangles.add(bounds);
			}
		}

		for (ModuleLayout layout : layouts) {
			layout.module().renderEditorPreview(graphics, font, layout.bounds());
		}
		renderModuleOutlines(graphics, layouts, rectangles);
		renderResizeHandles(graphics, layouts);
		renderResizePercent(graphics, font, screenWidth, screenHeight, mouseX, mouseY);
		return !layouts.isEmpty();
	}

	public void renderLabels(
			GuiGraphicsExtractor graphics,
			Font font,
			Component title,
			int screenWidth,
			boolean hasEnabledModules,
			float animationProgress
	) {
		graphics.centeredText(font, title, screenWidth / 2, 18, withAlpha(0xFFFFFFFF, animationProgress));
		String guidance = hasEnabledModules
				? "Drag HUD modules to reposition them; drag a corner handle to resize"
				: "No HUD modules enabled";
		graphics.centeredText(font, guidance, screenWidth / 2, 32, withAlpha(0xFF9CA3AF, animationProgress));
	}

	private void renderModuleOutlines(
			GuiGraphicsExtractor graphics,
			List<ModuleLayout> layouts,
			List<HudRectangle> rectangles
	) {
		for (ModuleLayout layout : layouts) {
			HudRectangle rectangle = layout.bounds();
			drawOutline(graphics, rectangle, HudOutlineResolver.visibleSegments(rectangle, rectangles));
		}
	}

	private void renderResizeHandles(GuiGraphicsExtractor graphics, List<ModuleLayout> layouts) {
		for (ModuleLayout layout : layouts) {
			if (layout.module() instanceof ResizableHudModule) {
				HudRectangle handle = selection.resizeHandle(
						layout.bounds(),
						layout.orientation().cornerFacingCenter(),
						handleSize
				);
				graphics.fill(handle.x(), handle.y(), handle.right(), handle.bottom(), RESIZE_HANDLE_COLOR);
			}
		}
	}

	private void renderResizePercent(
			GuiGraphicsExtractor graphics,
			Font font,
			int screenWidth,
			int screenHeight,
			int mouseX,
			int mouseY
	) {
		ResizableHudModule resizedModule = resizeController.activeModule();
		if (resizedModule == null) {
			return;
		}

		String percent = Math.round(resizedModule.scale() * 100.0) + "%";
		int textWidth = font.width(percent);
		int textX = clamp(mouseX - textWidth / 2, 0, Math.max(0, screenWidth - textWidth));
		int textY = clamp(mouseY + 8, 0, Math.max(0, screenHeight - font.lineHeight));
		graphics.text(font, percent, textX, textY, 0xFFFFFFFF, true);
	}

	private static void drawOutline(GuiGraphicsExtractor graphics, HudRectangle rectangle, HudOutlineSegments segments) {
		for (HudOutlineSegment segment : segments.top()) {
			graphics.fill(segment.start(), rectangle.y() - 1, segment.end(), rectangle.y(), OUTLINE_COLOR);
		}
		for (HudOutlineSegment segment : segments.right()) {
			graphics.fill(rectangle.right(), segment.start(), rectangle.right() + 1, segment.end(), OUTLINE_COLOR);
		}
		for (HudOutlineSegment segment : segments.bottom()) {
			graphics.fill(segment.start(), rectangle.bottom(), segment.end(), rectangle.bottom() + 1, OUTLINE_COLOR);
		}
		for (HudOutlineSegment segment : segments.left()) {
			graphics.fill(rectangle.x() - 1, segment.start(), rectangle.x(), segment.end(), OUTLINE_COLOR);
		}
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.clamp(value, minimum, maximum);
	}

	private static int withAlpha(int color, float alpha) {
		int originalAlpha = color >>> 24;
		int animatedAlpha = Math.round(originalAlpha * Math.clamp(alpha, 0.0F, 1.0F));
		return (animatedAlpha << 24) | (color & 0x00FFFFFF);
	}

	private record ModuleLayout(HudModule module, HudRectangle bounds, HudOrientation orientation) {
	}
}
