package dev.combatlab.client.hud;

import dev.combatlab.client.config.CombatLabOptions;
import dev.combatlab.client.debug.DebugLogger;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ArmorHud extends ResizableBaseHudModule implements AdaptiveLayoutHudModule {
	private static final int PADDING = 1;
	private static final int ITEM_SIZE = 16;
	private static final HudModuleDefinition DEFINITION = new HudModuleDefinition(
			Identifier.fromNamespaceAndPath("combatlab", "armor"),
			Component.literal("Armor HUD"),
			0.02,
			0.50,
			true
	);
	private ArmorHudLayout lockedLayout;

	public ArmorHud(CombatLabOptions options, DebugLogger debug) {
		super(DEFINITION, options, debug);
	}

	@Override
	public HudSize unscaledSize() {
		ArmorHudLayout layout = layout();
		return new HudSize(
				PADDING * 2 + ITEM_SIZE * layout.columns(),
				PADDING * 2 + ITEM_SIZE * layout.rows()
		);
	}

	@Override
	public void lockLayout() {
		lockedLayout = resolvedLayout();
	}

	@Override
	public void unlockLayout() {
		String configId = id().toString();
		ArmorHudLayout finalLayout = ArmorHudLayout.resolve(
				options().hudX(configId),
				options().hudY(configId),
				lockedLayout != null ? lockedLayout : storedLayout(configId)
		);
		options().updateHudLayout(configId, finalLayout.name());
		lockedLayout = null;
	}

	@Override
	protected void renderModule(GuiGraphicsExtractor graphics, HudRenderContext context) {
		if (context.client().player == null) {
			return;
		}

		graphics.pose().pushMatrix();
		graphics.pose().translate(context.bounds().x(), context.bounds().y());
		graphics.pose().scale((float) scale(), (float) scale());
		ArmorHudLayout layout = layout();
		for (int index = 0; index < layout.slots().size(); index++) {
			ItemStack stack = context.client().player.getItemBySlot(layout.slots().get(index));
			if (stack.isEmpty()) {
				continue;
			}

			int x = PADDING + index % layout.columns() * ITEM_SIZE;
			int y = PADDING + index / layout.columns() * ITEM_SIZE;
			graphics.item(context.client().player, stack, x, y, 0);
			graphics.itemDecorations(context.font(), stack, x, y);
		}
		graphics.pose().popMatrix();
	}

	private ArmorHudLayout layout() {
		return lockedLayout != null ? lockedLayout : resolvedLayout();
	}

	private ArmorHudLayout resolvedLayout() {
		String configId = id().toString();
		return ArmorHudLayout.resolve(
				options().hudX(configId),
				options().hudY(configId),
				storedLayout(configId)
		);
	}

	private ArmorHudLayout storedLayout(String configId) {
		return ArmorHudLayout.fromStored(options().hudLayout(configId));
	}
}
