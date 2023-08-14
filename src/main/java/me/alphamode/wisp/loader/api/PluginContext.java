package me.alphamode.wisp.loader.api;

import me.alphamode.wisp.loader.WispClassLoader;
import me.alphamode.wisp.loader.WispLoaderPlugin;

import java.util.ArrayList;
import java.util.List;

public class PluginContext {
    public static final WispClassLoader CLASS_LOADER = new WispClassLoader();
    private GameLocator locator;
    private List<ClassTransformer> transformers = new ArrayList<>();

    /**
     * Only use this if you know what you are doing!
     * <p>
     * This is used to provide a custom game jar, such as a patched version of minecraft.
     * Only one of these can exist. Registering one will override the default game locator.
     */
    public void registerGameLocator(GameLocator locator) {
        if (this.locator instanceof WispLoaderPlugin.MappedGameLocator || this.locator == null)
            this.locator = locator;
        else
            throw new RuntimeException(String.format("Only one game locator can be registered! %s %s", locator, this.locator));
    }

    public GameLocator getLocator() {
        return locator;
    }

    public void registerClassTransformer(ClassTransformer transformer) {
        this.transformers.add(transformer);
    }

    public List<ClassTransformer> getTransformers() {
        return transformers;
    }

    public boolean isDevelopment() {
        return true;
    }

    public WispClassLoader getClassLoader() {
        return null;
    }
}
