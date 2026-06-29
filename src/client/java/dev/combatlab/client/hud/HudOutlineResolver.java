package dev.combatlab.client.hud;

import java.util.ArrayList;
import java.util.List;

public final class HudOutlineResolver {
  private HudOutlineResolver() {}

  public static HudOutlineSegments visibleSegments(HudRectangle module, List<HudRectangle> others) {
    List<HudOutlineSegment> top = horizontalSide(module);
    List<HudOutlineSegment> right = verticalSide(module);
    List<HudOutlineSegment> bottom = horizontalSide(module);
    List<HudOutlineSegment> left = verticalSide(module);

    int topY = module.y() - 1;
    int bottomY = module.bottom();
    int leftX = module.x() - 1;
    int rightX = module.right();

    for (HudRectangle other : others) {
      if (other == module) {
        continue;
      }
      if (containsY(other, topY)) {
        top = subtract(top, other.x(), other.right());
      }
      if (containsY(other, bottomY)) {
        bottom = subtract(bottom, other.x(), other.right());
      }
      if (containsX(other, leftX)) {
        left = subtract(left, other.y(), other.bottom());
      }
      if (containsX(other, rightX)) {
        right = subtract(right, other.y(), other.bottom());
      }
    }

    return new HudOutlineSegments(top, right, bottom, left);
  }

  private static boolean containsX(HudRectangle rectangle, int x) {
    return x >= rectangle.x() && x < rectangle.right();
  }

  private static boolean containsY(HudRectangle rectangle, int y) {
    return y >= rectangle.y() && y < rectangle.bottom();
  }

  private static List<HudOutlineSegment> horizontalSide(HudRectangle rectangle) {
    return List.of(new HudOutlineSegment(rectangle.x() - 1, rectangle.right() + 1));
  }

  private static List<HudOutlineSegment> verticalSide(HudRectangle rectangle) {
    return List.of(new HudOutlineSegment(rectangle.y() - 1, rectangle.bottom() + 1));
  }

  private static List<HudOutlineSegment> subtract(
      List<HudOutlineSegment> source, int removedStart, int removedEnd) {
    List<HudOutlineSegment> result = new ArrayList<>(source.size() + 1);
    for (HudOutlineSegment segment : source) {
      if (removedEnd <= segment.start() || removedStart >= segment.end()) {
        result.add(segment);
        continue;
      }
      if (removedStart > segment.start()) {
        result.add(new HudOutlineSegment(segment.start(), removedStart));
      }
      if (removedEnd < segment.end()) {
        result.add(new HudOutlineSegment(removedEnd, segment.end()));
      }
    }
    return List.copyOf(result);
  }
}
