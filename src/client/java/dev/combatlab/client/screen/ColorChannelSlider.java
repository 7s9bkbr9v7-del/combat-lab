package dev.combatlab.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

final class ColorChannelSlider extends AbstractSliderButton {
  private final String key;
  private final int shift;
  private final ColorSetting setting;

  ColorChannelSlider(
      int x, int y, int width, int height, String key, int shift, ColorSetting setting) {
    super(x, y, width, height, Component.empty(), channel(setting.colorRgb(), shift) / 255.0D);
    this.key = key;
    this.shift = shift;
    this.setting = setting;
    updateMessage();
  }

  @Override
  public void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    value = channel(setting.colorRgb(), shift) / 255.0D;
    updateMessage();
    super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  protected void updateMessage() {
    setMessage(
        Component.literal(
            Component.translatable(key).getString() + ": " + channel(setting.colorRgb(), shift)));
  }

  @Override
  protected void applyValue() {
    int color = setting.colorRgb();
    int masked = color & ~(0xFF << shift);
    int channel = (int) Math.round(value * 255.0D);
    setting.updateColorRgb(masked | (channel << shift));
  }

  private static int channel(int color, int shift) {
    return (color >> shift) & 0xFF;
  }
}
