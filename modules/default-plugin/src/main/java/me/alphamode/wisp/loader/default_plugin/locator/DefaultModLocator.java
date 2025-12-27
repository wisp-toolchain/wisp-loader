package me.alphamode.wisp.loader.default_plugin.locator;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.document.TomlDocument;
import me.alphamode.wisp.loader.LibraryFinder;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.api.components.ClasspathComponent;
import me.alphamode.wisp.loader.api.components.TomlComponent;
import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.ModLocator;
import me.alphamode.wisp.loader.impl.LoadingModImpl;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefaultModLocator implements ModLocator {
    @Override
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

    public void locateMods(Map<String, List<LoadingMod>> buildingModList) {
        JToml toml = JToml.jToml();
        for (Path path : locateMods()) {
            try {
                try (FileSystem mod = FileSystems.newFileSystem(path)) {
                    var modFile = mod.getPath("wisp.mod.toml");
                    if (Files.exists(modFile)) {
                        TomlDocument result = toml.read(modFile);
                        if (result.contains("mod-id")) {
                            String modId = result.get("mod-id").asPrimitive().asString(); // path, result
                            var jarMod = new LoadingModImpl(modId, result.get("version").asPrimitive().asString())
                                    .addComponent(new ClasspathComponent(path))
                                    .addComponent(new TomlComponent(result));
                            buildingModList.computeIfAbsent(modId, id -> new ArrayList<>()).add(jarMod);
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
                    TomlDocument result = toml.read(jarURLConnection.getInputStream());

                    if (result.contains("mod-id")) {
                        String modId = result.get("mod-id").asPrimitive().asString();
                        var mod = new LoadingModImpl(modId, result.get("version").asPrimitive().asString())
                                .addComponent(new ClasspathComponent(LibraryFinder.getCodeSource(url, "wisp.mod.toml")))
                                .addComponent(new TomlComponent(result));
                        buildingModList.computeIfAbsent(modId, id -> new ArrayList<>()).add(mod);
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
