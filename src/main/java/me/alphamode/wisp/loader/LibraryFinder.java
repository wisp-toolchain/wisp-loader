package me.alphamode.wisp.loader;

import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryFinder {
    private static Path libsPath;

    public static Path findLibsPath() {
        if (libsPath == null) {
            libsPath = Path.of("/home/alpha/.minecraft/libraries/");//Path.of(System.getProperty("libraryDirectory","crazysnowmannonsense/cheezwhizz"));
            if (!Files.isDirectory(libsPath)) {
                throw new IllegalStateException("Missing libraryDirectory system property, cannot continue");
            }
        }
        return libsPath;
    }

    static String pathStatus(final Path path) {
        return Files.exists(path) ? "present" : "missing";
    }

    public static Path findPathForMaven(final String group, final String artifact, final String extension, final String classifier, final String version) {
        return findLibsPath().resolve(get(group, artifact, extension, classifier, version));
    }
    public static Path findPathForMaven(final String maven) {
        return findLibsPath().resolve(get(maven));
    }

    public static Path get(final String coordinate) {
        final String[] parts = coordinate.split(":");
        final String groupId = parts[0];
        final String artifactId = parts[1];
        final String classifier = parts.length > 3 ? parts[2] : "";
        final String[] versext = parts[parts.length-1].split("@");
        final String version = versext[0];
        final String extension = versext.length > 1 ? versext[1] : "";
        return get(groupId, artifactId, extension, classifier, version);
    }

    public static Path get(final String groupId, final String artifactId, final String extension, final String classifier, final String version) {
        final String fileName = artifactId + "-" + version +
                (!classifier.isEmpty() ? "-"+ classifier : "") +
                (!extension.isEmpty() ? "." + extension : ".jar");

        String[] groups = groupId.split("\\.");
        Path result = Paths.get(groups[0]);
        for (int i = 1; i < groups.length; i++) {
            result = result.resolve(groups[i]);
        }

        return result.resolve(artifactId).resolve(version).resolve(fileName);
    }

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

    public static Path asPath(URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
