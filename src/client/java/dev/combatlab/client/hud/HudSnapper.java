package dev.combatlab.client.hud;

import java.util.List;

public final class HudSnapper {
	private HudSnapper() {
	}

	public static HudPosition snap(HudRectangle moving, List<HudRectangle> others, int threshold) {
		return snap(moving, others, threshold, -1, -1);
	}

	public static HudPosition snap(
			HudRectangle moving,
			List<HudRectangle> others,
			int threshold,
			int screenWidth,
			int screenHeight
	) {
		return snapWithGuides(moving, others, threshold, screenWidth, screenHeight).position();
	}

	public static HudSnapResult snapWithGuides(
			HudRectangle moving,
			List<HudRectangle> others,
			int threshold,
			int screenWidth,
			int screenHeight
	) {
		AxisSnapResult nearby = snapNearby(moving, others, threshold, screenWidth, screenHeight);
		HudRectangle nearbyRectangle = new HudRectangle(
				nearby.position().x(),
				nearby.position().y(),
				moving.width(),
				moving.height()
		);
		HudPosition position = snapAligned(nearbyRectangle, others, threshold, !nearby.snappedX(), !nearby.snappedY());
		HudRectangle snapped = new HudRectangle(position.x(), position.y(), moving.width(), moving.height());
		return new HudSnapResult(
				position,
				guidesForAlignment(moving, snapped, others, threshold, screenWidth, screenHeight)
		);
	}

	public static HudPosition snapToGrid(HudRectangle moving, int gridSize, int screenWidth, int screenHeight) {
		if (gridSize <= 1) {
			return new HudPosition(moving.x(), moving.y());
		}

		int maxX = screenWidth >= moving.width() ? screenWidth - moving.width() : moving.x();
		int maxY = screenHeight >= moving.height() ? screenHeight - moving.height() : moving.y();
		return new HudPosition(
				Math.clamp(nearestGridLine(moving.x(), gridSize), 0, Math.max(0, maxX)),
				Math.clamp(nearestGridLine(moving.y(), gridSize), 0, Math.max(0, maxY))
		);
	}

	public static HudPosition snapToEdges(
			HudRectangle moving,
			List<HudRectangle> others,
			int threshold,
			int screenWidth,
			int screenHeight
	) {
		AxisSnap xSnap = AxisSnap.none(moving.x(), threshold);
		if (screenWidth >= moving.width()) {
			xSnap = xSnap.nearest(moving.x(), 0);
			xSnap = xSnap.nearest(moving.x(), screenWidth - moving.width());
		}
		for (HudRectangle other : others) {
			if (rangesNear(moving.y(), moving.bottom(), other.y(), other.bottom(), threshold)) {
				xSnap = xSnap.nearest(moving.x(), xGeometry(moving, other).edgeAlignedPositions());
			}
		}

		HudRectangle horizontal = new HudRectangle(xSnap.value(), moving.y(), moving.width(), moving.height());
		AxisSnap ySnap = AxisSnap.none(moving.y(), threshold);
		if (screenHeight >= moving.height()) {
			ySnap = ySnap.nearest(moving.y(), 0);
			ySnap = ySnap.nearest(moving.y(), screenHeight - moving.height());
		}
		for (HudRectangle other : others) {
			if (rangesNear(horizontal.x(), horizontal.right(), other.x(), other.right(), threshold)) {
				ySnap = ySnap.nearest(moving.y(), yGeometry(moving, other).edgeAlignedPositions());
			}
		}

		return new HudPosition(
				Math.clamp(xSnap.value(), 0, Math.max(0, screenWidth - moving.width())),
				Math.clamp(ySnap.value(), 0, Math.max(0, screenHeight - moving.height()))
		);
	}

