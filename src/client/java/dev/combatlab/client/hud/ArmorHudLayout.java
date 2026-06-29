package dev.combatlab.client.hud;

import dev.combatlab.client.state.ArmorSlot;
import java.util.List;

enum ArmorHudLayout {
  VERTICAL(1, 4, List.of(ArmorSlot.HEAD, ArmorSlot.CHEST, ArmorSlot.LEGS, ArmorSlot.FEET)),
  HORIZONTAL(4, 1, List.of(ArmorSlot.HEAD, ArmorSlot.CHEST, ArmorSlot.LEGS, ArmorSlot.FEET)),
  GRID(2, 2, List.of(ArmorSlot.HEAD, ArmorSlot.LEGS, ArmorSlot.CHEST, ArmorSlot.FEET));

  private final int columns;
  private final int rows;
  private final List<ArmorSlot> slots;

  ArmorHudLayout(int columns, int rows, List<ArmorSlot> slots) {
    this.columns = columns;
    this.rows = rows;
    this.slots = slots;
  }

  static ArmorHudLayout resolve(double normalizedX, double normalizedY) {
    return resolve(normalizedX, normalizedY, GRID);
  }

  static ArmorHudLayout resolve(
      double normalizedX, double normalizedY, ArmorHudLayout floatingLayout) {
    HudEdgeContact edgeContact = HudEdgeContact.fromNormalizedPosition(normalizedX, normalizedY);
    if (edgeContact.corner()) {
      return GRID;
    }
    if (edgeContact.sideEdge()) {
      return VERTICAL;
    }
    if (edgeContact.topOrBottomEdge()) {
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

  List<ArmorSlot> slots() {
    return slots;
  }
}
