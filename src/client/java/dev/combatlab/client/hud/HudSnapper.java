package dev.combatlab.client.hud;

import java.util.List;

public final class HudSnapper {
	private HudSnapper() {
	}

	public static HudPosition snap(HudRectangle moving, List<HudRectangle> others, int threshold) {
		SnapResult nearby = snapNearby(moving, others, threshold);
		HudRectangle nearbyRectangle = new HudRectangle(
				nearby.position().x(),
				nearby.position().y(),
				moving.width(),
				moving.height()
		);
		return snapAligned(nearbyRectangle, others, threshold, !nearby.snappedX(), !nearby.snappedY());
	}

	static SnapResult snapNearby(HudRectangle moving, List<HudRectangle> others, int threshold) {
		AxisSnap xSnap = AxisSnap.none(moving.x(), threshold);
		for (HudRectangle other : others) {
			if (rangesNear(moving.y(), moving.bottom(), other.y(), other.bottom(), threshold)) {
				xSnap = xSnap.nearest(moving.x(), other.x());
				xSnap = xSnap.nearest(moving.x(), other.right() - moving.width());
				xSnap = xSnap.nearest(moving.x(), other.x() - moving.width());
				xSnap = xSnap.nearest(moving.x(), other.right());
			}
		}

		HudRectangle horizontal = new HudRectangle(xSnap.value(), moving.y(), moving.width(), moving.height());
		AxisSnap ySnap = AxisSnap.none(moving.y(), threshold);
		for (HudRectangle other : others) {
			if (rangesNear(horizontal.x(), horizontal.right(), other.x(), other.right(), threshold)) {
				ySnap = ySnap.nearest(moving.y(), other.y());
				ySnap = ySnap.nearest(moving.y(), other.bottom() - moving.height());
				ySnap = ySnap.nearest(moving.y(), other.y() - moving.height());
				ySnap = ySnap.nearest(moving.y(), other.bottom());
			}
		}

		return new SnapResult(new HudPosition(xSnap.value(), ySnap.value()), xSnap.snapped(), ySnap.snapped());
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
				xSnap = xSnap.nearest(moving.x(), other.x());
				xSnap = xSnap.nearest(moving.x(), other.right() - moving.width());
				xSnap = xSnap.nearest(moving.x(), other.x() + (other.width() - moving.width()) / 2);
			}
			if (allowYAlignment) {
				ySnap = ySnap.nearest(moving.y(), other.y());
				ySnap = ySnap.nearest(moving.y(), other.bottom() - moving.height());
				ySnap = ySnap.nearest(moving.y(), other.y() + (other.height() - moving.height()) / 2);
			}
		}

		return new HudPosition(xSnap.value(), ySnap.value());
	}

	private static boolean rangesNear(int firstStart, int firstEnd, int secondStart, int secondEnd, int threshold) {
		return firstEnd + threshold >= secondStart && secondEnd + threshold >= firstStart;
	}

	record SnapResult(HudPosition position, boolean snappedX, boolean snappedY) {
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

		boolean snapped() {
			return distance <= threshold;
		}
	}
}
