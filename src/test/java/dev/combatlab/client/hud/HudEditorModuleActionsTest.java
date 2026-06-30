package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.screen.hudeditor.HudEditorHistory;
import dev.combatlab.client.screen.hudeditor.HudEditorModuleActions;
import dev.combatlab.client.screen.hudeditor.HudSelection;
import java.nio.file.Path;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HudEditorModuleActionsTest {
  private static final int SCREEN_WIDTH = 320;
  private static final int SCREEN_HEIGHT = 180;

  @TempDir Path temporaryDirectory;

  @Test
  void disablingAnchorPromotesFirstChildInPlace() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("anchor"));
    registry.registerDescriptor(descriptor("child"));
    registry.registerDescriptor(descriptor("grandchild"));
    registry.setEnabled("combatlab:anchor", true);
    registry.setEnabled("combatlab:child", true);
    registry.setEnabled("combatlab:grandchild", true);

    HudModule anchor = registry.module("combatlab:anchor");
    HudModule child = registry.module("combatlab:child");
    HudModule grandchild = registry.module("combatlab:grandchild");
    anchor.updatePosition(80, 40, SCREEN_WIDTH, SCREEN_HEIGHT);
    child.attachTo(anchor, HudAttachmentSide.BELOW, 0);
    grandchild.attachTo(child, HudAttachmentSide.BELOW, 0);

    HudEditorModuleActions actions =
        new HudEditorModuleActions(
            registry, new HudSelection(registry), new HudEditorHistory(registry), 6);
    actions.disable(anchor, SCREEN_WIDTH, SCREEN_HEIGHT);

    assertEquals(80, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).x());
    assertEquals(40, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).y());
    assertNull(registry.settings("combatlab:child").attachedTo());
    assertEquals(80, grandchild.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).x());
    assertEquals(50, grandchild.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).y());

    registry.setEnabled("combatlab:anchor", true);

    assertEquals(80, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).x());
    assertEquals(40, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).y());
  }

  @Test
  void registryDisablePromotesFirstChildInPlace() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("anchor"));
    registry.registerDescriptor(descriptor("child"));
    registry.setEnabled("combatlab:anchor", true);
    registry.setEnabled("combatlab:child", true);

    HudModule anchor = registry.module("combatlab:anchor");
    HudModule child = registry.module("combatlab:child");
    anchor.updatePosition(80, 40, SCREEN_WIDTH, SCREEN_HEIGHT);
    child.attachTo(anchor, HudAttachmentSide.BELOW, 0);

    registry.setEnabled("combatlab:anchor", false, SCREEN_WIDTH, SCREEN_HEIGHT);

    assertEquals(80, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).x());
    assertEquals(40, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).y());
    assertNull(registry.settings("combatlab:child").attachedTo());

    registry.setEnabled("combatlab:anchor", true, SCREEN_WIDTH, SCREEN_HEIGHT);

    assertEquals(80, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).x());
    assertEquals(50, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).y());
    assertEquals("combatlab:anchor", registry.settings("combatlab:child").attachedTo());
  }

  @Test
  void registryEnableLeavesPromotedChildAloneAfterItMoved() {
    HudModuleRegistry registry = registry();
    registry.registerDescriptor(descriptor("anchor"));
    registry.registerDescriptor(descriptor("child"));
    registry.setEnabled("combatlab:anchor", true);
    registry.setEnabled("combatlab:child", true);

    HudModule anchor = registry.module("combatlab:anchor");
    HudModule child = registry.module("combatlab:child");
    anchor.updatePosition(80, 40, SCREEN_WIDTH, SCREEN_HEIGHT);
    child.attachTo(anchor, HudAttachmentSide.BELOW, 0);

    registry.setEnabled("combatlab:anchor", false, SCREEN_WIDTH, SCREEN_HEIGHT);
    child.updatePosition(120, 70, SCREEN_WIDTH, SCREEN_HEIGHT);

    registry.setEnabled("combatlab:anchor", true, SCREEN_WIDTH, SCREEN_HEIGHT);

    assertEquals(120, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).x());
    assertEquals(70, child.bounds(SCREEN_WIDTH, SCREEN_HEIGHT).y());
    assertNull(registry.settings("combatlab:child").attachedTo());
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
    return new HudModuleDescriptor(
        definition,
        dependencies -> new TestHudModule(definition, dependencies.options(), dependencies.debug()),
        false);
  }

  private static final class TestHudModule extends BaseHudModule {
    private TestHudModule(
        HudModuleDefinition definition, CombatLabOptions options, DebugLogger debug) {
      super(definition, options, debug);
    }

    @Override
    public HudSize size() {
      return new HudSize(10, 10);
    }

    @Override
    protected void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {}
  }
}
