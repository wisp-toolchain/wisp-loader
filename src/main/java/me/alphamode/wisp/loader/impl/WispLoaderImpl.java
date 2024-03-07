package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.env.Environment;
import me.alphamode.wisp.loader.JarMod;
import me.alphamode.wisp.loader.Main;
import me.alphamode.wisp.loader.api.*;
import me.alphamode.wisp.loader.api.extension.Extension;
import me.alphamode.wisp.loader.api.extension.ExtensionType;
import me.alphamode.wisp.loader.impl.minecraft.WispLoaderPlugin;
import me.alphamode.wisp.loader.impl.mixin.MixinLoaderPlugin;
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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class WispLoaderImpl implements WispLoader {
    public static final WispLoaderImpl INSTANCE = new WispLoaderImpl();
    private final ModDiscoverer discoverer = new ModDiscoverer();
    private final SortedMap<String, LoaderPlugin> plugins = new TreeMap<>();
    private final SortedMap<String, Mod> buildingModList = new TreeMap<>();
    private Map<String, ? extends Extension> extensions;
    private Map<String, Mod> mods;
    private GameLocator locator;

    public List<Path> load(ArgumentList argumentList) {
        plugins.put("wisp-loader", new WispLoaderPlugin());
        plugins.put("mixin", new MixinLoaderPlugin());
        discoverer.locateMods(buildingModList);

        discoverer.locatePlugins().forEach((id, plugin) -> {
            try {
                plugins.put(id, (LoaderPlugin) PluginContext.CLASS_LOADER.loadClass(plugin).getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        plugins.forEach((modId, mod) -> {
            if (plugins.containsKey(modId))
                return;
            try {
                PluginContext.CLASS_LOADER.addMod(buildingModList.get(modId));
            } catch (IOException e) {
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
            plugin.modifyMods(buildingModList);
            plugin.modifyClassPath(libs);
        });

        locator = context.getLocator();

        for (ClassTransformer transformer : context.getTransformers())
            PluginContext.CLASS_LOADER.registerClassTransformer(transformer);

        this.mods = Map.copyOf(buildingModList);

        mods.forEach((s, mod) -> {
            if (plugins.containsKey(s))
                return;
            try {
                PluginContext.CLASS_LOADER.addMod(mod);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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
                        var jarMod = new JarMod(path, result);
                        if (result.contains("plugin-id")) {
                            PluginContext.CLASS_LOADER.addMod(jarMod);
                            try {
                                plugins.put(modId, (LoaderPlugin) PluginContext.CLASS_LOADER.loadClass(result.getString("plugin")).getDeclaredConstructor().newInstance());
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        buildingModList.put(modId, jarMod);
                    }
                }
            }
        } catch (IOException ignored) {

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
                    buildingModList.put(modId, new JarMod(Path.of(url.getPath()), result));
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
}
