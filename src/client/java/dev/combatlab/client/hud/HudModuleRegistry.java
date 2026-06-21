package dev.combatlab.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HudModuleRegistry {
	private final List<HudModule> modules = new ArrayList<>();
	private final List<HudModule> moduleView = Collections.unmodifiableList(modules);
	private final Set<String> ids = new HashSet<>();
	private boolean frozen;

	public <T extends HudModule> T register(T module) {
		if (frozen) {
			throw new IllegalStateException("HUD module registration is frozen");
		}
		String id = module.id().toString();
		if (!ids.add(id)) {
			throw new IllegalArgumentException("Duplicate HUD module id: " + id);
		}

		modules.add(module);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, module.id(), module);
		return module;
	}

	public List<HudModule> modules() {
		return moduleView;
	}

	public boolean hasEnabledModules() {
		for (HudModule module : modules) {
			if (module.enabled()) {
				return true;
			}
		}
		return false;
	}

	public void tick() {
		modules.forEach(HudModule::tick);
	}

	public void freeze() {
		frozen = true;
	}
}
