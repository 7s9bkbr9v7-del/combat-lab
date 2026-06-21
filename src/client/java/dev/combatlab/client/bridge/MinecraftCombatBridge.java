package dev.combatlab.client.bridge;

import dev.combatlab.client.model.CombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.phys.EntityHitResult;

import java.util.UUID;

public final class MinecraftCombatBridge implements CombatBridge {
	private static final long PING_SAMPLE_INTERVAL_TICKS = 20;
	private long lastPingSampleTick = -PING_SAMPLE_INTERVAL_TICKS;
	private UUID pingPlayerId;
	private int cachedPing = -1;

	@Override
	public void update(Minecraft client, CombatState state) {
		if (client.player == null) {
			state.clear();
			pingPlayerId = null;
			cachedPing = -1;
			return;
		}

		state.setAttackStrength(client.player.getAttackStrengthScale(0.0F));
		long gameTick = client.player.level().getGameTime();
		UUID playerId = client.player.getUUID();
		if (!playerId.equals(pingPlayerId) || gameTick < lastPingSampleTick
				|| gameTick - lastPingSampleTick >= PING_SAMPLE_INTERVAL_TICKS) {
			cachedPing = resolvePing(client);
			pingPlayerId = playerId;
			lastPingSampleTick = gameTick;
		}
		state.setPing(cachedPing);

		if (client.hitResult instanceof EntityHitResult hit) {
			UUID targetId = hit.getEntity().getUUID();
			String targetName = targetId.equals(state.targetId())
					? state.targetName()
					: hit.getEntity().getName().getString();
			state.setTarget(targetId, targetName, client.player.distanceTo(hit.getEntity()));
		} else {
			state.clearTarget();
		}
	}

	private static int resolvePing(Minecraft client) {
		if (client.getConnection() == null || client.player == null) {
			return -1;
		}

		PlayerInfo playerInfo = client.getConnection().getPlayerInfo(client.player.getUUID());
		return playerInfo == null ? -1 : playerInfo.getLatency();
	}
}
