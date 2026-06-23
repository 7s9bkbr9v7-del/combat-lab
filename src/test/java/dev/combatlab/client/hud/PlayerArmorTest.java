package dev.combatlab.client.hud;

import dev.combatlab.client.state.ArmorSlot;
import dev.combatlab.client.state.PlayerArmor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerArmorTest {
	@Test
	void emptyArmorReturnsEmptyStacks() {
		PlayerArmor armor = PlayerArmor.empty();

		assertTrue(armor.stack(ArmorSlot.HEAD).isEmpty());
		assertTrue(armor.stack(ArmorSlot.CHEST).isEmpty());
		assertTrue(armor.stack(ArmorSlot.LEGS).isEmpty());
		assertTrue(armor.stack(ArmorSlot.FEET).isEmpty());
	}
}
