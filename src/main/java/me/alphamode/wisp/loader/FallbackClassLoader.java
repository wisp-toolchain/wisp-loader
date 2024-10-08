package me.alphamode.wisp.loader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class FallbackClassLoader extends ClassLoader {
    private static final Enumeration<URL> NULL_ENUMERATION = new Enumeration<URL>() {
        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public URL nextElement() {
            return null;
        }
    };

    static {
        registerAsParallelCapable();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String var1) {
        return NULL_ENUMERATION;
    }
}