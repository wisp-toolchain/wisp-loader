package me.alphamode.wisp.loader.impl;

import io.github.wasabithumb.jtoml.document.TomlDocument;
import me.alphamode.wisp.loader.LibraryFinder;
import me.alphamode.wisp.loader.api.*;
import me.alphamode.wisp.loader.api.extension.Extension;
import me.alphamode.wisp.loader.api.extension.ExtensionType;
import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.Mod;
import me.alphamode.wisp.loader.api.plugin.ClassTransformer;
import me.alphamode.wisp.loader.api.plugin.Provider;
import me.alphamode.wisp.loader.api.plugin.LoaderPlugin;
import me.alphamode.wisp.loader.api.plugin.PluginContext;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class WispLoaderImpl implements WispLoader {

    public static final WispLoaderImpl INSTANCE = new WispLoaderImpl();
    private final PluginLocator discoverer = new PluginLocator();
    private final SortedMap<String, LoaderPlugin> plugins = new TreeMap<>();
    private final SortedMap<String, LoadingMod> buildingModList = new TreeMap<>();
    private Map<String, ? extends Extension> extensions;
    private Map<String, Mod> mods;
    private Provider locator;

    public List<Path> load(ArgumentList argumentList) {
        discoverer.locatePlugins().forEach((id, plugin) -> {
            try {
                plugins.put(id, (LoaderPlugin) PluginContext.CLASS_LOADER.loadClass(plugin).getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        PluginContext context = new PluginContext(argumentList);

        WispLoader.LOGGER.info("Loading loader plugins");
        plugins.forEach((modId, mod) -> {
            WispLoader.LOGGER.info("   " + modId);
        });

        // TODO: remove internal plugin and introduce a priority system

        plugins.forEach((modId, plugin) -> {
            if (plugin instanceof InternalPlugin)
                plugin.preInit(context);
        });

        plugins.forEach((modId, plugin) -> {
            if (plugin instanceof InternalPlugin)
                return;
            plugin.preInit(context);
        });

        PluginContext.CLASS_LOADER.validParentCodeSources.add(LibraryFinder.getCodeSource(TomlDocument.class));

        List<Path> libs = context.getLocator().getClassPaths(argumentList.toArray());
        context.setClassPath(libs);

        PluginContext.CLASS_LOADER.addUrls(context.getClassPath().stream().map(path -> {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new));


        plugins.forEach((modId, plugin) -> {
            plugin.init(context);
            extensions = context.getExtensions();
        });



        LOGGER.debug("Resolving mods");

        plugins.forEach((modId, plugin) -> {
            plugin.resolveMods(context.getBuildingModList());
        });

        context.getBuildingModList().forEach((modId, mods) -> {
            if (mods.isEmpty())
                return;
            if (mods.size() > 1) {
                throw new ModResolveException(String.format("Multiple Mods found for %s in %s", modId, mods));
            }
            buildingModList.put(modId, mods.get(0));
        });

        locator = context.getLocator();

        for (ClassTransformer transformer : context.getTransformers())
            PluginContext.CLASS_LOADER.registerClassTransformer(transformer);

        Map<String, LoadingMod> finalModList = Map.copyOf(buildingModList);
        plugins.forEach((modId, plugin) -> {
            plugin.onLoadingFinalized(finalModList);
        });
        Map<String, Mod> tempMods = new TreeMap<>();
        buildingModList.forEach((id, mod) -> tempMods.put(id, mod.toMod()));
        this.mods = Map.copyOf(tempMods);

        return context.getClassPath();
    }

    public Provider getLocator() {
        return locator;
    }

    @Override
    public boolean isDevelopment() {
        return true;
    }

    @Override
    public Path getRunDir() {
        return Path.of("run");
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public Map<String, Mod> getMods() {
        return this.mods;
    }

    @Nullable
    @Override
    public Mod getMod(String id) {
        if (!this.mods.containsKey(id))
            return null;
        return this.mods.get(id);
    }

    @Override
    public String getVersion() {
        return "wisp-loader";
    }

    @Override
    public <Ext extends Extension> Ext getExtension(ExtensionType<Ext> type) {
        if (this.extensions == null)
            throw new RuntimeException("Extensions have not been initialized yet!");
        return (Ext) this.extensions.get(type.getId());
    }

    public SortedMap<String, LoaderPlugin> getPlugins() {
        return plugins;
    }

    public static class InitHelper {
        private static WispLoaderImpl instance;

        public static WispLoaderImpl get() {
            if (instance == null) instance = new WispLoaderImpl();

            return instance;
        }
    }

    public static void verifyNotInTargetCl(Class<?> cls) {
        if (cls.getClassLoader().getClass().getName().equals("me.alphamode.wisp.loader.WispClassLoader")) {
            // This usually happens when fabric loader has been added to the target class loader. This is a bad state.
            // Such additions may be indirect, a JAR can use the Class-Path manifest attribute to drag additional
            // libraries with it, likely recursively.
            throw new IllegalStateException("trying to load "+cls.getName()+" from target class loader");
        }
    }

    static {
        verifyNotInTargetCl(WispLoaderImpl.class);
    }
}
