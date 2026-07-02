package dev.combatlab.client.screen;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class HudModuleOptionsRowWidget extends AbstractWidget {
  private static final int OPTIONS_BUTTON_WIDTH = 20;
  private static final int GAP = 4;

  private final AbstractWidget enabledButton;
  private final AbstractWidget optionsButton;
  private final List<AbstractWidget> children;

  HudModuleOptionsRowWidget(AbstractWidget enabledButton, AbstractWidget optionsButton) {
    super(0, 0, 150, 20, Component.empty());
    this.enabledButton = enabledButton;
    this.optionsButton = optionsButton;
    this.children = List.of(enabledButton, optionsButton);
    layoutChildren();
  }

  @Override
  public void setX(int x) {
    super.setX(x);
    layoutChildren();
  }

  @Override
  public void setY(int y) {
    super.setY(y);
    layoutChildren();
  }

  @Override
  public void setWidth(int width) {
    super.setWidth(width);
    layoutChildren();
  }

  @Override
  public void setHeight(int height) {
    super.setHeight(height);
    layoutChildren();
  }

  @Override
  protected void extractWidgetRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    for (AbstractWidget child : children) {
      child.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    for (AbstractWidget child : children) {
      if (child.mouseClicked(event, doubleClick)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent event) {
    for (AbstractWidget child : children) {
      if (child.mouseReleased(event)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void visitWidgets(Consumer<AbstractWidget> consumer) {
    children.forEach(consumer);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
    enabledButton.updateNarration(output);
    optionsButton.updateNarration(output);
  }

  private void layoutChildren() {
    int optionsWidth = Math.min(OPTIONS_BUTTON_WIDTH, width);
    int enabledWidth = Math.max(0, width - optionsWidth - GAP);
    enabledButton.setSize(enabledWidth, height);
    enabledButton.setPosition(getX(), getY());
    optionsButton.setSize(optionsWidth, height);
    optionsButton.setPosition(getX() + enabledWidth + GAP, getY());
  }
}
