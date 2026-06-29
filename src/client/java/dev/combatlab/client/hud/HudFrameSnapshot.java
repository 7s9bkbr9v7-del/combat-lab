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

  void capture(ClientGameState gameState, Font font, int screenWidth, int screenHeight) {
    for (ModuleFrame moduleFrame : moduleFrames) {
      moduleFrame.capture(gameState, font, screenWidth, screenHeight);
    }
  }

  void render(GuiGraphicsExtractor graphics) {
    for (ModuleFrame moduleFrame : moduleFrames) {
      moduleFrame.render(graphics);
    }
  }

  private static final class ModuleFrame {
    private final HudModule module;
    private HudRenderContext context;

    private ModuleFrame(HudModule module) {
      this.module = module;
    }

    private void capture(ClientGameState gameState, Font font, int screenWidth, int screenHeight) {
      context =
          module.enabled()
              ? new HudRenderContext(
                  font,
                  module.bounds(screenWidth, screenHeight),
                  screenWidth,
                  screenHeight,
                  gameState)
              : null;
    }

    private void render(GuiGraphicsExtractor graphics) {
      if (context != null) {
        module.renderInGame(graphics, context);
      }
    }
  }
}
