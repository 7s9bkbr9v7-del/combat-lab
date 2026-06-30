package dev.combatlab.client.hud;

import dev.combatlab.client.state.ClientGameState;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Reuses fixed module slots while refreshing only their per-frame render state. */
final class HudFrameSnapshot {
  private final List<ModuleFrame> moduleFrames;

  HudFrameSnapshot(List<HudModule> modules) {
    this.moduleFrames = modules.stream().map(ModuleFrame::new).toList();
  }

  void capture(
      ClientGameState gameState,
      Font font,
      int screenWidth,
      int screenHeight,
      float frameDeltaTicks) {
    for (ModuleFrame moduleFrame : moduleFrames) {
      moduleFrame.capture(gameState, font, screenWidth, screenHeight, frameDeltaTicks);
    }
  }

  void render(GuiGraphicsExtractor graphics) {
    for (ModuleFrame moduleFrame : moduleFrames) {
      moduleFrame.render(graphics);
    }
  }

  private static final class ModuleFrame {
    private final HudModule module;
    private final HudRectangle bounds = new HudRectangle(0, 0, 0, 0);
    private final HudRenderContext context = new HudRenderContext(null, bounds, 0, 0, 1.0F, null);
    private boolean active;

    private ModuleFrame(HudModule module) {
      this.module = module;
    }

    private void capture(
        ClientGameState gameState,
        Font font,
        int screenWidth,
        int screenHeight,
        float frameDeltaTicks) {
      active = module.enabled();
      if (!active) {
        return;
      }
      captureBounds(screenWidth, screenHeight);
      context.update(font, bounds, screenWidth, screenHeight, frameDeltaTicks, gameState);
    }

    private void render(GuiGraphicsExtractor graphics) {
      if (active) {
        module.renderInGame(graphics, context);
      }
    }

    private void captureBounds(int screenWidth, int screenHeight) {
      if (module instanceof BaseHudModule baseModule) {
        baseModule.resolveBoundsInto(bounds, screenWidth, screenHeight);
        return;
      }
      bounds.set(module.bounds(screenWidth, screenHeight));
    }
  }
}
