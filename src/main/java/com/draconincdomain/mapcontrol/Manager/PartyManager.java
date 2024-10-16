package com.draconincdomain.mapcontrol.Manager;

import com.draconincdomain.mapcontrol.Enums.PartyNotificationAlert;
import com.draconincdomain.mapcontrol.MapControl;
import com.draconincdomain.mapcontrol.Objects.InvitationRequest;
import com.draconincdomain.mapcontrol.Objects.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
        Party newParty = new Party(player.getName() + "'s Party", player.getUniqueId(), nextPartyId++);
        allParties.put(newParty.getPartyId(), newParty);
        return newParty;
    }

    public void invitePlayer(Party party, Player player) {
        InvitationRequest invitationRequest = new InvitationRequest(party.getLeader(), player.getUniqueId(), party);
        pendingInvitations.put(player.getUniqueId(), invitationRequest);

        player.sendMessage(ChatColor.GREEN + "You have been invited to join: " + party.getName());

        Bukkit.getScheduler().runTaskLater(MapControl.getInstance(), () -> {
            if (pendingInvitations.containsKey(player.getUniqueId())) {
                pendingInvitations.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Your invitation to join: " + party.getName() + " has expired");
            }
        }, 1200);
    }

    public void joinParty(Player player) {
        InvitationRequest invitationRequest = pendingInvitations.get(player.getUniqueId());

        if (invitationRequest != null && !invitationRequest.isExpired()) {
            Party party = findPlayerParty(invitationRequest.getInviter());
            if (party != null) {
                party.addMember(player.getUniqueId());
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
        party.removeMember(playerUUID);
    }

    public void disbandParty(Party party) {
        alertParty(party, PartyNotificationAlert.SERVER, "The party has been disbanded");
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
