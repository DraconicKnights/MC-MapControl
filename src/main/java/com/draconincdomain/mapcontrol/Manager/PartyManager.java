package com.draconincdomain.mapcontrol.Manager;

import com.draconincdomain.mapcontrol.Enums.PartyNotificationAlert;
import com.draconincdomain.mapcontrol.MapControl;
import com.draconincdomain.mapcontrol.Objects.InvitationRequest;
import com.draconincdomain.mapcontrol.Objects.Party;
import com.draconincdomain.mapcontrol.Objects.PartyMap;
import com.draconincdomain.mapcontrol.Utils.ColourUtil;
import com.draconincdomain.mapcontrol.Utils.ComponentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyManager {
    private static PartyManager Instance;
    private static Map<Integer, Party> allParties = new HashMap<>();
    private static Map<UUID, InvitationRequest> pendingInvitations = new HashMap<>();
    private static int nextPartyId = 1;

    public PartyManager() {
        Instance = this;
    }

    public Map<Integer, Party> getAllParties() {
        return allParties;
    }
    public Map<UUID, InvitationRequest> getPendingInvitations() {
        return pendingInvitations;
    }

    public Party createParty(Player player) {
        Party newParty = new Party(player.getName() + "'s Party", player.getUniqueId(), player.getName(), nextPartyId++, LocalDateTime.now());
        allParties.put(newParty.getPartyId(), newParty);
        return newParty;
    }

    public void invitePlayer(Party party, Player player) {
        InvitationRequest invitationRequest = new InvitationRequest(party.getLeader(), player.getUniqueId(), party);
        pendingInvitations.put(player.getUniqueId(), invitationRequest);

        Component message = ComponentBuilder.create("You have been invited to join: ", ColourUtil.fromEnum(ColourUtil.CustomColour.AQUA))
                .hover("[Click to join Party]")
                .click("/party join")
                .build();
        Component clickToJoin = ComponentBuilder.create("[Click to join]", ColourUtil.fromEnum(ColourUtil.CustomColour.AQUA))
                .hover("Click to join")
                .click("/party join")
                .build();

        player.sendMessage(message.append(clickToJoin));

        Bukkit.getScheduler().runTaskLater(MapControl.getInstance(), () -> {
            if (pendingInvitations.containsKey(player.getUniqueId())) {
                pendingInvitations.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Your invitation to join: " + party.getName() + " has expired");
                Player sender = Bukkit.getPlayer(party.getLeader());
                sender.sendMessage(ChatColor.RED + "Player: " + ChatColor.AQUA + player.getName() + ChatColor.RED + " Has not accepted the invitation");
            }
        }, 1200);
    }

    public void joinParty(Player player) {
        InvitationRequest invitationRequest = pendingInvitations.get(player.getUniqueId());

        if (invitationRequest != null && !invitationRequest.isExpired()) {
            Party party = invitationRequest.getParty();
            if (party != null) {
                addPlayerToParty(party, player.getUniqueId());
                pendingInvitations.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "you have joined: " + party.getName());
                alertParty(party, PartyNotificationAlert.INFO, player.getName() + " Has joined the party");
            } else {
                player.sendMessage(ChatColor.RED + "This party no longer exists");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Your invitation to this party has expired or no longer exists");
        }
    }

    public void acceptInvitation(Player leader, UUID invitee) {
        if (pendingInvitations.containsKey(invitee)) {
            InvitationRequest invitationRequest = pendingInvitations.get(invitee);
            if (invitationRequest.getInviter().equals(leader.getUniqueId())) {
                joinParty(Bukkit.getPlayer(invitee));
            }
        }
    }

    public void removePlayerFromParty(Party party, UUID playerUUID) {
        Map<PartyMap, Party> activeInstances = MapManager.getInstance().getActiveInstances();
        PartyMap partyMap = MapManager.getInstance().getActivePartyMapInstance(party);
        Player player = Bukkit.getPlayer(playerUUID);

        if (player != null) {
            if (activeInstances.containsKey(partyMap)) {
                Party activeParty = activeInstances.get(partyMap);

                if (activeParty != null && activeParty.getPlayers().containsKey(playerUUID)) {
                    World mainWorld = Bukkit.getWorld("world");
                    if (mainWorld != null) {
                        player.teleport(mainWorld.getSpawnLocation());
                        player.sendMessage(ChatColor.YELLOW + "You have been teleported back to the main world.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Main world not found.");
                    }
                }
            }

        }
        party.removeMember(playerUUID);

        if (party.getPlayers().isEmpty()) {
            if (partyMap != null) {
                MapManager.getInstance().cleanupMapInstance(partyMap);
                Bukkit.getLogger().info("Party is empty. Map instance for " + partyMap.getName() + " has been unloaded and cleaned up.");
            }
        }
    }

    public void addPlayerToParty(Party party, UUID playerUUID) {
        party.addMember(playerUUID);
    }

    public void disbandParty(Party party) {
        allParties.remove(party.getPartyId());
    }

    public void alertParty(Party party, PartyNotificationAlert alert, String message) {
        party.getPlayers().forEach((playerUUID, role) -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {

                switch (alert) {
                    case INFO -> player.sendMessage(ChatColor.GREEN + message);
                    case WARNING -> player.sendMessage(ChatColor.RED + message);
                    case SERVER -> player.sendMessage(ChatColor.BLUE + message);
                }
            }
        });
    }

    public Party findPlayerParty(UUID playerUUID) {
        for (Party party : getAllParties().values()) {
            if (party.getPlayers().containsKey(playerUUID)) {
                return party;
            }
        }
        return null;
    }

    public boolean isPlayerInParty(UUID playerUUID) {
        for (Party party : getAllParties().values()) {
            if (party.getPlayers().containsKey(playerUUID))
                return true;
        }
        return false;
    }
    public static PartyManager getInstance() {
        return Instance;
    }
}
