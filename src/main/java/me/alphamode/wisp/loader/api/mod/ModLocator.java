package me.alphamode.wisp.loader.api.mod;

import java.nio.file.Path;
import java.util.List;

public interface ModLocator {
    List<Path> locateMods();
}
