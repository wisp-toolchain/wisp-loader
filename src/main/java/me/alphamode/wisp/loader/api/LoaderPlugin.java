package me.alphamode.wisp.loader.api;

import java.nio.file.Path;
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
    default void modifyMods(Map<String, Mod> mods) {}

    /**
     * Allows you to modify the games runtime classpath before the game launches.
     * <p>
     * Please note if your trying to change the game jar register a {@link GameLocator} in the {@link LoaderPlugin#init(PluginContext)} method using {@link PluginContext#registerGameLocator(GameLocator)}
     * @param classPaths The current classpath; usually if unmodified result will be the same as System.getProperty("java.class.path").split(File.pathSeparator) minus the game jar.
     */
    @Deprecated
    default void modifyClassPath(List<Path> classPaths) {}

    default void onFinish(Map<String, Mod> mods) {

    }
}