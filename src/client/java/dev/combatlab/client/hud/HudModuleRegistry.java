package dev.combatlab.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HudModuleRegistry implements HudElement {
	private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("combatlab", "hud");
	private final List<HudModule> modules = new ArrayList<>();
	private final List<HudModule> moduleView = Collections.unmodifiableList(modules);
	private final Set<String> ids = new HashSet<>();
	private boolean frozen;
	private boolean editorOpen;
	private HudFrameSnapshot frameSnapshot;

	public <T extends HudModule> T register(T module) {
		if (frozen) {
			throw new IllegalStateException("HUD module registration is frozen");
		}
		String id = module.id().toString();
		if (!ids.add(id)) {
			throw new IllegalArgumentException("Duplicate HUD module id: " + id);
		}

		modules.add(module);
		return module;
	}

	public List<HudModule> modules() {
		return moduleView;
	}

	public void tick() {
		for (HudModule module : modules) {
			if (module.enabled() || module.ticksWhenDisabled()) {
				module.tick();
			}
		}
	}

	public void freeze() {
		if (frozen) {
			return;
		}
		frameSnapshot = new HudFrameSnapshot(moduleView);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HUD_ID, this);
		frozen = true;
	}

	public void setEditorOpen(boolean editorOpen) {
		this.editorOpen = editorOpen;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || editorOpen) {
			return;
		}

		frameSnapshot.capture(client, client.font, graphics.guiWidth(), graphics.guiHeight());
		frameSnapshot.render(graphics);
	}
}
