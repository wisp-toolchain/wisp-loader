package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.env.Environment;
import me.alphamode.wisp.loader.Main;
import me.alphamode.wisp.loader.api.*;
import me.alphamode.wisp.loader.api.components.ClasspathComponent;
import me.alphamode.wisp.loader.api.components.TomlComponent;
import me.alphamode.wisp.loader.api.extension.Extension;
import me.alphamode.wisp.loader.api.extension.ExtensionType;
import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.Mod;
import org.jetbrains.annotations.Nullable;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class WispLoaderImpl implements WispLoader {
    public static final WispLoaderImpl INSTANCE = new WispLoaderImpl();
    private final PluginLocator discoverer = new PluginLocator();
    private final SortedMap<String, LoaderPlugin> plugins = new TreeMap<>();
    private final SortedMap<String, LoadingMod> buildingModList = new TreeMap<>();
    private Map<String, ? extends Extension> extensions;
    private Map<String, Mod> mods;
    private GameLocator locator;

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

        plugins.forEach((modId, plugin) -> {
            if (plugin instanceof InternalPlugin)
                plugin.preInit(context);
        });

        plugins.forEach((modId, plugin) -> {
            if (plugin instanceof InternalPlugin)
                return;
            plugin.preInit(context);
        });

        List<Path> libs = context.getLocator().getGameClassPaths(argumentList.toArray());
        context.setClassPath(libs);

        plugins.forEach((modId, plugin) -> {
            plugin.init(context);
            extensions = context.getExtensions();
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
            plugin.onModsFinalized(finalModList);
        });
        Map<String, Mod> tempMods = new TreeMap<>();
        buildingModList.forEach((id, mod) -> tempMods.put(id, mod.toMod()));
        this.mods = Map.copyOf(tempMods);

        return context.getClassPath();
    }

    public GameLocator getLocator() {
        return locator;
    }

    private void locateMods() {
        Path modsFolder = Path.of("mods");

        try (Stream<Path> files = Files.list(modsFolder)) {
            for (Path path : files.toList()) {
                try (FileSystem mod = FileSystems.newFileSystem(path)) {
                    var modFile = mod.getPath("wisp.mod.toml");
                    if (Files.exists(modFile)) {
                        TomlParseResult result = Toml.parse(modFile);
                        result.errors().forEach(error -> System.err.println(error.toString()));
                        String modId = result.getString("mod-id");
                        String version = result.getString("version"); // path, result
                        var jarMod = new LoadingModImpl(modId, version)
                                .addComponent(new ClasspathComponent(path))
                                .addComponent(new TomlComponent(result));
                        if (result.contains("plugin-id")) {
//                            PluginContext.CLASS_LOADER.addMod(jarMod);
                            try {
                                plugins.put(result.getString("plugin-id"), (LoaderPlugin) PluginContext.CLASS_LOADER.loadClass(result.getString("plugin")).getDeclaredConstructor().newInstance());
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        buildingModList.put(modId, jarMod);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Main.class.getClassLoader().getResources("wisp.mod.toml").asIterator().forEachRemaining(url -> {
                try {
                    URLConnection jarURLConnection = url.openConnection();
                    TomlParseResult result = Toml.parse(jarURLConnection.getInputStream());
                    result.errors().forEach(error -> System.err.println(error.toString()));
                    String modId = result.getString("mod-id");
                    if (result.contains("plugin")) {
                        try {
                            this.plugins.put(modId, (LoaderPlugin) Class.forName(result.getString("plugin")).getDeclaredConstructor().newInstance());
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    buildingModList.put(modId, new LoadingModImpl(modId, result.getString("version"))
                            .addComponent(new ClasspathComponent(Path.of(url.getPath())))
                            .addComponent(new TomlComponent(result)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDevelopment() {
        return true;
    }

    @Override
    public Path getGameDir() {
        return Path.of("run");
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public Environment getEnvironment() {
        return Environment.CLIENT;
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
