package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudOutlineResolver;
import dev.combatlab.client.hud.HudOutlineSegment;
import dev.combatlab.client.hud.HudOutlineSegments;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudSnapper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HudEditorScreen extends Screen {
	private static final Component TITLE = Component.literal("Combat Lab HUD Editor");
	private static final int SNAP_THRESHOLD = 6;

	private final CombatLabOptions options;
	private final HudModuleRegistry modules;
	private final DebugLogger debug;
	private HudModule draggedModule;
	private int dragOffsetX;
	private int dragOffsetY;

	public HudEditorScreen(CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
		super(TITLE);
		this.options = options;
		this.modules = modules;
		this.debug = debug;
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(
				Component.literal("HUD Options"),
				button -> {
					debug.info("Opening HUD options screen");
					minecraft.setScreenAndShow(new CombatLabOptionsScreen(this, options, modules, debug));
				}
		).bounds(width / 2 - 75, height / 2 - 10, 150, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
				.bounds(width / 2 - 75, height - 35, 150, 20)
				.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		for (HudModule module : modules.modules()) {
			if (module.enabled()) {
				module.renderEditorPreview(graphics, font);
			}
		}
		renderModuleOutlines(graphics);

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF);
		String guidance = modules.hasEnabledModules()
				? "Drag HUD modules to reposition them"
				: "No HUD modules enabled";
		graphics.centeredText(font, guidance, width / 2, 32, 0xFF9CA3AF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		draggedModule = findTopModuleAt(event.x(), event.y());
		if (event.button() == 0 && draggedModule != null) {
			HudPosition position = draggedModule.position(width, height);
			dragOffsetX = (int) event.x() - position.x();
			dragOffsetY = (int) event.y() - position.y();
			return true;
		}

		draggedModule = null;
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (draggedModule == null) {
			return super.mouseDragged(event, deltaX, deltaY);
		}

		HudSize size = draggedModule.size();
		int x = clamp((int) event.x() - dragOffsetX, 0, Math.max(0, width - size.width()));
		int y = clamp((int) event.y() - dragOffsetY, 0, Math.max(0, height - size.height()));
		HudPosition snapped = HudSnapper.snap(
				new HudRectangle(x, y, size.width(), size.height()),
				otherModuleRectangles(),
				SNAP_THRESHOLD
		);
		x = clamp(snapped.x(), 0, Math.max(0, width - size.width()));
		y = clamp(snapped.y(), 0, Math.max(0, height - size.height()));
		draggedModule.updatePosition(x, y, width, height);
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (draggedModule != null && event.button() == 0) {
			draggedModule.savePosition();
			draggedModule = null;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreenAndShow(null);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private HudModule findTopModuleAt(double mouseX, double mouseY) {
		List<HudModule> registered = modules.modules();
		for (HudModule module : registered.reversed()) {
			if (module.enabled() && module.contains(mouseX, mouseY, width, height)) {
				return module;
			}
		}
		return null;
	}

	private List<HudRectangle> otherModuleRectangles() {
		return modules.modules().stream()
				.filter(module -> module != draggedModule && module.enabled())
				.map(module -> {
					HudPosition position = module.position(width, height);
					HudSize size = module.size();
					return new HudRectangle(position.x(), position.y(), size.width(), size.height());
				})
				.toList();
	}

	private void renderModuleOutlines(GuiGraphicsExtractor graphics) {
		List<HudRectangle> rectangles = enabledModuleRectangles();
		for (HudRectangle rectangle : rectangles) {
			List<HudRectangle> others = rectangles.stream().filter(other -> other != rectangle).toList();
			HudOutlineSegments segments = HudOutlineResolver.visibleSegments(rectangle, others);
			drawOutline(graphics, rectangle, segments);
		}
	}

	private List<HudRectangle> enabledModuleRectangles() {
		return modules.modules().stream()
				.filter(HudModule::enabled)
				.map(module -> {
					HudPosition position = module.position(width, height);
					HudSize size = module.size();
					return new HudRectangle(position.x(), position.y(), size.width(), size.height());
				})
				.toList();
	}

	private static void drawOutline(GuiGraphicsExtractor graphics, HudRectangle rectangle, HudOutlineSegments segments) {
		int color = 0xFF60A5FA;
		for (HudOutlineSegment segment : segments.top()) {
			graphics.fill(segment.start(), rectangle.y() - 1, segment.end(), rectangle.y(), color);
		}
		for (HudOutlineSegment segment : segments.right()) {
			graphics.fill(rectangle.right(), segment.start(), rectangle.right() + 1, segment.end(), color);
		}
		for (HudOutlineSegment segment : segments.bottom()) {
			graphics.fill(segment.start(), rectangle.bottom(), segment.end(), rectangle.bottom() + 1, color);
		}
		for (HudOutlineSegment segment : segments.left()) {
			graphics.fill(rectangle.x() - 1, segment.start(), rectangle.x(), segment.end(), color);
		}
	}

	private static int clamp(int value, int minimum, int maximum) {
		return Math.clamp(value, minimum, maximum);
	}
}
