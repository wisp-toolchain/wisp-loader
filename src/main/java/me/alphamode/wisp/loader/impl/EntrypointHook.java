package me.alphamode.wisp.loader.impl;

import me.alphamode.wisp.loader.JarMod;
import me.alphamode.wisp.loader.api.WispLoader;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import java.lang.reflect.InvocationTargetException;

public class EntrypointHook {
    public static void launchMods() {
        WispLoader.get().getMods().forEach((modId, mod) -> {
            if (mod instanceof JarMod jarMod) {
                TomlParseResult modToml = jarMod.getTomlInfo();
                if (modToml.contains("entrypoints") && modToml.getTable("entrypoints").contains("client") && modToml.getTable("entrypoints").getTable("client").contains("classes")) {
                    TomlArray classes = modToml.getTable("entrypoints").getTable("client").getArray("classes");
                    for (int i = 0; i < classes.size(); i++) {
                        WispLoader.LOGGER.warn("Loading class: " + classes.getString(i));
                        try {
                            Class.forName(classes.getString(i)).getDeclaredConstructor().newInstance();
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }
}