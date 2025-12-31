package me.alphamode.wisp.loader.minecraft;

import io.github.wasabithumb.jtoml.document.TomlDocument;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.api.components.TomlComponent;
import net.minecraft.world.entity.vehicle.minecart.Minecart;

import java.lang.reflect.InvocationTargetException;

public class EntrypointHook {
    public static void launchMods() {
        Minecart
        WispLoader.get().getMods().forEach((modId, mod) -> {
            if (mod.hasComponent(TomlComponent.class)) {
                TomlDocument modToml = mod.getComponent(TomlComponent.class).toml();
                if (modToml.contains("entrypoints") && modToml.get("entrypoints").asTable().contains("client") && modToml.get("entrypoints").asTable().get("client").asTable().contains("classes")) {
                    TomlArray classes = modToml.get("entrypoints").asTable().get("client").asTable().get("classes").asArray();
                    for (int i = 0; i < classes.size(); i++) {
                        WispLoader.LOGGER.warn("Loading class: " + classes.get(i).asPrimitive().asString());
                        try {
                            Class.forName(classes.get(i).asPrimitive().asString()).getDeclaredConstructor().newInstance();
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