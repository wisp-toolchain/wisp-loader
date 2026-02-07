package me.alphamode.wisp.loader.minecraft;

import me.alphamode.wisp.loader.api.ArgumentList;
import me.alphamode.wisp.loader.api.plugin.Provider;
import me.alphamode.wisp.loader.api.plugin.PluginContext;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public abstract class AbstractGameLocator implements Provider {

    @Override
    public void launch(ArgumentList arguments) {
        try {
            MethodHandles.lookup().findStatic(PluginContext.CLASS_LOADER.loadClass(getMainClass()), "main", MethodType.methodType(Void.TYPE, String[].class)).asFixedArity().invokeExact(arguments.toArray());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
