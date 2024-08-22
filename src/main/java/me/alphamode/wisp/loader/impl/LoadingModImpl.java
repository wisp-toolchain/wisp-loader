package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingModImpl implements LoadingMod {

    private final String modId, version;
    private final Map<Class<?>, List<Object>> components = new HashMap<>();

    public LoadingModImpl(String modId, String version) {
        this.modId = modId;
        this.version = version;
    }

    @Override
    public String getId() {
        return this.modId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public <C> LoadingMod addComponent(C component) {
        this.components.computeIfAbsent(component.getClass(), k -> new ArrayList<>()).add(component);
        return this;
    }

    @Override
    public Map<Class<?>, List<Object>> getComponents() {
        return components;
    }

    @Override
    public <C> List<C> getComponents(Class<C> clazz) {
        return (List<C>) components.computeIfAbsent(clazz, aClass -> new ArrayList<>());
    }

    @Override
    public Mod toMod() {
        return new ModImpl(modId, version, components);
    }
}
