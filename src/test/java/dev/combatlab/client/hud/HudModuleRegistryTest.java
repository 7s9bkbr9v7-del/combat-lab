package dev.combatlab.client.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudModuleRegistryTest {
	@Test
	void ticksOnlyEnabledOrExplicitBackgroundModules() {
		HudModuleRegistry registry = new HudModuleRegistry();
		CountingModule disabled = registry.register(new CountingModule("disabled", false, false));
		CountingModule enabled = registry.register(new CountingModule("enabled", true, false));
		CountingModule background = registry.register(new CountingModule("background", false, true));

		registry.tick();

		assertEquals(0, disabled.tickCount);
		assertEquals(1, enabled.tickCount);
		assertEquals(1, background.tickCount);
	}

	@Test
	void gameplaySnapshotCapturesAndRendersOnlyEnabledModules() {
		CountingModule disabled = new CountingModule("disabled", false, false);
		CountingModule enabled = new CountingModule("enabled", true, false);
		HudFrameSnapshot snapshot = new HudFrameSnapshot(List.of(disabled, enabled));

		snapshot.capture(null, null, 320, 180);
		snapshot.render(null);

		assertEquals(0, disabled.boundsCount);
		assertEquals(0, disabled.renderCount);
		assertEquals(1, enabled.boundsCount);
		assertEquals(1, enabled.renderCount);
	}

	private static final class CountingModule implements HudModule {
		private final Identifier id;
		private final boolean enabled;
		private final boolean ticksWhenDisabled;
		private int tickCount;
		private int boundsCount;
		private int renderCount;

		private CountingModule(String path, boolean enabled, boolean ticksWhenDisabled) {
			this.id = Identifier.fromNamespaceAndPath("combatlab", path);
			this.enabled = enabled;
			this.ticksWhenDisabled = ticksWhenDisabled;
		}

		@Override
		public Identifier id() {
			return id;
		}

		@Override
		public Component displayName() {
			return Component.literal(id.toString());
		}

		@Override
		public boolean enabled() {
			return enabled;
		}

		@Override
		public void setEnabled(boolean enabled) {
		}

		@Override
		public HudPosition position(int screenWidth, int screenHeight) {
			return new HudPosition(0, 0);
		}

		@Override
		public HudSize size() {
			return new HudSize(1, 1);
		}

		@Override
		public HudRectangle bounds(int screenWidth, int screenHeight) {
			boundsCount++;
			return new HudRectangle(0, 0, 1, 1);
		}

		@Override
		public HudOrientation orientation(int screenWidth, int screenHeight) {
			return HudOrientationResolver.resolve(bounds(screenWidth, screenHeight), screenWidth, screenHeight);
		}

		@Override
		public void updatePosition(int x, int y, int screenWidth, int screenHeight) {
		}

		@Override
		public void savePosition() {
		}

		@Override
		public void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context) {
			renderCount++;
		}

		@Override
		public void renderEditorPreview(GuiGraphicsExtractor graphics, Font font, HudRectangle bounds) {
		}

		@Override
		public void tick() {
			tickCount++;
		}

		@Override
		public boolean ticksWhenDisabled() {
			return ticksWhenDisabled;
		}
	}
}
