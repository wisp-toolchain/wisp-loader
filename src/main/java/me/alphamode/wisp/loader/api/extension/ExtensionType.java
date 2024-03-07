package me.alphamode.wisp.loader.api.extension;

// TODO: Possible runtime class generation?
public abstract class ExtensionType<Ext extends Extension> {
    private final String id;
    private final Class<Ext> extensionClass;

    public ExtensionType(String id, Class<Ext> extensionClass) {
        this.id = id;
        this.extensionClass = extensionClass;
    }

    public String getId() {
        return this.id;
    }

    public Class<Ext> getExtensionClass() {
        return this.extensionClass;
    }
}
