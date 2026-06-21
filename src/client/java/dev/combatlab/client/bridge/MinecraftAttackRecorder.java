package dev.combatlab.client.bridge;

import dev.combatlab.client.model.AttackEvent;
import dev.combatlab.client.model.AttackHistory;
import dev.combatlab.client.model.AttackSample;
import dev.combatlab.client.model.CombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.UUID;

/**
 * Converts a version-specific Minecraft attack attempt into stable telemetry.
 */
public final class MinecraftAttackRecorder {
	public AttackEvent record(Minecraft client, LocalPlayer player, CombatState state, AttackHistory history) {
		Entity target = client.hitResult instanceof EntityHitResult hit ? hit.getEntity() : null;
		UUID targetId = target == null ? null : target.getUUID();
		String targetName = target == null ? null : target.getName().getString();
		float distance = target == null ? 0.0F : player.distanceTo(target);

		return history.record(new AttackSample(
				player.level().getGameTime(),
				System.nanoTime(),
				targetId,
				targetName,
				distance,
				player.getAttackStrengthScale(0.0F),
				state.ping()
		));
	}
}
