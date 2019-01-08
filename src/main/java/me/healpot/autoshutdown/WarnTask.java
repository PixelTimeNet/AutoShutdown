package me.healpot.autoshutdown;

import me.healpot.autoshutdown.misc.Log;
import me.healpot.autoshutdown.misc.Util;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WarnTask extends TimerTask {
    protected final AutoShutdownPlugin plugin;
    protected final Log log;
    protected long seconds = 0L;

    public WarnTask(AutoShutdownPlugin plugin, long seconds) {
        this.plugin = plugin;
        log = plugin.log;
        this.seconds = seconds;
    }

    public void run() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                if (TimeUnit.SECONDS.toMinutes(seconds) > 0L) {
                    if (TimeUnit.SECONDS.toMinutes(seconds) == 1L) {
                        if (seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)) == 0L)
                            Util.broadcast(plugin.shutdownMessage + "  in 1 minute ...");
                        else {
                            Util.broadcast(
                                    plugin.shutdownMessage + "  in 1 minute %d seconds ...",
                                    new Object[]{Long.valueOf(seconds
                                            - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)))});
                        }

                    } else if (seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)) == 0L) {
                        Util.broadcast(plugin.shutdownMessage + "  in %d minutes ...",
                                new Object[]{Long.valueOf(TimeUnit.SECONDS.toMinutes(seconds))});
                    } else {
                        Util.broadcast(
                                plugin.shutdownMessage + "  in %d minutes %d seconds ...",
                                new Object[]{
                                        Long.valueOf(TimeUnit.SECONDS.toMinutes(seconds)),
                                        Long.valueOf(seconds
                                                - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)))});
                    }

                } else if (TimeUnit.SECONDS.toSeconds(seconds) == 1L)
                    Util.broadcast(plugin.shutdownMessage + " NOW!");
                else
                    Util.broadcast(plugin.shutdownMessage + " in %d seconds ...",
                            new Object[]{Long.valueOf(seconds)});
            }
        });
    }
}