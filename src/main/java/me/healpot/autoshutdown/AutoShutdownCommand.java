package me.healpot.autoshutdown;

import me.healpot.autoshutdown.misc.Log;
import me.healpot.autoshutdown.misc.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;

public class AutoShutdownCommand implements CommandExecutor {
    private final AutoShutdownPlugin plugin;
    private final Log log;

    public AutoShutdownCommand(AutoShutdownPlugin plugin) {
        this.plugin = plugin;
        log = plugin.log;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (((sender instanceof Player)) && (!((Player) sender).hasPermission("autoshutdown.admin"))) {
            Util.replyError(sender, "You don't have permission to use that command.");
            return true;
        }

        if (args.length == 0) {
            args = new String[]{"HELP"};
            // Util.replyError(sender,
            // "Not enough arguments to command. Use /as help to list available commands.");
            // return true;
        }

        switch (SubCommand.toSubCommand(args[0].toUpperCase()).ordinal()) {
            case 0:
                Util.reply(sender, "AutoShutdown plugin help:");
                Util.reply(sender, " /%s help", new Object[]{command.getName()});
                Util.reply(sender, "     Shows this help page");
                Util.reply(sender, " /%s reload", new Object[]{command.getName()});
                Util.reply(sender, "     Reloads the configuration file");
                Util.reply(sender, " /%s cancel", new Object[]{command.getName()});
                Util.reply(sender, "     Cancels the currently executing shutdown");
                Util.reply(sender, " /%s set HH:MM:SS", new Object[]{command.getName()});
                Util.reply(sender, "     Sets a new scheduled shutdown time");
                Util.reply(sender, " /%s set now", new Object[]{command.getName()});
                Util.reply(sender, "     Orders the server to shutdown immediately");
                Util.reply(sender, " /%s list", new Object[]{command.getName()});
                Util.reply(sender, "     lists the currently scheduled shutdowns");
                break;
            case 1:
                plugin.reloadConfig();
                plugin.scheduleAll();
                Util.reply(sender, "Configuration reloaded.");
                break;
            case 2:
                if (plugin.shutdownTimer != null) {
                    plugin.shutdownTimer.cancel();
                    plugin.shutdownTimer.purge();
                    plugin.shutdownTimer = null;
                    plugin.shutdownImminent = false;

                    Util.reply(sender, "Shutdown was aborted.");
                } else {
                    Util.replyError(sender, "There is no impending shutdown. If you wish to remove");
                    Util.replyError(sender, "a scheduled shutdown, remove it from the configuration");
                    Util.replyError(sender, "and reload.");
                }
                break;
            case 3:
                if (args.length < 2) {
                    Util.replyError(sender, "Usage:");
                    Util.replyError(sender, "   /as set <time>");
                    Util.replyError(sender, "<time> can be either 'now' or a 24h time in HH:MM format.");
                    return true;
                }

                Calendar stopTime = null;
                try {
                    stopTime = plugin.scheduleShutdownTime(args[1]);
                } catch (Exception e) {
                    Util.replyError(sender, "Usage:");
                    Util.replyError(sender, "   /as set <time>");
                    Util.replyError(sender, "<time> can be either 'now' or a 24h time in HH:MM format.");
                }
                if (stopTime != null) {
                    Util.reply(sender, "Shutdown scheduled for %s", new Object[]{stopTime.getTime().toString()});
                }
                String timeString = "";

                for (Calendar shutdownTime : plugin.shutdownTimes) {
                    if (((Calendar) plugin.shutdownTimes.first()).equals(shutdownTime))
                        timeString = timeString
                                .concat(String.format("%d:%02d", new Object[]{Integer.valueOf(shutdownTime.get(11)),
                                        Integer.valueOf(shutdownTime.get(12))}));
                    else {
                        timeString = timeString
                                .concat(String.format(",%d:%02d", new Object[]{Integer.valueOf(shutdownTime.get(11)),
                                        Integer.valueOf(shutdownTime.get(12))}));
                    }
                }

                plugin.getConfig().set("shutdowntimes", timeString);
                try {
                    plugin.saveConfig();
                } catch (Exception e) {
                    Util.replyError(sender, "Unable to save configuration: %s", new Object[]{e.getMessage()});
                }
                break;
            case 4:
                if (plugin.shutdownTimes.size() != 0) {
                    Util.reply(sender, "Shutdowns scheduled at");
                    for (Calendar shutdownTime : plugin.shutdownTimes)
                        Util.reply(sender, "   %s", new Object[]{shutdownTime.getTime().toString()});
                } else {
                    Util.replyError(sender, "No shutdowns scheduled.");
                }
                break;
            case 5:
                Util.replyError(sender, "Unknown command. Use /as help to list available commands.");
        }

        return true;
    }

    private void reply(CommandSender sender, String message) {
        if (sender == null)
            log.info(message);
        else
            sender.sendMessage(message);
    }

    static enum SubCommand {
        HELP, RELOAD, CANCEL, SET, LIST, UNKNOWN;

        private static SubCommand toSubCommand(String str) {
            try {
                return valueOf(str);
            } catch (Exception ex) {
            }
            return HELP;
        }
    }
}