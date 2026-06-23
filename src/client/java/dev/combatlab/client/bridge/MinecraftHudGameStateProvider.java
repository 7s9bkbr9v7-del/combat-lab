package dev.combatlab.client.bridge;

import dev.combatlab.client.hud.HudGameState;
import dev.combatlab.client.input.CpsTracker;
import dev.combatlab.client.model.CombatState;
import dev.combatlab.client.state.ArmorSlot;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.MovementState;
import dev.combatlab.client.state.PlayerArmor;
import dev.combatlab.client.state.PlayerState;
import dev.combatlab.client.state.TargetState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;

public final class MinecraftHudGameStateProvider {
	public ClientGameState snapshot(Minecraft client, CombatState combatState, CpsTracker cpsTracker, long nowNanos) {
		LocalPlayer player = client.player;
		PlayerState playerState = playerState(client, player);
		InputState inputState = new InputState(cpsTracker.currentCps(nowNanos));
		CombatSnapshot combatSnapshot = combatSnapshot(combatState);
		int fps = client.getFps();
		return new ClientGameState(
				HudGameState.from(fps, playerState, inputState, combatSnapshot),
				playerState,
				inputState,
				combatSnapshot,
				fps
		);
	}

	private static PlayerState playerState(Minecraft client, LocalPlayer player) {
		if (player == null) {
			return PlayerState.absent();
		}
		return new PlayerState(true, movement(client, player), armor(player));
	}

	private static MovementState movement(Minecraft client, LocalPlayer player) {
		if (player == null) {
			return MovementState.inactive();
		}
		return new MovementState(
				player.isCrouching(),
				player.isSprinting(),
				client.options.toggleSprint().get() && client.options.keySprint.isDown()
		);
	}

	private static PlayerArmor armor(LocalPlayer player) {
		if (player == null) {
			return PlayerArmor.empty();
		}

		EnumMap<ArmorSlot, ItemStack> stacks = new EnumMap<>(ArmorSlot.class);
		stacks.put(ArmorSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
		stacks.put(ArmorSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
		stacks.put(ArmorSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
		stacks.put(ArmorSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
		return PlayerArmor.of(stacks);
	}

	private static CombatSnapshot combatSnapshot(CombatState combatState) {
		TargetState target = combatState.targetId() == null
				? TargetState.none()
				: new TargetState(combatState.targetId(), combatState.targetName(), combatState.targetDistance());
		return new CombatSnapshot(combatState.attackStrength(), combatState.ping(), target);
	}
}
