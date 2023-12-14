package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.api.Mod;

import java.nio.file.Path;
import java.util.List;

public class LoaderMod implements Mod {
    @Override
    public List<Path> getPaths() {
        return List.of();
    }

    @Override
    public String getId() {
        return "wisp-loader";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
