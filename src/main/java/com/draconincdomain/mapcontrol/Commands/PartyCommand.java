package com.draconincdomain.mapcontrol.Commands;

import com.draconincdomain.mapcontrol.Annotations.Commands;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import com.draconincdomain.mapcontrol.Objects.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Commands(name = "party", permission = "mapControl.admin", hasCooldown = true, cooldownDuration = 10)
public class PartyCommand extends CommandCore{

    @Override
    protected void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /party <create|invite|leave>");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "create":
                createParty(player, args);
                break;
            case "invite":
                invitePlayer(player, args);
                break;
            case "leave":
                leaveParty(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid party command.");
                break;
        }
    }

    private void createParty(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Party created!");
        Party existingParty = PartyManager.getInstance().findPlayerParty(player.getUniqueId());

        if (existingParty != null) {
            player.sendMessage(ChatColor.RED + "You are already in a party");
            return;
        }
        Party newParty = PartyManager.getInstance().createParty(player);
        player.sendMessage(ChatColor.GREEN + "Party has been created, you are the leader");
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /party invite <playerName>");
            return;
        }
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be the party leader to invite players.");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        PartyManager.getInstance().invitePlayer(party, target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + target.getName() + " has been invited to the party.");
    }

    private void leaveParty(Player player) {
        player.sendMessage(ChatColor.GREEN + "You left the party.");
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }
        PartyManager.getInstance().removePlayerFromParty(party, player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You left the party.");
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        List<String> completions = new ArrayList<>();
        if (strings.length == 1) {
            completions.add("create");
            completions.add("invite");
            completions.add("leave");
        }
        return completions;
    }
}
