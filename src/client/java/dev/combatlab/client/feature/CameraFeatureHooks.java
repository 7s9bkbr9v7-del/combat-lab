package dev.combatlab.client.feature;

import net.minecraft.client.CameraType;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

public final class CameraFeatureHooks {
	private CameraFeatureHooks() {
	}

	public static Object dynamicFovScale(OptionInstance<?> option) {
		Object configuredScale = option.get();
		return DynamicFovController.enabled() ? configuredScale : 0.0D;
	}

	public static CameraType cameraType(Options options) {
		return FreelookController.active() ? CameraType.THIRD_PERSON_BACK : options.getCameraType();
	}

	public static float yaw(float yaw) {
		return FreelookController.active() ? FreelookController.yaw() : yaw;
	}

	public static float pitch(float pitch) {
		return FreelookController.active() ? FreelookController.pitch() : pitch;
	}

	public static float fov(float fov) {
		return ZoomController.apply(fov);
	}
}
