package me.alphamode.wisp.loader;

import me.alphamode.wisp.loader.api.Mod;

import java.nio.file.Path;

public class JarMod implements Mod {
    private final Path jarPath;
    private final String modId, version;

    public JarMod(Path jarPath, String modId, String version) {
        this.jarPath = jarPath;
        this.modId = modId;
        this.version = version;
    }

    @Override
    public Path getPath() {
        return this.jarPath;
    }

    @Override
    public String getId() {
        return this.modId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }
}
