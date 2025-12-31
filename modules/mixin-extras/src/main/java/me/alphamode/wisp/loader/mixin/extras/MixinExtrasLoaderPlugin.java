package me.alphamode.wisp.loader.mixin.extras;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.alphamode.wisp.loader.api.plugin.LoaderPlugin;
import me.alphamode.wisp.loader.api.plugin.PluginContext;

public class MixinExtrasLoaderPlugin implements LoaderPlugin {
    @Override
    public void init(PluginContext context) {
        MixinExtrasBootstrap.init();
    }
}
