package dev.combatlab.client;

import dev.combatlab.client.bridge.MinecraftAttackRecorder;
import dev.combatlab.client.bridge.MinecraftCombatBridge;
import dev.combatlab.client.bridge.MinecraftHudGameStateProvider;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.debug.DebugTelemetry;
import dev.combatlab.client.event.AttackRecordedEvent;
import dev.combatlab.client.event.CombatClickEvent;
import dev.combatlab.client.event.CombatEventBus;
import dev.combatlab.client.event.TargetChangedEvent;
import dev.combatlab.client.feature.AchievementToastController;
import dev.combatlab.client.feature.DynamicFovController;
import dev.combatlab.client.feature.FreelookController;
import dev.combatlab.client.feature.FullbrightController;
import dev.combatlab.client.feature.ZoomController;
import dev.combatlab.client.hud.ArmorHud;
import dev.combatlab.client.hud.CpsHud;
import dev.combatlab.client.hud.FpsHud;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.MovementStatusHud;
import dev.combatlab.client.hud.PingHud;
import dev.combatlab.client.input.CpsTracker;
import dev.combatlab.client.model.AttackEvent;
import dev.combatlab.client.model.AttackHistory;
import dev.combatlab.client.model.CombatState;
import dev.combatlab.client.screen.HudEditorScreen;
import dev.combatlab.client.screen.ScreenNavigator;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Coordinates Combat Lab's client-side state and event handling after Fabric startup.
 */
public final class CombatLabRuntime {
	private final CombatLabOptions options;
	private final DebugLogger debug;
	private final KeyMapping openOptions;
	private final KeyMapping zoom;
	private final KeyMapping freelook;
	private final CombatState combatState;
	private final CombatEventBus combatEvents;
	private final AttackHistory attackHistory;
	private final MinecraftCombatBridge bridge;
	private final MinecraftHudGameStateProvider hudGameStateProvider;
	private final MinecraftAttackRecorder attackRecorder;
	private final CpsTracker cpsTracker;
	private final HudModuleRegistry hudModules;

	private CombatLabRuntime(
			CombatLabOptions options,
			DebugLogger debug,
			KeyMapping openOptions,
			KeyMapping zoom,
			KeyMapping freelook,
			CombatState combatState,
			CombatEventBus combatEvents,
			AttackHistory attackHistory,
			MinecraftCombatBridge bridge,
			MinecraftHudGameStateProvider hudGameStateProvider,
			MinecraftAttackRecorder attackRecorder,
			CpsTracker cpsTracker,
			HudModuleRegistry hudModules
	) {
		this.options = options;
		this.debug = debug;
		this.openOptions = openOptions;
		this.zoom = zoom;
		this.freelook = freelook;
		this.combatState = combatState;
		this.combatEvents = combatEvents;
		this.attackHistory = attackHistory;
		this.bridge = bridge;
		this.hudGameStateProvider = hudGameStateProvider;
		this.attackRecorder = attackRecorder;
		this.cpsTracker = cpsTracker;
		this.hudModules = hudModules;
	}

	public static CombatLabRuntime create(KeyMapping openOptions, KeyMapping zoom, KeyMapping freelook) {
		CombatLabOptions options = CombatLabOptions.load();
		FullbrightController.setEnabled(options.fullbrightEnabled());
		AchievementToastController.setDisabled(options.achievementToastsDisabled());
		DynamicFovController.setEnabled(options.dynamicFovEnabled());

		DebugLogger debug = new DebugLogger(options::debugLoggingEnabled);
		CpsTracker cpsTracker = new CpsTracker();
		CombatState combatState = new CombatState();
		CombatEventBus combatEvents = new CombatEventBus();
		combatEvents.subscribe(CombatClickEvent.class, event -> cpsTracker.recordClicks(event.clickCount(), event.capturedAtNanos()));
		DebugTelemetry debugTelemetry = new DebugTelemetry();
		combatEvents.subscribe(TargetChangedEvent.class, event -> debugTelemetry.onTargetChanged(event, options.debugLoggingEnabled(), debug));
		combatEvents.subscribe(AttackRecordedEvent.class, event -> debugTelemetry.onAttackRecorded(event, options.debugLoggingEnabled(), debug));
		HudModuleRegistry hudModules = new HudModuleRegistry();
		hudModules.register(new FpsHud(options, debug));
		hudModules.register(new CpsHud(options, debug));
		hudModules.register(new MovementStatusHud(options, debug));
		hudModules.register(new PingHud(options, debug));
		hudModules.register(new ArmorHud(options, debug));
		hudModules.freeze();

		return new CombatLabRuntime(
				options,
				debug,
				openOptions,
				zoom,
				freelook,
				combatState,
				combatEvents,
				new AttackHistory(64),
				new MinecraftCombatBridge(),
				new MinecraftHudGameStateProvider(),
				new MinecraftAttackRecorder(),
				cpsTracker,
				hudModules
		);
	}

	public void tick(Minecraft client) {
		String previousTargetName = combatState.targetName();
		java.util.UUID previousTargetId = combatState.targetId();
		bridge.update(client, combatState);
		long nowNanos = System.nanoTime();
		publishTargetChange(previousTargetId, previousTargetName, nowNanos);
		ZoomController.tick(client, zoom);
		FreelookController.tick(client, freelook);
		hudModules.tick(hudGameStateProvider.snapshot(client, combatState, cpsTracker, nowNanos));
		while (openOptions.consumeClick()) {
			debug.info("Opening HUD editor");
			ScreenNavigator.open(client, new HudEditorScreen(options, hudModules, debug));
		}
	}

	public boolean onPreAttack(Minecraft client, LocalPlayer player, int clickCount) {
		if (clickCount == 0) {
			return false;
		}

		long nowNanos = System.nanoTime();
		combatEvents.publish(new CombatClickEvent(clickCount, nowNanos));
		AttackEvent event = attackRecorder.record(client, player, combatState, attackHistory);
		combatEvents.publish(new AttackRecordedEvent(event));
		return false;
	}

	private void publishTargetChange(java.util.UUID previousTargetId, String previousTargetName, long nowNanos) {
		if (java.util.Objects.equals(previousTargetId, combatState.targetId())) {
			return;
		}
		combatEvents.publish(new TargetChangedEvent(
				previousTargetId,
				previousTargetName,
				combatState.targetId(),
				combatState.targetName(),
				combatState.targetDistance(),
				nowNanos
		));
	}

	public int hudModuleCount() {
		return hudModules.modules().size();
	}
}
