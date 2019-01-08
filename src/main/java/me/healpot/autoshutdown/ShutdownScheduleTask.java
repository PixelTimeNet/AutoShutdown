package me.healpot.autoshutdown;

import me.healpot.autoshutdown.misc.Log;
import me.healpot.autoshutdown.misc.Util;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class ShutdownScheduleTask extends TimerTask {
    protected AutoShutdownPlugin plugin = null;
    protected Log log = null;

    ShutdownScheduleTask(AutoShutdownPlugin instance) {
        plugin = instance;
        log = plugin.log;
    }

    public void run() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                ShutdownScheduleTask.this.runTask();
            }
        });
    }

    private void runTask() {
        if (plugin.shutdownImminent == true) {
            return;
        }
        Calendar now = Calendar.getInstance();

        long firstWarning = ((Integer) plugin.warnTimes.get(0)).intValue() * 1000;

        for (Calendar cal : plugin.shutdownTimes)
            if (cal.getTimeInMillis() - now.getTimeInMillis() <= firstWarning) {
                plugin.shutdownImminent = true;
                plugin.shutdownTimer = new Timer();

                for (Integer warnTime : plugin.warnTimes) {
                    long longWarnTime = warnTime.longValue() * 1000L;

                    if (longWarnTime <= cal.getTimeInMillis() - now.getTimeInMillis()) {
                        plugin.shutdownTimer.schedule(new WarnTask(plugin, warnTime.longValue()), cal.getTimeInMillis()
                                - now.getTimeInMillis() - longWarnTime);
                    }

                }

                plugin.shutdownTimer.schedule(new ShutdownTask(plugin), cal.getTime());

                Util.broadcast(plugin.shutdownMessage + " at %s", new Object[]{cal.getTime().toString()});

                break;
            }
    }
}