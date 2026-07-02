package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.hud.AdaptiveLayoutHudModule;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudOrientation;
import dev.combatlab.client.hud.HudOrientationResolver;
import dev.combatlab.client.hud.HudOutlineResolver;
import dev.combatlab.client.hud.HudOutlineSegment;
import dev.combatlab.client.hud.HudOutlineSegments;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudSnapGuide;
import dev.combatlab.client.hud.ResizableHudModule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class HudEditorRenderer {
  private static final int OUTLINE_COLOR = 0xFF60A5FA;
  private static final int ATTACHMENT_ANCHOR_OUTLINE_COLOR = 0xFFFBBF24;
  private static final int HOVER_FILL_COLOR = 0x554B5563;
  private static final int SELECTION_BOX_FILL_COLOR = 0x3360A5FA;
  private static final int RESIZE_HANDLE_COLOR = 0xFF22C55E;
  private static final int LAYOUT_BUTTON_ICON_COLOR = 0xFFC7CCD4;
  private static final int LAYOUT_BUTTON_ICON_HOVER_COLOR = 0xFFFFFFFF;
  private static final int LABEL_SHADOW_CORE_COLOR = 0x3A000000;
  private static final int LABEL_SHADOW_OUTER_COLOR = 0x12000000;
  private static final int LABEL_SHADOW_CORE_PADDING = 2;
  private static final int LABEL_SHADOW_OUTER_SPREAD = 1;
  private static final int LABEL_SHADOW_CORE_RADIUS = 3;
  private static final int LABEL_SHADOW_OUTER_RADIUS = 4;
  private static final int TITLE_Y = 18;
  private static final int GUIDANCE_Y = 32;
  private static final String ENABLED_MODULE_GUIDANCE = "Drag to move. Right-click for actions.";
  private static final String EMPTY_MODULE_GUIDANCE = "No HUD modules enabled";

  private final HudModuleRegistry modules;
  private final HudSelection selection;
  private final HudModuleSelection moduleSelection;
  private final HudDragController dragController;
  private final HudBoxSelectionController boxSelectionController;
  private final HudResizeController resizeController;
  private final int handleSize;
  private final int layoutButtonSize;
  private List<HudRectangle> visibleModuleBounds = List.of();

  public HudEditorRenderer(
      HudModuleRegistry modules,
      HudSelection selection,
      HudModuleSelection moduleSelection,
      HudDragController dragController,
      HudBoxSelectionController boxSelectionController,
      HudResizeController resizeController,
      int handleSize,
      int layoutButtonSize) {
    this.modules = modules;
    this.selection = selection;
    this.moduleSelection = moduleSelection;
    this.dragController = dragController;
    this.boxSelectionController = boxSelectionController;
    this.resizeController = resizeController;
    this.handleSize = handleSize;
    this.layoutButtonSize = layoutButtonSize;
  }

  public boolean renderEditorLayer(
      GuiGraphicsExtractor graphics,
      Font font,
      int screenWidth,
      int screenHeight,
      int mouseX,
      int mouseY) {
    List<ModuleLayout> layouts = new ArrayList<>();
    List<HudRectangle> rectangles = new ArrayList<>();
    for (HudModule module : modules.modules()) {
      if (module.enabled()) {
        HudRectangle bounds = selection.rectangle(module, screenWidth, screenHeight);
        layouts.add(
            new ModuleLayout(
                module, bounds, HudOrientationResolver.resolve(bounds, screenWidth, screenHeight)));
        rectangles.add(bounds);
      }
    }
    visibleModuleBounds = List.copyOf(rectangles);
    Set<String> attachmentRootIds = attachmentRootIds(layouts);

    renderSnapGuide(graphics, screenWidth, screenHeight);
    for (ModuleLayout layout : layouts) {
      layout
          .module()
          .renderEditorPreview(
              graphics, font, layout.bounds(), screenWidth, screenHeight, modules.gameState());
    }
    renderSelectedModules(graphics, layouts);
    renderModuleHover(graphics, layouts, attachmentRootIds, focusedModule(), mouseX, mouseY);
    renderModuleOutlines(graphics, layouts, rectangles, attachmentRootIds);
    renderResizeHandles(graphics, layouts);
    renderLayoutButtons(graphics, layouts, mouseX, mouseY);
    renderSelectionBox(graphics);
    renderResizePercent(graphics, font, screenWidth, screenHeight, mouseX, mouseY);
    return !layouts.isEmpty();
  }

  public void renderLabels(
      GuiGraphicsExtractor graphics,
      Font font,
      Component title,
      int screenWidth,
      boolean hasEnabledModules,
      float titleProgress,
      float guidanceProgress,
      float titleShadowProgress,
      float guidanceShadowProgress) {
    HudRectangle titleBounds =
        centeredTextBounds(font.width(title), screenWidth / 2, TITLE_Y, font.lineHeight);
    if (overlapsVisibleModule(titleBounds) && titleShadowProgress > 0.0F) {
      renderLabelBackground(graphics, titleBounds, titleShadowProgress);
    }
    graphics.centeredText(
        font, title, screenWidth / 2, TITLE_Y, withAlpha(0xFFFFFFFF, titleProgress));
    String guidance = guidanceText(hasEnabledModules);
    HudRectangle guidanceBounds =
        centeredTextBounds(font.width(guidance), screenWidth / 2, GUIDANCE_Y, font.lineHeight);
    if (overlapsVisibleModule(guidanceBounds) && guidanceShadowProgress > 0.0F) {
      renderLabelBackground(graphics, guidanceBounds, guidanceShadowProgress);
    }
    graphics.centeredText(
        font, guidance, screenWidth / 2, GUIDANCE_Y, withAlpha(0xFF9CA3AF, guidanceProgress));
  }

  public boolean titleOverlapsModule(Font font, Component title, int screenWidth) {
    return overlapsVisibleModule(
        centeredTextBounds(font.width(title), screenWidth / 2, TITLE_Y, font.lineHeight));
  }

  public boolean guidanceOverlapsModule(Font font, int screenWidth) {
    String guidance = guidanceText(true);
    return overlapsVisibleModule(
        centeredTextBounds(font.width(guidance), screenWidth / 2, GUIDANCE_Y, font.lineHeight));
  }

  private boolean overlapsVisibleModule(HudRectangle bounds) {
    for (HudRectangle moduleBounds : visibleModuleBounds) {
      if (moduleBounds.intersects(bounds)) {
        return true;
      }
    }
    return false;
  }

  private static String guidanceText(boolean hasEnabledModules) {
    return hasEnabledModules ? ENABLED_MODULE_GUIDANCE : EMPTY_MODULE_GUIDANCE;
  }

  private static HudRectangle centeredTextBounds(
      int textWidth, int centerX, int topY, int textHeight) {
    return new HudRectangle(centerX - textWidth / 2, topY, textWidth, textHeight);
  }

  private static void renderLabelBackground(
      GuiGraphicsExtractor graphics, HudRectangle bounds, float alpha) {
    int coreLeft = bounds.x() - LABEL_SHADOW_CORE_PADDING;
    int coreTop = bounds.y() - LABEL_SHADOW_CORE_PADDING;
    int coreRight = bounds.right() + LABEL_SHADOW_CORE_PADDING;
    int coreBottom = bounds.bottom() + LABEL_SHADOW_CORE_PADDING;
    drawRoundedFill(
        graphics,
        coreLeft - LABEL_SHADOW_OUTER_SPREAD,
        coreTop - LABEL_SHADOW_OUTER_SPREAD,
        coreRight + LABEL_SHADOW_OUTER_SPREAD,
        coreBottom + LABEL_SHADOW_OUTER_SPREAD,
        LABEL_SHADOW_OUTER_RADIUS,
        withAlpha(LABEL_SHADOW_OUTER_COLOR, alpha));
    drawRoundedFill(
        graphics,
        coreLeft,
        coreTop,
        coreRight,
        coreBottom,
        LABEL_SHADOW_CORE_RADIUS,
        withAlpha(LABEL_SHADOW_CORE_COLOR, alpha));
  }

  private static void drawRoundedFill(
      GuiGraphicsExtractor graphics,
      int left,
      int top,
      int right,
      int bottom,
      int radius,
      int color) {
    for (int y = top; y < bottom; y++) {
      int edgeDistance = Math.min(y - top, bottom - y - 1);
      int inset = Math.max(0, radius - edgeDistance - 1);
      graphics.fill(left + inset, y, right - inset, y + 1, color);
    }
  }

  private void renderModuleOutlines(
      GuiGraphicsExtractor graphics,
      List<ModuleLayout> layouts,
      List<HudRectangle> rectangles,
      Set<String> attachedTargetIds) {
    for (ModuleLayout layout : layouts) {
      HudRectangle rectangle = layout.bounds();
      int color =
          attachedTargetIds.contains(layout.module().id().toString())
              ? ATTACHMENT_ANCHOR_OUTLINE_COLOR
              : OUTLINE_COLOR;
      drawOutline(
          graphics, rectangle, HudOutlineResolver.visibleSegments(rectangle, rectangles), color);
    }
  }

  private void renderSelectedModules(GuiGraphicsExtractor graphics, List<ModuleLayout> layouts) {
    for (ModuleLayout layout : layouts) {
      if (moduleSelection.selected(layout.module())) {
        renderHoverFill(graphics, layout.bounds());
      }
    }
  }

  private static void renderModuleHover(
      GuiGraphicsExtractor graphics,
      List<ModuleLayout> layouts,
      Set<String> attachmentRootIds,
      HudModule focusedModule,
      int mouseX,
      int mouseY) {
    if (focusedModule != null) {
      renderFocusedModuleHover(graphics, layouts, attachmentRootIds, focusedModule);
      return;
    }

    for (ModuleLayout layout : layouts.reversed()) {
      HudRectangle rectangle = layout.bounds();
      if (rectangle.contains(mouseX, mouseY)) {
        String moduleId = layout.module().id().toString();
        if (attachmentRootIds.contains(moduleId)) {
          renderAttachmentGroupHover(graphics, layouts, moduleId);
        } else {
          renderHoverFill(graphics, rectangle);
        }
        return;
      }
    }
  }

  private HudModule focusedModule() {
    HudModule resizedModule = resizeController.activeModule();
    return resizedModule != null ? resizedModule : dragController.activeModule();
  }

  private static void renderFocusedModuleHover(
      GuiGraphicsExtractor graphics,
      List<ModuleLayout> layouts,
      Set<String> attachmentRootIds,
      HudModule focusedModule) {
    String moduleId = focusedModule.id().toString();
    if (attachmentRootIds.contains(moduleId)) {
      renderAttachmentGroupHover(graphics, layouts, moduleId);
      return;
    }
    ModuleLayout layout = layoutById(layouts, moduleId);
    if (layout != null) {
      renderHoverFill(graphics, layout.bounds());
    }
  }

  private static void renderAttachmentGroupHover(
      GuiGraphicsExtractor graphics, List<ModuleLayout> layouts, String rootId) {
    for (ModuleLayout layout : layouts) {
      if (rootId.equals(attachmentRootId(layout, layouts))) {
        renderHoverFill(graphics, layout.bounds());
      }
    }
  }

  private static void renderHoverFill(GuiGraphicsExtractor graphics, HudRectangle rectangle) {
    graphics.fill(
        rectangle.x(), rectangle.y(), rectangle.right(), rectangle.bottom(), HOVER_FILL_COLOR);
  }

  private static Set<String> attachmentRootIds(List<ModuleLayout> layouts) {
    Set<String> attachedModuleIds = new HashSet<>();
    Set<String> roots = new HashSet<>();
    Set<String> visibleModuleIds = new HashSet<>();
    for (ModuleLayout layout : layouts) {
      visibleModuleIds.add(layout.module().id().toString());
    }

    for (ModuleLayout layout : layouts) {
      if (visibleModuleIds.contains(layout.module().attachmentTargetId())) {
        attachedModuleIds.add(layout.module().id().toString());
      }
    }

    for (ModuleLayout layout : layouts) {
      String moduleId = layout.module().id().toString();
      if (!attachedModuleIds.contains(moduleId) && hasAttachedDescendant(moduleId, layouts)) {
        roots.add(moduleId);
      }
    }
    return roots;
  }

  private static boolean hasAttachedDescendant(String moduleId, List<ModuleLayout> layouts) {
    for (ModuleLayout layout : layouts) {
      if (moduleId.equals(layout.module().attachmentTargetId())) {
        return true;
      }
    }
    return false;
  }

  private static String attachmentRootId(ModuleLayout layout, List<ModuleLayout> layouts) {
    String currentId = layout.module().id().toString();
    for (int depth = 0; depth <= layouts.size(); depth++) {
      String parentId = attachmentTargetId(currentId, layouts);
      if (parentId == null) {
        return currentId;
      }
      currentId = parentId;
    }
    return currentId;
  }

  private static String attachmentTargetId(String moduleId, List<ModuleLayout> layouts) {
    for (ModuleLayout layout : layouts) {
      if (moduleId.equals(layout.module().id().toString())) {
        return layout.module().attachmentTargetId();
      }
    }
    return null;
  }

  private static ModuleLayout layoutById(List<ModuleLayout> layouts, String moduleId) {
    for (ModuleLayout layout : layouts) {
      if (moduleId.equals(layout.module().id().toString())) {
        return layout;
      }
    }
    return null;
  }

  private void renderResizeHandles(GuiGraphicsExtractor graphics, List<ModuleLayout> layouts) {
    for (ModuleLayout layout : layouts) {
      if (layout.module() instanceof ResizableHudModule) {
        HudRectangle handle =
            selection.resizeHandle(
                layout.bounds(), layout.orientation().cornerFacingCenter(), handleSize);
        graphics.fill(handle.x(), handle.y(), handle.right(), handle.bottom(), RESIZE_HANDLE_COLOR);
      }
    }
  }

  private void renderLayoutButtons(
      GuiGraphicsExtractor graphics, List<ModuleLayout> layouts, int mouseX, int mouseY) {
    for (ModuleLayout layout : layouts) {
      if (layout.module() instanceof AdaptiveLayoutHudModule adaptive
          && adaptive.availableLayouts().size() > 1) {
        HudRectangle button = selection.layoutButton(layout.bounds(), layoutButtonSize);
        boolean hovered = button.contains(mouseX, mouseY);
        renderCycleGlyph(
            graphics, button, hovered ? LAYOUT_BUTTON_ICON_HOVER_COLOR : LAYOUT_BUTTON_ICON_COLOR);
      }
    }
  }

  private static void renderCycleGlyph(
      GuiGraphicsExtractor graphics, HudRectangle button, int color) {
    int x = button.x() + 3;
    int y = button.y() + 3;
    graphics.fill(x + 1, y, x + 5, y + 2, color);
    graphics.fill(x + 5, y + 1, x + 7, y + 4, color);
    graphics.fill(x + 4, y + 3, x + 7, y + 5, color);
    graphics.fill(x + 2, y + 5, x + 6, y + 7, color);
    graphics.fill(x, y + 3, x + 2, y + 6, color);
    graphics.fill(x, y + 2, x + 3, y + 4, color);
  }

  private void renderSelectionBox(GuiGraphicsExtractor graphics) {
    if (!boxSelectionController.active()) {
      return;
    }
    HudRectangle bounds = boxSelectionController.bounds();
    graphics.fill(
        bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), SELECTION_BOX_FILL_COLOR);
    graphics.outline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), OUTLINE_COLOR);
  }

  private void renderSnapGuide(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
    for (HudSnapGuide guide : dragController.snapGuides()) {
      HudRectangle bounds = snapGuideLineBounds(guide, screenWidth, screenHeight);
      if (bounds != null) {
        graphics.fill(
            bounds.x(),
            bounds.y(),
            bounds.right(),
            bounds.bottom(),
            ATTACHMENT_ANCHOR_OUTLINE_COLOR);
      }
    }
  }

  static HudRectangle snapGuideLineBounds(HudSnapGuide guide, int screenWidth, int screenHeight) {
    if (guide.axis() == HudSnapGuide.Axis.VERTICAL) {
      if (guide.coordinate() <= 0 || guide.coordinate() >= screenWidth - 1 || screenHeight <= 2) {
        return null;
      }
      return new HudRectangle(guide.coordinate(), 1, 1, screenHeight - 2);
    }
    if (guide.coordinate() <= 0 || guide.coordinate() >= screenHeight - 1 || screenWidth <= 2) {
      return null;
    }
    return new HudRectangle(1, guide.coordinate(), screenWidth - 2, 1);
  }

  private void renderResizePercent(
      GuiGraphicsExtractor graphics,
      Font font,
      int screenWidth,
      int screenHeight,
      int mouseX,
      int mouseY) {
    ResizableHudModule resizedModule = resizeController.activeModule();
    if (resizedModule == null) {
      return;
    }

    String percent = HudModuleSettings.displayPercent(resizedModule.scale()) + "%";
    int textWidth = font.width(percent);
    int textX = clampToScreen(mouseX - textWidth / 2, Math.max(0, screenWidth - textWidth));
    int textY = clampToScreen(mouseY + 8, Math.max(0, screenHeight - font.lineHeight));
    graphics.text(font, percent, textX, textY, 0xFFFFFFFF, true);
  }

  private static void drawOutline(
      GuiGraphicsExtractor graphics,
      HudRectangle rectangle,
      HudOutlineSegments segments,
      int color) {
    for (HudOutlineSegment segment : segments.top()) {
      graphics.fill(segment.start(), rectangle.y() - 1, segment.end(), rectangle.y(), color);
    }
    for (HudOutlineSegment segment : segments.right()) {
      graphics.fill(
          rectangle.right(), segment.start(), rectangle.right() + 1, segment.end(), color);
    }
    for (HudOutlineSegment segment : segments.bottom()) {
      graphics.fill(
          segment.start(), rectangle.bottom(), segment.end(), rectangle.bottom() + 1, color);
    }
    for (HudOutlineSegment segment : segments.left()) {
      graphics.fill(rectangle.x() - 1, segment.start(), rectangle.x(), segment.end(), color);
    }
  }

  private static int clampToScreen(int value, int maximum) {
    return Math.clamp(value, 0, maximum);
  }

  private static int withAlpha(int color, float alpha) {
    int originalAlpha = color >>> 24;
    int animatedAlpha = Math.round(originalAlpha * Math.clamp(alpha, 0.0F, 1.0F));
    return (animatedAlpha << 24) | (color & 0x00FFFFFF);
  }

  private record ModuleLayout(HudModule module, HudRectangle bounds, HudOrientation orientation) {}
}
