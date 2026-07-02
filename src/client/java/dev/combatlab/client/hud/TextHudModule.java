package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

abstract class TextHudModule extends ResizableBaseHudModule {
  private static final int PADDING = 1;
  private String text;
  private HudSize unscaledSize;

  protected TextHudModule(
      HudModuleDefinition definition,
      String initialText,
      CombatLabOptions options,
      DebugLogger debug) {
    super(definition, options, debug);
    this.text = initialText;
    this.unscaledSize = new HudSize(initialText.length() * 6 + PADDING * 2, 9 + PADDING * 2);
  }

  @Override
  public final HudSize unscaledSize() {
    return unscaledSize;
  }

  protected final void setText(String text) {
    Objects.requireNonNull(text, "text");
    if (text.equals(this.text)) {
      return;
    }
    this.text = text;
    Font font = Minecraft.getInstance().font;
    this.unscaledSize = new HudSize(font.width(text) + PADDING * 2, font.lineHeight + PADDING * 2);
  }

  @Override
  protected final void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
    HudRectangle bounds = context.bounds();
    double moduleScale = scale();
    double textScale = HudTextScale.nearest(moduleScale);
    HudTextScale.draw(
        graphics,
        context.font(),
        text,
        bounds.x() + PADDING * moduleScale,
        bounds.y() + PADDING * moduleScale,
        textScale,
        settings().textColor(),
        true);
  }
}
