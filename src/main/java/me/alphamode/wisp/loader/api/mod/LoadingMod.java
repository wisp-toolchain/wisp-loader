package me.alphamode.wisp.loader.api.mod;

import me.alphamode.wisp.loader.impl.LoadingModImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Represents a mod being loaded, you are free to implement this on your own or use {@link LoadingMod#create(String, String)}
 */
public interface LoadingMod {

    /**
     * Helper method for creating loading mods
     */
    static LoadingMod create(String id, String version) {
        return new LoadingModImpl(id, version);
    }

    String getId();

    String getVersion();

    default <C> boolean hasComponent(Class<C> clazz) {
        return getComponents().containsKey(clazz);
    }

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
