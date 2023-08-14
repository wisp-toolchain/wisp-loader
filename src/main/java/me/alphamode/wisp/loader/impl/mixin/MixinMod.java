package me.alphamode.wisp.loader.impl.mixin;

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
}
