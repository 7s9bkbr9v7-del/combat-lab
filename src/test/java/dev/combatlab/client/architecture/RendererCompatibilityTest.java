package dev.combatlab.client.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.constantpool.ClassEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RendererCompatibilityTest {
  private static final List<String> FORBIDDEN_PREFIXES =
      List.of("org/lwjgl/opengl", "org/lwjgl/vulkan", "com/mojang/blaze3d/opengl");

  @Test
  void clientBytecodeDoesNotReferenceGraphicsBackendsDirectly() throws IOException {
    String configuredRoots = System.getProperty("combatlab.clientClasses", "");
    List<String> violations = new ArrayList<>();

    for (String root : configuredRoots.split(java.io.File.pathSeparator)) {
      if (root.isBlank() || !Files.isDirectory(Path.of(root))) {
        continue;
      }
      try (var classes = Files.walk(Path.of(root))) {
        for (Path classFile : classes.filter(path -> path.toString().endsWith(".class")).toList()) {
          inspect(classFile, violations);
        }
      }
    }

    assertTrue(
        violations.isEmpty(),
        () -> "Direct renderer backend references:\n" + String.join("\n", violations));
  }

  private static void inspect(Path classFile, List<String> violations) throws IOException {
    var model = ClassFile.of().parse(classFile);
    for (var entry : model.constantPool()) {
      if (entry instanceof ClassEntry referencedClass) {
        String name = referencedClass.asInternalName();
        if (FORBIDDEN_PREFIXES.stream().anyMatch(name::startsWith)
            || name.endsWith("/GlStateManager")) {
          violations.add(model.thisClass().asInternalName() + " -> " + name);
        }
      }
    }
  }
}
