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
  private static final long LAYOUT_ANIMATION_NANOS = 220_000_000L;
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
  private ArmorHudLayout overrideLayout;
  private ArmorHudLayout floatingLayout = ArmorHudLayout.GRID;
  private final double[] animationStartX = new double[ARMOR_SLOT_COUNT];
  private final double[] animationStartY = new double[ARMOR_SLOT_COUNT];
  private final double[] previewX = new double[ARMOR_SLOT_COUNT];
  private final double[] previewY = new double[ARMOR_SLOT_COUNT];
  private ArmorHudLayout previewLayout;
  private long layoutAnimationStartNanos;
  private boolean previewPositionsInitialized;

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
      previewPositionsInitialized = false;
      setPreviewPositionsTo(layout);
      return;
    }

    if (lockedLayout != null) {
      previewLayout = layout;
      previewPositionsInitialized = true;
      setPreviewPositionsTo(layout);
      return;
    }

    long nowNanos = System.nanoTime();
    if (!previewPositionsInitialized) {
      previewLayout = layout;
      layoutAnimationStartNanos = nowNanos - LAYOUT_ANIMATION_NANOS;
      previewPositionsInitialized = true;
      setPreviewPositionsTo(layout);
      return;
    }

    if (previewLayout != layout) {
      System.arraycopy(previewX, 0, animationStartX, 0, previewX.length);
      System.arraycopy(previewY, 0, animationStartY, 0, previewY.length);
      previewLayout = layout;
      layoutAnimationStartNanos = nowNanos;
    }

    double progress =
        Math.clamp(
            (double) (nowNanos - layoutAnimationStartNanos) / LAYOUT_ANIMATION_NANOS, 0.0D, 1.0D);
    double easedProgress = settleProgress(progress);
    for (ArmorSlot slot : ArmorSlot.values()) {
      int slotIndex = slot.ordinal();
      previewX[slotIndex] = lerp(animationStartX[slotIndex], slotX(layout, slot), easedProgress);
      previewY[slotIndex] = lerp(animationStartY[slotIndex], slotY(layout, slot), easedProgress);
    }
    if (progress >= 1.0D) {
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

  private static double settleProgress(double progress) {
    return progress * progress * progress * (progress * (progress * 6.0D - 15.0D) + 10.0D);
  }

  private static double lerp(double start, double end, double progress) {
    return start + (end - start) * progress;
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
