package dev.combatlab.client.hud;

import java.util.Objects;

public final class HudRectangle {
  private int x;
  private int y;
  private int width;
  private int height;

  public HudRectangle(int x, int y, int width, int height) {
    set(x, y, width, height);
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  void set(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  void set(HudRectangle other) {
    set(other.x, other.y, other.width, other.height);
  }

  public int right() {
    return x + width;
  }

  public int bottom() {
    return y + height;
  }

  public int centerX() {
    return x + width / 2;
  }

  public int centerY() {
    return y + height / 2;
  }

  public boolean contains(double pointX, double pointY) {
    return pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
  }

  public boolean intersects(HudRectangle other) {
    return right() > other.x && other.right() > x && bottom() > other.y && other.bottom() > y;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof HudRectangle rectangle)) {
      return false;
    }
    return x == rectangle.x
        && y == rectangle.y
        && width == rectangle.width
        && height == rectangle.height;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, width, height);
  }

  @Override
  public String toString() {
    return "HudRectangle[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
  }
}
