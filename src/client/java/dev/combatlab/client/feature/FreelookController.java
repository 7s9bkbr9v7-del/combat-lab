package dev.combatlab.client.feature;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class FreelookController {
	private static boolean active;
	private static float yaw;
	private static float pitch;

	private FreelookController() {
	}

	public static void tick(Minecraft client, KeyMapping freelookKey) {
		boolean shouldBeActive = client.player != null && freelookKey.isDown();
		if (shouldBeActive && !active) {
			start(client.player);
		} else if (!shouldBeActive && active) {
			stop();
		}
	}

	public static boolean active() {
		return active;
	}

	public static float yaw() {
		return yaw;
	}

	public static float pitch() {
		return pitch;
	}

	public static void turn(double deltaX, double deltaY) {
		yaw += (float) deltaX * 0.15F;
		pitch = Mth.clamp(pitch + (float) deltaY * 0.15F, -90.0F, 90.0F);
	}

	private static void start(LocalPlayer player) {
		yaw = player.getViewYRot(1.0F);
		pitch = player.getViewXRot(1.0F);
		active = true;
	}

	private static void stop() {
		active = false;
	}
}
