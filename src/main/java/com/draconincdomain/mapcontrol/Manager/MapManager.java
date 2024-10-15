package com.draconincdomain.mapcontrol.Manager;

import com.draconincdomain.mapcontrol.Objects.MC_Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MapManager {
    private final MapManager Instance;
    private final Map<MC_Map, List<UUID>> activeInstances = new HashMap<>();
    private final List<MC_Map> allMaps = new ArrayList<>();

    public MapManager() {
        this.Instance = this;
    }

    public List<MC_Map> getAllMaps() {
        return allMaps;
    }

    public MC_Map getMapByName(String name) {
        for (MC_Map map : allMaps) {
            if (map.getName().equals(name)) {
                return map;
            }
            return null;
        }
        return null;
    }

    public void createNewMapInstance(String mapName, List<Player> players) {
        MC_Map selectedMap = getMapByName(mapName);

        if (selectedMap == null) {
            for (Player player : players) {
                player.sendMessage("Map not found");
            }
            return;
        }

        if (players.size() > selectedMap.getMaxPlayers()) {
            for (Player player : players) {
                player.sendMessage("Too many players for this activity. adjust party size");
            }
            return;
        }

        World world = selectedMap.loadWorld("map_instance_" + selectedMap.getName() + "_" + System.currentTimeMillis());

        selectedMap.setMaxPlayers(players.size());

        for (Player player : players) {
            player.teleport(world.getSpawnLocation());
            player.sendMessage("You have joined map: " + selectedMap.getName());
        }

        activeInstances.put(selectedMap, players.stream().map(Player::getUniqueId).collect(Collectors.toList()));
    }

    public void cleanupMapInstance(MC_Map map) {
        World world = map.getWorld();
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            world.getWorldFolder().delete();
        }
        activeInstances.remove(map);
    }

    public void playerRemoval(MC_Map targetMap, Player player) {
        if (activeInstances.containsValue(player.getUniqueId())) {
            activeInstances.get(targetMap).remove(player.getUniqueId());
        }
    }
}
