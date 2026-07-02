package dev.combatlab.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

final class DelayedConfirmActionButton {
  private static final long CONFIRM_DELAY_NANOS = 3_000_000_000L;

  private final Component initialMessage;
  private final Component confirmMessage;
  private final Runnable action;
  private final Button button;
  private long armedAtNanos = -1L;

  DelayedConfirmActionButton(
      Component initialMessage, Component confirmMessage, Component tooltip, Runnable action) {
    this.initialMessage = initialMessage;
    this.confirmMessage = confirmMessage;
    this.action = action;
    this.button =
        Button.builder(initialMessage, ignoredButton -> press()).bounds(0, 0, 150, 20).build();
    this.button.setTooltip(Tooltip.create(tooltip));
  }

  Button button() {
    return button;
  }

  void reset() {
    armedAtNanos = -1L;
    button.setMessage(initialMessage);
  }

  void update() {
    if (armedAtNanos < 0L) {
      return;
    }
    button.setMessage(message(System.nanoTime()));
  }

  private void press() {
    long nowNanos = System.nanoTime();
    if (armedAtNanos < 0L) {
      armedAtNanos = nowNanos;
      button.setMessage(message(nowNanos));
      return;
    }
    if (!ready(nowNanos)) {
      button.setMessage(message(nowNanos));
      return;
    }
    action.run();
  }

  private Component message(long nowNanos) {
    if (ready(nowNanos)) {
      return confirmMessage.copy().withStyle(ChatFormatting.RED);
    }
    return Component.literal(confirmMessage.getString() + " (" + secondsRemaining(nowNanos) + ")")
        .withStyle(ChatFormatting.GRAY);
  }

  private int secondsRemaining(long nowNanos) {
    long remaining = Math.max(0L, CONFIRM_DELAY_NANOS - (nowNanos - armedAtNanos));
    return Math.max(1, (int) Math.ceil(remaining / 1_000_000_000.0D));
  }

  private boolean ready(long nowNanos) {
    return armedAtNanos >= 0L && nowNanos - armedAtNanos >= CONFIRM_DELAY_NANOS;
  }
}
