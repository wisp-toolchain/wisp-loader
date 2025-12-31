package me.alphamode.wisp.loader.mixin;

import me.alphamode.wisp.loader.api.plugin.ClassTransformer;

public class MixinClassTransformer implements ClassTransformer {
    @Override
    public byte[] transform(String name, byte[] classBytes) {
        return WispMixinService.getTransformer().transformClassBytes(name, name, classBytes);
    }
}
