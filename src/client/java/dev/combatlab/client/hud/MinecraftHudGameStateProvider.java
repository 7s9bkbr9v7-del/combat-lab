package dev.combatlab.client.hud;

import dev.combatlab.client.input.CpsTracker;
import dev.combatlab.client.model.CombatState;
import dev.combatlab.client.state.ArmorSlot;
import dev.combatlab.client.state.CombatSnapshot;
import dev.combatlab.client.state.DirectionState;
import dev.combatlab.client.state.InputState;
import dev.combatlab.client.state.MovementState;
import dev.combatlab.client.state.PlayerArmor;
import dev.combatlab.client.state.PlayerEffectTimer;
import dev.combatlab.client.state.PlayerEffects;
import dev.combatlab.client.state.PlayerState;
import dev.combatlab.client.state.TargetState;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class MinecraftHudGameStateProvider {
  public HudGameState snapshot(
      Minecraft client, CombatState combatState, CpsTracker cpsTracker, long nowNanos) {
    LocalPlayer player = client.player;
    PlayerState playerState = playerState(client, player);
    InputState inputState = inputState(client, cpsTracker, nowNanos);
    CombatSnapshot combatSnapshot = combatSnapshot(combatState);
    int fps = client.getFps();
    return HudGameState.from(fps, playerState, inputState, combatSnapshot);
  }

  private static PlayerState playerState(Minecraft client, LocalPlayer player) {
    if (player == null) {
      return PlayerState.absent();
    }
    return new PlayerState(
        true, direction(player), movement(client, player), armor(player), effects(player));
  }

  private static DirectionState direction(LocalPlayer player) {
    if (player == null) {
      return DirectionState.absent();
    }
    return DirectionState.of(180.0F - player.getYRot());
  }

  private static MovementState movement(Minecraft client, LocalPlayer player) {
    if (player == null) {
      return MovementState.inactive();
    }
    return new MovementState(
        player.isCrouching(),
        player.isSprinting(),
        client.options.toggleSprint().get() && client.options.keySprint.isDown());
  }

  private static InputState inputState(Minecraft client, CpsTracker cpsTracker, long nowNanos) {
    return new InputState(
        cpsTracker.currentCps(nowNanos),
        client.options.keyUp.isDown(),
        client.options.keyLeft.isDown(),
        client.options.keyDown.isDown(),
        client.options.keyRight.isDown(),
        client.options.keyJump.isDown(),
        client.options.keyShift.isDown(),
        client.options.keySprint.isDown(),
        client.options.keyAttack.isDown(),
        client.options.keyUse.isDown());
  }

  private static PlayerArmor armor(LocalPlayer player) {
    if (player == null) {
      return PlayerArmor.empty();
    }

    EnumMap<ArmorSlot, ItemStack> stacks = new EnumMap<>(ArmorSlot.class);
    stacks.put(ArmorSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
    stacks.put(ArmorSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
    stacks.put(ArmorSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
    stacks.put(ArmorSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
    return PlayerArmor.of(stacks);
  }

  private static PlayerEffects effects(LocalPlayer player) {
    if (player == null) {
      return PlayerEffects.empty();
    }

    List<PlayerEffectTimer> active =
        player.getActiveEffects().stream()
            .filter(MobEffectInstance::showIcon)
            .map(MinecraftHudGameStateProvider::effectTimer)
            .sorted(
                Comparator.comparing(PlayerEffectTimer::ambient)
                    .thenComparing(PlayerEffectTimer::displayName))
            .toList();
    return new PlayerEffects(active);
  }

  private static PlayerEffectTimer effectTimer(MobEffectInstance effect) {
    MobEffect type = effect.getEffect().value();
    Identifier effectId =
        effect
            .getEffect()
            .unwrapKey()
            .map(ResourceKey::identifier)
            .orElse(Identifier.withDefaultNamespace(type.getDescriptionId()));
    return new PlayerEffectTimer(
        effectId.toString(),
        type.getDisplayName().getString(),
        effect.getAmplifier(),
        effect.getDuration(),
        effect.isInfiniteDuration(),
        effect.isAmbient(),
        type.getColor(),
        Identifier.fromNamespaceAndPath(
            effectId.getNamespace(), "textures/mob_effect/" + effectId.getPath() + ".png"));
  }

  private static CombatSnapshot combatSnapshot(CombatState combatState) {
    TargetState target =
        combatState.targetId() == null
            ? TargetState.none()
            : new TargetState(
                combatState.targetId(), combatState.targetName(), combatState.targetDistance());
    return new CombatSnapshot(combatState.attackStrength(), combatState.ping(), target);
  }
}
