package dev.combatlab.client.state;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record PlayerEffectTimer(
		String id,
		String displayName,
		int amplifier,
		int durationTicks,
		boolean infinite,
		boolean ambient,
		int color,
		Identifier iconTexture
) {
	public PlayerEffectTimer {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(displayName, "displayName");
	}
}
