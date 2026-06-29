package dev.combatlab.client.screen.hudeditor;

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

  private final HudModuleRegistry modules;
  private final HudSelection selection;
  private final HudModuleSelection moduleSelection;
  private final HudDragController dragController;
  private final HudBoxSelectionController boxSelectionController;
  private final HudResizeController resizeController;
  private final int handleSize;

  public HudEditorRenderer(
      HudModuleRegistry modules,
      HudSelection selection,
      HudModuleSelection moduleSelection,
      HudDragController dragController,
      HudBoxSelectionController boxSelectionController,
      HudResizeController resizeController,
      int handleSize) {
    this.modules = modules;
    this.selection = selection;
    this.moduleSelection = moduleSelection;
    this.dragController = dragController;
    this.boxSelectionController = boxSelectionController;
    this.resizeController = resizeController;
    this.handleSize = handleSize;
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
    Set<String> attachmentRootIds = attachmentRootIds(layouts);

    renderSnapGuide(graphics, screenWidth, screenHeight);
    for (ModuleLayout layout : layouts) {
      layout
          .module()
          .renderEditorPreview(
              graphics, font, layout.bounds(), screenWidth, screenHeight, modules.gameState());
    }
    renderSelectedModules(graphics, layouts);
    renderModuleHover(
        graphics, layouts, attachmentRootIds, dragController.activeModule(), mouseX, mouseY);
    renderModuleOutlines(graphics, layouts, rectangles, attachmentRootIds);
    renderResizeHandles(graphics, layouts);
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
      float animationProgress) {
    graphics.centeredText(
        font, title, screenWidth / 2, 18, withAlpha(0xFFFFFFFF, animationProgress));
    String guidance =
        hasEnabledModules
            ? "Drag HUD modules to reposition them; right-click a module for actions"
            : "No HUD modules enabled";
    graphics.centeredText(
        font, guidance, screenWidth / 2, 32, withAlpha(0xFF9CA3AF, animationProgress));
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
    for (ModuleLayout layout : layouts) {
      if (layout.module().attachmentTargetId() != null) {
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
      if (guide.axis() == HudSnapGuide.Axis.VERTICAL) {
        graphics.fill(
            guide.coordinate(),
            0,
            guide.coordinate() + 1,
            screenHeight,
            ATTACHMENT_ANCHOR_OUTLINE_COLOR);
      } else {
        graphics.fill(
            0,
            guide.coordinate(),
            screenWidth,
            guide.coordinate() + 1,
            ATTACHMENT_ANCHOR_OUTLINE_COLOR);
      }
    }
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

    String percent = Math.round(resizedModule.scale() * 100.0) + "%";
    int textWidth = font.width(percent);
    int textX = clamp(mouseX - textWidth / 2, 0, Math.max(0, screenWidth - textWidth));
    int textY = clamp(mouseY + 8, 0, Math.max(0, screenHeight - font.lineHeight));
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

  private static int clamp(int value, int minimum, int maximum) {
    return Math.clamp(value, minimum, maximum);
  }

  private static int withAlpha(int color, float alpha) {
    int originalAlpha = color >>> 24;
    int animatedAlpha = Math.round(originalAlpha * Math.clamp(alpha, 0.0F, 1.0F));
    return (animatedAlpha << 24) | (color & 0x00FFFFFF);
  }

  private record ModuleLayout(HudModule module, HudRectangle bounds, HudOrientation orientation) {}
}
