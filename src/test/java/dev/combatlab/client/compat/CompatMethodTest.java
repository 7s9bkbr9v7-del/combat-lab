package dev.combatlab.client.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatMethodTest {
	@Test
	void missingClassOrMethodIsSafeToInvoke() {
		assertFalse(CompatMethod.find("missing.compat.Class", "open").present());
		assertFalse(CompatMethod.find(Target.class, "missing").present());
		assertFalse(CompatMethod.missing().invoke(new Target()));
	}

	@Test
	void invokesPresentMethodAndPropagatesRuntimeFailure() {
		Target target = new Target();
		CompatMethod open = CompatMethod.find(Target.class, "open");
		CompatMethod fail = CompatMethod.find(Target.class, "fail");

		assertTrue(open.present());
		assertTrue(open.invoke(target));
		assertTrue(target.opened);
		assertThrows(IllegalStateException.class, () -> fail.invoke(target));
	}

	@Test
	void featureSwitchIsCheapResolvedRuntimeState() {
		CompatFeatureSwitch feature = CompatFeatureSwitch.initiallyDisabled();

		assertFalse(feature.enabled());
		feature.setEnabled(true);
		assertTrue(feature.enabled());
		feature.setEnabled(false);
		assertFalse(feature.enabled());
	}

	public static final class Target {
		private boolean opened;

		public void open() {
			opened = true;
		}

		public void fail() {
			throw new IllegalStateException("boom");
		}
	}
}
