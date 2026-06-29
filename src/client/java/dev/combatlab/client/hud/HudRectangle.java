package dev.combatlab.client.hud;

public record HudRectangle(int x, int y, int width, int height) {
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
}
