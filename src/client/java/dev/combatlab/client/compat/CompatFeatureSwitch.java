package dev.combatlab.client.compat;

/**
 * A resolved compatibility/runtime switch intended for render or tick paths.
 *
 * <p>Version probing, reflection, or module lookups should happen before updating
 * this switch. Hot paths should only call {@link #enabled()}, which is just a
 * volatile boolean read.</p>
 */
public final class CompatFeatureSwitch {
	private volatile boolean enabled;

	private CompatFeatureSwitch(boolean enabled) {
		this.enabled = enabled;
	}

	public static CompatFeatureSwitch initiallyDisabled() {
		return new CompatFeatureSwitch(false);
	}

	public static CompatFeatureSwitch initiallyEnabled() {
		return new CompatFeatureSwitch(true);
	}

	public boolean enabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
