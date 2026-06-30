package dev.combatlab.client.screen.hudeditor;

import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudContextMenu {
  private static final int MAIN_WIDTH = 112;
  private static final int SUBMENU_WIDTH = 132;
  private static final int ITEM_HEIGHT = 14;
  private static final int VISIBLE_SUBMENU_ITEMS = 8;
  private static final int PADDING = 3;
  private static final int BACKGROUND_COLOR = 0xDD111827;
  private static final int BORDER_COLOR = 0xAA60A5FA;
  private static final int HOVER_COLOR = 0x554B5563;
  private static final int DISABLED_TEXT_COLOR = 0xFF9CA3AF;
  private static final int TEXT_COLOR = 0xFFE5E7EB;

  private final HudModuleRegistry modules;
  private final HudEditorModuleActions moduleActions;
  private HudModule module;
  private List<HudModule> selectedModules = List.of();
  private MenuType type = MenuType.CLOSED;
  private int x;
  private int y;
  private int addX;
  private int addY;
  private int screenWidth;
  private int screenHeight;
  private int scrollOffset;
  private boolean addSubmenuOpen;

  public HudContextMenu(HudModuleRegistry modules, HudEditorModuleActions moduleActions) {
    this.modules = modules;
    this.moduleActions = moduleActions;
  }

  public void openModuleMenu(
      HudModule module, int requestedX, int requestedY, int screenWidth, int screenHeight) {
    this.module = module;
    this.selectedModules = List.of();
    this.type = MenuType.MODULE;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.scrollOffset = 0;
    this.addSubmenuOpen = false;
    this.x = Math.clamp(requestedX, 0, Math.max(0, screenWidth - MAIN_WIDTH));
    this.y = Math.clamp(requestedY, 0, Math.max(0, screenHeight - mainHeight()));
  }

  public void openCanvasMenu(int requestedX, int requestedY, int screenWidth, int screenHeight) {
    this.module = null;
    this.selectedModules = List.of();
    this.type = MenuType.CANVAS;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.addX = requestedX;
    this.addY = requestedY;
    this.scrollOffset = 0;
    this.addSubmenuOpen = false;
    this.x = Math.clamp(requestedX, 0, Math.max(0, screenWidth - MAIN_WIDTH));
    this.y = Math.clamp(requestedY, 0, Math.max(0, screenHeight - mainHeight()));
  }

  public void openSelectionMenu(
      List<HudModule> selectedModules,
      int requestedX,
      int requestedY,
      int screenWidth,
      int screenHeight) {
    this.module = null;
    this.selectedModules = List.copyOf(selectedModules);
    this.type = MenuType.SELECTION;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.scrollOffset = 0;
    this.addSubmenuOpen = false;
    this.x = Math.clamp(requestedX, 0, Math.max(0, screenWidth - MAIN_WIDTH));
    this.y = Math.clamp(requestedY, 0, Math.max(0, screenHeight - mainHeight()));
  }

  public void close() {
    module = null;
    selectedModules = List.of();
    type = MenuType.CLOSED;
    scrollOffset = 0;
    addSubmenuOpen = false;
  }

  public boolean open() {
    return type != MenuType.CLOSED;
  }

  public boolean mouseClicked(double mouseX, double mouseY) {
    if (!open()) {
      return false;
    }

    if (type == MenuType.MODULE
        && itemIndexAt(mouseX, mouseY, x, y, MAIN_WIDTH, mainHeight()) == 0) {
      moduleActions.disable(module, screenWidth, screenHeight);
      close();
      return true;
    }

    if (type == MenuType.SELECTION
        && itemIndexAt(mouseX, mouseY, x, y, MAIN_WIDTH, mainHeight()) == 0) {
      moduleActions.disableAll(selectedModules, screenWidth, screenHeight);
      close();
      return true;
    }

    if (type == MenuType.CANVAS) {
      int submenuIndex = addSubmenuOpen ? submenuItemIndexAt(mouseX, mouseY) : -1;
      if (submenuIndex >= 0) {
        List<HudModuleDescriptor> disabledModules = disabledModules();
        int descriptorIndex = scrollOffset + submenuIndex;
        if (descriptorIndex < disabledModules.size()) {
          moduleActions.enableAt(
              disabledModules.get(descriptorIndex).id(), addX, addY, screenWidth, screenHeight);
          close();
          return true;
        }
      }
      if (itemIndexAt(mouseX, mouseY, x, y, MAIN_WIDTH, mainHeight()) == 0) {
        addSubmenuOpen = true;
        return true;
      }
    }

    close();
    return true;
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
    if (type != MenuType.CANVAS || !addSubmenuOpen || !submenuBounds().contains(mouseX, mouseY)) {
      return false;
    }

    int maxScrollOffset = maxScrollOffset();
    if (maxScrollOffset <= 0) {
      return true;
    }

    scrollOffset = Math.clamp(scrollOffset - (int) Math.signum(verticalAmount), 0, maxScrollOffset);
    return true;
  }

  public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
    if (!open()) {
      return;
    }

    graphics.fill(x, y, x + MAIN_WIDTH, y + mainHeight(), BACKGROUND_COLOR);
    graphics.outline(x, y, MAIN_WIDTH, mainHeight(), BORDER_COLOR);
    if (type == MenuType.MODULE || type == MenuType.SELECTION) {
      renderItem(
          graphics,
          font,
          "Disable",
          x,
          y,
          MAIN_WIDTH,
          itemIndexAt(mouseX, mouseY, x, y, MAIN_WIDTH, mainHeight()) == 0,
          TEXT_COLOR);
      return;
    }

    boolean addHovered = itemIndexAt(mouseX, mouseY, x, y, MAIN_WIDTH, mainHeight()) == 0;
    boolean submenuHovered = addSubmenuOpen && submenuBounds().contains(mouseX, mouseY);
    if (addHovered) {
      addSubmenuOpen = true;
    } else if (!submenuHovered) {
      addSubmenuOpen = false;
    }
    renderItem(graphics, font, "Add HUD Module >", x, y, MAIN_WIDTH, addHovered, TEXT_COLOR);
    if (addSubmenuOpen) {
      renderSubmenu(graphics, font, mouseX, mouseY);
    }
  }

  private void renderSubmenu(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
    HudRectangleLike bounds = submenuBounds();
    List<HudModuleDescriptor> disabledModules = disabledModules();
    graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), BACKGROUND_COLOR);
    graphics.outline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), BORDER_COLOR);

    if (disabledModules.isEmpty()) {
      renderItem(
          graphics,
          font,
          "No disabled modules",
          bounds.x(),
          bounds.y(),
          bounds.width(),
          false,
          DISABLED_TEXT_COLOR);
      return;
    }

    int visibleItems = visibleSubmenuItems(disabledModules.size());
    int hoveredIndex = submenuItemIndexAt(mouseX, mouseY);
    for (int index = 0; index < visibleItems; index++) {
      HudModuleDescriptor descriptor = disabledModules.get(scrollOffset + index);
      renderItem(
          graphics,
          font,
          descriptor.definition().displayName().getString(),
          bounds.x(),
          bounds.y() + index * ITEM_HEIGHT,
          bounds.width(),
          hoveredIndex == index,
          TEXT_COLOR);
    }
  }

  private void renderItem(
      GuiGraphicsExtractor graphics,
      Font font,
      String label,
      int itemX,
      int itemY,
      int width,
      boolean hovered,
      int textColor) {
    if (hovered) {
      graphics.fill(itemX + 1, itemY + 1, itemX + width - 1, itemY + ITEM_HEIGHT - 1, HOVER_COLOR);
    }
    graphics.text(font, label, itemX + PADDING, itemY + 3, textColor, true);
  }

  private List<HudModuleDescriptor> disabledModules() {
    return modules.descriptors().stream()
        .filter(descriptor -> !modules.enabled(descriptor.id()))
        .toList();
  }

  private HudRectangleLike submenuBounds() {
    List<HudModuleDescriptor> disabledModules = disabledModules();
    int height = Math.max(1, visibleSubmenuItems(disabledModules.size())) * ITEM_HEIGHT;
    int submenuX = x + MAIN_WIDTH;
    if (submenuX + SUBMENU_WIDTH > screenWidth) {
      submenuX = x - SUBMENU_WIDTH;
    }
    submenuX = Math.clamp(submenuX, 0, Math.max(0, screenWidth - SUBMENU_WIDTH));
    int submenuY = Math.clamp(y, 0, Math.max(0, screenHeight - height));
    return new HudRectangleLike(submenuX, submenuY, SUBMENU_WIDTH, height);
  }

  private int submenuItemIndexAt(double mouseX, double mouseY) {
    HudRectangleLike bounds = submenuBounds();
    int visibleItems = visibleSubmenuItems(disabledModules().size());
    int index =
        itemIndexAt(
            mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), visibleItems * ITEM_HEIGHT);
    return index >= visibleItems ? -1 : index;
  }

  private int itemIndexAt(
      double mouseX, double mouseY, int itemX, int itemY, int width, int height) {
    if (mouseX < itemX || mouseX >= itemX + width || mouseY < itemY || mouseY >= itemY + height) {
      return -1;
    }
    return Math.clamp(
        (int) ((mouseY - itemY) / ITEM_HEIGHT), 0, Math.max(0, height / ITEM_HEIGHT - 1));
  }

  private int mainHeight() {
    return ITEM_HEIGHT;
  }

  private int visibleSubmenuItems(int itemCount) {
    return Math.clamp(itemCount, 1, VISIBLE_SUBMENU_ITEMS);
  }

  private int maxScrollOffset() {
    int itemCount = disabledModules().size();
    return Math.max(0, itemCount - visibleSubmenuItems(itemCount));
  }

  private enum MenuType {
    CLOSED,
    MODULE,
    SELECTION,
    CANVAS
  }

  private record HudRectangleLike(int x, int y, int width, int height) {
    int right() {
      return x + width;
    }

    int bottom() {
      return y + height;
    }

    boolean contains(double mouseX, double mouseY) {
      return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
    }
  }
}
