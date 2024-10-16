package com.draconincdomain.mapcontrol.Commands;

import com.draconincdomain.mapcontrol.Annotations.Commands;
import com.draconincdomain.mapcontrol.Enums.PartyRoles;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import com.draconincdomain.mapcontrol.Objects.InvitationRequest;
import com.draconincdomain.mapcontrol.Objects.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Commands(name = "party", permission = "mapControl.default", hasCooldown = true, cooldownDuration = 2)
public class PartyCommand extends CommandCore {

    @Override
    protected void execute(Player player, String[] args) {
        if (args.length == 0) {
            displayPartyCommands(player);
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
            case "list":
                listParty(player);
                break;
            case "promote":
                promoteMember(player, args);
                break;
            case "demote":
                demoteMember(player, args);
                break;
            case "chat":
                handlePlayerChat(player, args);
                break;
            case "join":
                handleJoinParty(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid party command.");
                break;
        }
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {

    }

    private void displayPartyCommands(Player player) {
        StringBuilder message = new StringBuilder();

        message.append(ChatColor.GOLD).append("==========[ Party Commands ]===========\n");

        message.append(ChatColor.AQUA).append("/party create ")
                .append(ChatColor.GOLD).append("- Create a new party\n");

        message.append(ChatColor.AQUA).append("/party invite <playerName> ")
                .append(ChatColor.GOLD).append("- Invite a player to your party\n");

        message.append(ChatColor.AQUA).append("/party leave ")
                .append(ChatColor.GOLD).append("- Leave your current party\n");

        message.append(ChatColor.AQUA).append("/party list ")
                .append(ChatColor.GOLD).append("- List all members in your party\n");

        message.append(ChatColor.AQUA).append("/party promote <playerName> ")
                .append(ChatColor.GOLD).append("- Promote a member to party leader\n");

        message.append(ChatColor.AQUA).append("/party disband ")
                .append(ChatColor.GOLD).append("- Disband your party\n");

        message.append(ChatColor.AQUA).append("/party chat ")
                .append(ChatColor.GOLD).append("- Chat with your party members\n");

        message.append(ChatColor.AQUA).append("/party join ")
                .append(ChatColor.GOLD).append("- Joins a party\n");

        message.append(ChatColor.GOLD).append("====================================");

        player.sendMessage(message.toString());
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
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You must be in a party to use this command");
            return;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must be the party leader to invite players.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (PartyManager.getInstance().isPlayerInParty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Player is already in the party.");
            return;
        }

        PartyManager.getInstance().invitePlayer(party, target);
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

    private void listParty(Player player) {
        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GOLD + "==========[ Party Info ]===========\n");
        message.append(ChatColor.AQUA + "Party Name: " + ChatColor.DARK_AQUA + party.getName() + "\n");

        Player leader = Bukkit.getPlayer(party.getLeader());
        if (leader != null) {
            message.append(ChatColor.YELLOW + "Party Lead: " +  ChatColor.LIGHT_PURPLE + leader.getName() + "\n");
        }

        message.append(ChatColor.AQUA + "==========[ Party Members ]===========\n");
        for (UUID memberUUID : party.getPlayers().keySet()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                if (memberUUID.equals(party.getLeader())) {
                    message.append(ChatColor.DARK_GREEN + member.getName() + " - " + ChatColor.GOLD + party.getRole(memberUUID).toString() + "\n");
                } else {
                    message.append(ChatColor.GREEN + member.getName() + " - " + ChatColor.AQUA + party.getRole(memberUUID).toString() + "\n");
                }
            }
        }

        message.append(ChatColor.GOLD + "====================================");
        player.sendMessage(message.toString());
    }

    private void promoteMember(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /party promote <playerName>");
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        if (!party.isPartyLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the party leader can promote members.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !party.getPlayers().containsKey(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Player is not in your party.");
            return;
        }

        PartyRoles currentRole = party.getRole(target.getUniqueId());
        if (currentRole == PartyRoles.LEADER) {
            player.sendMessage(ChatColor.RED + "You cannot promote the leader.");
            return;
        }

        if (currentRole == PartyRoles.MEMBER) {
            party.promotePlayer(target.getUniqueId(), PartyRoles.OFFICER);
            player.sendMessage(ChatColor.GREEN + target.getName() + " has been promoted to Officer.");
        } else {
            player.sendMessage(ChatColor.RED + target.getName() + " is already an Officer.");
        }
    }

    private void demoteMember(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /party demote <playerName>");
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        if (!party.isPartyLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the party leader can demote members.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !party.getPlayers().containsKey(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Player is not in your party.");
            return;
        }

        PartyRoles currentRole = party.getRole(target.getUniqueId());
        if (currentRole == PartyRoles.OFFICER) {
            party.demotePlayer(target.getUniqueId(), PartyRoles.MEMBER);
            player.sendMessage(ChatColor.GREEN + target.getName() + " has been demoted to Member.");
        } else {
            player.sendMessage(ChatColor.RED + target.getName() + " is not an Officer.");
        }
    }

    private void handleJoinParty(Player player) {
        UUID playerId = player.getUniqueId();

        if (PartyManager.getInstance().getPendingInvitations().containsKey(playerId)) {
            PartyManager.getInstance().joinParty(player);
        }
    }

    private void handlePlayerChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /party chat <message>");
            return;
        }

        Party party = PartyManager.getInstance().findPlayerParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        sendPartyChatMessage(player, party, message);
    }

    private void sendPartyChatMessage(Player sender, Party party, String message) {
        String formattedMessage = ChatColor.DARK_AQUA + "[Party] " + ChatColor.GOLD + "[" + party.getRole(sender.getUniqueId()) + "] " + ChatColor.DARK_GREEN + sender.getName() + ": " + ChatColor.AQUA + message;

        party.getPlayers().forEach((memberUUID, role) -> {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(formattedMessage);
            }
        });
    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        List<String> completions = new ArrayList<>();
        if (strings.length == 1) {
            completions.add("create");
            completions.add("invite");
            completions.add("leave");
            completions.add("list");
            completions.add("promote");
            completions.add("demote");
            completions.add("chat");
        }
        return completions;
    }
}
