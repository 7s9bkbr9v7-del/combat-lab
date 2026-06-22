package dev.combatlab.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.combatlab.client.bridge.MinecraftCombatBridge;
import dev.combatlab.client.bridge.MinecraftAttackRecorder;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugTelemetry;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.CpsHud;
import dev.combatlab.client.hud.ArmorHud;
import dev.combatlab.client.hud.FpsHud;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.MovementStatusHud;
import dev.combatlab.client.input.CpsTracker;
import dev.combatlab.client.feature.FullbrightController;
import dev.combatlab.client.feature.AchievementToastController;
import dev.combatlab.client.model.AttackHistory;
import dev.combatlab.client.model.AttackEvent;
import dev.combatlab.client.model.CombatState;
import dev.combatlab.client.screen.HudEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CombatLabClient implements ClientModInitializer {
	public static final String MOD_ID = "combatlab";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final CombatState combatState = new CombatState();
	private final AttackHistory attackHistory = new AttackHistory(64);
	private final MinecraftCombatBridge bridge = new MinecraftCombatBridge();
	private final MinecraftAttackRecorder attackRecorder = new MinecraftAttackRecorder();
	private final DebugTelemetry debugTelemetry = new DebugTelemetry();
	private CombatLabOptions options;

	@Override
	public void onInitializeClient() {
		options = CombatLabOptions.load();
		FullbrightController.setEnabled(options.fullbrightEnabled());
		AchievementToastController.setDisabled(options.achievementToastsDisabled());
		DebugLogger debug = new DebugLogger(options::debugLoggingEnabled);
		CpsTracker cpsTracker = new CpsTracker();
		HudModuleRegistry hudModules = new HudModuleRegistry();
		hudModules.register(new FpsHud(options, debug));
		hudModules.register(new CpsHud(cpsTracker, options, debug));
		hudModules.register(new MovementStatusHud(options, debug));
		hudModules.register(new ArmorHud(options, debug));
		hudModules.freeze();
		KeyMapping openOptions = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.combatlab.open_options",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_RSHIFT,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			bridge.update(client, combatState);
			hudModules.tick();
			debugTelemetry.update(combatState, options.debugLoggingEnabled(), debug);
			while (openOptions.consumeClick()) {
				debug.info("Opening HUD editor");
				client.setScreenAndShow(new HudEditorScreen(options, hudModules, debug));
			}
		});
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (clickCount != 0) {
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
			}
			return false;
		});
		debug.info("Combat Lab initialized with {} HUD module(s)", hudModules.modules().size());
		LOGGER.info("Combat Lab initialized");
	}
}
