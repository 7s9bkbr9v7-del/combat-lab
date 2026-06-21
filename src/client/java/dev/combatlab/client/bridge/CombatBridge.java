package dev.combatlab.client.bridge;

import dev.combatlab.client.model.CombatState;
import net.minecraft.client.Minecraft;

/**
 * The version-sensitive boundary between Minecraft and client features.
 */
public interface CombatBridge {
	void update(Minecraft client, CombatState state);
}
