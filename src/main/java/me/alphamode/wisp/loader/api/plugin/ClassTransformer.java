package me.alphamode.wisp.loader.api.plugin;

public interface ClassTransformer {
    byte[] transform(String name, byte[] classBytes);
}
