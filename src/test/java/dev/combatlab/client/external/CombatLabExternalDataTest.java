package dev.combatlab.client.external;

import dev.combatlab.client.config.CombatLabConfigCodec;
import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.ConfigStore;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.hud.HudGameState;
import dev.combatlab.client.hud.HudAttachmentSide;
import dev.combatlab.client.hud.HudCorner;
import dev.combatlab.client.hud.HudHorizontalSide;
import dev.combatlab.client.hud.HudModule;
import dev.combatlab.client.hud.HudModuleDefinition;
import dev.combatlab.client.hud.HudModuleDescriptor;
import dev.combatlab.client.hud.HudModuleRegistry;
import dev.combatlab.client.hud.HudOrientation;
import dev.combatlab.client.hud.HudPosition;
import dev.combatlab.client.hud.HudRectangle;
import dev.combatlab.client.hud.HudRenderContext;
import dev.combatlab.client.hud.HudSize;
import dev.combatlab.client.hud.HudVerticalSide;
import dev.combatlab.client.state.ClientGameState;
import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.PlayerState;
import dev.combatlab.client.state.TargetState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatLabExternalDataTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void exportsLauncherFacingSettingsAndModuleManifest() {
		CombatLabOptions options = options();
		HudModuleRegistry registry = registry(options);
		registry.registerDescriptor(descriptor("fps", "FPS", 0.02, 0.03, false, false));
		registry.registerDescriptor(descriptor("combat", "Combat", 0.50, 0.70, true, true));
		registry.settings("combatlab:fps").setEnabled(true);
		registry.settings("combatlab:fps").updatePosition(0.25, 0.75);
		registry.settings("combatlab:fps").updateScale(1.5);

		ExternalCombatLabSettingsDocument settings = CombatLabExternalData.settingsDocument(options, registry);
		ExternalHudModuleManifest manifest = CombatLabExternalData.moduleManifest(registry);

		assertEquals(CombatLabExternalSchema.SETTINGS_SCHEMA_VERSION, settings.schemaVersion());
		assertEquals(2, settings.hudModules().size());
		ExternalHudModuleSettings fps = settings.hudModules().getFirst();
		assertEquals("combatlab:fps", fps.id());
		assertEquals("FPS", fps.displayName());
		assertTrue(fps.enabled());
		assertEquals(0.25, fps.normalizedX());
		assertEquals(0.75, fps.normalizedY());
		assertEquals(1.5, fps.scale());

		assertEquals(CombatLabExternalSchema.MODULE_MANIFEST_SCHEMA_VERSION, manifest.schemaVersion());
		assertEquals("combatlab:combat", manifest.modules().get(1).id());
		assertTrue(manifest.modules().get(1).resizable());
		assertTrue(manifest.modules().get(1).loadWhenDisabled());
	}

	@Test
	void exportsTelemetrySnapshotWithoutMinecraftInternals() {
		UUID targetId = UUID.fromString("0b15b31f-fd8b-4c2e-8fd6-0cd7c11a861d");
		ClientGameState state = new ClientGameState(
				HudGameState.empty(),
				PlayerState.absent(),
				new InputState(12),
				new CombatSnapshot(0.75F, 42, new TargetState(targetId, "Target", 3.5F)),
				144
		);

		ExternalTelemetrySnapshot snapshot = CombatLabExternalData.telemetrySnapshot(state);
		String encoded = new CombatLabExternalCodec().encode(snapshot);

		assertEquals(CombatLabExternalSchema.TELEMETRY_SCHEMA_VERSION, snapshot.schemaVersion());
		assertEquals(144, snapshot.fps());
		assertEquals(12, snapshot.cps());
		assertEquals(42, snapshot.ping());
		assertEquals(0.75F, snapshot.attackStrength());
		assertTrue(snapshot.target().present());
		assertEquals(targetId.toString(), snapshot.target().id());
		assertTrue(encoded.contains("\"schemaVersion\""));
		assertTrue(encoded.contains("\"target\""));
	}

	private HudModuleRegistry registry() {
		return registry(options());
	}

	private HudModuleRegistry registry(CombatLabOptions options) {
		return new HudModuleRegistry(options, new DebugLogger(() -> false));
	}

	private CombatLabOptions options() {
		ConfigStore store = new ConfigStore(
				temporaryDirectory.resolve("combatlab.json"),
				new CombatLabConfigCodec()
		);
		return CombatLabOptions.load(store);
	}

	private static HudModuleDescriptor descriptor(
			String path,
			String displayName,
			double defaultX,
			double defaultY,
			boolean resizable,
			boolean loadWhenDisabled
	) {
		HudModuleDefinition definition = new HudModuleDefinition(
				Identifier.fromNamespaceAndPath("combatlab", path),
				Component.literal(displayName),
				defaultX,
				defaultY,
				resizable
		);
		return new HudModuleDescriptor(definition, dependencies -> new NoopModule(definition), loadWhenDisabled);
	}

	private static final class NoopModule implements HudModule {
		private final HudModuleDefinition definition;

		private NoopModule(HudModuleDefinition definition) {
			this.definition = definition;
		}

		@Override
		public Identifier id() {
			return definition.id();
		}

		@Override
		public Component displayName() {
			return definition.displayName();
		}

		@Override
		public boolean enabled() {
			return false;
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
			return new HudRectangle(0, 0, 1, 1);
		}

		@Override
		public HudOrientation orientation(int screenWidth, int screenHeight) {
			return new HudOrientation(HudHorizontalSide.RIGHT, HudVerticalSide.BOTTOM, HudCorner.BOTTOM_RIGHT);
		}

		@Override
		public void updatePosition(int x, int y, int screenWidth, int screenHeight) {
		}

		@Override
		public void savePosition() {
		}

		@Override
		public String attachmentTargetId() {
			return null;
		}

		@Override
		public void attachTo(HudModule target, HudAttachmentSide side, int offset) {
		}

		@Override
		public void clearAttachment() {
		}

		@Override
		public void detach(int screenWidth, int screenHeight) {
		}

		@Override
		public void renderInGame(GuiGraphicsExtractor graphics, HudRenderContext context) {
		}

		@Override
		public void renderEditorPreview(
				GuiGraphicsExtractor graphics,
				Font font,
				HudRectangle bounds,
				ClientGameState gameState
		) {
		}
	}
}
