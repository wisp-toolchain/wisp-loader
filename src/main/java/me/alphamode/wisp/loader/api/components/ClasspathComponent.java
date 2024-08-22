package me.alphamode.wisp.loader.api.components;

import java.nio.file.Path;
import java.util.List;

/**
 * Gets the current path were the jar is located.
 */
public record ClasspathComponent(List<Path> paths) {
    public ClasspathComponent(Path path) {
        this(List.of(path));
    }
}
