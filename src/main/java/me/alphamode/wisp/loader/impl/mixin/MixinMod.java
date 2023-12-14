package me.alphamode.wisp.loader.impl.mixin;

import me.alphamode.wisp.loader.api.Mod;

import java.nio.file.Path;
import java.util.List;

public class MixinMod implements Mod {
    @Override
    public List<Path> getPaths() {
        return List.of();
    }

    @Override
    public String getId() {
        return "mixin";
    }

    @Override
    public String getVersion() {
        return "0.12.5+mixin.0.8.5";
    }
}
