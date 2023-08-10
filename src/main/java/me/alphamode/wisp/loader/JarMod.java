package me.alphamode.wisp.loader;

import me.alphamode.wisp.loader.api.Mod;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.nio.file.Path;

public class JarMod implements Mod {
    private final Path jarPath;
    private final String modId, version;
    private final TomlParseResult toml;

    public JarMod(Path jarPath, TomlParseResult toml) {
        this.jarPath = jarPath;
        this.modId = toml.getString("mod-id");
        this.version = toml.getString("version");
        this.toml = toml;
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

    public TomlParseResult getTomlInfo() {
        return this.toml;
    }
}
