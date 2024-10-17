package com.draconincdomain.mapcontrol.Commands;

import com.draconincdomain.mapcontrol.Annotations.Commands;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import com.draconincdomain.mapcontrol.Objects.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Commands(name = "aparty", permission = "mapcontrol.admin")
public class AdminPartyCommand extends CommandCore{
    @Override
    protected void execute(Player player, String[] args) {
        if (args.length == 0) {
            displayPartyCommands(player);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "list":
                displayPartyList(player);
                break;
            case "details":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /aparty details <partyId>");
                    return;
                }
                displayPartyDetails(player, args[1]);
                break;
            case "forcejoin":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /aparty forcejoin <partyId>");
                    return;
                }
                joinParty(player, args[1]);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid aparty command.");
                break;
        }
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {

    }

    private void displayPartyCommands(Player player) {
        StringBuilder message = new StringBuilder();

        message.append(ChatColor.GOLD).append("==========[ Party Commands ]===========\n");

        message.append(ChatColor.AQUA).append("/aparty list")
                .append(ChatColor.GOLD).append("- Create a new party\n");

        message.append(ChatColor.AQUA).append("/aparty details <partyID>")
                .append(ChatColor.GOLD).append("- Gives details of the target party\n");

        message.append(ChatColor.AQUA).append("/aparty forcejoin")
                .append(ChatColor.GOLD).append("- Force joins the target party\n");

        message.append(ChatColor.GOLD).append("====================================");

        player.sendMessage(message.toString());
    }

    private void displayPartyList(Player player) {
        StringBuilder message = new StringBuilder();

        message.append(ChatColor.GOLD).append("==========[ Party List ]===========\n");

        Map<Integer, Party> allParties = PartyManager.getInstance().getAllParties();

        if (allParties.isEmpty()) {
            message.append(ChatColor.RED).append("No active parties at the moment.\n");
        } else {
            for (Party party : allParties.values()) {
                Player leader = Bukkit.getPlayer(party.getLeader());
                message.append(ChatColor.AQUA)
                        .append("Party ID: ").append(ChatColor.DARK_AQUA).append(party.getPartyId())
                        .append(ChatColor.AQUA).append(" - Leader: ").append(ChatColor.GOLD).append(leader != null ? leader.getName() : "Unknown")
                        .append("\n");
            }
        }

        message.append(ChatColor.GOLD).append("====================================");

        player.sendMessage(message.toString());
    }

    private void displayPartyDetails(Player player, String partyIdStr) {
        int partyId;
        try {
            partyId = Integer.parseInt(partyIdStr);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid party ID.");
            return;
        }

        Party party = PartyManager.getInstance().getAllParties().get(partyId);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "Party not found.");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GOLD + "==========[ Party Details ]===========\n");

        message.append(ChatColor.DARK_GREEN + "Founder: " + ChatColor.GOLD + party.getFounderName() + "\n");
        message.append(ChatColor.YELLOW + "Founded Date: " + ChatColor.LIGHT_PURPLE + party.getFormattedDate() + "\n");

        message.append(ChatColor.GOLD + "==========[ Party Info ]===========\n");

        message.append(ChatColor.AQUA + "Party Name: " + ChatColor.DARK_AQUA + party.getName() + "\n");

        Player leader = Bukkit.getPlayer(party.getLeader());
        if (leader != null) {
            message.append(ChatColor.YELLOW + "Party Leader: " + ChatColor.LIGHT_PURPLE + leader.getName() + "\n");
        }

        message.append(ChatColor.GOLD + "==========[ Party Members ]===========\n");
        for (UUID memberUUID : party.getPlayers().keySet()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                message.append(ChatColor.DARK_GREEN + member.getName() + " - " + ChatColor.GOLD + party.getRole(memberUUID).toString() + "\n");
            }
        }

        message.append(ChatColor.GOLD + "====================================");
        player.sendMessage(message.toString());
    }

    private void joinParty(Player player, String partyIdStr) {
        int partyId;
        try {
            partyId = Integer.parseInt(partyIdStr);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid party ID.");
            return;
        }

        Party party = PartyManager.getInstance().getAllParties().get(partyId);

        if (party == null) {
            player.sendMessage(ChatColor.RED + "Party not found.");
            return;
        }

        if (party.getPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in this party");
            return;
        }

        if (PartyManager.getInstance().isPlayerInParty(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a party, leave before using this command");
            return;
        }

        PartyManager.getInstance().addPlayerToParty(party, player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You have joined the party: " + party.getName());
    }



    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        List<String> completions = new ArrayList<>();
        if (strings.length == 1) {
            completions.add("list");
            completions.add("details");
            completions.add("forcejoin");
        }
        return completions;
    }
}
