package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.DirectionState;
import dev.combatlab.client.state.PlayerState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class DirectionHud extends ResizableBaseHudModule implements AdaptiveLayoutHudModule {
  private static final long LAYOUT_ANIMATION_NANOS = 220_000_000L;
  private static final int WIDTH = 112;
  private static final int SIDE_WIDTH = 58;
  private static final int COMPASS_HEIGHT = 12;
  private static final int HEIGHT = 22;
  private static final int CONTENT_LEFT = 3;
  private static final int CONTENT_PADDING = 3;
  private static final int DEGREE_GAP = 1;
  private static final int LABEL_Y = 2;
  private static final int ACCENT_COLOR = 0xFF60A5FA;
  private static final double DEGREES_PER_PIXEL = 2.25D;
  private static final double SPRING_STRENGTH = 0.92D;
  private static final double FAST_TURN_EXTRA_STRENGTH = 0.28D;
  private static final double DAMPING = 0.44D;
  private static final double FAST_TURN_DAMPING = 0.38D;
  private static final double FAST_TURN_THRESHOLD = 60.0D;
  private static final double MAX_VELOCITY = 70.0D;
  private static final double FAST_TURN_MAX_VELOCITY = 108.0D;
  private static final double SNAP_DISPLACEMENT = 0.24D;
  private static final double SNAP_VELOCITY = 0.32D;
  private static final DirectionHudLayout FLOATING_LAYOUT =
      new DirectionHudLayout(WIDTH, DEGREES_PER_PIXEL);
  private static final DirectionHudLayout SIDE_LAYOUT =
      new DirectionHudLayout(SIDE_WIDTH, DEGREES_PER_PIXEL);
  private static final DirectionMark[] MARKS = {
    new DirectionMark("N", 0),
    new DirectionMark("NE", 45),
    new DirectionMark("E", 90),
    new DirectionMark("SE", 135),
    new DirectionMark("S", 180),
    new DirectionMark("SW", 225),
    new DirectionMark("W", 270),
    new DirectionMark("NW", 315)
  };
  private static final HudModuleDefinition DEFINITION =
      new HudModuleDefinition(
          Identifier.fromNamespaceAndPath("combatlab", "direction"),
          Component.literal("Direction"),
          0.5,
          0.08,
          true);
  private boolean bearingInitialized;
  private double previousAnimatedBearing;
  private double animatedBearing;
  private double bearingVelocity;
  private DirectionHudLayout lockedLayout;
  private DirectionHudLayout overrideLayout;
  private DirectionHudLayout floatingLayout = FLOATING_LAYOUT;
  private DirectionHudLayout previewLayout;
  private double previewWidth;
  private double layoutAnimationStartWidth;
  private long layoutAnimationStartNanos;
  private boolean previewWidthInitialized;

  public static HudModuleDescriptor descriptor() {
    return new HudModuleDescriptor(
        DEFINITION, dependencies -> new DirectionHud(dependencies.options(), dependencies.debug()));
  }

  public DirectionHud(CombatLabOptions options, DebugLogger debug) {
    super(DEFINITION, options, debug);
  }

  @Override
  public HudSize unscaledSize() {
    return layout().size();
  }

  @Override
  public List<String> availableLayouts() {
    return List.of(ADAPTIVE_LAYOUT, "FLOATING", "SIDE");
  }

  @Override
  public String currentLayout() {
    return overrideLayout == null ? ADAPTIVE_LAYOUT : layoutName(overrideLayout);
  }

  @Override
  public void cycleLayout() {
    DirectionHudLayout adaptiveLayout = resolvedLayout();
    DirectionHudLayout nextLayout =
        overrideLayout == null ? nextLayout(adaptiveLayout) : nextLayout(overrideLayout);
    overrideLayout = nextLayout == adaptiveLayout ? null : nextLayout;
  }

  @Override
  public void lockLayout() {
    lockedLayout = layout();
  }

  @Override
  public void unlockLayout() {
    HudEdgeContact edgeContact =
        HudEdgeContact.fromNormalizedPosition(settings().normalizedX(), settings().normalizedY());
    if (snappedToAdaptiveEdge()) {
      overrideLayout = null;
    }
    if (edgeContact.sideEdge()) {
      floatingLayout = SIDE_LAYOUT;
    } else if (edgeContact.topOrBottomEdge()) {
      floatingLayout = FLOATING_LAYOUT;
    } else {
      floatingLayout = lockedLayout != null ? lockedLayout : floatingLayout;
    }
    lockedLayout = null;
  }

  @Override
  public void tick(ClientGameState gameState) {
    DirectionState direction = direction(gameState);
    if (!direction.present()) {
      bearingInitialized = false;
      bearingVelocity = 0.0D;
      previousAnimatedBearing = 21.0D;
      animatedBearing = 21.0D;
      return;
    }

    double target = direction.bearingDegrees();
    if (!bearingInitialized) {
      previousAnimatedBearing = target;
      animatedBearing = target;
      bearingVelocity = 0.0D;
      bearingInitialized = true;
      return;
    }

    previousAnimatedBearing = animatedBearing;
    animateBearing(target);
  }

  @Override
  protected void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
    double bearing = renderBearing(context);

    graphics.pose().pushMatrix();
    graphics.pose().translate(context.bounds().x(), context.bounds().y());
    graphics.pose().scale((float) scale(), (float) scale());

    DirectionHudLayout layout = layout();
    int compassWidth = animatedCompassWidth(context, layout);
    int compassX = (layout.width() - compassWidth) / 2;
    int degreeY = degreeY(context);
    int compassY = compassY(context, context.font());
    graphics.fill(
        compassX, compassY, compassX + compassWidth, compassY + COMPASS_HEIGHT, 0x77000000);
    graphics.outline(compassX, compassY, compassWidth, COMPASS_HEIGHT, 0x44FFFFFF);
    graphics.fill(
        layout.centerX(),
        compassY + 1,
        layout.centerX() + 1,
        compassY + COMPASS_HEIGHT - 1,
        0x99D1D5DB);
    renderTicks(graphics, layout, compassY, bearing);
    renderMarks(graphics, context.font(), layout, compassY, bearing);
    renderDegree(graphics, context.font(), layout, degreeY, bearing);

    graphics.pose().popMatrix();
  }

  private int animatedCompassWidth(HudRenderContext context, DirectionHudLayout layout) {
    if (!context.editorPreview()) {
      previewWidthInitialized = false;
      return layout.width();
    }

    if (lockedLayout != null) {
      previewLayout = layout;
      previewWidth = layout.width();
      layoutAnimationStartWidth = previewWidth;
      previewWidthInitialized = true;
      return layout.width();
    }

    long nowNanos = System.nanoTime();
    if (!previewWidthInitialized) {
      previewLayout = layout;
      previewWidth = layout.width();
      layoutAnimationStartWidth = previewWidth;
      layoutAnimationStartNanos = nowNanos - LAYOUT_ANIMATION_NANOS;
      previewWidthInitialized = true;
      return layout.width();
    }

    if (previewLayout != layout) {
      previewLayout = layout;
      layoutAnimationStartWidth = previewWidth;
      layoutAnimationStartNanos = nowNanos;
    }

    double progress =
        Math.clamp(
            (double) (nowNanos - layoutAnimationStartNanos) / LAYOUT_ANIMATION_NANOS, 0.0D, 1.0D);
    previewWidth = lerp(layoutAnimationStartWidth, layout.width(), settleProgress(progress));
    if (progress >= 1.0D) {
      previewWidth = layout.width();
    }
    return Math.max(SIDE_WIDTH, (int) Math.round(previewWidth));
  }

  private double renderBearing(HudRenderContext context) {
    if (context.editorPreview()) {
      DirectionState direction = direction(context.gameState());
      return direction.present() ? direction.bearingDegrees() : 21.0D;
    }
    if (!bearingInitialized) {
      return 21.0D;
    }
    return interpolatedBearing(context.frameDeltaTicks());
  }

  private void animateBearing(double target) {
    double displacement = wrapDegrees(target - animatedBearing);
    if (Math.abs(displacement) <= SNAP_DISPLACEMENT && Math.abs(bearingVelocity) <= SNAP_VELOCITY) {
      animatedBearing = target;
      bearingVelocity = 0.0D;
      return;
    }

    double fastTurn = Math.min(1.0D, Math.abs(displacement) / FAST_TURN_THRESHOLD);
    double strength = SPRING_STRENGTH + FAST_TURN_EXTRA_STRENGTH * fastTurn;
    double damping = DAMPING + (FAST_TURN_DAMPING - DAMPING) * fastTurn;
    double maxVelocity = MAX_VELOCITY + (FAST_TURN_MAX_VELOCITY - MAX_VELOCITY) * fastTurn;
    bearingVelocity =
        clamp((bearingVelocity + displacement * strength) * damping, -maxVelocity, maxVelocity);
    animatedBearing = normalizeDegrees(animatedBearing + bearingVelocity);
  }

  private double interpolatedBearing(float frameDeltaTicks) {
    double frameTicks = clampFrameProgress(frameDeltaTicks);
    return normalizeDegrees(
        previousAnimatedBearing
            + wrapDegrees(animatedBearing - previousAnimatedBearing) * frameTicks);
  }

  private static double clampFrameProgress(float frameDeltaTicks) {
    if (!Float.isFinite(frameDeltaTicks) || frameDeltaTicks <= 0.0F) {
      return 0.0D;
    }
    return Math.min(frameDeltaTicks, 1.0F);
  }

  private static DirectionState direction(ClientGameState gameState) {
    PlayerState player = gameState.player();
    return player == null ? DirectionState.absent() : player.direction();
  }

  private static int degreeY(HudRenderContext context) {
    return context.orientation().verticalSideFacingCenter() == HudVerticalSide.TOP
        ? 0
        : COMPASS_HEIGHT + DEGREE_GAP;
  }

  private static int compassY(HudRenderContext context, Font font) {
    return context.orientation().verticalSideFacingCenter() == HudVerticalSide.TOP
        ? font.lineHeight + DEGREE_GAP
        : 0;
  }

  private DirectionHudLayout layout() {
    if (lockedLayout != null) {
      return lockedLayout;
    }
    return overrideLayout != null ? overrideLayout : resolvedLayout();
  }

  private DirectionHudLayout resolvedLayout() {
    return HudEdgeContact.fromNormalizedPosition(settings().normalizedX(), settings().normalizedY())
            .sideEdge()
        ? SIDE_LAYOUT
        : floatingLayout;
  }

  private boolean snappedToAdaptiveEdge() {
    HudEdgeContact edgeContact =
        HudEdgeContact.fromNormalizedPosition(settings().normalizedX(), settings().normalizedY());
    return edgeContact.sideEdge() || edgeContact.topOrBottomEdge();
  }

  private static DirectionHudLayout nextLayout(DirectionHudLayout layout) {
    return layout == SIDE_LAYOUT ? FLOATING_LAYOUT : SIDE_LAYOUT;
  }

  private static String layoutName(DirectionHudLayout layout) {
    return layout == SIDE_LAYOUT ? "SIDE" : "FLOATING";
  }

  private static void renderTicks(
      GuiGraphicsExtractor graphics, DirectionHudLayout layout, int compassY, double bearing) {
    for (int degrees = 0; degrees < 360; degrees += 15) {
      int x = xFor(layout, degrees, bearing);
      if (x < CONTENT_LEFT || x >= layout.contentRight()) {
        continue;
      }
      boolean cardinal = degrees % 45 == 0;
      int height = cardinal ? 3 : 2;
      int color = cardinal ? 0x77FFFFFF : 0x44FFFFFF;
      graphics.fill(
          x, compassY + COMPASS_HEIGHT - height - 1, x + 1, compassY + COMPASS_HEIGHT - 1, color);
    }
  }

  private static void renderMarks(
      GuiGraphicsExtractor graphics,
      Font font,
      DirectionHudLayout layout,
      int compassY,
      double bearing) {
    int labelY = compassY + LABEL_Y;
    List<VisibleDirectionMark> visibleMarks = new ArrayList<>(MARKS.length);
    int activeDirection = nearestCardinal(bearing);
    for (DirectionMark mark : MARKS) {
      int x = xFor(layout, mark.degrees(), bearing);
      int textWidth = font.width(mark.label());
      int textX = x - textWidth / 2;
      if (textX < CONTENT_LEFT || textX + textWidth > layout.contentRight()) {
        continue;
      }
      int color = mark.degrees() == activeDirection ? ACCENT_COLOR : 0xFFE5E7EB;
      visibleMarks.add(new VisibleDirectionMark(mark.label(), textX, textWidth, color));
    }

    visibleMarks.sort(Comparator.comparingInt(VisibleDirectionMark::textX));
    int lastTextRight = Integer.MIN_VALUE;
    for (VisibleDirectionMark mark : visibleMarks) {
      if (mark.textX() <= lastTextRight + 2) {
        continue;
      }
      graphics.text(font, mark.label(), mark.textX(), labelY, mark.color(), true);
      lastTextRight = mark.textX() + mark.textWidth();
    }
  }

  private static void renderDegree(
      GuiGraphicsExtractor graphics, Font font, DirectionHudLayout layout, int y, double bearing) {
    String text = Integer.toString(normalize(bearing));
    graphics.text(font, text, layout.centerX() - font.width(text) / 2, y, 0xFFE5E7EB, true);
  }

  private static int xFor(DirectionHudLayout layout, int degrees, double bearing) {
    return layout.centerX()
        + (int) Math.round(wrapDegrees(degrees - bearing) / layout.degreesPerPixel());
  }

  private static int nearestCardinal(double bearing) {
    return normalize(Math.round(bearing / 45.0D) * 45.0D);
  }

  private static double wrapDegrees(double degrees) {
    double wrapped = normalizeDegrees(degrees);
    return wrapped > 180 ? wrapped - 360 : wrapped;
  }

  private static int normalize(double degrees) {
    int normalized = (int) Math.round(degrees) % 360;
    return normalized < 0 ? normalized + 360 : normalized;
  }

  private static double normalizeDegrees(double degrees) {
    double normalized = degrees % 360.0D;
    return normalized < 0.0D ? normalized + 360.0D : normalized;
  }

  private static double clamp(double value, double minimum, double maximum) {
    return Math.clamp(value, minimum, maximum);
  }

  private static double settleProgress(double progress) {
    return progress * progress * progress * (progress * (progress * 6.0D - 15.0D) + 10.0D);
  }

  private static double lerp(double start, double end, double progress) {
    return start + (end - start) * progress;
  }

  private record DirectionMark(String label, int degrees) {}

  private record VisibleDirectionMark(String label, int textX, int textWidth, int color) {}

  private record DirectionHudLayout(int width, double degreesPerPixel) {
    HudSize size() {
      return new HudSize(width, HEIGHT);
    }

    int centerX() {
      return width / 2;
    }

    int contentRight() {
      return width - CONTENT_PADDING;
    }
  }
}
