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
     * You cannot remove any mod that provides a loader plugin.
     * @param mods The current mod list
     */
    default void resolveMods(Map<String, List<LoadingMod>> mods) {}

    /**
     * Used to modify components of mods. At this point all mods declared and cannot be modified, if you wish to modify mods or add new mods please use {@link LoaderPlugin#resolveMods(Map)}
     * @param mods
     */
    default void onModsFinalized(Map<String, LoadingMod> mods) {}

    /**
     * Used for 
     * @param mods
     */
    default void onFinish(Map<String, Mod> mods) {}
}