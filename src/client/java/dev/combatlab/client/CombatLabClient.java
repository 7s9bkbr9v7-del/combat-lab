package dev.combatlab.client;

import com.mojang.blaze3d.platform.InputConstants;
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

	@Override
	public void onInitializeClient() {
		KeyMapping openOptions = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.combatlab.open_options",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_RSHIFT,
				KeyMapping.Category.MISC
		));
		KeyMapping zoom = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.combatlab.zoom",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_C,
				KeyMapping.Category.MISC
		));
		KeyMapping freelook = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.combatlab.freelook",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_LALT,
				KeyMapping.Category.MISC
		));
		CombatLabRuntime runtime = CombatLabRuntime.create(openOptions, zoom, freelook);

		ClientTickEvents.END_CLIENT_TICK.register(runtime::tick);
		ClientPreAttackCallback.EVENT.register(runtime::onPreAttack);
		LOGGER.info("Combat Lab initialized with {} HUD module(s)", runtime.hudModuleCount());
	}
}
