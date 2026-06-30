package dev.combatlab.client.screen;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.AdaptiveLayoutHudModule;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.screen.hudeditor.HudBoxSelectionController;
import dev.combatlab.client.screen.hudeditor.HudContextMenu;
import dev.combatlab.client.screen.hudeditor.HudDragController;
import dev.combatlab.client.screen.hudeditor.HudEditorHistory;
import dev.combatlab.client.screen.hudeditor.HudEditorModuleActions;
import dev.combatlab.client.screen.hudeditor.HudEditorRenderer;
import dev.combatlab.client.screen.hudeditor.HudModuleSelection;
import dev.combatlab.client.screen.hudeditor.HudOptionsNavigation;
import dev.combatlab.client.screen.hudeditor.HudResizeController;
import dev.combatlab.client.screen.hudeditor.HudSelection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
  private static final Component TITLE = Component.literal("Combat Lab HUD Editor");
  private static final int SNAP_THRESHOLD = 6;
  private static final int ADD_SNAP_THRESHOLD = 18;
  private static final int RESIZE_HANDLE_SIZE = 3;
  private static final int LAYOUT_BUTTON_SIZE = 11;
  private static final long OPEN_ANIMATION_NANOS = 180_000_000L;
  private static final long LABEL_FADE_NANOS = 700_000_000L;

  private final HudDragController dragController;
  private final HudBoxSelectionController boxSelectionController;
  private final HudResizeController resizeController;
  private final HudEditorRenderer renderer;
  private final HudOptionsNavigation navigation;
  private final HudSelection selection;
  private final HudModuleSelection moduleSelection = new HudModuleSelection();
  private final HudContextMenu contextMenu;
  private final HudEditorModuleActions moduleActions;
  private final HudEditorHistory history;
  private final HudModuleRegistry modules;
  private final long openedAtNanos;
  private long lastLabelFadeNanos = -1L;
  private float titleVisibilityProgress = 1.0F;
  private float guidanceVisibilityProgress = 1.0F;

  public HudEditorScreen(CombatLabOptions options, HudModuleRegistry modules, DebugLogger debug) {
    super(TITLE);
    this.modules = modules;
    this.openedAtNanos = System.nanoTime();
    this.selection = new HudSelection(modules);
    this.history = new HudEditorHistory(modules);
    this.moduleActions =
        new HudEditorModuleActions(modules, selection, history, ADD_SNAP_THRESHOLD);
    this.contextMenu = new HudContextMenu(modules, moduleActions);
    this.dragController = new HudDragController(selection, moduleSelection, SNAP_THRESHOLD);
    this.boxSelectionController = new HudBoxSelectionController(selection, moduleSelection);
    this.resizeController = new HudResizeController(selection, debug, RESIZE_HANDLE_SIZE);
    this.renderer =
        new HudEditorRenderer(
            modules,
            selection,
            moduleSelection,
            dragController,
            boxSelectionController,
            resizeController,
            RESIZE_HANDLE_SIZE,
            LAYOUT_BUTTON_SIZE);
    this.navigation = new HudOptionsNavigation(options, modules, debug);
  }

  @Override
  protected void init() {
    modules.setEditorOpen(true);
    navigation
        .createButtons(this, minecraft, width, height, this::onClose)
        .forEach(this::addRenderableWidget);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    long nowNanos = System.nanoTime();
    float animationProgress = openingAnimationProgress(nowNanos);
    boolean hasEnabledModules =
        renderer.renderEditorLayer(graphics, font, width, height, mouseX, mouseY);
    boolean titleOverlapped = hasEnabledModules && renderer.titleOverlapsModule(font, title, width);
    boolean guidanceOverlapped = hasEnabledModules && renderer.guidanceOverlapsModule(font, width);
    updateLabelVisibility(nowNanos, titleOverlapped, guidanceOverlapped);
    float titleProgress = animationProgress * titleVisibilityProgress;
    float guidanceProgress = animationProgress * guidanceVisibilityProgress;
    fadeWidgets(animationProgress);
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    renderer.renderLabels(
        graphics, font, title, width, hasEnabledModules, titleProgress, guidanceProgress);
    contextMenu.render(graphics, font, mouseX, mouseY);
  }

  @Override
  public void removed() {
    modules.setEditorOpen(false);
    super.removed();
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && contextMenu.open()) {
      return contextMenu.mouseClicked(event.x(), event.y());
    }

    if (super.mouseClicked(event, doubleClick)) {
      return true;
    }

    if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
      HudModule module = selection.topModuleAt(event.x(), event.y(), width, height);
      if (module != null) {
        if (moduleSelection.selected(module) && moduleSelection.hasMultipleSelected()) {
          contextMenu.openSelectionMenu(
              moduleSelection.selectedModules(modules.modules()),
              (int) event.x(),
              (int) event.y(),
              width,
              height);
          return true;
        }
        contextMenu.openModuleMenu(module, (int) event.x(), (int) event.y(), width, height);
        return true;
      }
      contextMenu.close();
      boxSelectionController.beginForContextMenu(event.x(), event.y());
      return true;
    }

    if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
      contextMenu.close();
      return false;
    }
    contextMenu.close();
    HudModule layoutButtonModule =
        selection.topLayoutButtonAt(event.x(), event.y(), width, height, LAYOUT_BUTTON_SIZE);
    if (layoutButtonModule instanceof AdaptiveLayoutHudModule adaptive) {
      history.recordChange(adaptive::cycleLayout);
      moduleSelection.select(layoutButtonModule, false, false, modules.modules());
      return true;
    }
    HudModule clickedModule = selection.topModuleAt(event.x(), event.y(), width, height);
    history.beginChange();
    boolean resizeStarted = resizeController.begin(event.x(), event.y(), width, height);
    if (resizeStarted) {
      selectModuleForMouseDown(resizeController.activeModule());
      return true;
    }
    history.cancelChange();
    if (clickedModule != null) {
      selectModuleForMouseDown(clickedModule);
    }
    history.beginChange();
    if (dragController.begin(event.x(), event.y(), width, height)) {
      return true;
    }
    history.cancelChange();
    if (clickedModule == null) {
      boxSelectionController.begin(event.x(), event.y(), leftControlDown());
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (contextMenu.mouseScrolled(mouseX, mouseY, verticalAmount)) {
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public boolean keyPressed(KeyEvent event) {
    if (controlDown(event)
        && (event.key() == GLFW.GLFW_KEY_Y
            || (event.key() == GLFW.GLFW_KEY_Z && shiftDown(event)))) {
      if (history.redo()) {
        moduleSelection.clear();
        contextMenu.close();
      }
      return true;
    }
    if (controlDown(event) && event.key() == GLFW.GLFW_KEY_Z) {
      if (history.undo()) {
        moduleSelection.clear();
        contextMenu.close();
      }
      return true;
    }
    if (controlDown(event) && event.key() == GLFW.GLFW_KEY_A) {
      moduleSelection.selectAll(modules.modules());
      contextMenu.close();
      return true;
    }
    if (controlDown(event) && event.key() == GLFW.GLFW_KEY_D) {
      moduleSelection.clear();
      contextMenu.close();
      return true;
    }
    if (event.key() == GLFW.GLFW_KEY_DELETE || event.key() == GLFW.GLFW_KEY_BACKSPACE) {
      moduleActions.disableAll(moduleSelection.selectedModules(modules.modules()));
      moduleSelection.clear();
      contextMenu.close();
      return true;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
    if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
        && boxSelectionController.drag(event.x(), event.y(), width, height)) {
      return true;
    }
    if (resizeController.resize(event.x(), event.y(), width, height)
        || dragController.drag(event.x(), event.y(), width, height, leftShiftDown())
        || boxSelectionController.drag(event.x(), event.y(), width, height)) {
      return true;
    }
    return super.mouseDragged(event, deltaX, deltaY);
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    if (event.button() == 0
        && (releaseResize() || releaseDrag() || boxSelectionController.release())) {
      return true;
    }
    if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && boxSelectionController.active()) {
      boolean contextSelection = boxSelectionController.contextSelection();
      boxSelectionController.release();
      if (contextSelection && moduleSelection.size() > 0) {
        contextMenu.openSelectionMenu(
            moduleSelection.selectedModules(modules.modules()),
            (int) event.x(),
            (int) event.y(),
            width,
            height);
      } else {
        contextMenu.openCanvasMenu((int) event.x(), (int) event.y(), width, height);
      }
      return true;
    }
    return super.mouseReleased(event);
  }

  @Override
  public void onClose() {
    ScreenNavigator.open(minecraft, null);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  private float openingAnimationProgress(long nowNanos) {
    float elapsed =
        Math.clamp((float) (nowNanos - openedAtNanos) / OPEN_ANIMATION_NANOS, 0.0F, 1.0F);
    float remaining = 1.0F - elapsed;
    return 1.0F - remaining * remaining * remaining;
  }

  private void updateLabelVisibility(
      long nowNanos, boolean titleOverlapped, boolean guidanceOverlapped) {
    if (lastLabelFadeNanos < 0L) {
      lastLabelFadeNanos = nowNanos;
      return;
    }
    float fadeStep =
        Math.clamp((float) (nowNanos - lastLabelFadeNanos) / LABEL_FADE_NANOS, 0.0F, 1.0F);
    lastLabelFadeNanos = nowNanos;
    titleVisibilityProgress =
        approach(titleVisibilityProgress, titleOverlapped ? 0.0F : 1.0F, fadeStep);
    guidanceVisibilityProgress =
        approach(guidanceVisibilityProgress, guidanceOverlapped ? 0.0F : 1.0F, fadeStep);
  }

  private static float approach(float value, float target, float step) {
    if (value < target) {
      return Math.min(value + step, target);
    }
    if (value > target) {
      return Math.max(value - step, target);
    }
    return value;
  }

  private boolean leftShiftDown() {
    return GLFW.glfwGetKey(minecraft.getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT)
        == GLFW.GLFW_PRESS;
  }

  private boolean leftControlDown() {
    return GLFW.glfwGetKey(minecraft.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL)
        == GLFW.GLFW_PRESS;
  }

  private static boolean controlDown(KeyEvent event) {
    return (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
  }

  private static boolean shiftDown(KeyEvent event) {
    return (event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
  }

  private void selectModuleForMouseDown(HudModule module) {
    if (module != null) {
      if (!leftControlDown()
          && !leftShiftDown()
          && moduleSelection.hasMultipleSelected()
          && moduleSelection.selected(module)) {
        return;
      }
      moduleSelection.select(module, leftControlDown(), leftShiftDown(), modules.modules());
    }
  }

  private boolean releaseResize() {
    if (!resizeController.release()) {
      return false;
    }
    history.commitChange();
    return true;
  }

  private boolean releaseDrag() {
    if (!dragController.release()) {
      return false;
    }
    history.commitChange();
    return true;
  }
}
