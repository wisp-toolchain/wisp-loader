package me.alphamode.wisp.loader.impl.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;

public class WispMixinLogger implements ILogger {
    private final Logger logger;

    public WispMixinLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public String getId() {
        return this.logger.getName();
    }

    @Override
    public String getType() {
        return "slf4j";
    }

    @Override
    public void catching(Level level, Throwable t) {

    }

    @Override
    public void catching(Throwable t) {

    }

    @Override
    public void debug(String message, Object... params) {
        this.logger.debug(message, params);
    }

    @Override
    public void debug(String message, Throwable t) {
        this.logger.debug(message, t);
    }

    @Override
    public void error(String message, Object... params) {
        this.logger.error(message, params);
    }

    @Override
    public void error(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public void fatal(String message, Object... params) {
        this.logger.error(message, params);
    }

    @Override
    public void fatal(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public void info(String message, Object... params) {
        this.logger.info(message, params);
    }

    @Override
    public void info(String message, Throwable t) {
        this.logger.info(message, t);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        this.info(message, params);
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        this.info(message, t);
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        return null;
    }

    @Override
    public void trace(String message, Object... params) {
        this.logger.trace(message, params);
    }

    @Override
    public void trace(String message, Throwable t) {
        this.logger.trace(message, t);
    }

    @Override
    public void warn(String message, Object... params) {
        this.logger.warn(message, params);
    }

    @Override
    public void warn(String message, Throwable t) {
        this.logger.warn(message, t);
    }

    @Override
    public int hashCode() {
        return this.logger.hashCode();
    }
}
