package com.draconincdomain.mapcontrol.Commands;

import com.draconincdomain.mapcontrol.MapControl;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class CommandCore implements CommandExecutor, TabExecutor {
    protected String commandName;
    protected String permission;
    protected boolean requiresPlayer;
    protected boolean hasCooldown;
    protected int cooldownDuration;
    protected Map<UUID, Long> cooldowns = new HashMap<>();

    public CommandCore() {

    }

    public void register(String commandName, String permission, boolean requiresPlayer, boolean hasCooldown, int cooldownDuration) {
        this.commandName = commandName;
        this.permission = permission;
        this.requiresPlayer = requiresPlayer;
        this.hasCooldown = hasCooldown;
        this.cooldownDuration = cooldownDuration;

        MapControl.getInstance().getCommand(commandName).setExecutor(this);
        MapControl.getInstance().getCommand(commandName).setTabCompleter(this);
    }

    protected abstract void execute(Player player, String[] args);
    protected abstract List<String> commandCompletion(Player player, Command command, String[] strings);
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            if (!player.hasPermission(this.permission)) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command, please contact a server administrator");
                return true;
            }

            if (hasCooldown) {
                UUID playerID = player.getUniqueId();
                if (cooldowns.containsKey(playerID)) {
                    long cooldownEnds = cooldowns.get(playerID);
                    if (cooldownEnds > System.currentTimeMillis()) {
                        return true;
                    }
                }
                cooldowns.put(playerID, System.currentTimeMillis() + cooldownDuration * 1000);
            }

            execute(player, strings);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return commandCompletion((Player) commandSender, command, strings);
    }
}
