package me.alphamode.wisp.loader.minecraft;

import me.alphamode.wisp.loader.LibraryFinder;
import me.alphamode.wisp.loader.WispClassLoader;
import me.alphamode.wisp.loader.api.components.ClasspathComponent;
import me.alphamode.wisp.loader.api.mod.Mod;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.WispLoader;
import me.alphamode.wisp.loader.impl.InternalPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Map;

public class MinecraftLoaderPlugin extends InternalPlugin {

    @Override
    public void preInit(PluginContext context) {
        context.registerGameLocator(WispLoader.get().isServer() ? new ServerGameLocator() : new ClientGameLocator());
    }

    @Override
    public void init(PluginContext context) {
        context.registerClassTransformer(new EntrypointClassTransformer());
        context.registerClassTransformer(new WispBrandingPatch());

        CodeSource cs = WispClassLoader.class.getProtectionDomain().getCodeSource();
        if (cs == null) return;

        context.getClassPath().remove(LibraryFinder.asPath(cs.getLocation()));

//        if (WispLoader.get().isDevelopment()) {
//            context.getArgumentList().getArguments().remove("username");
//            context.getArgumentList().replace("version", "wisp-dev");
//            context.getArgumentList().replace("uuid", UUID.randomUUID().toString());
//        }
    }

    @Override
    public void onFinish(Map<String, Mod> mods) {
        mods.forEach((modId, mod) -> {
            for (ClasspathComponent c : mod.getComponents(ClasspathComponent.class)) {
                try {
                    PluginContext.CLASS_LOADER.addCodeSources(c.paths().toArray(Path[]::new));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}