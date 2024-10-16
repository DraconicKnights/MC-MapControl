package com.draconincdomain.mapcontrol.Manager;

import com.draconincdomain.mapcontrol.Enums.PartyNotificationAlert;
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
    private static int nextPartyId = 1;

    public PartyManager() {
        Instance = this;
    }

    public Map<Integer, Party> getAllParties() {
        return allParties;
    }

    public void removeParty(Party party) {
        allParties.remove(party.getPartyId());
    }

    public Party createParty(Player player) {
        Party newParty = new Party(player.getName() + "'s Party", player.getUniqueId(), nextPartyId++);
        allParties.put(newParty.getPartyId(), newParty);
        return newParty;
    }

    public void invitePlayer(Party party, UUID playerUUID) {
        party.addMember(playerUUID);
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
    public static PartyManager getInstance() {
        return Instance;
    }
}
