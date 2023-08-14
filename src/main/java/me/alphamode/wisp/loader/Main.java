package me.alphamode.wisp.loader;

import com.google.common.collect.ImmutableMap;
import me.alphamode.wisp.loader.api.*;
import me.alphamode.wisp.loader.impl.WispLoaderImpl;
import me.alphamode.wisp.loader.impl.WispUtils;
import me.alphamode.wisp.loader.impl.mixin.MixinLoaderPlugin;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.JarURLConnection;
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

public class Main {
    public static void main(String[] args) {
        WispLoader.LOGGER.info("Yooo its wisp loader");
        WispClassLoader classLoader = PluginContext.CLASS_LOADER;

        var gameLibsPaths = System.getProperty("java.class.path").split(File.pathSeparator);

        Thread.currentThread().setContextClassLoader(PluginContext.CLASS_LOADER);

        WispLoaderImpl loader = WispLoaderImpl.INSTANCE;

        ArrayList<Path> gameLibs = new ArrayList<>();

        for (String gameLib : gameLibsPaths) {
            gameLibs.add(Path.of(gameLib));
        }

        loader.load(gameLibs);

        GameLocator locator = loader.getLocator();

        Path gameLib = locator.locateGame(gameLibs, args);
        if (gameLib != null)
            gameLibs.add(gameLib);

        classLoader.addUrls(gameLibs.stream().map(path -> {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new));



        WispLoader.LOGGER.info("Loading mods {}", loader.getMods().size());
        loader.getMods().forEach((s, mod) -> {
            WispLoader.LOGGER.info("   " + s);
        });

        try {
            MethodHandles.lookup().findStatic(PluginContext.CLASS_LOADER.loadClass(locator.getMainClass()), "main", MethodType.methodType(void.class, String[].class)).invokeExact(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
