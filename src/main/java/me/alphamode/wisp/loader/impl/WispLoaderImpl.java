package me.alphamode.wisp.loader.impl;

import com.google.common.collect.ImmutableMap;
import me.alphamode.wisp.loader.JarMod;
import me.alphamode.wisp.loader.Main;
import me.alphamode.wisp.loader.WispLoader;
import me.alphamode.wisp.loader.WispLoaderPlugin;
import me.alphamode.wisp.loader.api.*;
import me.alphamode.wisp.loader.impl.mixin.MixinLoaderPlugin;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class WispLoaderImpl implements WispLoader {
    public static final WispLoaderImpl INSTANCE = new WispLoaderImpl();//InitHelper.get();
    private final Map<String, LoaderPlugin> plugins = new HashMap<>();
    private final Map<String, Mod> buildingModList = new HashMap<>();
    private Map<String, Mod> mods;
    private GameLocator locator;

    public void load(List<Path> libs) {
        plugins.put("wisp-loader", new WispLoaderPlugin());
        plugins.put("mixin", new MixinLoaderPlugin());
        locateMods();

        plugins.forEach((modId, mod) -> {
            if (!buildingModList.containsKey(modId))
                return;
            Path modLocation = buildingModList.get(modId).getPath();
            if (modLocation != null) {
                try {
                    PluginContext.CLASS_LOADER.addUrl(modLocation.toUri().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        PluginContext context = new PluginContext();

        WispLoader.LOGGER.info("Loading loader plugins");
        plugins.forEach((modId, mod) -> {
            WispLoader.LOGGER.info("   " + modId);
        });

        plugins.forEach((modId, plugin) -> {
            plugin.init(context);
            plugin.modifyMods(buildingModList);
            plugin.modifyClassPath(libs);
        });

        locator = context.getLocator();

        for (ClassTransformer transformer : context.getTransformers())
            PluginContext.CLASS_LOADER.registerClassTransformer(transformer);

        this.mods = ImmutableMap.copyOf(buildingModList);

        plugins.forEach((s, loaderPlugin) -> {
            loaderPlugin.onFinish(mods);
        });
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
                        if (result.contains("plugin")) {
                            try {
                                this.plugins.put(modId, (LoaderPlugin) Class.forName(result.getString("plugin")).getDeclaredConstructor().newInstance());
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        buildingModList.put(modId, new JarMod(path, result));
                    }
                }
            }
        } catch (IOException ignored) {

        }

        try {
            Main.class.getClassLoader().getResources("wisp.mod.toml").asIterator().forEachRemaining(url -> {
                try {
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
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
                    buildingModList.put(modId, new JarMod(Path.of(jarURLConnection.getJarFileURL().getPath()), result));
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
    public Map<String, Mod> getMods() {
        return this.mods;
    }

    public static class InitHelper {
        private static WispLoaderImpl instance;

        public static WispLoaderImpl get() {
            if (instance == null) instance = new WispLoaderImpl();

            return instance;
        }
    }
}
