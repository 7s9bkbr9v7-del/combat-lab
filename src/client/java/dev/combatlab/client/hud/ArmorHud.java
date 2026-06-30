package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ArmorHud extends ResizableBaseHudModule implements AdaptiveLayoutHudModule {
  private static final int PADDING = 1;
  private static final int ITEM_SIZE = 16;
  private static final HudModuleDefinition DEFINITION =
      new HudModuleDefinition(
          Identifier.fromNamespaceAndPath("combatlab", "armor"),
          Component.literal("Armor HUD"),
          0.02,
          0.50,
          true);

  public static HudModuleDescriptor descriptor() {
    return new HudModuleDescriptor(
        DEFINITION, dependencies -> new ArmorHud(dependencies.options(), dependencies.debug()));
  }

  private ArmorHudLayout lockedLayout;
  private ArmorHudLayout overrideLayout;
  private ArmorHudLayout floatingLayout = ArmorHudLayout.GRID;

  public ArmorHud(CombatLabOptions options, DebugLogger debug) {
    super(DEFINITION, options, debug);
    floatingLayout = storedLayout();
  }

  @Override
  public HudSize unscaledSize() {
    ArmorHudLayout layout = layout();
    return new HudSize(
        PADDING * 2 + ITEM_SIZE * layout.columns(), PADDING * 2 + ITEM_SIZE * layout.rows());
  }

  @Override
  public List<String> availableLayouts() {
    return List.of(
        ADAPTIVE_LAYOUT,
        ArmorHudLayout.VERTICAL.name(),
        ArmorHudLayout.HORIZONTAL.name(),
        ArmorHudLayout.GRID.name());
  }

  @Override
  public String currentLayout() {
    return overrideLayout == null ? ADAPTIVE_LAYOUT : overrideLayout.name();
  }

  @Override
  public void cycleLayout() {
    ArmorHudLayout adaptiveLayout = resolvedLayout();
    ArmorHudLayout nextLayout =
        overrideLayout == null ? adaptiveLayout.next() : overrideLayout.next();
    overrideLayout = nextLayout == adaptiveLayout ? null : nextLayout;
  }

  @Override
  public void lockLayout() {
    lockedLayout = layout();
  }

  @Override
  public void unlockLayout() {
    if (snappedToAdaptiveEdge()) {
      overrideLayout = null;
    }
    floatingLayout =
        ArmorHudLayout.resolve(
            settings().normalizedX(),
            settings().normalizedY(),
            lockedLayout != null ? lockedLayout : floatingLayout);
    lockedLayout = null;
  }

  @Override
  protected void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
    graphics.pose().pushMatrix();
    graphics.pose().translate(context.bounds().x(), context.bounds().y());
    graphics.pose().scale((float) scale(), (float) scale());
    ArmorHudLayout layout = layout();
    for (int index = 0; index < layout.slots().size(); index++) {
      ItemStack stack = context.hud().armor().stack(layout.slots().get(index));
      if (stack.isEmpty()) {
        continue;
      }

      int x = PADDING + index % layout.columns() * ITEM_SIZE;
      int y = PADDING + index / layout.columns() * ITEM_SIZE;
      graphics.item(stack, x, y, 0);
      graphics.itemDecorations(context.font(), stack, x, y);
    }
    graphics.pose().popMatrix();
  }

  private ArmorHudLayout layout() {
    if (lockedLayout != null) {
      return lockedLayout;
    }
    return overrideLayout != null ? overrideLayout : resolvedLayout();
  }

  private ArmorHudLayout resolvedLayout() {
    return ArmorHudLayout.resolve(
        settings().normalizedX(), settings().normalizedY(), floatingLayout);
  }

  private ArmorHudLayout storedLayout() {
    return ArmorHudLayout.fromStored(settings().layout());
  }

  private boolean snappedToAdaptiveEdge() {
    HudEdgeContact edgeContact =
        HudEdgeContact.fromNormalizedPosition(settings().normalizedX(), settings().normalizedY());
    return edgeContact.sideEdge() || edgeContact.topOrBottomEdge();
  }
}