	public static List<HudSnapGuide> guidesForAlignment(
			HudRectangle original,
			HudRectangle snapped,
			List<HudRectangle> others,
			int threshold,
			int screenWidth,
			int screenHeight
	) {
		List<HudSnapGuide> guides = new java.util.ArrayList<>(2);
		addScreenCenterGuides(guides, original, snapped, threshold, screenWidth, screenHeight);
		addDistantModuleEdgeGuides(guides, original, snapped, others, threshold);
		return List.copyOf(guides);
	}

	static AxisSnapResult snapNearby(HudRectangle moving, List<HudRectangle> others, int threshold) {
		return snapNearby(moving, others, threshold, -1, -1);
	}

	private static AxisSnapResult snapNearby(
			HudRectangle moving,
			List<HudRectangle> others,
			int threshold,
			int screenWidth,
			int screenHeight
	) {
		AxisSnap xSnap = AxisSnap.none(moving.x(), threshold);
		if (screenWidth >= moving.width()) {
			xSnap = xSnap.nearest(moving.x(), 0);
			xSnap = xSnap.nearest(moving.x(), screenWidth - moving.width());
			xSnap = xSnap.nearest(moving.x(), (screenWidth - moving.width()) / 2);
		}
		for (HudRectangle other : others) {
			if (rangesNear(moving.y(), moving.bottom(), other.y(), other.bottom(), threshold)) {
				xSnap = xSnap.nearest(moving.x(), xGeometry(moving, other).edgeAlignedPositions());
			}
		}

		HudRectangle horizontal = new HudRectangle(xSnap.value(), moving.y(), moving.width(), moving.height());
		AxisSnap ySnap = AxisSnap.none(moving.y(), threshold);
		if (screenHeight >= moving.height()) {
			ySnap = ySnap.nearest(moving.y(), 0);
			ySnap = ySnap.nearest(moving.y(), screenHeight - moving.height());
			ySnap = ySnap.nearest(moving.y(), (screenHeight - moving.height()) / 2);
		}
		for (HudRectangle other : others) {
			if (rangesNear(horizontal.x(), horizontal.right(), other.x(), other.right(), threshold)) {
				ySnap = ySnap.nearest(moving.y(), yGeometry(moving, other).edgeAlignedPositions());
			}
		}

		return new AxisSnapResult(new HudPosition(xSnap.value(), ySnap.value()), xSnap.snapped(), ySnap.snapped());
	}

	static HudPosition snapAligned(
			HudRectangle moving,
			List<HudRectangle> others,
			int threshold,
			boolean allowXAlignment,
			boolean allowYAlignment
	) {
		AxisSnap xSnap = AxisSnap.none(moving.x(), threshold);
		AxisSnap ySnap = AxisSnap.none(moving.y(), threshold);

		for (HudRectangle other : others) {
			if (allowXAlignment) {
				xSnap = xSnap.nearest(moving.x(), xGeometry(moving, other).edgeAlignedPositions());
				xSnap = xSnap.nearest(moving.x(), other.x() + (other.width() - moving.width()) / 2);
			}
			if (allowYAlignment) {
				ySnap = ySnap.nearest(moving.y(), yGeometry(moving, other).edgeAlignedPositions());
				ySnap = ySnap.nearest(moving.y(), other.y() + (other.height() - moving.height()) / 2);
			}
		}

		return new HudPosition(xSnap.value(), ySnap.value());
	}

	private static boolean rangesNear(int firstStart, int firstEnd, int secondStart, int secondEnd, int threshold) {
		return firstEnd + threshold >= secondStart && secondEnd + threshold >= firstStart;
	}

	private static int nearestGridLine(int value, int gridSize) {
		return Math.round((float) value / gridSize) * gridSize;
	}

