package me.alphamode.wisp.loader.api;

import java.nio.file.Path;
import java.util.List;

public interface Mod {
    /**
     * Gets the current path were the jar is located.
     * @return The mod jar.
     */
    List<Path> getPaths();

    String getId();

    String getVersion();
}