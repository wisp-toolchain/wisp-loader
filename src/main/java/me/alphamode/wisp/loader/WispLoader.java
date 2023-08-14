package me.alphamode.wisp.loader;

import com.mojang.logging.LogUtils;
import me.alphamode.wisp.loader.api.Mod;
import me.alphamode.wisp.loader.impl.WispLoaderImpl;
import me.alphamode.wisp.loader.impl.WispUtils;
import org.slf4j.Logger;

import java.util.Map;

public interface WispLoader {
    Logger LOGGER = LogUtils.getLogger();

    boolean isDevelopment();

    static WispLoader get() {
        return WispLoaderImpl.INSTANCE;
    }

    Map<String, Mod> getMods();
}
