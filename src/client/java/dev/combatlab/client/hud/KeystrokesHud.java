package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.InputState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class KeystrokesHud extends ResizableBaseHudModule {
  private static final int KEY = 20;
  private static final int GAP = 2;
  private static final int WIDTH = KEY * 3 + GAP * 2;
  private static final int MOUSE_HEIGHT = 13;
  private static final int SPACE_HEIGHT = 13;
  private static final int SPACE_Y = KEY * 2 + GAP * 2;
  private static final int MOUSE_Y = SPACE_Y + SPACE_HEIGHT + GAP;
  private static final HudSize SIZE = new HudSize(WIDTH, MOUSE_Y + MOUSE_HEIGHT);
  private static final HudModuleDefinition DEFINITION =
      new HudModuleDefinition(
          Identifier.fromNamespaceAndPath("combatlab", "keystrokes"),
          Component.literal("Keystrokes"),
          0.02,
          0.68,
          true);

  public static HudModuleDescriptor descriptor() {
    return new HudModuleDescriptor(
        DEFINITION,
        dependencies -> new KeystrokesHud(dependencies.options(), dependencies.debug()));
  }

  public KeystrokesHud(CombatLabOptions options, DebugLogger debug) {
    super(DEFINITION, options, debug);
  }

  @Override
  public HudSize unscaledSize() {
    return SIZE;
  }

  @Override
  protected void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
    InputState input = context.hud().input();
    if (context.editorPreview() && !input.anyMovementKeyDown() && !input.anyMouseButtonDown()) {
      input =
          new InputState(input.cps(), true, true, false, false, true, false, false, true, false);
    }

    HudRectangle bounds = context.bounds();
    double moduleScale = scale();
    double textScale = HudTextScale.nearest(moduleScale);

    graphics.pose().pushMatrix();
    graphics.pose().translate(bounds.x(), bounds.y());
    graphics.pose().scale((float) moduleScale, (float) moduleScale);

    renderKeyBackground(graphics, 0, KEY + GAP, KEY, KEY, input.left());
    renderKeyBackground(graphics, KEY + GAP, 0, KEY, KEY, input.forward());
    renderKeyBackground(graphics, KEY + GAP, KEY + GAP, KEY, KEY, input.back());
    renderKeyBackground(graphics, (KEY + GAP) * 2, KEY + GAP, KEY, KEY, input.right());
    renderKeyBackground(graphics, 0, SPACE_Y, WIDTH, SPACE_HEIGHT, input.jump());

    int mouseWidth = (WIDTH - GAP) / 2;
    renderKeyBackground(graphics, 0, MOUSE_Y, mouseWidth, MOUSE_HEIGHT, input.attack());
    renderKeyBackground(graphics, mouseWidth + GAP, MOUSE_Y, mouseWidth, MOUSE_HEIGHT, input.use());

    graphics.pose().popMatrix();

    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "W",
        KEY + GAP,
        0,
        KEY,
        KEY,
        input.forward(),
        settings().textColor());
    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "A",
        0,
        KEY + GAP,
        KEY,
        KEY,
        input.left(),
        settings().textColor());
    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "S",
        KEY + GAP,
        KEY + GAP,
        KEY,
        KEY,
        input.back(),
        settings().textColor());
    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "D",
        (KEY + GAP) * 2,
        KEY + GAP,
        KEY,
        KEY,
        input.right(),
        settings().textColor());
    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "SPACE",
        0,
        SPACE_Y,
        WIDTH,
        SPACE_HEIGHT,
        input.jump(),
        settings().textColor());
    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "LMB",
        0,
        MOUSE_Y,
        mouseWidth,
        MOUSE_HEIGHT,
        input.attack(),
        settings().textColor());
    renderKeyLabel(
        graphics,
        context.font(),
        bounds,
        moduleScale,
        textScale,
        "RMB",
        mouseWidth + GAP,
        MOUSE_Y,
        mouseWidth,
        MOUSE_HEIGHT,
        input.use(),
        settings().textColor());
  }

  private static void renderKeyBackground(
      GuiGraphicsExtractor graphics, int x, int y, int width, int height, boolean pressed) {
    int fill = pressed ? 0xCCF3F4F6 : 0x99000000;
    int outline = pressed ? 0xFFE5E7EB : 0x55FFFFFF;
    graphics.fill(x, y, x + width, y + height, fill);
    graphics.outline(x, y, width, height, outline);
  }

  private static void renderKeyLabel(
      GuiGraphicsExtractor graphics,
      Font font,
      HudRectangle bounds,
      double moduleScale,
      double textScale,
      String label,
      int x,
      int y,
      int width,
      int height,
      boolean pressed,
      int textColor) {
    int color = pressed ? 0xFF111827 : textColor;
    double centerX = bounds.x() + (x + width / 2.0D) * moduleScale;
    double centerY = bounds.y() + (y + height / 2.0D) * moduleScale;
    HudTextScale.draw(
        graphics,
        font,
        label,
        HudTextScale.centeredX(font, label, centerX, textScale)
            + horizontalTextOffset(label) * moduleScale,
        HudTextScale.centeredY(font, centerY, textScale) + moduleScale,
        textScale,
        color,
        !pressed);
  }

  private static int horizontalTextOffset(String label) {
    return switch (label) {
      case "SPACE", "LMB", "RMB" -> 1;
      default -> 0;
    };
  }
}
