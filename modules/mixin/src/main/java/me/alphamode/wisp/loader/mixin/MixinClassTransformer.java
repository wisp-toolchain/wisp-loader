package me.alphamode.wisp.loader.mixin;

import me.alphamode.wisp.loader.api.plugin.ClassTransformer;

public class MixinClassTransformer implements ClassTransformer {
    @Override
    public byte[] transform(String name, byte[] classBytes) {
        if (classBytes == null || WispMixinService.getTransformer() == null)
            return null;
        return WispMixinService.getTransformer().transformClassBytes(name, name, classBytes);
    }
}
