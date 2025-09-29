package me.alphamode.wisp.loader.mixin;

import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.api.mod.Mod;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.mixin.components.MixinComponent;
import me.alphamode.wisp.loader.api.components.TomlComponent;
import me.alphamode.wisp.loader.impl.InternalPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.tomlj.TomlArray;

import java.lang.reflect.Method;
import java.util.Map;

public class MixinLoaderPlugin extends InternalPlugin {
//    public static ExtensionType<WispMixinService> SERVICE_EXTENSION = new ExtensionType<>("mixin", WispMixinService.class) {};

    @Override
    public void init(PluginContext context) {
        System.setProperty("mixin.bootstrapService", WispMixinBootstrap.class.getName());
        System.setProperty("mixin.service", WispMixinService.class.getName());

        context.registerClassTransformer(new MixinClassTransformer());

        MixinEnvironment.CompatibilityLevel.MAX_SUPPORTED = MixinEnvironment.CompatibilityLevel.JAVA_18;

        MixinBootstrap.init();
        try {
            Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            m.setAccessible(true);
            m.invoke(null, MixinEnvironment.Phase.INIT);
            m.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        context.registerExtension(SERVICE_EXTENSION, (WispMixinService) WispMixinService.getTransformer());

    }

    @Override
    public void onModsFinalized(Map<String, LoadingMod> mods) {
        for (LoadingMod mod : mods.values()) {
            TomlComponent toml = mod.getComponent(TomlComponent.class);
            if (toml != null && toml.toml().contains("mixins")) {
                TomlArray mixins = toml.toml().getArray("mixins");
                mod.addComponent(new MixinComponent(mixins.toList().toArray(String[]::new)));
            }
        }
    }

    @Override
    public void onFinish(Map<String, Mod> mods) {
        for (Mod mod : mods.values()) {
            if (mod.hasComponent(MixinComponent.class)) {
                String[] mixins = mod.getComponent(MixinComponent.class).configs();
                for (String mixin : mixins) {
                    Mixins.addConfiguration(mixin);
                }
            }
        }
    }
}