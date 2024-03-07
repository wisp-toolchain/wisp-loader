package me.alphamode.wisp.loader.api;

import com.mojang.logging.LogUtils;
import me.alphamode.wisp.env.Environment;
import me.alphamode.wisp.loader.impl.WispLoaderImpl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;

public interface WispLoader {
    Logger LOGGER = LogUtils.getLogger();

    boolean isDevelopment();

    boolean isServer();

    Environment getEnvironment();

    static WispLoader get() {
        return WispLoaderImpl.INSTANCE;
    }

    Path getGameDir();

    Map<String, Mod> getMods();

    @Nullable
    Mod getMod(String id);

    String getVersion();
}