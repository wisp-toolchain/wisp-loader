package me.alphamode.wisp.loader.mixin;

import me.alphamode.wisp.loader.WispLoader;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

public class WispMixinService implements IMixinService, IClassProvider {
    @Override
    public String getName() {
        return "WispLoader";
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void prepare() {

    }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return null;
    }

    @Override
    public void offer(IMixinInternal internal) {
//        if (internal instanceof IMixinTransformerFactory) {
//            transformer = ((IMixinTransformerFactory) internal).createTransformer();
//        }
    }

    @Override
    public void init() {

    }

    @Override
    public void beginPhase() {

    }

    @Override
    public void checkEnv(Object bootSource) {

    }

    @Override
    public ReEntranceLock getReEntranceLock() {
        return null;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return null;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return null;
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return null;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return null;
    }

    @Override
    public String getSideName() {
        return null;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_6;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_18;
    }

    @Override
    public ILogger getLogger(String name) {
        return null;
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return WispLoader.get();
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return null;
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return null;
    }
}
