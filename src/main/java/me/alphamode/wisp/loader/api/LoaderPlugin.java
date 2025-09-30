package me.alphamode.wisp.loader.api;

import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.Mod;

import java.util.List;
import java.util.Map;

/**
 * A loader plugin is a powerful tool that allows you to modify how the loader handles loading mods and libraries.
 */
public interface LoaderPlugin {
    /**
     * Register game locators here
     */
    default void preInit(PluginContext context) {}

    /**
     * Do your main logic here
     */
    void init(PluginContext context);

    /**
     * Allows you to modify the loading mod list.
     * Used for dependency resolution and SAT solving.
     * @param mods The current loading mod candidate list
     */
    default void resolveMods(Map<String, List<LoadingMod>> mods) {}

    /**
     * Used to modify components of mods. At this point all mods are declared and cannot be modified, if you wish to modify mods or add new mods please use {@link LoaderPlugin#resolveMods(Map)}
     * @param mods An immutable copy of the loading mod list, this is mainly used to add new components to mods before they are converted to {@link Mod}'s.
     */
    default void onLoadingFinalized(Map<String, LoadingMod> mods) {}

    /**
     * Most commonly used to do any component handling on mods, such as taking a mixin component and registering it to mixin
     * @param mods An immutable copy of all loaded mods.
     */
    default void onModsFinalized(Map<String, Mod> mods) {}
}