package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.api.Mod;

import java.nio.file.Path;

public class LoaderMod implements Mod {
    @Override
    public Path getPath() {
        return null;
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
