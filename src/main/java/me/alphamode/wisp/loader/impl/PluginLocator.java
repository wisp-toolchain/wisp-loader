package me.alphamode.wisp.loader.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import me.alphamode.wisp.loader.LibraryFinder;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.mod.ModLocator;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PluginLocator implements ModLocator {
    public Map<String, String> locatePlugins() {
        JToml toml = JToml.jToml();
        Map<String, String> plugins = new HashMap<>();
        for (Path path : locateMods()) {
            try (FileSystem mod = FileSystems.newFileSystem(path)) {
                var modFile = mod.getPath("wisp.mod.toml");
                if (Files.exists(modFile)) {
                    TomlDocument result = toml.read(modFile);
                    if (result.contains("plugin-id")) {
                        String modId = result.get("plugin-id").asPrimitive().asString();
                        plugins.put(modId, result.get("plugin").asPrimitive().asString());
                        PluginContext.CLASS_LOADER.addCodeSources(path);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            PluginLocator.class.getClassLoader().getResources("wisp.mod.toml").asIterator().forEachRemaining(url -> {
                try {
                    URLConnection jarURLConnection = url.openConnection();
                    TomlDocument result = toml.read(jarURLConnection.getInputStream());

                    if (result.contains("plugin-id")) {
                        String pluginId = result.get("plugin-id").asPrimitive().asString();
                        String plugin = result.get("plugin").asPrimitive().asString();
                        plugins.put(pluginId, plugin);
                        PluginContext.CLASS_LOADER.addCodeSources(LibraryFinder.getCodeSource(Class.forName(plugin, false, PluginLocator.class.getClassLoader())));
//                        PluginContext.CLASS_LOADER.addCodeSources(LibraryFinder.getCodeSource(url, "wisp.mod.toml"));
                    }
                } catch (IOException | ClassNotFoundException e) {
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
}
