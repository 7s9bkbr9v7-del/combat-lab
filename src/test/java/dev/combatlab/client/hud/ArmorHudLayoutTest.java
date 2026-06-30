package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.debug.DebugLogger;
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
    assertEquals(new HudSize(18, 66), hud.size());

    hud.cycleLayout();

    assertEquals("HORIZONTAL", hud.currentLayout());
    assertEquals(new HudSize(66, 18), hud.size());

    hud.cycleLayout();

    assertEquals("GRID", hud.currentLayout());
    assertEquals(new HudSize(34, 34), hud.size());

    hud.cycleLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(new HudSize(18, 66), hud.size());
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
    assertEquals(new HudSize(18, 66), hud.size());
  }

  @Test
  void keepsAdaptiveLockedLayoutWhenDraggedAwayFromEdge() {
    ArmorHud hud = armorHud();
    hud.updatePosition(0, 80, 320, 180);
    hud.lockLayout();

    hud.updatePosition(120, 80, 320, 180);
    hud.unlockLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(new HudSize(18, 66), hud.size());
  }

  private ArmorHud armorHud() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    return new ArmorHud(CombatLabOptions.load(store), new DebugLogger(() -> false));
  }
}
