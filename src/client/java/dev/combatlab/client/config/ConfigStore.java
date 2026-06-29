package dev.combatlab.client.config;

import dev.combatlab.client.CombatLabClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigStore {
  private final Path file;
  private final CombatLabConfigCodec codec;

  public ConfigStore(Path file, CombatLabConfigCodec codec) {
    this.file = file;
    this.codec = codec;
  }

  public static ConfigStore createDefault() {
    Path file = FabricLoader.getInstance().getConfigDir().resolve("combatlab.json");
    return new ConfigStore(file, new CombatLabConfigCodec());
  }

  public CombatLabConfig load() {
    if (!Files.exists(file)) {
      return new CombatLabConfig();
    }

    try {
      return codec.decode(Files.readString(file, StandardCharsets.UTF_8));
    } catch (IOException | RuntimeException exception) {
      CombatLabClient.LOGGER.warn("Could not read {}; using defaults", file, exception);
      return new CombatLabConfig();
    }
  }

  public void save(CombatLabConfig config) {
    Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
    try {
      Files.createDirectories(file.getParent());
      Files.writeString(temporary, codec.encode(config), StandardCharsets.UTF_8);
      try {
        Files.move(
            temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      CombatLabClient.LOGGER.warn("Could not save {}", file, exception);
    }
  }
}
