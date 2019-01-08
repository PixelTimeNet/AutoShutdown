package me.healpot.autoshutdown.misc;

import java.util.logging.Logger;

public class Log {
    private final Logger log;
    private final String pluginName;

    public Log(String pluginName) {
        this.pluginName = pluginName;
        log = Logger.getLogger("Minecraft." + pluginName);
    }

    public void info(String msg) {
        log.info(String.format("[%s] %s", new Object[]{pluginName, msg}));
    }

    public void info(String format, Object[] args) {
        info(String.format(format, args));
    }

    public void warning(String msg) {
        log.warning(String.format("[%s] %s", new Object[]{pluginName, msg}));
    }

    public void warning(String format, Object[] args) {
        warning(String.format(format, args));
    }

    public void severe(String msg) {
        log.severe(String.format("[%s] %s", new Object[]{pluginName, msg}));
    }

    public void severe(String format, Object[] args) {
        severe(String.format(format, args));
    }
}