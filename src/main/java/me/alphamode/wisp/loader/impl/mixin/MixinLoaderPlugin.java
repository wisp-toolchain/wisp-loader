package me.alphamode.wisp.loader.impl.mixin;

import me.alphamode.wisp.loader.JarMod;
import me.alphamode.wisp.loader.api.Mod;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.impl.InternalPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.tomlj.TomlArray;

import java.lang.reflect.Method;
import java.util.Map;

public class MixinLoaderPlugin extends InternalPlugin {
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
    }

    @Override
    public void onFinish(Map<String, Mod> mods) {
        for (Mod mod : mods.values()) {
            if (mod instanceof JarMod jarMod && jarMod.getTomlInfo().contains("mixins")) {
                TomlArray mixins = jarMod.getTomlInfo().getArray("mixins");
                for (int i = 0; i < mixins.size(); i++) {
                    Mixins.addConfiguration(mixins.getString(i));
                }
            }
        }
    }
}