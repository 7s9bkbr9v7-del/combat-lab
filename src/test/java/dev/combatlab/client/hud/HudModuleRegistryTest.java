package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HudModuleRegistryTest {
  @TempDir Path temporaryDirectory;

  @Test
  void loadsOnlyEnabledOrExplicitBackgroundModules() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("disabled", false));
    registry.registerDescriptor(descriptor("enabled", false));
    registry.registerDescriptor(descriptor("background", true));

    registry.setEnabled("combatlab:enabled", true);

    registry.tick(ClientGameState.empty());

    assertNull(registry.module("combatlab:disabled"));
    assertNotNull(registry.module("combatlab:enabled"));
    assertNotNull(registry.module("combatlab:background"));
    assertEquals(1, ((CountingModule) registry.module("combatlab:enabled")).tickCount);
    assertEquals(1, ((CountingModule) registry.module("combatlab:background")).tickCount);
  }

  @Test
  void disablingModuleUnloadsItsInstanceButKeepsDescriptorAvailable() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("enabled", false));

    registry.setEnabled("combatlab:enabled", true);
    assertNotNull(registry.module("combatlab:enabled"));

    registry.setEnabled("combatlab:enabled", false);

    assertNull(registry.module("combatlab:enabled"));
    assertEquals(1, registry.descriptors().size());
  }

  @Test
  void gameplaySnapshotCapturesAndRendersOnlyEnabledModules() {
    CountingModule disabled = new CountingModule("disabled", false, false);
    CountingModule enabled = new CountingModule("enabled", true, false);
    HudFrameSnapshot snapshot = new HudFrameSnapshot(List.of(disabled, enabled));

    snapshot.capture(ClientGameState.empty(), null, 320, 180, 1.0F);
    snapshot.render(null);

    assertEquals(0, disabled.boundsCount);
    assertEquals(0, disabled.renderCount);
    assertEquals(1, enabled.boundsCount);
    assertEquals(1, enabled.renderCount);
  }

  private HudModuleRegistry registry() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    return new HudModuleRegistry(CombatLabOptions.load(store), new DebugLogger(() -> false));
  }

  private static HudModuleDescriptor descriptor(String path, boolean loadWhenDisabled) {
    HudModuleDefinition definition =
        new HudModuleDefinition(
            Identifier.fromNamespaceAndPath("combatlab", path),
            Component.literal(path),
            0.0,
            0.0,
            false);
    return new HudModuleDescriptor(
        definition,
        _ -> new CountingModule(path, !loadWhenDisabled, loadWhenDisabled),
        loadWhenDisabled);
  }

  private static final class CountingModule implements HudModule {
    private final Identifier id;
    private boolean enabled;
    private final boolean ticksWhenDisabled;
    private int tickCount;
    private int boundsCount;
    private int renderCount;

    private CountingModule(String path, boolean enabled, boolean ticksWhenDisabled) {
      this.id = Identifier.fromNamespaceAndPath("combatlab", path);
      this.enabled = enabled;
      this.ticksWhenDisabled = ticksWhenDisabled;
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
      return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

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
      boundsCount++;
      return new HudRectangle(0, 0, 1, 1);
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
    public void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context) {
      renderCount++;
    }

    @Override
    public void renderEditorPreview(
        GuiGraphicsExtractor graphics,
        Font font,
        HudRectangle bounds,
        int screenWidth,
        int screenHeight,
        ClientGameState gameState) {}

    @Override
    public void tick(ClientGameState gameState) {
      tickCount++;
    }

    @Override
    public boolean ticksWhenDisabled() {
      return ticksWhenDisabled;
    }
  }
}