	private static void addScreenCenterGuides(
			List<HudSnapGuide> guides,
			HudRectangle original,
			HudRectangle snapped,
			int threshold,
			int screenWidth,
			int screenHeight
	) {
		if (screenWidth >= snapped.width()) {
			int centeredX = (screenWidth - snapped.width()) / 2;
			if (snapped.x() == centeredX && Math.abs(original.x() - centeredX) <= threshold) {
				guides.add(new HudSnapGuide(HudSnapGuide.Axis.VERTICAL, screenWidth / 2));
			}
		}
		if (screenHeight >= snapped.height()) {
			int centeredY = (screenHeight - snapped.height()) / 2;
			if (snapped.y() == centeredY && Math.abs(original.y() - centeredY) <= threshold) {
				guides.add(new HudSnapGuide(HudSnapGuide.Axis.HORIZONTAL, screenHeight / 2));
			}
		}
	}

	private static void addDistantModuleEdgeGuides(
			List<HudSnapGuide> guides,
			HudRectangle original,
			HudRectangle snapped,
			List<HudRectangle> others,
			int threshold
	) {
		for (HudRectangle other : others) {
			if (!rangesNear(snapped.y(), snapped.bottom(), other.y(), other.bottom(), threshold)) {
				addEdgeGuides(guides, HudSnapGuide.Axis.VERTICAL, xGeometry(snapped, other), original.x(), snapped.x(), threshold);
			}
			if (!rangesNear(snapped.x(), snapped.right(), other.x(), other.right(), threshold)) {
				addEdgeGuides(guides, HudSnapGuide.Axis.HORIZONTAL, yGeometry(snapped, other), original.y(), snapped.y(), threshold);
			}
		}
	}

	private static void addEdgeGuides(
			List<HudSnapGuide> guides,
			HudSnapGuide.Axis axis,
			AxisGeometry geometry,
			int originalPosition,
			int snappedPosition,
			int threshold
	) {
		for (EdgeSnapTarget target : geometry.edgeTargets()) {
			if (snappedPosition == target.position() && Math.abs(originalPosition - target.position()) <= threshold) {
				addGuide(guides, new HudSnapGuide(axis, target.guideCoordinate()));
			}
		}
	}

	private static void addGuide(List<HudSnapGuide> guides, HudSnapGuide guide) {
		if (!guides.contains(guide)) {
			guides.add(guide);
		}
	}

	private static AxisGeometry xGeometry(HudRectangle moving, HudRectangle other) {
		return new AxisGeometry(moving.width(), other.x(), other.right());
	}

	private static AxisGeometry yGeometry(HudRectangle moving, HudRectangle other) {
		return new AxisGeometry(moving.height(), other.y(), other.bottom());
	}

	record AxisSnapResult(HudPosition position, boolean snappedX, boolean snappedY) {
	}

	private record AxisSnap(int value, int distance, int threshold) {
		static AxisSnap none(int value, int threshold) {
			return new AxisSnap(value, threshold + 1, threshold);
		}

		AxisSnap nearest(int original, int candidate) {
			int candidateDistance = Math.abs(candidate - original);
			return candidateDistance < distance && candidateDistance <= threshold
					? new AxisSnap(candidate, candidateDistance, threshold)
					: this;
		}

		AxisSnap nearest(int original, int[] candidates) {
			AxisSnap nearest = this;
			for (int candidate : candidates) {
				nearest = nearest.nearest(original, candidate);
			}
			return nearest;
		}

		boolean snapped() {
			return distance <= threshold;
		}
	}

	private record AxisGeometry(int movingSize, int otherStart, int otherEnd) {
		int[] edgeAlignedPositions() {
			return new int[] {
					otherStart,
					otherEnd - movingSize,
					otherEnd,
					otherStart - movingSize
			};
		}

		EdgeSnapTarget[] edgeTargets() {
			return new EdgeSnapTarget[] {
					new EdgeSnapTarget(otherStart, otherStart),
					new EdgeSnapTarget(otherEnd - movingSize, otherEnd),
					new EdgeSnapTarget(otherEnd, otherEnd),
					new EdgeSnapTarget(otherStart - movingSize, otherStart)
			};
		}
	}

	private record EdgeSnapTarget(int position, int guideCoordinate) {
	}
}
