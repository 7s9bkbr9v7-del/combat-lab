/**
 * Small compatibility spine for Minecraft-version drift.
 *
 * <p>Keep expensive or fragile compatibility work here: class-name catalogs,
 * optional method lookup, and capability decisions. Gameplay, render, and tick
 * paths should consume resolved methods or switches instead of probing versions,
 * doing reflection, or branching across many Minecraft layouts each frame.</p>
 *
 * <p>Preferred pattern for future compatibility work:</p>
 * <ol>
 *     <li>Add class names or target groups to {@link dev.combatlab.client.compat.MinecraftClasses}
 *     or {@link dev.combatlab.client.compat.MinecraftCapabilities}.</li>
 *     <li>Resolve reflective methods once into {@link dev.combatlab.client.compat.CompatMethod}.</li>
 *     <li>For per-frame feature decisions, update a {@link dev.combatlab.client.compat.CompatFeatureSwitch}
 *     at startup or feature-toggle time and read that switch from the hot path.</li>
 *     <li>Keep mixins as thin delegates into normal Combat Lab feature code.</li>
 * </ol>
 */
package dev.combatlab.client.compat;
