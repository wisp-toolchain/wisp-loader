package me.alphamode.wisp.loader.api;

import me.alphamode.wisp.loader.WispLoaderPlugin;

import java.nio.file.Path;

public class LoaderMod implements Mod {
    private final WispLoaderPlugin plugin = new WispLoaderPlugin();

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

    @Override
    public LoaderPlugin getLoaderPlugin() {
        return this.plugin;
    }
}
