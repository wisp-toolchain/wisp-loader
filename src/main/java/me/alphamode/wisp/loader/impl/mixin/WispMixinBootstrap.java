package me.alphamode.wisp.loader.impl.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class WispMixinBootstrap implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "WispLoader";
    }

    @Override
    public String getServiceClassName() {
        return "me.alphamode.wisp.loader.mixin.WispMixinService";
    }

    @Override
    public void bootstrap() {

    }
}
