package dev.combatlab.client;

import dev.combatlab.client.bridge.MinecraftAttackRecorder;
import dev.combatlab.client.bridge.MinecraftCombatBridge;
import dev.combatlab.client.bridge.MinecraftHudGameStateProvider;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.debug.DebugTelemetry;
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
	private final AttackHistory attackHistory;
	private final MinecraftCombatBridge bridge;
	private final MinecraftHudGameStateProvider hudGameStateProvider;
	private final MinecraftAttackRecorder attackRecorder;
	private final DebugTelemetry debugTelemetry;
	private final CpsTracker cpsTracker;
	private final HudModuleRegistry hudModules;

	private CombatLabRuntime(
			CombatLabOptions options,
			DebugLogger debug,
			KeyMapping openOptions,
			KeyMapping zoom,
			KeyMapping freelook,
			CombatState combatState,
			AttackHistory attackHistory,
			MinecraftCombatBridge bridge,
			MinecraftHudGameStateProvider hudGameStateProvider,
			MinecraftAttackRecorder attackRecorder,
			DebugTelemetry debugTelemetry,
			CpsTracker cpsTracker,
			HudModuleRegistry hudModules
	) {
		this.options = options;
		this.debug = debug;
		this.openOptions = openOptions;
		this.zoom = zoom;
		this.freelook = freelook;
		this.combatState = combatState;
		this.attackHistory = attackHistory;
		this.bridge = bridge;
		this.hudGameStateProvider = hudGameStateProvider;
		this.attackRecorder = attackRecorder;
		this.debugTelemetry = debugTelemetry;
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
				new AttackHistory(64),
				new MinecraftCombatBridge(),
				new MinecraftHudGameStateProvider(),
				new MinecraftAttackRecorder(),
				new DebugTelemetry(),
				cpsTracker,
				hudModules
		);
	}

	public void tick(Minecraft client) {
		bridge.update(client, combatState);
		long nowNanos = System.nanoTime();
		ZoomController.tick(client, zoom);
		FreelookController.tick(client, freelook);
		hudModules.tick(hudGameStateProvider.snapshot(client, combatState, cpsTracker, nowNanos));
		debugTelemetry.update(combatState, options.debugLoggingEnabled(), debug);
		while (openOptions.consumeClick()) {
			debug.info("Opening HUD editor");
			ScreenNavigator.open(client, new HudEditorScreen(options, hudModules, debug));
		}
	}

	public boolean onPreAttack(Minecraft client, LocalPlayer player, int clickCount) {
		if (clickCount == 0) {
			return false;
		}

		cpsTracker.recordClicks(clickCount, System.nanoTime());
		AttackEvent event = attackRecorder.record(client, player, combatState, attackHistory);
		if (debug.isEnabled()) {
			debug.info(
					"Attack #{}: target={}, distance={}, strength={}%, ping={}ms, tick={}",
					event.sequence(),
					event.hasTarget() ? event.targetName() : "miss",
					event.hasTarget() ? String.format("%.2f", event.targetDistance()) : "n/a",
					Math.round(event.attackStrength() * 100.0F),
					event.ping(),
					event.gameTick()
			);
		}
		return false;
	}

	public int hudModuleCount() {
		return hudModules.modules().size();
	}
}
