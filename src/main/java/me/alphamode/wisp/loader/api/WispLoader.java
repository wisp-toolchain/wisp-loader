package me.alphamode.wisp.loader.api;

import me.alphamode.wisp.loader.api.extension.Extension;
import me.alphamode.wisp.loader.api.extension.ExtensionType;
import me.alphamode.wisp.loader.api.mod.Mod;
import me.alphamode.wisp.loader.impl.WispLoaderImpl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

public interface WispLoader {
    Logger LOGGER = LoggerFactory.getLogger(WispLoader.class);

    /**
     * @return The active wisp loader implementation
     */
    static WispLoader get() {
        return WispLoaderImpl.INSTANCE;
    }

    boolean isDevelopment();

    boolean isServer();

    Path getRunDir();

    Map<String, Mod> getMods();

    @Nullable
    Mod getMod(String id);

    String getVersion();

    <Ext extends Extension> Ext getExtension(ExtensionType<Ext> type);
}