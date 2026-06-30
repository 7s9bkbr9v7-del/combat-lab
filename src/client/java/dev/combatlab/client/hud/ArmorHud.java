package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ArmorSlot;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ArmorHud extends ResizableBaseHudModule implements AdaptiveLayoutHudModule {
  private static final int PADDING = 1;
  private static final int ITEM_SIZE = 16;
  private static final int ARMOR_SLOT_COUNT = ArmorSlot.values().length;
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
  private ArmorHudLayout floatingLayout = ArmorHudLayout.GRID;
  private final double[] animationStartX = new double[ARMOR_SLOT_COUNT];
  private final double[] animationStartY = new double[ARMOR_SLOT_COUNT];
  private final double[] previewX = new double[ARMOR_SLOT_COUNT];
  private final double[] previewY = new double[ARMOR_SLOT_COUNT];
  private final HudLayoutTransition<ArmorHudLayout> layoutTransition = new HudLayoutTransition<>();
  private final HudAdaptiveLayoutAnimation<ArmorHudLayout> layoutAnimation =
      new HudAdaptiveLayoutAnimation<>();

  public ArmorHud(CombatLabOptions options, DebugLogger debug) {
    super(DEFINITION, options, debug);
    floatingLayout = storedLayout();
  }

  @Override
  public HudSize unscaledSize() {
    return unscaledSize(layout());
  }

  @Override
  public HudRectangle editorBounds(int screenWidth, int screenHeight) {
    ArmorHudLayout layout = layout();
    return layoutAnimation.editorBounds(
        this, layout, unscaledSize(layout), lockedLayout != null, screenWidth, screenHeight);
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
    ArmorHudLayout manualLayout = manualLayout();
    return manualLayout == null ? ADAPTIVE_LAYOUT : manualLayout.name();
  }

  @Override
  public void cycleLayout() {
    ArmorHudLayout manualLayout = manualLayout();
    ArmorHudLayout adaptiveLayout = resolvedLayout();
    ArmorHudLayout nextLayout = manualLayout == null ? adaptiveLayout.next() : manualLayout.next();
    settings().updateLayout(nextLayout == adaptiveLayout ? null : nextLayout.name());
    settings().save();
  }

  @Override
  public void lockLayout() {
    lockedLayout = layout();
  }

  @Override
  public void unlockLayout() {
    if (snappedToAdaptiveEdge()) {
      settings().updateLayout(null);
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
    updatePreviewPositions(context, layout);
    for (int index = 0; index < layout.slots().size(); index++) {
      ItemStack stack = context.hud().armor().stack(layout.slots().get(index));
      if (stack.isEmpty()) {
        continue;
      }

      ArmorSlot slot = layout.slots().get(index);
      int x = (int) Math.round(previewX[slot.ordinal()]);
      int y = (int) Math.round(previewY[slot.ordinal()]);
      graphics.item(stack, x, y, 0);
      graphics.itemDecorations(context.font(), stack, x, y);
    }
    graphics.pose().popMatrix();
  }

  private void updatePreviewPositions(HudRenderContext context, ArmorHudLayout layout) {
    if (!context.editorPreview()) {
      layoutTransition.reset();
      layoutAnimation.reset();
      setPreviewPositionsTo(layout);
      return;
    }

    if (lockedLayout != null) {
      layoutTransition.snapTo(layout);
      setPreviewPositionsTo(layout);
      return;
    }

    HudLayoutTransition.Update transition = layoutTransition.update(layout);
    if (transition.firstUpdate()) {
      setPreviewPositionsTo(layout);
      return;
    }

    if (transition.targetChanged()) {
      System.arraycopy(previewX, 0, animationStartX, 0, previewX.length);
      System.arraycopy(previewY, 0, animationStartY, 0, previewY.length);
    }

    for (ArmorSlot slot : ArmorSlot.values()) {
      int slotIndex = slot.ordinal();
      previewX[slotIndex] =
          HudLayoutTransition.lerp(
              animationStartX[slotIndex], slotX(layout, slot), transition.progress());
      previewY[slotIndex] =
          HudLayoutTransition.lerp(
              animationStartY[slotIndex], slotY(layout, slot), transition.progress());
    }
    if (transition.complete()) {
      setPreviewPositionsTo(layout);
    }
  }

  private void setPreviewPositionsTo(ArmorHudLayout layout) {
    for (ArmorSlot slot : ArmorSlot.values()) {
      int slotIndex = slot.ordinal();
      double x = slotX(layout, slot);
      double y = slotY(layout, slot);
      previewX[slotIndex] = x;
      previewY[slotIndex] = y;
      animationStartX[slotIndex] = x;
      animationStartY[slotIndex] = y;
    }
  }

  private static int slotX(ArmorHudLayout layout, ArmorSlot slot) {
    int index = layout.slots().indexOf(slot);
    return PADDING + index % layout.columns() * ITEM_SIZE;
  }

  private static int slotY(ArmorHudLayout layout, ArmorSlot slot) {
    int index = layout.slots().indexOf(slot);
    return PADDING + index / layout.columns() * ITEM_SIZE;
  }

  private ArmorHudLayout layout() {
    if (lockedLayout != null) {
      return lockedLayout;
    }
    ArmorHudLayout manualLayout = manualLayout();
    return manualLayout != null ? manualLayout : resolvedLayout();
  }

  private static HudSize unscaledSize(ArmorHudLayout layout) {
    return new HudSize(
        PADDING * 2 + ITEM_SIZE * layout.columns(), PADDING * 2 + ITEM_SIZE * layout.rows());
  }

  private ArmorHudLayout resolvedLayout() {
    return ArmorHudLayout.resolve(
        settings().normalizedX(), settings().normalizedY(), floatingLayout);
  }

  private ArmorHudLayout storedLayout() {
    return ArmorHudLayout.fromStored(settings().layout());
  }

  private ArmorHudLayout manualLayout() {
    String layout = settings().layout();
    if (layout == null || ADAPTIVE_LAYOUT.equals(layout)) {
      return null;
    }
    return ArmorHudLayout.fromStored(layout);
  }

  private boolean snappedToAdaptiveEdge() {
    HudEdgeContact edgeContact =
        HudEdgeContact.fromNormalizedPosition(settings().normalizedX(), settings().normalizedY());
    return edgeContact.sideEdge() || edgeContact.topOrBottomEdge();
  }
}
