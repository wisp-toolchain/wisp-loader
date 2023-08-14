package me.alphamode.wisp.loader.impl.mixin;

import me.alphamode.wisp.loader.api.ClassTransformer;

public class MixinClassTransformer implements ClassTransformer {
    @Override
    public byte[] transform(String name, byte[] classBytes) {
        return WispMixinService.getTransformer().transformClassBytes(name, name, classBytes);
    }
}
