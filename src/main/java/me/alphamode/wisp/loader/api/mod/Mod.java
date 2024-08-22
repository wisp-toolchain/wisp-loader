package me.alphamode.wisp.loader.api.mod;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public interface Mod {
    String getId();

    String getVersion();

    <C> boolean hasComponent(Class<C> clazz);

    @Nullable
    default <C> C getComponent(Class<C> clazz) {
        var components = getComponents(clazz);
        if (components.isEmpty()) return null;
        return components.get(0);
    }

    <C> List<C> getComponents(Class<C> clazz);
}