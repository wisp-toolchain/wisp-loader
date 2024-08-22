package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.Mod;
import org.tomlj.TomlParseResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModImpl implements Mod {
    private final String modId, version;
    private final Map<Class<?>, List<Object>> components;

    public ModImpl(String modId, String version, Map<Class<?>, List<Object>> components) {
        this.modId = modId;
        this.version = version;
        this.components = components;
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
    public <C> boolean hasComponent(Class<C> clazz) {
        return components.containsKey(clazz);
    }

    @Override
    public <C> List<C> getComponents(Class<C> clazz) {
        return (List<C>) components.computeIfAbsent(clazz, k -> new ArrayList<>());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.modId, this.version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModImpl mod) {
            return this.modId.equals(mod.modId);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "ModImpl[modId=" + this.modId + ", version=" + this.version + "]";
    }
}
