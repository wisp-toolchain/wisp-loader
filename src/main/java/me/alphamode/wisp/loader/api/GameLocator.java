package me.alphamode.wisp.loader.api;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface GameLocator {
    Path locateGame(List<Path> classPaths, String[] args);

    String getMainClass();
}
