package me.alphamode.wisp.loader.api;

import me.alphamode.wisp.loader.WispClassLoader;
import me.alphamode.wisp.loader.WispLoader;
import me.alphamode.wisp.loader.WispLoaderPlugin;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * A loader plugin is a powerful tool that allows you to modify how the loader handles loading mods and libraries.
 */
public interface LoaderPlugin {
    /**
     * Do your main logic here
     */
    void init();

    default void modifyMods(Map<String, Mod> mods) {}

    /**
     * Allows you to modify the games runtime classpath before the game launches.
     * <p>
     * Please note if your trying to change the game jar register a {@link GameLocator} in the {@link LoaderPlugin#init()} method using {@link LoaderPlugin#registerGameLocator(GameLocator)}
     * @param classPaths The current classpath; usually if unmodified result will be the same as System.getProperty("java.class.path").split(File.pathSeparator) minus the game jar.
     */
    default void modifyClassPath(List<URL> classPaths) {}

    /**
     * Only use this if you know what you are doing!
     * <p>
     * This is used to provide a custom game jar, such as a patched version of minecraft.
     * Only one of these can exist. Registering one will override the default game locator.
     */
    default void registerGameLocator(GameLocator locator) {
        if (WispLoader.get().locator instanceof WispLoaderPlugin.MappedGameLocator || WispLoader.get().locator == null)
            WispLoader.get().locator = locator;
    }

    default void registerClassTransformer(WispClassLoader classLoader) {}
}