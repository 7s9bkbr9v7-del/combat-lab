package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.screen.hudeditor.HudDragController;
import dev.combatlab.client.screen.hudeditor.HudEditorRenderer;
import dev.combatlab.client.screen.hudeditor.HudOptionsNavigation;
import dev.combatlab.client.screen.hudeditor.HudResizeController;
import dev.combatlab.client.screen.hudeditor.HudSelection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
	private static final Component TITLE = Component.literal("Combat Lab HUD Editor");
	private static final int SNAP_THRESHOLD = 6;
	private static final int RESIZE_HANDLE_SIZE = 3;

	private final HudDragController dragController;
	private final HudResizeController resizeController;
	private final HudEditorRenderer renderer;
	private final HudOptionsNavigation navigation;
	private final HudModuleRegistry modules;

	public HudEditorScreen(CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
		super(TITLE);
		this.modules = modules;
		HudSelection selection = new HudSelection(modules);
		this.dragController = new HudDragController(selection, SNAP_THRESHOLD);
		this.resizeController = new HudResizeController(selection, debug, RESIZE_HANDLE_SIZE);
		this.renderer = new HudEditorRenderer(modules, selection, resizeController, RESIZE_HANDLE_SIZE);
		this.navigation = new HudOptionsNavigation(options, modules, debug);
	}

	@Override
	protected void init() {
		modules.setEditorOpen(true);
		navigation.createButtons(this, minecraft, width, height, this::onClose).forEach(this::addRenderableWidget);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		boolean hasEnabledModules = renderer.renderEditorLayer(graphics, font, width, height, mouseX, mouseY);
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		renderer.renderLabels(graphics, font, title, width, hasEnabledModules);
	}

	@Override
	public void removed() {
		modules.setEditorOpen(false);
		super.removed();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		if (event.button() != 0) {
			return false;
		}
		return resizeController.begin(event.x(), event.y(), width, height)
				|| dragController.begin(event.x(), event.y(), width, height);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (resizeController.resize(event.x(), event.y(), width, height)
				|| dragController.drag(event.x(), event.y(), width, height)) {
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0 && (resizeController.release() || dragController.release())) {
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
}
