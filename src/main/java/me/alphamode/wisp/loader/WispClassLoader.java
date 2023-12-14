package me.alphamode.wisp.loader;

import me.alphamode.wisp.loader.api.ClassTransformer;
import me.alphamode.wisp.loader.api.Mod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WispClassLoader extends SecureClassLoader {

    private final ClassLoader parent;
    private final WispURLClassLoader urlLoader;
    public final Set<Path> validParentCodeSources = new HashSet<>();
    private volatile Set<Path> codeSources = Collections.emptySet();
    public final Set<String> parentSourcedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static class WispURLClassLoader extends URLClassLoader {

        static {
            registerAsParallelCapable();
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }

        @Override
        public Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) {
            return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
        }

        public WispURLClassLoader() {
            super(new URL[]{});
        }
    }

    public WispClassLoader() {
        super(new FallbackClassLoader());
        this.parent = getClass().getClassLoader();
        this.urlLoader = new WispURLClassLoader();
    }

    public void addUrls(URL[] urls) {
        for (URL url : urls)
            this.urlLoader.addURL(url);
    }

    public void addUrl(URL url) {
        this.urlLoader.addURL(url);
    }

    public void addMod(Mod mod) throws IOException {
        for (Path root : mod.getPaths())
            addUrl(root.toUri().toURL());
        for (Path path : mod.getPaths()) {

            synchronized (this) {
                Set<Path> codeSources = this.codeSources;
                if (codeSources.contains(path)) return;

                Set<Path> newCodeSources = new HashSet<>(codeSources.size() + 1, 1);
                newCodeSources.addAll(codeSources);
                newCodeSources.add(path);

                this.codeSources = newCodeSources;
            }
        }
    }

    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name);

        URL url = urlLoader.getResource(name);

        if (url == null) {
            url = parent.getResource(name);
        }

        return url;
    }

    @Override
    public URL findResource(String name) {
        Objects.requireNonNull(name);

        return urlLoader.findResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        Objects.requireNonNull(name);

        InputStream inputStream = urlLoader.getResourceAsStream(name);

        if (inputStream == null) {
            inputStream = parent.getResourceAsStream(name);
        }

        return inputStream;
    }
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);

        final Enumeration<URL> resources = urlLoader.getResources(name);

        if (!resources.hasMoreElements()) {
            return parent.getResources(name);
        }

        return resources;
    }

    public static String getClassFileName(String className) {
        return className.replace('.', '/').concat(".class");
    }

    @Override
    protected Class<?> findClass(String name) {
        return findClass(name, false);
    }

    protected Class<?> findClass(String name, boolean allowFromParent) {
        if (name.startsWith("java.")) {
            return null;
        }

        if (!allowFromParent && !parentSourcedClasses.isEmpty()) { // propagate loadIntoTarget behavior to its nested classes
            int pos = name.length();

            while ((pos = name.lastIndexOf('$', pos - 1)) > 0) {
                if (parentSourcedClasses.contains(name.substring(0, pos))) {
                    allowFromParent = true;
                    break;
                }
            }
        }

        byte[] input = getProcessedClassByteArray(name, allowFromParent);
        if (input == null) return null;

        // The class we're currently loading could have been loaded already during Mixin initialization triggered by `getPostMixinClassByteArray`.
        // If this is the case, we want to return the instance that was already defined to avoid attempting a duplicate definition.
        Class<?> existingClass = findLoadedClass(name);

        if (existingClass != null) {
            return existingClass;
        }

        if (allowFromParent) {
            parentSourcedClasses.add(name);
        }

//        int pkgDelimiterPos = name.lastIndexOf('.');
//
//        if (pkgDelimiterPos > 0) {
//            // TODO: package definition stub
//            String pkgString = name.substring(0, pkgDelimiterPos);
//
//            if (getPackage(pkgString) == null) {
//                try {
//                    urlLoader.definePackage(pkgString, null, null, null, null, null, null, null);
//                } catch (IllegalArgumentException e) { // presumably concurrent package definition
//                    if (getPackage(pkgString) == null) throw e; // still not defined?
//                }
//            }
//        }

        return defineClass(name, input, 0, input.length);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                if (name.startsWith("java.")) { // fast path for java.** (can only be loaded by the platform CL anyway)
                    c = PLATFORM_CLASS_LOADER.loadClass(name);
                } else {
                    c = findClass(name, false); // try local load

                    if (c == null) { // not available locally, try system class loader
                        String fileName = getClassFileName(name);
                        URL url = parent.getResource(fileName);

                        if (url == null) { // no .class file
                            try {
                                c = PLATFORM_CLASS_LOADER.loadClass(name);
                            } catch (ClassNotFoundException e) {
                                throw e;
                            }
                        } /*else if (!isValidParentUrl(url, fileName)) { // available, but restricted
                            // The class would technically be available, but the game provider restricted it from being
                            // loaded by setting validParentUrls and not including "url". Typical causes are:
                            // - accessing classes too early (game libs shouldn't be used until Loader is ready)
                            // - using jars that are only transient (deobfuscation input or pass-through installers)
                            String msg = String.format("can't load class %s at %s as it hasn't been exposed to the game",
                                    name, LibraryFinder.getCodeSource(url, fileName));
                            throw new ClassNotFoundException(msg);
                        } */else { // load from system cl
                            c = parent.loadClass(name);
                        }
                    }
                }
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    private final List<ClassTransformer> transformers = new ArrayList<>();

    public byte[] getProcessedClassByteArray(String name, boolean allowFromParent) {
        try {
            byte[] classBytes = getRawClassByteArray(name, allowFromParent);
            for (ClassTransformer transformer : transformers) {
                classBytes = transformer.transform(name, classBytes);
            }
            return classBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getRawClassByteArray(String name, boolean allowFromParent) throws IOException {
        name = getClassFileName(name);
        URL url = findResource(name);

        if (url == null) {
            if (!allowFromParent) return null;

            url = parent.getResource(name);

            if (!isValidParentUrl(url, name)) {
                return null;
            }
        }

        try (InputStream inputStream = url.openStream()) {
            int a = inputStream.available();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(a < 32 ? 32768 : a);
            byte[] buffer = new byte[8192];
            int len;

            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            return outputStream.toByteArray();
        }
    }

    public void registerClassTransformer(ClassTransformer transformer) {
        this.transformers.add(transformer);
    }

    public boolean isClassLoaded(String name) {
        synchronized (getClassLoadingLock(name)) {
            return findLoadedClass(name) != null;
        }
    }

    public Class<?> loadIntoTarget(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                c = loadClass(name, true);

                if (c == null) {
                    throw new ClassNotFoundException("can't find class "+name);
                }
            }

            resolveClass(c);

            return c;
        }
    }

    private static final ClassLoader PLATFORM_CLASS_LOADER = getPlatformClassLoader();

    private static boolean hasRegularCodeSource(URL url) {
        return url.getProtocol().equals("file") || url.getProtocol().equals("jar");
    }

    /**
     * Check if an url is loadable by the parent class loader.
     *
     * <p>This handles explicit parent url whitelisting by {@link #validParentCodeSources} or shadowing by {@link #codeSources}
     */
    private boolean isValidParentUrl(URL url, String fileName) {
        if (url == null) return false;
        if (!hasRegularCodeSource(url)) return true;

        Path codeSource = LibraryFinder.getCodeSource(url, fileName);
        Set<Path> validParentCodeSources = this.validParentCodeSources;

        if (validParentCodeSources != null) { // explicit whitelist (in addition to platform cl classes)
            return validParentCodeSources.contains(codeSource) || PLATFORM_CLASS_LOADER.getResource(fileName) != null;
        } else { // reject urls shadowed by this cl
            return !codeSources.contains(codeSource);
        }
    }
}