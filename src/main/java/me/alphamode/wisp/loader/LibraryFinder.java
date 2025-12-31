package me.alphamode.wisp.loader;

import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

public class LibraryFinder {

    public static Path getCodeSource(URL url, String localPath) throws RuntimeException {
        try {
            URLConnection connection = url.openConnection();

            if (connection instanceof JarURLConnection) {
                return asPath(((JarURLConnection) connection).getJarFileURL());
            } else {
                String path = url.getPath();

                if (path.endsWith(localPath)) {
                    return asPath(new URL(url.getProtocol(), url.getHost(), url.getPort(), path.substring(0, path.length() - localPath.length())));
                } else {
                    throw new RuntimeException("Could not figure out code source for file '" + localPath + "' in URL '" + url + "'!");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getCodeSource(Class<?> cls) {
        CodeSource cs = cls.getProtectionDomain().getCodeSource();
        if (cs == null) return null;

        return asPath(cs.getLocation());
    }

    public static Path asPath(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
