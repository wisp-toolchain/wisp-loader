package me.alphamode.wisp.loader.api;

public interface ClassTransformer {
    byte[] transform(String name, byte[] classBytes);
}
