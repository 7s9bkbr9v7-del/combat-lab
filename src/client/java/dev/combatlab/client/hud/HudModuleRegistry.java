package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.config.HudModuleSettings;
import dev.combatlab.client.debug.DebugLogger;
import dev.combatlab.client.feature.VanillaHudFeatureHooks;
import dev.combatlab.client.state.ClientGameState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class HudModuleRegistry implements HudElement {
  private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("combatlab", "hud");
  private final HudModuleDependencies dependencies;
  private final CombatLabOptions options;
  private final DebugLogger debug;
  private final List<HudModuleDescriptor> descriptors = new ArrayList<>();
  private final List<HudModuleDescriptor> descriptorView =
      Collections.unmodifiableList(descriptors);
  private final Map<String, HudModuleDescriptor> descriptorsById = new HashMap<>();
  private final Map<String, HudModuleSettings> settingsById = new HashMap<>();
  private final List<HudModule> modules = new ArrayList<>();
  private final List<HudModule> moduleView = Collections.unmodifiableList(modules);
  private final Map<String, HudModule> modulesById = new HashMap<>();
  private final Map<String, PendingAnchorPromotion> pendingAnchorPromotions = new HashMap<>();
  private boolean frozen;
  private boolean editorOpen;
  private ClientGameState gameState = ClientGameState.empty();
  private HudFrameSnapshot frameSnapshot;

  public HudModuleRegistry(CombatLabOptions options, DebugLogger debug) {
    this.options = options;
    this.debug = debug;
    this.dependencies = new HudModuleDependencies(options, debug);
  }

  public HudModuleRegistry() {
    this(CombatLabOptions.load(), new DebugLogger(() -> false));
  }

  public void registerDescriptor(HudModuleDescriptor descriptor) {
    if (frozen) {
      throw new IllegalStateException("HUD module registration is frozen");
    }
    String id = descriptor.id();
    if (descriptorsById.putIfAbsent(id, descriptor) != null) {
      throw new IllegalArgumentException("Duplicate HUD module id: " + id);
    }

    descriptors.add(descriptor);
    settingsById.put(
        id,
        options.bindHudModule(
            id, descriptor.definition().defaultX(), descriptor.definition().defaultY()));
    VanillaHudFeatureHooks.updateHudModuleState(id, enabled(id));
    if (enabled(id) || descriptor.loadWhenDisabled()) {
      load(id);
    }
  }

  public List<HudModuleDescriptor> descriptors() {
    return descriptorView;
  }

  public List<HudModule> modules() {
    return moduleView;
  }

  public HudModule module(String id) {
    return modulesById.get(id);
  }

  public HudModuleSettings settings(String id) {
    return requireSettings(id);
  }

  public boolean enabled(String id) {
    HudModuleSettings settings = settingsById.get(id);
    return settings != null && settings.enabled();
  }

  public void setEnabled(String id, boolean enabled) {
    HudModuleSettings settings = requireSettings(id);
    if (settings.enabled() == enabled) {
      return;
    }

    settings.setEnabled(enabled);
    VanillaHudFeatureHooks.updateHudModuleState(id, enabled);
    HudModuleDescriptor descriptor = descriptorsById.get(id);
    if (enabled) {
      load(id);
    } else if (!descriptor.loadWhenDisabled()) {
      unload(id);
    }
    debug.info(
        "{} {}",
        descriptor.definition().displayName().getString(),
        enabled ? "enabled" : "disabled");
  }

  public void setEnabled(String id, boolean enabled, int screenWidth, int screenHeight) {
    if (!enabled) {
      promoteFirstChild(id, screenWidth, screenHeight);
      setEnabled(id, false);
      return;
    }

    setEnabled(id, enabled);
    restoreFirstChild(id, screenWidth, screenHeight);
  }

  public void tick(ClientGameState gameState) {
    this.gameState = gameState;
    for (HudModule module : modules) {
      if (module.enabled() || module.ticksWhenDisabled()) {
        module.tick(gameState);
      }
    }
  }

  public ClientGameState gameState() {
    return gameState;
  }

  public void freeze() {
    if (frozen) {
      return;
    }
    refreshFrameSnapshot();
    HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HUD_ID, this);
    frozen = true;
  }

  public void setEditorOpen(boolean editorOpen) {
    this.editorOpen = editorOpen;
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    Minecraft client = Minecraft.getInstance();
    if (client.player == null || editorOpen) {
      return;
    }

    frameSnapshot.capture(
        gameState,
        client.font,
        graphics.guiWidth(),
        graphics.guiHeight(),
        deltaTracker.getRealtimeDeltaTicks());
    frameSnapshot.render(graphics);
  }

  private void load(String id) {
    if (modulesById.containsKey(id)) {
      return;
    }

    HudModuleDescriptor descriptor = descriptorsById.get(id);
    if (descriptor == null) {
      throw new IllegalArgumentException("Unknown HUD module id: " + id);
    }
    HudModule module = descriptor.factory().create(dependencies);
    modulesById.put(id, module);
    modules.add(module);
    if (module instanceof BaseHudModule baseModule) {
      baseModule.bindModuleLookup(modulesById::get);
    }
    refreshFrameSnapshot();
  }

  private void unload(String id) {
    HudModule module = modulesById.remove(id);
    if (module == null) {
      return;
    }
    modules.remove(module);
    refreshFrameSnapshot();
  }

  private void promoteFirstChild(String parentId, int screenWidth, int screenHeight) {
    HudModule parent = module(parentId);
    HudModule child = firstEnabledChild(parentId);
    if (parent == null || child == null) {
      pendingAnchorPromotions.remove(parentId);
      return;
    }

    HudRectangle parentBounds = parent.editorBounds(screenWidth, screenHeight);
    HudAttachmentSide side =
        HudAttachmentSide.fromStored(settings(child.id().toString()).attachmentSide());
    if (side == null) {
      pendingAnchorPromotions.remove(parentId);
      return;
    }

    pendingAnchorPromotions.put(
        parentId,
        new PendingAnchorPromotion(
            child.id().toString(),
            parentBounds.x(),
            parentBounds.y(),
            side,
            settings(child.id().toString()).attachmentOffset()));
    child.clearAttachment();
    child.updatePosition(parentBounds.x(), parentBounds.y(), screenWidth, screenHeight);
    child.savePosition();
  }

  private void restoreFirstChild(String parentId, int screenWidth, int screenHeight) {
    PendingAnchorPromotion promotion = pendingAnchorPromotions.remove(parentId);
    HudModule parent = module(parentId);
    HudModule child = promotion == null ? null : module(promotion.childId());
    if (parent == null || child == null || child.attachmentTargetId() != null) {
      return;
    }

    HudRectangle childBounds = child.editorBounds(screenWidth, screenHeight);
    if (childBounds.x() != promotion.promotedX() || childBounds.y() != promotion.promotedY()) {
      return;
    }

    child.attachTo(parent, promotion.side(), promotion.offset());
    child.savePosition();
  }

  private HudModule firstEnabledChild(String parentId) {
    for (HudModule candidate : modules) {
      if (candidate.enabled() && parentId.equals(candidate.attachmentTargetId())) {
        return candidate;
      }
    }
    return null;
  }

  private HudModuleSettings requireSettings(String id) {
    HudModuleSettings settings = settingsById.get(id);
    if (settings == null) {
      throw new IllegalArgumentException("Unknown HUD module id: " + id);
    }
    return settings;
  }

  private void refreshFrameSnapshot() {
    frameSnapshot = new HudFrameSnapshot(moduleView);
  }

  private record PendingAnchorPromotion(
      String childId, int promotedX, int promotedY, HudAttachmentSide side, int offset) {}
}
