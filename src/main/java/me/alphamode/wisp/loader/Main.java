package me.alphamode.wisp.loader;

import me.alphamode.wisp.loader.api.ArgumentList;
import me.alphamode.wisp.loader.api.GameLocator;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.impl.WispLoaderImpl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

public class Main {
    public static void main(String[] args) {
        WispLoader.LOGGER.info("Yooo its wisp loader");
        WispClassLoader classLoader = PluginContext.CLASS_LOADER;

        Thread.currentThread().setContextClassLoader(PluginContext.CLASS_LOADER);

        WispLoaderImpl loader = WispLoaderImpl.INSTANCE;

        ArgumentList argumentList = new ArgumentList(args);

        var gameLibs = loader.load(argumentList);

        GameLocator locator = loader.getLocator();

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

        loader.getPlugins().forEach((s, loaderPlugin) -> {
            loaderPlugin.onFinish(loader.getMods());
        });

        try {
            locator.launch(argumentList);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static String computeHash(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream output = Files.newInputStream(file)) {
            output.transferTo(new DigestOutputStream(OutputStream.nullOutputStream(), digest));
            return byteToHex(digest.digest());
        }
    }

    private static String byteToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);

        for(byte b : bytes) {
            result.append(Character.forDigit(b >> 4 & 15, 16));
            result.append(Character.forDigit(b >> 0 & 15, 16));
        }

        return result.toString();
    }
}
