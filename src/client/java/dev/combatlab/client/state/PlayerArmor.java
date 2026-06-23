package dev.combatlab.client.state;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumMap;
import java.util.Map;

public final class PlayerArmor {
	private final EnumMap<ArmorSlot, ItemStack> stacks;

	private PlayerArmor(EnumMap<ArmorSlot, ItemStack> stacks) {
		this.stacks = stacks;
	}

	public static PlayerArmor empty() {
		return of(Map.of());
	}

	public static PlayerArmor editorPreview(PlayerArmor liveArmor) {
		EnumMap<ArmorSlot, ItemStack> previewStacks = new EnumMap<>(ArmorSlot.class);
		for (ArmorSlot slot : ArmorSlot.values()) {
			ItemStack liveStack = liveArmor.stack(slot);
			previewStacks.put(slot, liveStack.isEmpty() ? previewStack(slot) : liveStack);
		}
		return new PlayerArmor(previewStacks);
	}

	public static PlayerArmor of(Map<ArmorSlot, ItemStack> stacks) {
		EnumMap<ArmorSlot, ItemStack> copy = new EnumMap<>(ArmorSlot.class);
		for (ArmorSlot slot : ArmorSlot.values()) {
			copy.put(slot, stacks.getOrDefault(slot, ItemStack.EMPTY));
		}
		return new PlayerArmor(copy);
	}

	public ItemStack stack(ArmorSlot slot) {
		return stacks.getOrDefault(slot, ItemStack.EMPTY);
	}

	private static ItemStack previewStack(ArmorSlot slot) {
		return switch (slot) {
			case HEAD -> Items.IRON_HELMET.getDefaultInstance();
			case CHEST -> Items.IRON_CHESTPLATE.getDefaultInstance();
			case LEGS -> Items.IRON_LEGGINGS.getDefaultInstance();
			case FEET -> Items.IRON_BOOTS.getDefaultInstance();
		};
	}
}
