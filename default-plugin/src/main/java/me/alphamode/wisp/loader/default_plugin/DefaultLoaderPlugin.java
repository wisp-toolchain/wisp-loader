package me.alphamode.wisp.loader.default_plugin;

import me.alphamode.wisp.loader.LibraryFinder;
import me.alphamode.wisp.loader.WispClassLoader;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.mod.LoadingMod;
import me.alphamode.wisp.loader.default_plugin.locator.DefaultModLocator;
import me.alphamode.wisp.loader.impl.InternalPlugin;
import org.tomlj.TomlParseResult;

import java.nio.file.Path;
import java.security.CodeSource;
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

    @Override
    public void modifyClassPath(List<Path> classPaths) {
        // Temp hack for now until I implement a system for removing loader depends from the game classpath
        CodeSource cs = TomlParseResult.class.getProtectionDomain().getCodeSource();
        if (cs == null) return;

        classPaths.remove(LibraryFinder.asPath(cs.getLocation()));
    }
}
