package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.PlayerEffectTimer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PotionEffectsHud extends ResizableBaseHudModule {
  private static final int PADDING = 3;
  private static final int ICON_SIZE = 18;
  private static final int ROW_HEIGHT = 22;
  private static final int MIN_WIDTH = 92;
  private static final int MAX_EFFECTS = 8;
  private static final HudSize EMPTY_SIZE = new HudSize(MIN_WIDTH, ROW_HEIGHT * 2);
  private static final HudModuleDefinition DEFINITION =
      new HudModuleDefinition(
          Identifier.fromNamespaceAndPath("combatlab", "effects"),
          Component.literal("Effect Timers"),
          0.02,
          0.26,
          true);

  public static HudModuleDescriptor descriptor() {
    return new HudModuleDescriptor(
        DEFINITION,
        dependencies -> new PotionEffectsHud(dependencies.options(), dependencies.debug()));
  }

  private HudSize unscaledSize = EMPTY_SIZE;

  public PotionEffectsHud(CombatLabOptions options, DebugLogger debug) {
    super(DEFINITION, options, debug);
  }

  @Override
  public HudSize unscaledSize() {
    return unscaledSize;
  }

  @Override
  public void tick(ClientGameState gameState) {
    updateSize(gameState.hud().effects().active());
  }

  @Override
  protected boolean shouldRenderInGame(HudRenderContext context) {
    return !context.hud().effects().emptyActiveEffects();
  }

  @Override
  protected void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
    List<PlayerEffectTimer> effects = visibleEffects(context.hud().effects().active());
    if (effects.isEmpty()) {
      return;
    }

    if (context.editorPreview()) {
      updateSize(effects);
    }

    graphics.pose().pushMatrix();
    graphics.pose().translate(context.bounds().x(), context.bounds().y());
    graphics.pose().scale((float) scale(), (float) scale());
    for (int index = 0; index < effects.size(); index++) {
      renderEffect(graphics, context.font(), effects.get(index), index * ROW_HEIGHT);
    }
    graphics.pose().popMatrix();
  }

  private void renderEffect(
      GuiGraphicsExtractor graphics, Font font, PlayerEffectTimer effect, int y) {
    int width = unscaledSize.width();
    int accent = 0xFF000000 | effect.color();
    graphics.fill(0, y, width, y + ROW_HEIGHT - 1, 0x99000000);
    graphics.fill(0, y, 2, y + ROW_HEIGHT - 1, accent);
    graphics.outline(0, y, width, ROW_HEIGHT - 1, 0x44FFFFFF);
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        effect.iconTexture(),
        PADDING,
        y + 2,
        0.0F,
        0.0F,
        ICON_SIZE,
        ICON_SIZE,
        ICON_SIZE,
        ICON_SIZE);

    String name = effect.displayName() + amplifierSuffix(effect.amplifier());
    String timer = durationText(effect);
    int textX = PADDING + ICON_SIZE + 4;
    graphics.text(font, name, textX, y + 3, 0xFFF3F4F6, true);
    graphics.text(font, timer, textX, y + 13, durationColor(effect), true);
  }

  private void updateSize(List<PlayerEffectTimer> activeEffects) {
    List<PlayerEffectTimer> effects = visibleEffects(activeEffects);
    if (effects.isEmpty()) {
      unscaledSize = EMPTY_SIZE;
      return;
    }

    Font font = Minecraft.getInstance().font;
    int width = MIN_WIDTH;
    if (font != null) {
      for (PlayerEffectTimer effect : effects) {
        int textWidth =
            Math.max(
                font.width(effect.displayName() + amplifierSuffix(effect.amplifier())),
                font.width(durationText(effect)));
        width = Math.max(width, PADDING * 2 + ICON_SIZE + 4 + textWidth);
      }
    }
    unscaledSize = new HudSize(width, ROW_HEIGHT * effects.size());
  }

  private static List<PlayerEffectTimer> visibleEffects(List<PlayerEffectTimer> effects) {
    return effects.size() <= MAX_EFFECTS ? effects : effects.subList(0, MAX_EFFECTS);
  }

  private static String amplifierSuffix(int amplifier) {
    if (amplifier <= 0) {
      return "";
    }
    return " " + romanNumeral(amplifier + 1);
  }

  private static String durationText(PlayerEffectTimer effect) {
    if (effect.infinite()) {
      return "∞";
    }
    int totalSeconds = Math.max(0, effect.durationTicks() / 20);
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
  }

  private static int durationColor(PlayerEffectTimer effect) {
    if (effect.infinite()) {
      return 0xFFE5E7EB;
    }
    int seconds = effect.durationTicks() / 20;
    if (seconds <= 10) {
      return 0xFFFF7070;
    }
    if (seconds <= 30) {
      return 0xFFFFD166;
    }
    return 0xFFB8F7B0;
  }

  private static String romanNumeral(int value) {
    return switch (value) {
      case 2 -> "II";
      case 3 -> "III";
      case 4 -> "IV";
      case 5 -> "V";
      default -> Integer.toString(value);
    };
  }
}
