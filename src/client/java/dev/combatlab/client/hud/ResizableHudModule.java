package dev.combatlab.client.hud;

public interface ResizableHudModule extends HudModule {
	double scale();

	void updateScale(double scale);

	HudSize unscaledSize();

	double minScale();

	double maxScale();
}
