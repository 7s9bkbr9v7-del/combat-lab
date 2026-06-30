package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.screen.hudeditor.HudEditorHistory;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArmorHudLayoutTest {
  @TempDir Path temporaryDirectory;

  @Test
  void usesVerticalLayoutOnlyOnLeftOrRightEdgesAwayFromCorners() {
    assertEquals(ArmorHudLayout.VERTICAL, ArmorHudLayout.resolve(0.0, 0.5));
    assertEquals(ArmorHudLayout.VERTICAL, ArmorHudLayout.resolve(1.0, 0.5));
  }

  @Test
  void usesHorizontalLayoutOnlyOnTopOrBottomEdgesAwayFromCorners() {
    assertEquals(ArmorHudLayout.HORIZONTAL, ArmorHudLayout.resolve(0.5, 0.0));
    assertEquals(ArmorHudLayout.HORIZONTAL, ArmorHudLayout.resolve(0.5, 1.0));
  }

  @Test
  void usesGridLayoutInCornersAndAwayFromScreenEdges() {
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.resolve(0.0, 0.0));
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.resolve(1.0, 1.0));
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.resolve(0.5, 0.5));
  }

  @Test
  void keepsThePreviousLayoutAwayFromScreenEdges() {
    assertEquals(
        ArmorHudLayout.VERTICAL, ArmorHudLayout.resolve(0.5, 0.5, ArmorHudLayout.VERTICAL));
    assertEquals(
        ArmorHudLayout.HORIZONTAL, ArmorHudLayout.resolve(0.5, 0.5, ArmorHudLayout.HORIZONTAL));
  }

  @Test
  void safelyDefaultsUnknownStoredLayoutsToGrid() {
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.fromStored(null));
    assertEquals(ArmorHudLayout.GRID, ArmorHudLayout.fromStored("unknown"));
    assertEquals(ArmorHudLayout.VERTICAL, ArmorHudLayout.fromStored("VERTICAL"));
  }

  @Test
  void cycleLayoutCanReturnToAdaptiveArmorLayout() {
    ArmorHud hud = armorHud();
    hud.updatePosition(0, 80, 320, 180);

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(scaledSize(18, 66), hud.size());

    hud.cycleLayout();

    assertEquals("HORIZONTAL", hud.currentLayout());
    assertEquals(scaledSize(66, 18), hud.size());

    hud.cycleLayout();

    assertEquals("GRID", hud.currentLayout());
    assertEquals(scaledSize(34, 34), hud.size());

    hud.cycleLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(scaledSize(18, 66), hud.size());
  }

  @Test
  void manualCycleLayoutPersistsAcrossReload() {
    Path configPath = temporaryDirectory.resolve("combatlab.json");
    ArmorHud hud = armorHud(configPath);
    hud.updatePosition(0, 80, 320, 180);

    hud.cycleLayout();

    ArmorHud reloaded = armorHud(configPath);

    assertEquals("HORIZONTAL", reloaded.currentLayout());
    assertEquals(scaledSize(66, 18), reloaded.size());
  }

  @Test
  void manualCycleLayoutCanBeUndoneByEditorHistory() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    HudModuleRegistry registry =
        new HudModuleRegistry(CombatLabOptions.load(store), new DebugLogger(() -> false));
    registry.registerDescriptor(ArmorHud.descriptor());
    registry.setEnabled("combatlab:armor", true);
    ArmorHud hud = (ArmorHud) registry.module("combatlab:armor");
    hud.updatePosition(0, 80, 320, 180);
    HudEditorHistory history = new HudEditorHistory(registry);

    history.recordChange(hud::cycleLayout);

    assertEquals("HORIZONTAL", hud.currentLayout());

    history.undo();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(scaledSize(18, 66), hud.size());
  }

  @Test
  void clearsManualLayoutWhenReleasedOnEdge() {
    ArmorHud hud = armorHud();
    hud.updatePosition(120, 80, 320, 180);
    hud.cycleLayout();

    assertEquals("VERTICAL", hud.currentLayout());

    hud.lockLayout();
    hud.updatePosition(0, 80, 320, 180);
    hud.unlockLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(scaledSize(18, 66), hud.size());
  }

  @Test
  void keepsAdaptiveLockedLayoutWhenDraggedAwayFromEdge() {
    ArmorHud hud = armorHud();
    hud.updatePosition(0, 80, 320, 180);
    hud.lockLayout();

    hud.updatePosition(120, 80, 320, 180);
    hud.unlockLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(scaledSize(18, 66), hud.size());
  }

  @Test
  void shrinkingPreviewBoundsStayAnimatedUntilSettled() {
    ArmorHud hud = armorHud();
    hud.updatePosition(0, 80, 320, 180);
    HudRectangle verticalBounds = hud.editorBounds(320, 180);

    assertEquals(scaled(18), verticalBounds.width());
    assertEquals(scaled(66), verticalBounds.height());

    hud.updatePosition(160, 0, 320, 180);
    HudRectangle animatedBounds = hud.editorBounds(320, 180);

    assertEquals(scaledSize(66, 18), hud.size());
    assertEquals(verticalBounds.width(), animatedBounds.width());
    assertEquals(verticalBounds.height(), animatedBounds.height());
  }

  @Test
  void attachedEditorBoundsFollowAnimatedTargetBounds() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    CombatLabOptions options = CombatLabOptions.load(store);
    DebugLogger debug = new DebugLogger(() -> false);
    ArmorHud armor = new ArmorHud(options, debug);
    DirectionHud direction = new DirectionHud(options, debug);
    armor.bindModuleLookup(id -> direction.id().toString().equals(id) ? direction : null);
    direction.bindModuleLookup(id -> armor.id().toString().equals(id) ? armor : null);
    armor.updatePosition(120, 80, 320, 180);
    armor.editorBounds(320, 180);
    direction.attachTo(armor, HudAttachmentSide.RIGHT_OF, 0);

    armor.updatePosition(0, 80, 320, 180);

    HudRectangle attachedEditorBounds = direction.editorBounds(320, 180);
    HudRectangle animatedArmorBounds = armor.editorBounds(320, 180);

    assertEquals(scaled(18), armor.bounds(320, 180).width());
    assertEquals(animatedArmorBounds.right(), attachedEditorBounds.x());
    assertEquals(animatedArmorBounds.y(), attachedEditorBounds.y());
  }

  private static HudSize scaledSize(int width, int height) {
    return new HudSize(scaled(width), scaled(height));
  }

  private static int scaled(int value) {
    return (int) Math.ceil(value * HudModuleSettings.DEFAULT_SCALE);
  }

  private ArmorHud armorHud() {
    return armorHud(temporaryDirectory.resolve("combatlab.json"));
  }

  private ArmorHud armorHud(Path configPath) {
    ConfigStore store = new ConfigStore(configPath, new CombatLabConfigCodec());
    return new ArmorHud(CombatLabOptions.load(store), new DebugLogger(() -> false));
  }
}
