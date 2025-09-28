package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.api.components.ClasspathComponent;
import me.alphamode.wisp.loader.api.components.TomlComponent;
import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.mod.ModLocator;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
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
                        PluginContext.CLASS_LOADER.addCodeSources(path);
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
                        PluginContext.CLASS_LOADER.addCodeSources(Path.of(url.getPath()));
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

    public void locateMods(SortedMap<String, LoadingMod> buildingModList) {
        for (Path path : locateMods()) {
            try {
                try (FileSystem mod = FileSystems.newFileSystem(path)) {
                    var modFile = mod.getPath("wisp.mod.toml");
                    if (Files.exists(modFile)) {
                        TomlParseResult result = Toml.parse(modFile);
                        result.errors().forEach(error -> System.err.println(error.toString()));
                        if (result.contains("mod-id")) {
                            String modId = result.getString("mod-id"); // path, result
                            var jarMod = new LoadingModImpl(modId, result.getString("version"))
                                    .addComponent(new ClasspathComponent(path))
                                    .addComponent(new TomlComponent(result));
                            buildingModList.put(modId, jarMod);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                WispLoader.LOGGER.error("Error loading mod path: " + path);
                e.printStackTrace();
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
                        var mod = new LoadingModImpl(result.getString("mod-id"), result.getString("version"))
                                .addComponent(new ClasspathComponent(Path.of(url.getPath())))
                                .addComponent(new TomlComponent(result));
                        buildingModList.put(modId, mod);
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
