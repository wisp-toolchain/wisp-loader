package me.alphamode.wisp.loader.api.plugin;

import me.alphamode.wisp.loader.api.ArgumentList;

import java.nio.file.Path;
import java.util.List;

public interface Provider {
    /**
     * This isn't called by default if your using a custom {@link Provider}, you should call this in {@link Provider#getClassPaths(String[])} if you are.
     * Exposed for plugins
     * @param classPaths
     * @param args
     * @return The location of the current game jar
     */
    Path provide(List<Path> classPaths, String[] args);

    /**
     * Implementors should always return a mutable list for loader plugins!
     * @return Return the class paths that wisp loader should use to launch.
     */
    List<Path> getClassPaths(String[] args);

    String getMainClass();

    void launch(ArgumentList arguments);
}