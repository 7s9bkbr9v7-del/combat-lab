package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudOutlineResolver;
import dev.combatlab.client.hud.HudOutlineSegment;
import dev.combatlab.client.hud.HudOutlineSegments;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.ResizableHudModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

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

	public void renderEditorLayer(
			GuiGraphicsExtractor graphics,
			Font font,
			int screenWidth,
			int screenHeight,
			int mouseX,
			int mouseY
	) {
		for (HudModule module : modules.modules()) {
			if (module.enabled()) {
				module.renderEditorPreview(graphics, font);
			}
		}
		renderModuleOutlines(graphics, screenWidth, screenHeight);
		renderResizeHandles(graphics, screenWidth, screenHeight);
		renderResizePercent(graphics, font, screenWidth, screenHeight, mouseX, mouseY);
	}

	public void renderLabels(
			GuiGraphicsExtractor graphics,
			Font font,
			Component title,
			int screenWidth
	) {
		graphics.centeredText(font, title, screenWidth / 2, 18, 0xFFFFFFFF);
		String guidance = modules.hasEnabledModules()
				? "Drag HUD modules to reposition them; drag a corner handle to resize"
				: "No HUD modules enabled";
		graphics.centeredText(font, guidance, screenWidth / 2, 32, 0xFF9CA3AF);
	}

	private void renderModuleOutlines(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		List<HudRectangle> rectangles = selection.enabledRectangles(screenWidth, screenHeight);
		for (HudRectangle rectangle : rectangles) {
			List<HudRectangle> others = rectangles.stream().filter(other -> other != rectangle).toList();
			drawOutline(graphics, rectangle, HudOutlineResolver.visibleSegments(rectangle, others));
		}
	}

	private void renderResizeHandles(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
		for (HudModule module : modules.modules()) {
			if (module.enabled() && module instanceof ResizableHudModule) {
				HudRectangle handle = selection.resizeHandle(module, screenWidth, screenHeight, handleSize);
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
}
