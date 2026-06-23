package dev.combatlab.client.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class CombatLabExternalCodec {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public String encode(Object document) {
		return GSON.toJson(document);
	}
}
