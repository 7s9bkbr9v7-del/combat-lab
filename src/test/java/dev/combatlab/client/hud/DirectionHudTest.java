package dev.combatlab.client.hud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.DirectionState;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.MovementState;
import dev.combatlab.client.state.PlayerArmor;
import dev.combatlab.client.state.PlayerEffects;
import dev.combatlab.client.state.PlayerState;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DirectionHudTest {
  @TempDir Path temporaryDirectory;

  @Test
  void interpolatesAcrossZeroDegreeWraparound() {
    DirectionHud hud = hud();
    hud.tick(stateWithBearing(359));
    hud.tick(stateWithBearing(1));

    double bearing = renderBearing(hud, context(0.5F, stateWithBearing(1)));

    assertTrue(bearing > 359.0D || bearing < 1.0D);
  }

  @Test
  void lowFpsFrameDeltaClampsToLatestTickBearing() {
    DirectionHud hud = hud();
    hud.tick(stateWithBearing(0));
    hud.tick(stateWithBearing(180));

    double latestTickBearing = renderBearing(hud, context(1.0F, stateWithBearing(180)));
    double lowFpsBearing = renderBearing(hud, context(3.0F, stateWithBearing(180)));

    assertEquals(latestTickBearing, lowFpsBearing);
  }

  @Test
  void editorPreviewUsesSnapshotDirectionWithoutTickState() {
    DirectionHud hud = hud();

    double bearing = renderBearing(hud, previewContext(stateWithBearing(123)));

    assertEquals(123.0D, bearing);
  }

  @Test
  void editorPreviewFallsBackWhenDirectionIsAbsent() {
    DirectionHud hud = hud();

    double bearing = renderBearing(hud, previewContext(ClientGameState.empty()));

    assertEquals(21.0D, bearing);
  }

  @Test
  void nullPlayerFallsBackToPlaceholderBearing() {
    DirectionHud hud = hud();
    ClientGameState nullPlayerState = nullPlayerState();

    assertDoesNotThrow(() -> hud.tick(nullPlayerState));
    assertEquals(21.0D, renderBearing(hud, context(1.0F, nullPlayerState)));
    assertEquals(21.0D, renderBearing(hud, previewContext(nullPlayerState)));
  }

  @Test
  void cycleLayoutCanReturnToAdaptiveDirectionLayout() {
    DirectionHud hud = hud();
    hud.updatePosition(0, 30, 320, 180);

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(new HudSize(58, 22), hud.size());

    hud.cycleLayout();

    assertEquals("FLOATING", hud.currentLayout());
    assertEquals(new HudSize(112, 22), hud.size());

    hud.cycleLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(new HudSize(58, 22), hud.size());
  }

  @Test
  void clearsManualLayoutWhenReleasedOnEdge() {
    DirectionHud hud = hud();
    hud.updatePosition(120, 30, 320, 180);
    hud.cycleLayout();

    assertEquals("SIDE", hud.currentLayout());

    hud.lockLayout();
    hud.updatePosition(120, 0, 320, 180);
    hud.unlockLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(new HudSize(112, 22), hud.size());
  }

  @Test
  void keepsAdaptiveLockedLayoutWhenDraggedAwayFromSideEdge() {
    DirectionHud hud = hud();
    hud.updatePosition(0, 30, 320, 180);
    hud.lockLayout();

    hud.updatePosition(120, 30, 320, 180);
    hud.unlockLayout();

    assertEquals(AdaptiveLayoutHudModule.ADAPTIVE_LAYOUT, hud.currentLayout());
    assertEquals(new HudSize(58, 22), hud.size());
  }

  private DirectionHud hud() {
    ConfigStore store =
        new ConfigStore(temporaryDirectory.resolve("combatlab.json"), new CombatLabConfigCodec());
    return new DirectionHud(CombatLabOptions.load(store), new DebugLogger(() -> false));
  }

  private static HudRenderContext context(float frameDeltaTicks, ClientGameState gameState) {
    return new HudRenderContext(
        null, new HudRectangle(0, 0, 112, 22), 320, 180, frameDeltaTicks, gameState);
  }

  private static HudRenderContext previewContext(ClientGameState gameState) {
    return new HudRenderContext(
        null, new HudRectangle(0, 0, 112, 22), 320, 180, true, 1.0F, gameState);
  }

  private static ClientGameState stateWithBearing(int bearingDegrees) {
    return stateWithPlayer(
        new PlayerState(
            true,
            DirectionState.of(bearingDegrees),
            MovementState.inactive(),
            PlayerArmor.empty(),
            PlayerEffects.empty()));
  }

  private static ClientGameState nullPlayerState() {
    return new ClientGameState(
        HudGameState.empty(), null, InputState.empty(), CombatSnapshot.empty(), 60);
  }

  private static ClientGameState stateWithPlayer(PlayerState player) {
    InputState input = InputState.empty();
    CombatSnapshot combat = CombatSnapshot.empty();
    int fps = 60;
    return new ClientGameState(
        HudGameState.from(fps, player, input, combat), player, input, combat, fps);
  }

  private static double renderBearing(DirectionHud hud, HudRenderContext context) {
    try {
      Method method = DirectionHud.class.getDeclaredMethod("renderBearing", HudRenderContext.class);
      method.setAccessible(true);
      return (double) method.invoke(hud, context);
    } catch (NoSuchMethodException | IllegalAccessException exception) {
      throw new AssertionError("Unable to inspect DirectionHud bearing", exception);
    } catch (InvocationTargetException exception) {
      Throwable cause = exception.getCause();
      if (cause instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw new AssertionError("DirectionHud bearing inspection failed", cause);
    }
  }
}
