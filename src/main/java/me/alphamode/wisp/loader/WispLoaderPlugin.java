package me.alphamode.wisp.loader;

import me.alphamode.wisp.loader.api.GameLocator;
import me.alphamode.wisp.loader.api.LoaderPlugin;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class WispLoaderPlugin implements LoaderPlugin {
    @Override
    public void init() {
        registerGameLocator(new MappedGameLocator());
    }

    @Override
    public void modifyClassPath(List<URL> classPaths) {

    }

    public static class MappedGameLocator implements GameLocator {

        @Override
        public URL locateGame(List<URL> classPaths) {
            try {
                Path clientJar = Path.of("/home/alpha/github/temp/WispLoader/src/main/resources/client.jar");

                Path mappingsPath = Path.of("/home/alpha/github/temp/WispLoader/src/main/resources/mojang.tiny");
                ;
//        var tinyMappings = TinyRemapperMappingsHelper.create(TinyMappingFactory.loadWithDetection(new BufferedReader(new FileReader(mappingsPath.toFile()))), "target", "source");
                var mappings = TinyRemapper.newRemapper()
                        .withMappings(TinyUtils.createTinyMappingProvider(mappingsPath, "target", "source"))
                        .rebuildSourceFilenames(true)
                        .build();

                Path mappedClient = Path.of("mapped.jar");
                OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(mappedClient)
                        // force jar despite the .tmp extension
                        .assumeArchive(true)
                        .build();

                for (URL gameLib : classPaths) {
                    var path = Path.of(gameLib.getPath());

//                LOGGER.error(gameLib);
                    mappings.readClassPath(path);
                }

                outputConsumer.addNonClassFiles(clientJar, NonClassCopyMode.FIX_META_INF, mappings);
                mappings.readInputs(clientJar);

                mappings.apply(outputConsumer);

                outputConsumer.close();
                mappings.finish();
                return mappedClient.toUri().toURL();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getMainClass() {
            return "net.minecraft.client.main.Main";
        }
    }
}