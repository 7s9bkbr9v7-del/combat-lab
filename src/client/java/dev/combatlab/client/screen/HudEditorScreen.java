package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudCorner;
import dev.combatlab.client.hud.HudHorizontalSide;
import dev.combatlab.client.hud.HudOutlineResolver;
import dev.combatlab.client.hud.HudOutlineSegment;
import dev.combatlab.client.hud.HudOutlineSegments;
import dev.combatlab.client.hud.HudOrientationResolver;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudSnapper;
import dev.combatlab.client.hud.HudVerticalSide;
import dev.combatlab.client.hud.ResizableHudModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HudEditorScreen extends Screen {
	private static final Component TITLE = Component.literal("Combat Lab HUD Editor");
	private static final int SNAP_THRESHOLD = 6;
	private static final int RESIZE_HANDLE_SIZE = 3;
	private static final int RESIZE_HANDLE_COLOR = 0xFF22C55E;

	private final CombatLabOptions options;
	private final HudModuleRegistry modules;
	private final DebugLogger debug;
	private HudModule draggedModule;
	private ResizableHudModule resizedModule;
	private HudCorner resizedCorner;
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
		renderResizeHandles(graphics);
		renderResizePercent(graphics, mouseX, mouseY);

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF);
		String guidance = modules.hasEnabledModules()
				? "Drag HUD modules to reposition them; drag a corner handle to resize"
				: "No HUD modules enabled";
		graphics.centeredText(font, guidance, width / 2, 32, 0xFF9CA3AF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		if (event.button() != 0) {
			return false;
		}

		resizedModule = findTopResizableModuleHandleAt(event.x(), event.y());
		if (resizedModule != null) {
			return true;
		}

		draggedModule = findTopModuleAt(event.x(), event.y());
		if (draggedModule != null) {
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
		if (resizedModule != null) {
			resizeModule(resizedModule, event.x(), event.y());
			return true;
		}

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
		if (resizedModule != null && event.button() == 0) {
			resizedModule.savePosition();
			debug.info("{} resized to {}%", resizedModule.displayName().getString(), Math.round(resizedModule.scale() * 100.0));
			resizedModule = null;
			resizedCorner = null;
			return true;
		}
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

	private ResizableHudModule findTopResizableModuleHandleAt(double mouseX, double mouseY) {
		List<HudModule> registered = modules.modules();
		for (HudModule module : registered.reversed()) {
			if (module.enabled() && module instanceof ResizableHudModule resizable && resizeHandle(module).contains(mouseX, mouseY)) {
				resizedCorner = resizeCorner(module);
				return resizable;
			}
		}
		return null;
	}

	private void resizeModule(ResizableHudModule module, double mouseX, double mouseY) {
		HudCorner corner = resizedCorner == null ? resizeCorner(module) : resizedCorner;
		HudPosition position = module.position(width, height);
		HudSize currentSize = module.size();
		HudRectangle currentRectangle = new HudRectangle(position.x(), position.y(), currentSize.width(), currentSize.height());
		HudSize unscaled = module.unscaledSize();
		int fixedX = corner.oppositeX(currentRectangle);
		int fixedY = corner.oppositeY(currentRectangle);
		double targetWidthScale = corner.widthFromMouse(fixedX, mouseX) / unscaled.width();
		double targetHeightScale = corner.heightFromMouse(fixedY, mouseY) / unscaled.height();
		double requestedScale = Math.max(targetWidthScale, targetHeightScale);
		double maximumScale = Math.min(module.maxScale(), corner.maxScale(fixedX, fixedY, width, height, unscaled));
		double scale = Math.clamp(requestedScale, module.minScale(), Math.max(module.minScale(), maximumScale));
		module.updateScale(scale);
		HudSize scaledSize = module.size();
		module.updatePosition(
				corner.resizedX(fixedX, scaledSize),
				corner.resizedY(fixedY, scaledSize),
				width,
				height
		);
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

	private void renderResizeHandles(GuiGraphicsExtractor graphics) {
		for (HudModule module : modules.modules()) {
			if (module.enabled() && module instanceof ResizableHudModule) {
				HudRectangle handle = resizeHandle(module);
				graphics.fill(handle.x(), handle.y(), handle.right(), handle.bottom(), RESIZE_HANDLE_COLOR);
			}
		}
	}

	private void renderResizePercent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (resizedModule == null) {
			return;
		}

		String percent = Math.round(resizedModule.scale() * 100.0) + "%";
		int textX = clamp(mouseX - font.width(percent) / 2, 0, Math.max(0, width - font.width(percent)));
		int textY = clamp(mouseY + 8, 0, Math.max(0, height - font.lineHeight));
		graphics.text(font, percent, textX, textY, 0xFFFFFFFF, true);
	}

	private HudRectangle resizeHandle(HudModule module) {
		HudCorner corner = resizeCorner(module);
		HudPosition position = module.position(width, height);
		HudSize size = module.size();
		HudRectangle rectangle = new HudRectangle(position.x(), position.y(), size.width(), size.height());
		return new HudRectangle(
				corner.x(rectangle) - handleOffsetX(corner),
				corner.y(rectangle) - handleOffsetY(corner),
				RESIZE_HANDLE_SIZE,
				RESIZE_HANDLE_SIZE
		);
	}

	private HudCorner resizeCorner(HudModule module) {
		HudPosition position = module.position(width, height);
		HudSize size = module.size();
		return HudOrientationResolver.resolve(new HudRectangle(position.x(), position.y(), size.width(), size.height()), width, height)
				.cornerFacingCenter();
	}

	private static int handleOffsetX(HudCorner corner) {
		return corner.horizontalSide() == HudHorizontalSide.RIGHT ? RESIZE_HANDLE_SIZE : 0;
	}

	private static int handleOffsetY(HudCorner corner) {
		return corner.verticalSide() == HudVerticalSide.BOTTOM ? RESIZE_HANDLE_SIZE : 0;
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
