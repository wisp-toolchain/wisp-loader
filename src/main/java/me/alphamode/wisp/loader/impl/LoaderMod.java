package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.api.mod.Mod;

import java.util.List;

public class LoaderMod implements Mod {
    @Override
    public String getId() {
        return "wisp-loader";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public <C> boolean hasComponent(Class<C> clazz) {
        return false;
    }

    @Override
    public <C> List<C> getComponents(Class<C> clazz) {
        return List.of();
    }
}
