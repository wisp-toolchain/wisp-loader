package me.alphamode.wisp.loader.minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Game locator for minecraft's bundler system
public class ServerGameLocator extends AbstractGameLocator {
    @Override
    public Path provide(List<Path> classPaths, String[] args) {
        return null;
    }

    @Override
    public List<Path> getClassPaths(String[] args) {
        return new ArrayList<>(); // 
    }

    @Override
    public String getMainClass() {
        return "net.minecraft.server.Main";
    }
}