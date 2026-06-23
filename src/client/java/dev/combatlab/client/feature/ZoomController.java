package dev.combatlab.client.feature;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class ZoomController {
	private static final float ZOOM_FOV_MULTIPLIER = 0.25F;
	private static final float ANIMATION_SPEED = 18.0F;
	private static final long MAX_FRAME_NANOS = 100_000_000L;
	private static float progress;
	private static boolean zooming;
	private static long lastFrameNanos;

	private ZoomController() {
	}

	public static void tick(Minecraft client, KeyMapping zoomKey) {
		zooming = client.player != null && zoomKey.isDown();
	}

	public static float apply(float fov) {
		updateProgress(System.nanoTime());
		if (progress <= 0.0F) {
			return fov;
		}
		float zoomedFov = fov * ZOOM_FOV_MULTIPLIER;
		return fov + (zoomedFov - fov) * easedProgress();
	}

	public static double mouseSensitivityScale() {
		if (progress <= 0.0F) {
			return 1.0D;
		}
		return 1.0D + (ZOOM_FOV_MULTIPLIER - 1.0D) * easedProgress();
	}

	private static void updateProgress(long nowNanos) {
		if (lastFrameNanos == 0L) {
			lastFrameNanos = nowNanos;
		}
		long elapsedNanos = Math.min(Math.max(0L, nowNanos - lastFrameNanos), MAX_FRAME_NANOS);
		lastFrameNanos = nowNanos;

		float target = zooming ? 1.0F : 0.0F;
		float deltaSeconds = elapsedNanos / 1_000_000_000.0F;
		float blend = 1.0F - (float) Math.exp(-ANIMATION_SPEED * deltaSeconds);
		progress += (target - progress) * blend;
		if (Math.abs(target - progress) < 0.001F) {
			progress = target;
		}
	}

	private static float easedProgress() {
		return 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
	}
}
