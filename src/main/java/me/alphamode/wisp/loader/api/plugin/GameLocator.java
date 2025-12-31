package me.alphamode.wisp.loader.api.plugin;

import me.alphamode.wisp.loader.api.ArgumentList;

import java.nio.file.Path;
import java.util.List;

public interface GameLocator {
    /**
     * This isn't called by default if your using a custom {@link GameLocator}, you should call this in {@link GameLocator#getGameClassPaths(String[])} if you are.
     * Exposed for plugins
     * @param classPaths
     * @param args
     * @return The location of the current game jar
     */
    Path locateGame(List<Path> classPaths, String[] args);

    /**
     * Implementors should always return a mutable list for loader plugins!
     * @return Return the class paths that wisp loader should use to launch.
     */
    List<Path> getGameClassPaths(String[] args);

    String getMainClass();

    void launch(ArgumentList arguments);
}