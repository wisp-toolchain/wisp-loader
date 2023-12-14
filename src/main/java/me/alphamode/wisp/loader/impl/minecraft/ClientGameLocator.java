package me.alphamode.wisp.loader.impl.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alphamode.wisp.loader.AddConstructorMappingVisitor;
import me.alphamode.wisp.loader.api.GameLocator;
import me.alphamode.wisp.loader.api.PluginContext;
import me.alphamode.wisp.loader.api.WispLoader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class ClientGameLocator extends AbstractGameLocator {
    private static final Gson GSON = new Gson();

    @Override
    public Path locateGame(List<Path> classPaths, String[] args) {
        Path foundGame = null;
        for (Path gameLib : classPaths) {
            if ((gameLib.toString().contains("client-mapped-") && gameLib.toString().endsWith(".jar")) || gameLib.toString().endsWith("merged.jar")) {
                return gameLib;
            }
//            if (gameLib.endsWith(args[3] + ".jar")) {
//                WispLoader.LOGGER.warn("Found client jar: " + gameLib);
//                foundGame = gameLib;
//            }
        }

        if (foundGame == null)
            return null;

        var mappedJar = Path.of(foundGame.toString().replace(args[3] + ".jar", args[3] + "-mapped.jar"));
        try {
            if (mappedJar.toFile().exists())
                return mappedJar;

            WispLoader.LOGGER.warn(foundGame.toString());
            try (JarFile gameJar = new JarFile(foundGame.toFile())) {
                JsonObject currentVersion = GSON.fromJson(new InputStreamReader(gameJar.getInputStream(gameJar.getJarEntry("version.json"))), JsonObject.class);
                String versionId = currentVersion.get("id").getAsString();

                JsonArray versions = GSON.fromJson(readString(new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")), JsonObject.class).getAsJsonArray("versions");

                JsonObject foundVersion = null;

                for (JsonElement ver : versions) {
                    JsonObject versionObj = ver.getAsJsonObject();

                    if (versionObj.get("id").getAsString().equals(versionId))
                        foundVersion = versionObj;
                }

                if (foundVersion == null)
                    throw new RuntimeException("Unable to find version: " + versionId);

                JsonObject version = GSON.fromJson(readString(new URL(foundVersion.get("url").getAsString())), JsonObject.class);
                remapClient(version, foundGame, mappedJar, classPaths);
                return mappedJar;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Path> getGameClassPaths(String[] args) {
        var gameLibsPaths = System.getProperty("java.class.path").split(File.pathSeparator);
        ArrayList<Path> gameLibs = new ArrayList<>();

        for (String gameLib : gameLibsPaths) {
            gameLibs.add(Path.of(gameLib));
        }

        var game = locateGame(gameLibs, args);
        if (game != null)
            gameLibs.add(game);

        return gameLibs;
    }

    private void remapClient(JsonObject version, Path clientPath, Path mappedClient, List<Path> classPaths) {
        Path mappingsProguard = mappedClient.getParent().resolve("client_mappings.txt");
        Path mappings = mappedClient.getParent().resolve("mojang.tiny");
        try {
            Files.write(mappingsProguard, new URL(version.getAsJsonObject("downloads").get("client_mappings").getAsJsonObject().get("url").getAsString()).openStream().readAllBytes());
            try (Writer writer = new StringWriter()) {
                MappingWriter mappingTree = new Tiny2Writer(writer, false);
                MappingSourceNsSwitch sourceNsSwitch = new MappingSourceNsSwitch(mappingTree, "named", true);
                AddConstructorMappingVisitor constructorMappingVisitor = new AddConstructorMappingVisitor(sourceNsSwitch);
                ProGuardReader.read(new FileReader(mappingsProguard.toFile()), "named", "official", constructorMappingVisitor);
                Files.write(mappings, writer.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings, "official", "named"))
                .rebuildSourceFilenames(true)
                .build();

        System.out.println("Remapping client");
        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(mappedClient)
                // force jar despite the .tmp extension
                .assumeArchive(true)
                .build()) {

            for (Path gameLib : classPaths) {
                remapper.readClassPath(gameLib);
            }

            outputConsumer.addNonClassFiles(clientPath, NonClassCopyMode.FIX_META_INF, remapper);
            remapper.readInputs(clientPath);

            remapper.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            remapper.finish();
        }
    }

    public static String readString(URL url) throws IOException {
        try (InputStream is = openUrl(url)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static InputStream openUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) throw new IOException("HTTP request to "+url+" failed: "+responseCode);

        return conn.getInputStream();
    }

    @Override
    public String getMainClass() {
        return "net.minecraft.client.main.Main";
    }
}