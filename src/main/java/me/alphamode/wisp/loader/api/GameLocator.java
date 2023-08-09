package me.alphamode.wisp.loader.api;

import java.net.URL;
import java.util.List;

public interface GameLocator {
    URL locateGame(List<URL> classPaths);

    String getMainClass();
}
