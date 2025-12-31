package me.alphamode.wisp.loader.default_plugin;

import me.alphamode.wisp.loader.api.plugin.PluginContext;
import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.default_plugin.locator.DefaultModLocator;
import me.alphamode.wisp.loader.impl.InternalPlugin;

import java.util.List;
import java.util.Map;

public class DefaultLoaderPlugin extends InternalPlugin {
    private final DefaultModLocator locator = new DefaultModLocator();

    @Override
    public void init(PluginContext context) {
        locator.locateMods(context.getBuildingModList());
    }

    @Override
    public void resolveMods(Map<String, List<LoadingMod>> mods) {
        super.resolveMods(mods);
    }
}
