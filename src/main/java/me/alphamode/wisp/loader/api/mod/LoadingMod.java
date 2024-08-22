package me.alphamode.wisp.loader.api.mod;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface LoadingMod {

    String getId();

    String getVersion();

    <C> LoadingMod addComponent(C component);

    Map<Class<?>, List<Object>> getComponents();

    @Nullable
    default <C> C getComponent(Class<C> clazz) {
        var components = getComponents(clazz);
        if (components.isEmpty()) return null;
        return components.get(0);
    }

    <C> List<C> getComponents(Class<C> clazz);
    
    Mod toMod();
}
