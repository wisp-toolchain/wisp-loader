package me.alphamode.wisp.loader.api;

import java.nio.file.Path;

public interface Mod {
    /**
     * Gets the current path were the jar is located.
     * @return The mod jar.
     */
    Path getPath();

    String getId();

    String getVersion();

    default LoaderPlugin getLoaderPlugin() {
        return null;
    };
}