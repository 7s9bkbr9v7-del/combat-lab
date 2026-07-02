package dev.combatlab.client.screen;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class ColorSwatchOptionWidget extends AbstractWidget {
  private static final int SWATCH_SIZE = 20;
  private static final int BORDER_COLOR = 0xFFFFFFFF;
  private static final int BACKGROUND_COLOR = 0xAA000000;
  private static final int TEXT_COLOR = 0xFFFFFFFF;

  private final Component label;
  private final IntSupplier colorSupplier;
  private final Runnable onPress;

  ColorSwatchOptionWidget(Component label, IntSupplier colorSupplier, Runnable onPress) {
    super(0, 0, 150, 20, label);
    this.label = label;
    this.colorSupplier = colorSupplier;
    this.onPress = onPress;
  }

  @Override
  protected void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    int swatchX = getRight() - SWATCH_SIZE;
    int swatchY = getY();
    graphics.text(
        Minecraft.getInstance().font,
        label,
        getX(),
        getY() + (getHeight() - Minecraft.getInstance().font.lineHeight) / 2,
        TEXT_COLOR,
        true);
    graphics.fill(swatchX, swatchY, getRight(), getBottom(), BACKGROUND_COLOR);
    graphics.fill(swatchX + 2, swatchY + 2, getRight() - 2, getBottom() - 2, color());
    graphics.outline(swatchX, swatchY, SWATCH_SIZE, getHeight(), BORDER_COLOR);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    if (!isMouseOver(event.x(), event.y())) {
      return false;
    }
    onPress.run();
    return true;
  }

  @Override
  public void visitWidgets(Consumer<AbstractWidget> consumer) {
    consumer.accept(this);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
    defaultButtonNarrationText(output);
  }

  private int color() {
    return 0xFF000000 | colorSupplier.getAsInt();
  }
}
