package me.alphamode.wisp.loader.api.mod;

import me.alphamode.wisp.loader.api.plugin.PluginContext;

import java.nio.file.Path;
import java.util.List;

/**
 * Mods should be loaded through {@link PluginContext#getBuildingModList()} during the init phase
 */
@Deprecated(forRemoval = true)
public interface ModLocator {
    List<Path> locateMods();
}
