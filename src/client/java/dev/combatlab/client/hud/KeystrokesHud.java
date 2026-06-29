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
    InputState input = context.gameState().input();
    if (context.editorPreview() && !input.anyMovementKeyDown() && !input.anyMouseButtonDown()) {
      input =
          new InputState(input.cps(), true, true, false, false, true, false, false, true, false);
    }

    graphics.pose().pushMatrix();
    graphics.pose().translate(context.bounds().x(), context.bounds().y());
    graphics.pose().scale((float) scale(), (float) scale());

    renderKey(graphics, context.font(), "W", KEY + GAP, 0, KEY, KEY, input.forward());
    renderKey(graphics, context.font(), "A", 0, KEY + GAP, KEY, KEY, input.left());
    renderKey(graphics, context.font(), "S", KEY + GAP, KEY + GAP, KEY, KEY, input.back());
    renderKey(graphics, context.font(), "D", (KEY + GAP) * 2, KEY + GAP, KEY, KEY, input.right());
    renderKey(graphics, context.font(), "SPACE", 0, SPACE_Y, WIDTH, SPACE_HEIGHT, input.jump());

    int mouseWidth = (WIDTH - GAP) / 2;
    renderKey(
        graphics, context.font(), "LMB", 0, MOUSE_Y, mouseWidth, MOUSE_HEIGHT, input.attack());
    renderKey(
        graphics,
        context.font(),
        "RMB",
        mouseWidth + GAP,
        MOUSE_Y,
        mouseWidth,
        MOUSE_HEIGHT,
        input.use());

    graphics.pose().popMatrix();
  }

  private static void renderKey(
      GuiGraphicsExtractor graphics,
      Font font,
      String label,
      int x,
      int y,
      int width,
      int height,
      boolean pressed) {
    int fill = pressed ? 0xCCF3F4F6 : 0x99000000;
    int outline = pressed ? 0xFFE5E7EB : 0x55FFFFFF;
    int text = pressed ? 0xFF111827 : 0xFFF3F4F6;
    graphics.fill(x, y, x + width, y + height, fill);
    graphics.outline(x, y, width, height, outline);
    int textY = y + (height - font.lineHeight) / 2 + 1;
    int textX = x + (width - font.width(label)) / 2 + horizontalTextOffset(label);
    graphics.text(font, label, textX, textY, text, !pressed);
  }

  private static int horizontalTextOffset(String label) {
    return switch (label) {
      case "SPACE", "LMB", "RMB" -> 1;
      default -> 0;
    };
  }
}
