package dev.combatlab.client.screen;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ColorPickerScreen extends Screen {
  private static final int PICKER_SIZE = 128;
  private static final int HUE_BAR_WIDTH = 14;
  private static final int PICKER_GAP = 8;
  private static final int SLIDER_WIDTH = 220;
  private static final int SLIDER_HEIGHT = 20;
  private static final int ROW_GAP = 6;
  private static final int PREVIEW_SIZE = 20;
  private static final int PREVIEW_GAP = 10;
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final int PICKER_BOTTOM_GAP = 54;

  private final Screen parent;
  private final ColorSetting setting;
  private int pickerX;
  private int pickerY;
  private int pickerTotalWidth;

  public ColorPickerScreen(Screen parent, Component title, ColorSetting setting) {
    super(title);
    this.parent = parent;
    this.setting = setting;
  }

  @Override
  protected void init() {
    pickerTotalWidth = PICKER_SIZE + PICKER_GAP + HUE_BAR_WIDTH;
    pickerX = width / 2 - pickerTotalWidth / 2;
    pickerY = Math.max(42, height / 2 - 110);
    addRenderableWidget(
        new ColorPaletteWidget(pickerX, pickerY, PICKER_SIZE, HUE_BAR_WIDTH, PICKER_GAP, setting));

    int sliderX = width / 2 - SLIDER_WIDTH / 2;
    int sliderY = pickerY + PICKER_SIZE + PICKER_BOTTOM_GAP;
    for (ColorChannelSlider slider : sliders(sliderX, sliderY)) {
      addRenderableWidget(slider);
      sliderY += SLIDER_HEIGHT + ROW_GAP;
    }
    addRenderableWidget(
        Button.builder(CommonComponents.GUI_DONE, ignoredButton -> onClose())
            .bounds(width / 2 - 100, height - 35, 200, 20)
            .build());
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    graphics.centeredText(font, title, width / 2, 18, TEXT_COLOR);
    renderCurrentColor(graphics);
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public void onClose() {
    ScreenNavigator.open(minecraft, parent);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  private List<ColorChannelSlider> sliders(int x, int y) {
    return List.of(
        new ColorChannelSlider(
            x, y, SLIDER_WIDTH, SLIDER_HEIGHT, "options.combatlab.color_picker.red", 16, setting),
        new ColorChannelSlider(
            x,
            y + SLIDER_HEIGHT + ROW_GAP,
            SLIDER_WIDTH,
            SLIDER_HEIGHT,
            "options.combatlab.color_picker.green",
            8,
            setting),
        new ColorChannelSlider(
            x,
            y + (SLIDER_HEIGHT + ROW_GAP) * 2,
            SLIDER_WIDTH,
            SLIDER_HEIGHT,
            "options.combatlab.color_picker.blue",
            0,
            setting));
  }

  private void renderCurrentColor(GuiGraphicsExtractor graphics) {
    String hex = "#" + String.format("%06X", setting.colorRgb() & 0xFFFFFF);
    int groupWidth = PREVIEW_SIZE + PREVIEW_GAP + Minecraft.getInstance().font.width(hex);
    int previewX = pickerX + pickerTotalWidth / 2 - groupWidth / 2;
    int previewY = pickerY + PICKER_SIZE + 16;
    int color = 0xFF000000 | setting.colorRgb();
    graphics.fill(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, color);
    graphics.outline(previewX, previewY, PREVIEW_SIZE, PREVIEW_SIZE, TEXT_COLOR);
    graphics.text(
        Minecraft.getInstance().font,
        hex,
        previewX + PREVIEW_SIZE + PREVIEW_GAP,
        previewY + (PREVIEW_SIZE - Minecraft.getInstance().font.lineHeight) / 2,
        TEXT_COLOR,
        true);
  }
}
