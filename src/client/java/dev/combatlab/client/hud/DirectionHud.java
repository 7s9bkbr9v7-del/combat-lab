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
  private DirectionHudLayout floatingLayout = FLOATING_LAYOUT;
  private final HudAdaptiveLayoutAnimation<DirectionHudLayout> layoutAnimation =
      new HudAdaptiveLayoutAnimation<>();

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
  public HudRectangle editorBounds(int screenWidth, int screenHeight) {
    DirectionHudLayout layout = layout();
    return layoutAnimation.editorBounds(
        this, layout, layout.size(), lockedLayout != null, screenWidth, screenHeight);
  }

  @Override
  public List<String> availableLayouts() {
    return List.of(ADAPTIVE_LAYOUT, "FLOATING", "SIDE");
  }

  @Override
  public String currentLayout() {
    DirectionHudLayout manualLayout = manualLayout();
    return manualLayout == null ? ADAPTIVE_LAYOUT : layoutName(manualLayout);
  }

  @Override
  public void cycleLayout() {
    DirectionHudLayout manualLayout = manualLayout();
    DirectionHudLayout adaptiveLayout = resolvedLayout();
    DirectionHudLayout nextLayout =
        manualLayout == null ? nextLayout(adaptiveLayout) : nextLayout(manualLayout);
    settings().updateLayout(nextLayout == adaptiveLayout ? null : layoutName(nextLayout));
    settings().save();
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
      settings().updateLayout(null);
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

    DirectionHudLayout layout = layout();
    CompassRenderFrame renderFrame = renderFrame(context, layout);

    graphics.pose().pushMatrix();
    graphics.pose().translate((float) renderFrame.x(), (float) renderFrame.y());
    graphics.pose().scale((float) scale(), (float) scale());

    DirectionHudLayout renderLayout =
        new DirectionHudLayout(renderFrame.width(), layout.degreesPerPixel());
    int degreeY = degreeY(context);
    int compassY = compassY(context, context.font());
    double moduleScale = scale();
    double textScale = HudTextScale.nearest(moduleScale);
    graphics.fill(0, compassY, renderLayout.width(), compassY + COMPASS_HEIGHT, 0x77000000);
    graphics.outline(0, compassY, renderLayout.width(), COMPASS_HEIGHT, 0x44FFFFFF);
    graphics.fill(
        renderLayout.centerX(),
        compassY + 1,
        renderLayout.centerX() + 1,
        compassY + COMPASS_HEIGHT - 1,
        0x99D1D5DB);
    renderTicks(graphics, renderLayout, compassY, bearing);

    graphics.pose().popMatrix();

    renderMarks(
        graphics,
        context.font(),
        renderFrame,
        renderLayout,
        moduleScale,
        textScale,
        compassY,
        bearing);
    renderDegree(
        graphics,
        context.font(),
        renderFrame,
        renderLayout,
        moduleScale,
        textScale,
        degreeY,
        bearing);
  }

  private CompassRenderFrame renderFrame(HudRenderContext context, DirectionHudLayout layout) {
    if (!context.editorPreview()) {
      layoutAnimation.reset();
      return new CompassRenderFrame(context.bounds().x(), context.bounds().y(), layout.width());
    }

    HudAnimatedSize animatedSize = layoutAnimation.currentContextSize(context, scale());
    int renderWidth = Math.max(1, (int) Math.round(animatedSize.width()));
    return new CompassRenderFrame(
        renderX(context, animatedSize.width(), renderWidth / 2), context.bounds().y(), renderWidth);
  }

  private double renderX(HudRenderContext context, double animatedWidth, int renderCenterX) {
    return layoutAnimation.centeredRenderX(
        context,
        scale(),
        settings().normalizedX(),
        attachmentTargetId() != null,
        animatedWidth,
        renderCenterX);
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
    DirectionHudLayout manualLayout = manualLayout();
    return manualLayout != null ? manualLayout : resolvedLayout();
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

  private DirectionHudLayout manualLayout() {
    String layout = settings().layout();
    if (layout == null || ADAPTIVE_LAYOUT.equals(layout)) {
      return null;
    }
    if ("SIDE".equals(layout)) {
      return SIDE_LAYOUT;
    }
    if ("FLOATING".equals(layout)) {
      return FLOATING_LAYOUT;
    }
    return null;
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
      CompassRenderFrame frame,
      DirectionHudLayout layout,
      double moduleScale,
      double textScale,
      int compassY,
      double bearing) {
    double labelY = frame.y() + (compassY + LABEL_Y) * moduleScale;
    double contentLeft = frame.x() + CONTENT_LEFT * moduleScale;
    double contentRight = frame.x() + layout.contentRight() * moduleScale;
    List<VisibleDirectionMark> visibleMarks = new ArrayList<>(MARKS.length);
    int activeDirection = nearestCardinal(bearing);
    for (DirectionMark mark : MARKS) {
      int x = xFor(layout, mark.degrees(), bearing);
      double centerX = frame.x() + x * moduleScale;
      double textWidth = font.width(mark.label()) * textScale;
      double textX = centerX - textWidth / 2.0D;
      if (textX < contentLeft || textX + textWidth > contentRight) {
        continue;
      }
      int color = mark.degrees() == activeDirection ? ACCENT_COLOR : 0xFFE5E7EB;
      visibleMarks.add(new VisibleDirectionMark(mark.label(), textX, textWidth, color));
    }

    visibleMarks.sort(Comparator.comparingDouble(VisibleDirectionMark::textX));
    double lastTextRight = Double.NEGATIVE_INFINITY;
    for (VisibleDirectionMark mark : visibleMarks) {
      if (mark.textX() <= lastTextRight + 2) {
        continue;
      }
      HudTextScale.draw(
          graphics, font, mark.label(), mark.textX(), labelY, textScale, mark.color(), true);
      lastTextRight = mark.textX() + mark.textWidth();
    }
  }

  private static void renderDegree(
      GuiGraphicsExtractor graphics,
      Font font,
      CompassRenderFrame frame,
      DirectionHudLayout layout,
      double moduleScale,
      double textScale,
      int y,
      double bearing) {
    String text = Integer.toString(normalize(bearing));
    double centerX = frame.x() + layout.centerX() * moduleScale;
    HudTextScale.draw(
        graphics,
        font,
        text,
        HudTextScale.centeredX(font, text, centerX, textScale),
        frame.y() + y * moduleScale,
        textScale,
        0xFFE5E7EB,
        true);
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

  private record DirectionMark(String label, int degrees) {}

  private record VisibleDirectionMark(String label, double textX, double textWidth, int color) {}

  private record CompassRenderFrame(double x, int y, int width) {}

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
