package me.alphamode.wisp.loader.mixin;

import me.alphamode.wisp.loader.api.LoaderPlugin;
import me.alphamode.wisp.loader.api.Mod;

import java.nio.file.Path;

public class MixinMod implements Mod {
    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public String getId() {
        return "mixin";
    }

    @Override
    public String getVersion() {
        return "loser";
    }

    @Override
    public LoaderPlugin getLoaderPlugin() {
        return new MixinLoaderPlugin();
    }
}
