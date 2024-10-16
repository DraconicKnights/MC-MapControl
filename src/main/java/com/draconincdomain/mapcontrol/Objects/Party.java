package com.draconincdomain.mapcontrol.Objects;

import com.draconincdomain.mapcontrol.Enums.PartyRoles;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Party implements Serializable {
    private String Name;
    private Map<UUID, PartyRoles> Players;
    private UUID Leader;
    private int partyId;

    public Party(String name, UUID leader, int partyId) {
        Name = name;
        this.Players = new HashMap<>();
        this.Leader = leader;
        this.partyId = partyId;
        this.Players.put(leader, PartyRoles.LEADER);
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public UUID getLeader() {
        return Leader;
    }

    public void setLeader(UUID leader) {
        if (Players.containsKey(leader)) {
            this.Players.put(this.Leader, PartyRoles.MEMBER);
            this.Leader = leader;
            this.Players.put(leader, PartyRoles.LEADER);
        }
    }

    private void promoteNextLeader() {
        for (UUID memberUUID : Players.keySet()) {
            if (Players.get(memberUUID) == PartyRoles.OFFICER || Players.get(memberUUID) == PartyRoles.MEMBER) {
                setLeader(memberUUID);
                break;
            }
        }
    }

    public Map<UUID, PartyRoles> getPlayers() {
        return Players;
    }

    public void addMember(UUID playerUUID) {
        Players.put(playerUUID, PartyRoles.MEMBER);
    }

    public void removeMember(UUID playerUUID) {
        if (playerUUID.equals(Leader) && !Players.isEmpty()) {
            promoteNextLeader();
        }

        if (!Players.isEmpty()) {
            partyDisband();
        }
        Players.remove(playerUUID);
    }

    public void partyDisband() {
        Players.clear();
        PartyManager.getInstance().disbandParty(this);
    }

    public PartyRoles getRole(UUID playerUUID) {
        return getPlayers().getOrDefault(playerUUID, PartyRoles.MEMBER);
    }

    public int getSize() {
        return this.getPlayers().size();
    }

    public int getPartyId() {
        return partyId;
    }

    public boolean isPartyLeader(UUID playerUUID) {
        return Leader.equals(playerUUID);
    }

    public void promotePlayer(UUID playerUUID, PartyRoles newRole) {
        if (getPlayers().containsKey(playerUUID)) {
            getPlayers().put(playerUUID, newRole);
        }
    }

    public void demotePlayer(UUID playerUUID, PartyRoles newRole) {
        if (getPlayers().containsKey(playerUUID)) {
            getPlayers().put(playerUUID, newRole);
        }
    }

}
