package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.JarMod;
import me.alphamode.wisp.loader.Main;
import me.alphamode.wisp.loader.api.LoaderPlugin;
import me.alphamode.wisp.loader.api.Mod;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.mod.ModLocator;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;

public class ModDiscoverer implements ModLocator {
    public Map<String, String> locatePlugins() {
        Map<String, String> plugins = new HashMap<>();
        for (Path path : locateMods()) {
            try (FileSystem mod = FileSystems.newFileSystem(path)) {
                var modFile = mod.getPath("wisp.mod.toml");
                if (Files.exists(modFile)) {
                    TomlParseResult result = Toml.parse(modFile);
                    result.errors().forEach(error -> System.err.println(error.toString()));
                    String modId = result.getString("plugin-id");
                    if (result.contains("plugin-id")) {
                        plugins.put(modId, result.getString("plugin"));
                        PluginContext.CLASS_LOADER.addUrl(path.toUri().toURL());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            PluginContext.CLASS_LOADER.getResources("wisp.mod.toml").asIterator().forEachRemaining(url -> {
                try {
                    URLConnection jarURLConnection = url.openConnection();
                    TomlParseResult result = Toml.parse(jarURLConnection.getInputStream());
                    result.errors().forEach(error -> System.err.println(error.toString()));

                    if (result.contains("plugin-id")) {
                        String modId = result.getString("plugin-id");
                        plugins.put(modId, result.getString("plugin"));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return plugins;
    }

    public List<Path> locateMods() {
        Path modsFolder = Path.of("mods");
        if (!Files.exists(modsFolder))
            modsFolder.toFile().mkdirs();
        try (Stream<Path> files = Files.list(modsFolder)) {
            return files.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void locateMods(SortedMap<String, Mod> buildingModList) {
        for (Path path : locateMods()) {
            try (FileSystem mod = FileSystems.newFileSystem(path)) {
                var modFile = mod.getPath("wisp.mod.toml");
                if (Files.exists(modFile)) {
                    TomlParseResult result = Toml.parse(modFile);
                    result.errors().forEach(error -> System.err.println(error.toString()));
                    if (result.contains("mod-id")) {
                        String modId = result.getString("mod-id");
                        var jarMod = new JarMod(path, result);
                        buildingModList.put(modId, jarMod);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            PluginContext.CLASS_LOADER.getResources("wisp.mod.toml").asIterator().forEachRemaining(url -> {
                try {
                    URLConnection jarURLConnection = url.openConnection();
                    TomlParseResult result = Toml.parse(jarURLConnection.getInputStream());
                    result.errors().forEach(error -> System.err.println(error.toString()));

                    if (result.contains("mod-id")) {
                        String modId = result.getString("mod-id");
                        buildingModList.put(modId, new JarMod(Path.of(url.getPath()), result));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
