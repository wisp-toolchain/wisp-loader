package me.alphamode.wisp.loader.api;

import me.alphamode.wisp.loader.WispClassLoader;
import me.alphamode.wisp.loader.api.extension.Extension;
import me.alphamode.wisp.loader.api.extension.ExtensionType;
import me.alphamode.wisp.loader.impl.minecraft.ClientGameLocator;
import me.alphamode.wisp.loader.impl.minecraft.WispLoaderPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginContext {
    public static final WispClassLoader CLASS_LOADER = new WispClassLoader();
    private GameLocator locator;
    private final List<ClassTransformer> transformers = new ArrayList<>();
    private final Map<String, Mod> buildingModList = new HashMap<>();
    private final Map<String, Extension> extensions = new HashMap<>();
    private List<Path> classPath;
    private final ArgumentList argumentList;

    public PluginContext(ArgumentList argumentList) {
        this.argumentList = argumentList;
    }

    /**
     * Only use this if you know what you are doing!
     * <p>
     * This is used to provide a custom game jar, such as a patched version of minecraft.
     * Only one of these can exist. Registering one will override the default game locator.
     */
    public void registerGameLocator(GameLocator locator) {
        if (this.locator instanceof ClientGameLocator || this.locator == null)
            this.locator = locator;
        else
            throw new RuntimeException(String.format("Only one game locator can be registered! %s %s", locator, this.locator));
    }

    public GameLocator getLocator() {
        return this.locator;
    }

    public void registerClassTransformer(ClassTransformer transformer) {
        this.transformers.add(transformer);
    }

    public List<ClassTransformer> getTransformers() {
        return this.transformers;
    }

    public ArgumentList getArgumentList() {
        return this.argumentList;
    }

    public void setClassPath(List<Path> libs) {
        this.classPath = libs;
    }

    public List<Path> getClassPath() {
        return this.classPath;
    }

    public <Ext extends Extension> void registerExtension(ExtensionType<Ext> type, Ext extension) {
        extensions.put(type.getId(), extension);
    }

    public Map<String, ? extends Extension> getExtensions() {
        return extensions;
    }
}