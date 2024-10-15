package com.draconincdomain.mapcontrol.Manager;

import com.draconincdomain.mapcontrol.Objects.Party;
import com.draconincdomain.mapcontrol.Objects.PartyMap;
import com.draconincdomain.mapcontrol.Enums.PartyNotificationAlert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class MapManager {
    private final MapManager Instance;
    private final Map<PartyMap, Party> activeInstances = new HashMap<>();
    private final List<PartyMap> allMaps = new ArrayList<>();

    public MapManager() {
        this.Instance = this;
    }

    public List<PartyMap> getAllMaps() {
        return allMaps;
    }

    public PartyMap getMapByName(String name) {
        for (PartyMap map : allMaps) {
            if (map.getName().equalsIgnoreCase(name)) {
                return map;
            }
        }
        return null;
    }

    public void createNewMapInstance(String mapName, Party party) {
        PartyMap selectedMap = getMapByName(mapName);

        if (selectedMap == null) {
            alertParty(party, PartyNotificationAlert.WARNING, "Map not found");
        }

        if (!party.isPartyLeader(party.getLeader())) {
            Player leader = Bukkit.getPlayer(party.getLeader());
            if (leader != null) {
                leader.sendMessage(ChatColor.RED + "Only the part leader can start a map");
            }
            return;
        }

        if (party.getSize() > selectedMap.getMaxPlayers()) {
            alertParty(party, PartyNotificationAlert.WARNING, "Too many players for this map");
            return;
        }

        World world = selectedMap.loadWorld("map_instance_" + selectedMap.getName() + "_" + System.currentTimeMillis());
        selectedMap.setMaxPlayers(party.getSize());

        party.getPlayers().forEach((playerUUID, role) -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.teleport(world.getSpawnLocation());
                player.sendMessage(ChatColor.AQUA + "You have joined map: " + selectedMap.getName());
            }
        });

        //List<UUID> playerUUIDs = players.stream().map(Player::getUniqueId).distinct().collect(Collectors.toList());

        activeInstances.put(selectedMap, party);
    }

    public void cleanupMapInstance(PartyMap map) {
        World world = map.getWorld();
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            world.getWorldFolder().delete();
        }
        activeInstances.remove(map);
    }

    public void playerRemoval(PartyMap targetMap, Player player) {
        Party party = activeInstances.get(targetMap);
        if (party != null) {
            UUID playerUUID = player.getUniqueId();
            party.getPlayers().remove(playerUUID);
            player.sendMessage(ChatColor.YELLOW + "You have been removed from the map: " + targetMap.getName());

            // If party is empty remove and clean up map
            if (party.getSize() == 0) {
                cleanupMapInstance(targetMap);
            }
        }
    }

   private void alertParty(Party party, PartyNotificationAlert alert, String message) {
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
}
