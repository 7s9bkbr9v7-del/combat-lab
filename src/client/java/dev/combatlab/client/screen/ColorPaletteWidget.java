package dev.combatlab.client.screen;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class ColorPaletteWidget extends AbstractWidget {
  private static final int BORDER_COLOR = 0xFFFFFFFF;
  private static final int CURSOR_COLOR = 0xFF000000;

  private final int squareSize;
  private final int hueBarWidth;
  private final int gap;
  private final ColorSetting setting;
  private boolean draggingSquare;
  private boolean draggingHue;

  ColorPaletteWidget(int x, int y, int squareSize, int hueBarWidth, int gap, ColorSetting setting) {
    super(x, y, squareSize + gap + hueBarWidth, squareSize, Component.empty());
    this.squareSize = squareSize;
    this.hueBarWidth = hueBarWidth;
    this.gap = gap;
    this.setting = setting;
  }

  @Override
  protected void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    HsvColor hsv = HsvColor.fromRgb(setting.colorRgb());
    renderSquare(graphics, hsv.hue());
    renderHueBar(graphics);
    renderCursors(graphics, hsv);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    draggingSquare = squareContains(event.x(), event.y());
    draggingHue = hueContains(event.x(), event.y());
    if (draggingSquare) {
      updateSquare(event.x(), event.y());
      return true;
    }
    if (draggingHue) {
      updateHue(event.y());
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
    if (draggingSquare) {
      updateSquare(event.x(), event.y());
      return true;
    }
    if (draggingHue) {
      updateHue(event.y());
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    draggingSquare = false;
    draggingHue = false;
    return super.mouseReleased(event);
  }

  @Override
  public void visitWidgets(Consumer<AbstractWidget> consumer) {
    consumer.accept(this);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
    defaultButtonNarrationText(output);
  }

  private void renderSquare(GuiGraphicsExtractor graphics, double hue) {
    for (int x = 0; x < squareSize; x++) {
      double saturation = x / (double) Math.max(1, squareSize - 1);
      int topColor = 0xFF000000 | HsvColor.toRgb(hue, saturation, 1.0D);
      graphics.fillGradient(
          getX() + x, getY(), getX() + x + 1, getY() + squareSize, topColor, 0xFF000000);
    }
    graphics.outline(getX(), getY(), squareSize, squareSize, BORDER_COLOR);
  }

  private void renderHueBar(GuiGraphicsExtractor graphics) {
    int hueX = hueX();
    for (int y = 0; y < squareSize; y++) {
      double hue = y / (double) Math.max(1, squareSize - 1);
      graphics.fill(
          hueX,
          getY() + y,
          hueX + hueBarWidth,
          getY() + y + 1,
          0xFF000000 | HsvColor.toRgb(hue, 1.0D, 1.0D));
    }
    graphics.outline(hueX, getY(), hueBarWidth, squareSize, BORDER_COLOR);
  }

  private void renderCursors(GuiGraphicsExtractor graphics, HsvColor hsv) {
    int squareX = getX() + (int) Math.round(hsv.saturation() * (squareSize - 1));
    int squareY = getY() + (int) Math.round((1.0D - hsv.value()) * (squareSize - 1));
    graphics.outline(squareX - 2, squareY - 2, 5, 5, BORDER_COLOR);
    graphics.outline(squareX - 1, squareY - 1, 3, 3, CURSOR_COLOR);

    int hueY = getY() + (int) Math.round(hsv.hue() * (squareSize - 1));
    graphics.outline(hueX() - 2, hueY - 1, hueBarWidth + 4, 3, BORDER_COLOR);
    graphics.fill(hueX() - 1, hueY, hueX() + hueBarWidth + 1, hueY + 1, CURSOR_COLOR);
  }

  private void updateSquare(double mouseX, double mouseY) {
    HsvColor hsv = HsvColor.fromRgb(setting.colorRgb());
    double saturation = clamp((mouseX - getX()) / Math.max(1.0D, squareSize - 1.0D));
    double value = 1.0D - clamp((mouseY - getY()) / Math.max(1.0D, squareSize - 1.0D));
    setting.updateColorRgb(HsvColor.toRgb(hsv.hue(), saturation, value));
  }

  private void updateHue(double mouseY) {
    HsvColor hsv = HsvColor.fromRgb(setting.colorRgb());
    double hue = clamp((mouseY - getY()) / Math.max(1.0D, squareSize - 1.0D));
    setting.updateColorRgb(HsvColor.toRgb(hue, hsv.saturation(), hsv.value()));
  }

  private boolean squareContains(double mouseX, double mouseY) {
    return mouseX >= getX()
        && mouseX < getX() + squareSize
        && mouseY >= getY()
        && mouseY < getY() + squareSize;
  }

  private boolean hueContains(double mouseX, double mouseY) {
    return mouseX >= hueX()
        && mouseX < hueX() + hueBarWidth
        && mouseY >= getY()
        && mouseY < getY() + squareSize;
  }

  private int hueX() {
    return getX() + squareSize + gap;
  }

  private static double clamp(double value) {
    return Math.clamp(value, 0.0D, 1.0D);
  }
}
