package dev.combatlab.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MinecraftCapabilities {
  private MinecraftCapabilities() {}

  public static CompatMethod guiSetScreen() {
    return CompatMethod.find(MinecraftClasses.LEGACY_GUI, "setScreen", Screen.class);
  }

  public static CompatMethod minecraftSetScreen() {
    return CompatMethod.find(Minecraft.class, "setScreen", Screen.class);
  }
}
