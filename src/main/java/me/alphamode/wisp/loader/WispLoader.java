package me.alphamode.wisp.loader;

import com.mojang.logging.LogUtils;
import me.alphamode.wisp.loader.api.GameLocator;
import me.alphamode.wisp.loader.api.LoaderMod;
import me.alphamode.wisp.loader.api.Mod;
import me.alphamode.wisp.loader.mixin.MixinMod;
import org.slf4j.Logger;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class WispLoader {
    private static WispLoader INSTANCE;
    public static final Logger LOGGER = LogUtils.getLogger();

    private final WispClassLoader classLoader = new WispClassLoader();
    public GameLocator locator;
    private final Map<String, Mod> mods = new LinkedHashMap<>();

    public WispLoader(String[] args) {
        INSTANCE = this;
        mods.put("wisp-loader", new LoaderMod());
        mods.put("mixin", new MixinMod());

        Map<String, Mod> buildingModList = new HashMap<>();

        locateMods(buildingModList);

        var gameLibsPaths = System.getProperty("java.class.path").split(File.pathSeparator);

        ArrayList<URL> gameLibs = new ArrayList<>();

        for (String gameLib : gameLibsPaths) {
            try {
                gameLibs.add(Path.of(gameLib).toFile().toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        buildingModList.forEach((s, mod) -> {
            Path modLocation = mod.getPath();
            if (modLocation != null) {
                try {
                    classLoader.addUrl(modLocation.toUri().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        LOGGER.info("Loading loader plugins");
        mods.forEach((modId, mod) -> {
            var plugin = mod.getLoaderPlugin();
            if (plugin != null) {
                LOGGER.info("   " + modId);
            }
        });

        mods.forEach((modId, mod) -> {
            var plugin = mod.getLoaderPlugin();
            if (plugin != null) {
                plugin.init();
                plugin.modifyMods(buildingModList);
                plugin.modifyClassPath(gameLibs);
                plugin.registerClassTransformer(classLoader);
            }
        });

        mods.putAll(buildingModList);

        gameLibs.add(locator.locateGame(gameLibs));


        classLoader.addUrls(gameLibs.toArray(URL[]::new));

        Thread.currentThread().setContextClassLoader(classLoader);

        LOGGER.info("Loading mods {}", mods.size());
        mods.forEach((s, mod) -> {
            LOGGER.info("   " + s);
        });

        try {
            MethodHandles.lookup().findStatic(Class.forName(locator.getMainClass(), true, classLoader), "main", MethodType.methodType(void.class, String[].class)).invokeExact(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void locateMods(Map<String, Mod> buildingModList) {
        Path modsFolder = Path.of("mods");

        try (Stream<Path> files = Files.list(modsFolder)) {
            for (Path path : files.toList()) {
                try (FileSystem mod = FileSystems.newFileSystem(path)) {
                    var modFile = mod.getPath("wisp.mod.toml");
                    if (Files.exists(modFile)) {
                        TomlParseResult result = Toml.parse(modFile);
                        result.errors().forEach(error -> System.err.println(error.toString()));
                        String modId = result.getString("mod-id");
                        buildingModList.put(modId, new JarMod(path, result));
                    }
                }
            }
        } catch (IOException ignored) {

        }
    }

    public WispClassLoader getClassLoader() {
        return classLoader;
    }

    public boolean isDevelopment() {
        return true;
    }

    public static WispLoader get() {
        return INSTANCE;
    }

    public Map<String, Mod> getMods() {
        return mods;
    }
}
