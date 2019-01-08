package me.healpot.autoshutdown;

import me.healpot.autoshutdown.misc.Log;
import me.healpot.autoshutdown.misc.Util;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.*;

public class AutoShutdownPlugin extends JavaPlugin {
    public String pluginName = "AutoShutdown";
    public final Log log = new Log(pluginName);
    public PluginDescriptionFile pdf = null;
    protected ShutdownScheduleTask task = null;
    protected Timer backgroundTimer = null;
    protected Timer shutdownTimer = null;
    protected BukkitScheduler scheduler = null;
    protected boolean shutdownImminent = false;
    protected TreeSet<Calendar> shutdownTimes = new TreeSet();
    protected ArrayList<Integer> warnTimes = new ArrayList();
    protected String shutdownReason = "Scheduled Shutdown";
    protected String shutdownMessage;

    File propFile = null;

    public void onDisable() {
        shutdownImminent = false;

        if (backgroundTimer != null) {
            backgroundTimer.cancel();
            backgroundTimer.purge();
            backgroundTimer = null;
        }

        if (shutdownTimer != null) {
            shutdownTimer.cancel();
            shutdownTimer.purge();
            shutdownTimer = null;
        }

        log.info("Version %s disabled.", new Object[]{pdf.getVersion()});
    }

    public void onEnable() {
        pdf = getDescription();
        log.info("update " + pdf.getVersion().toString() + " by fishqq");
        scheduler = getServer().getScheduler();
        shutdownImminent = false;
        shutdownTimes.clear();
        this.shutdownMessage = getConfig().getString("shutdownmessage");

        getConfig();
        saveDefaultConfig();

        loadConfiguration();

        CommandExecutor autoShutdownCommandExecutor = new AutoShutdownCommand(this);
        getCommand("autoshutdown").setExecutor(autoShutdownCommandExecutor);
        getCommand("as").setExecutor(autoShutdownCommandExecutor);

        scheduleAll();

        Util.init(this, pluginName, log);

        if (backgroundTimer != null) {
            backgroundTimer.cancel();
            backgroundTimer.purge();
            backgroundTimer = null;
        }

        backgroundTimer = new Timer();

        if (shutdownTimer != null) {
            shutdownTimer.cancel();
            shutdownTimer.purge();
            shutdownTimer = null;
        }

        Calendar now = Calendar.getInstance();
        now.set(13, 0);
        now.add(12, 1);

        now.add(14, 50);
        try {
            backgroundTimer.scheduleAtFixedRate(new ShutdownScheduleTask(this), now.getTime(), 60000L);
        } catch (Exception e) {
            log.severe("Failed to schedule AutoShutdownTask: %s", new Object[]{e.getMessage()});
        }

        saveConfig();
    }

    protected void loadConfiguration() {
        getConfig().options().copyDefaults(true);
        getConfig().getString("shutdowntimes", "02:00,14:00");
        getConfig().getString("warntimes", "900,600,300,240,180,120,60,45,30,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1");
        getConfig().getBoolean("kickonshutdown", true);
        getConfig().getString("kickreason", "Scheduled Shutdown.");
        getConfig().getString("shutdownmessage", "Server is shutting down");
        getConfig().getInt("gracetime", 20);
    }

    protected void scheduleAll() {
        shutdownTimes.clear();
        warnTimes.clear();

        String[] shutdownTimeStrings = null;
        try {
            shutdownTimeStrings = getConfig().getString("shutdowntimes").split(",");
        } catch (Exception e) {
            shutdownTimeStrings[0] = getConfig().getString("shutdowntimes");
        }
        try {
            for (String timeString : shutdownTimeStrings) {
                Calendar cal = scheduleShutdownTime(timeString);
                log.info("Shutdown scheduled for %s", new Object[]{cal.getTime().toString()});
            }
            String[] strings = getConfig().getString("warntimes").split(",");
            for (String warnTime : strings)
                warnTimes.add(Integer.decode(warnTime));
        } catch (Exception e) {
            log.severe("Unable to configure Auto Shutdown using the configuration file.");
            log.severe("Is the format of shutdowntimes correct? It should be only HH:MM.");
            log.severe("Error: %s", new Object[]{e.getMessage()});
        }
    }

    protected Calendar scheduleShutdownTime(String timeSpec) throws Exception {
        if (timeSpec == null) {
            return null;
        }
        if (timeSpec.matches("^now$")) {
            Calendar now = Calendar.getInstance();
            int secondsToWait = getConfig().getInt("gracetime", 30);
            now.add(13, secondsToWait);

            shutdownImminent = true;
            shutdownTimer = new Timer();

            for (Integer warnTime : warnTimes) {
                long longWarnTime = warnTime.longValue() * 1000L;

                if (longWarnTime <= secondsToWait * 1000) {
                    shutdownTimer.schedule(new WarnTask(this, warnTime.longValue()), secondsToWait * 1000
                            - longWarnTime);
                }

            }

            shutdownTimer.schedule(new ShutdownTask(this), now.getTime());
            Util.broadcast("The server has been scheduled for immediate shutdown.");

            return now;
        }

        if (!timeSpec.matches("^[0-9]{1,2}:[0-9]{2}$")) {
            throw new Exception("Incorrect time specification. The format is HH:MM in 24h time.");
        }

        Calendar now = Calendar.getInstance();
        Calendar shutdownTime = Calendar.getInstance();

        String[] timecomponent = timeSpec.split(":");
        shutdownTime.set(11, Integer.valueOf(timecomponent[0]).intValue());
        shutdownTime.set(12, Integer.valueOf(timecomponent[1]).intValue());
        shutdownTime.set(13, 0);
        shutdownTime.set(14, 0);

        if (now.compareTo(shutdownTime) >= 0) {
            shutdownTime.add(5, 1);
        }

        shutdownTimes.add(shutdownTime);

        return shutdownTime;
    }

    protected void kickAll() {
        if (!(getConfig().getBoolean("kickonshutdown", true))) {
            return;
        }

        log.info("Kicking all players ...");

        Collection<? extends Player> players = getServer().getOnlinePlayers();

        for (Player player : players) {
            log.info("Kicking player %s.", new Object[]{player.getName()});
            player.kickPlayer(getConfig().getString("kickreason"));
        }
    }
}