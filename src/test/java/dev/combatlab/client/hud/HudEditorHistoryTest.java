package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.screen.hudeditor.HudEditorHistory;
import dev.combatlab.client.state.ClientGameState;
import java.nio.file.Path;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HudEditorHistoryTest {
  @TempDir Path temporaryDirectory;

  @Test
  void undoesManyChangesOneStepAtATime() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("fps"));
    HudEditorHistory history = new HudEditorHistory(registry);

    history.recordChange(() -> registry.settings("combatlab:fps").updatePosition(0.25, 0.75));
    history.recordChange(() -> registry.settings("combatlab:fps").updateScale(2.0));

    assertEquals(0.25, registry.settings("combatlab:fps").normalizedX(), 0.0001);
    assertEquals(0.75, registry.settings("combatlab:fps").normalizedY(), 0.0001);
    assertEquals(2.0, registry.settings("combatlab:fps").scale(), 0.0001);

    assertTrue(history.undo());
    assertEquals(0.25, registry.settings("combatlab:fps").normalizedX(), 0.0001);
    assertEquals(0.75, registry.settings("combatlab:fps").normalizedY(), 0.0001);
    assertEquals(1.0, registry.settings("combatlab:fps").scale(), 0.0001);

    assertTrue(history.undo());
    assertEquals(0.0, registry.settings("combatlab:fps").normalizedX(), 0.0001);
    assertEquals(0.0, registry.settings("combatlab:fps").normalizedY(), 0.0001);
    assertEquals(1.0, registry.settings("combatlab:fps").scale(), 0.0001);

    assertFalse(history.undo());
  }

  @Test
  void redoesUndoneChangesOneStepAtATime() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("fps"));
    HudEditorHistory history = new HudEditorHistory(registry);

    history.recordChange(() -> registry.settings("combatlab:fps").updatePosition(0.25, 0.75));
    history.recordChange(() -> registry.settings("combatlab:fps").updateScale(2.0));

    assertTrue(history.undo());
    assertTrue(history.undo());

    assertTrue(history.redo());
    assertEquals(0.25, registry.settings("combatlab:fps").normalizedX(), 0.0001);
    assertEquals(0.75, registry.settings("combatlab:fps").normalizedY(), 0.0001);
    assertEquals(1.0, registry.settings("combatlab:fps").scale(), 0.0001);

    assertTrue(history.redo());
    assertEquals(0.25, registry.settings("combatlab:fps").normalizedX(), 0.0001);
    assertEquals(0.75, registry.settings("combatlab:fps").normalizedY(), 0.0001);
    assertEquals(2.0, registry.settings("combatlab:fps").scale(), 0.0001);

    assertFalse(history.redo());
  }

  @Test
  void newChangeClearsRedoStack() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("fps"));
    HudEditorHistory history = new HudEditorHistory(registry);

    history.recordChange(() -> registry.settings("combatlab:fps").updatePosition(0.25, 0.75));
    assertTrue(history.undo());

    history.recordChange(() -> registry.settings("combatlab:fps").updatePosition(0.5, 0.5));

    assertFalse(history.redo());
    assertEquals(0.5, registry.settings("combatlab:fps").normalizedX(), 0.0001);
    assertEquals(0.5, registry.settings("combatlab:fps").normalizedY(), 0.0001);
  }

  @Test
  void undoRestoresAnUnloadedDisabledModule() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("cps"));
    registry.setEnabled("combatlab:cps", true);
    HudEditorHistory history = new HudEditorHistory(registry);

    history.recordChange(() -> registry.setEnabled("combatlab:cps", false));

    assertFalse(registry.enabled("combatlab:cps"));
    assertNull(registry.module("combatlab:cps"));

    assertTrue(history.undo());

    assertTrue(registry.enabled("combatlab:cps"));
    assertNotNull(registry.module("combatlab:cps"));
  }

  private HudModuleRegistry registry() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    return new HudModuleRegistry(CombatLabOptions.load(store), new DebugLogger(() -> false));
  }

  private static HudModuleDescriptor descriptor(String path) {
    Identifier id = Identifier.fromNamespaceAndPath("combatlab", path);
    HudModuleDefinition definition =
        new HudModuleDefinition(id, Component.literal(path), 0.0, 0.0, true);
    return new HudModuleDescriptor(definition, _ -> new TestModule(id), false);
  }

  private static final class TestModule implements HudModule {
    private final Identifier id;

    private TestModule(Identifier id) {
      this.id = id;
    }

    @Override
    public Identifier id() {
      return id;
    }

    @Override
    public Component displayName() {
      return Component.literal(id.toString());
    }

    @Override
    public boolean enabled() {
      return true;
    }

    @Override
    public void setEnabled(boolean enabled) {}

    @Override
    public HudPosition position(int screenWidth, int screenHeight) {
      return new HudPosition(0, 0);
    }

    @Override
    public HudSize size() {
      return new HudSize(1, 1);
    }

    @Override
    public HudRectangle bounds(int screenWidth, int screenHeight) {
      return new HudRectangle(0, 0, 1, 1);
    }

    @Override
    public HudOrientation orientation(int screenWidth, int screenHeight) {
      return HudOrientationResolver.resolve(
          bounds(screenWidth, screenHeight), screenWidth, screenHeight);
    }

    @Override
    public void updatePosition(int x, int y, int screenWidth, int screenHeight) {}

    @Override
    public void savePosition() {}

    @Override
    public String attachmentTargetId() {
      return null;
    }

    @Override
    public void attachTo(HudModule target, HudAttachmentSide side, int offset) {}

    @Override
    public void clearAttachment() {}

    @Override
    public void detach(int screenWidth, int screenHeight) {}

    @Override
    public void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context) {}

    @Override
    public void renderEditorPreview(
        GuiGraphicsExtractor graphics,
        Font font,
        HudRectangle bounds,
        int screenWidth,
        int screenHeight,
        ClientGameState gameState) {}
  }
}
