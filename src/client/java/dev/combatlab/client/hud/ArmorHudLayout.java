package dev.combatlab.client.hud;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

enum ArmorHudLayout {
	VERTICAL(1, 4, List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET
	)),
	HORIZONTAL(4, 1, List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET
	)),
	GRID(2, 2, List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.LEGS,
			EquipmentSlot.CHEST,
			EquipmentSlot.FEET
	));

	private static final double EDGE_EPSILON = 1.0E-6;
	private final int columns;
	private final int rows;
	private final List<EquipmentSlot> slots;

	ArmorHudLayout(int columns, int rows, List<EquipmentSlot> slots) {
		this.columns = columns;
		this.rows = rows;
		this.slots = slots;
	}

	static ArmorHudLayout resolve(double normalizedX, double normalizedY) {
		return resolve(normalizedX, normalizedY, GRID);
	}

	static ArmorHudLayout resolve(
			double normalizedX,
			double normalizedY,
			ArmorHudLayout floatingLayout
	) {
		boolean touchesHorizontalEdge = isEdge(normalizedX);
		boolean touchesVerticalEdge = isEdge(normalizedY);
		if (touchesHorizontalEdge && touchesVerticalEdge) {
			return GRID;
		}
		if (touchesHorizontalEdge) {
			return VERTICAL;
		}
		if (touchesVerticalEdge) {
			return HORIZONTAL;
		}
		return floatingLayout;
	}

	static ArmorHudLayout fromStored(String storedLayout) {
		if (storedLayout == null) {
			return GRID;
		}
		try {
			return valueOf(storedLayout);
		} catch (IllegalArgumentException ignored) {
			return GRID;
		}
	}

	int columns() {
		return columns;
	}

	int rows() {
		return rows;
	}

	List<EquipmentSlot> slots() {
		return slots;
	}

	private static boolean isEdge(double normalizedPosition) {
		return normalizedPosition <= EDGE_EPSILON || normalizedPosition >= 1.0 - EDGE_EPSILON;
	}
}
