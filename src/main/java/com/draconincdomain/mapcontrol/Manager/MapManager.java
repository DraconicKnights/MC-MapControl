package com.draconincdomain.mapcontrol.Manager;

import com.draconincdomain.mapcontrol.MapControl;
import com.draconincdomain.mapcontrol.Objects.Party;
import com.draconincdomain.mapcontrol.Objects.PartyMap;
import com.draconincdomain.mapcontrol.Enums.PartyNotificationAlert;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {
    private static MapManager Instance;
    private final Map<PartyMap, Party> activeInstances = new HashMap<>();
    private final List<PartyMap> allMaps = new ArrayList<>();

    public MapManager() {
        Instance = this;
        loadMapsFromDirectory();
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

    public void loadMapsFromDirectory() {

        File mapsDir = new File(MapControl.getInstance().getDataFolder(), "CustomMaps");

        if (!mapsDir.exists()) {
            if (mapsDir.mkdirs()) {
                Bukkit.getLogger().info("Custom maps directory did not exist. Created new directory: " + mapsDir.getPath());
            } else {
                Bukkit.getLogger().severe("Failed to create custom maps directory: " + mapsDir.getPath());
                return;
            }
        }

        if (!mapsDir.isDirectory()) {
            Bukkit.getLogger().severe("Custom maps path is not a directory: " + mapsDir.getPath());
            return;
        }

        for (File file : Objects.requireNonNull(mapsDir.listFiles())) {
            if (file.isDirectory()) {
                String mapName = file.getName();
                PartyMap partyMap = new PartyMap(mapName, 10, null, null);

                allMaps.add(partyMap);
                Bukkit.getLogger().info("Loaded map: " + mapName);
            }
        }
    }

    public void createNewMapInstance(String mapName, Party party) {
        PartyMap selectedMap = getMapByName(mapName);

        if (selectedMap == null) {
            PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Map not found");
            return;
        }

        if (!party.isPartyLeader(party.getLeader())) {
            Player leader = Bukkit.getPlayer(party.getLeader());
            if (leader != null) {
                leader.sendMessage(ChatColor.RED + "Only the party leader can start a map.");
            }
            return;
        }

        if (party.getSize() > selectedMap.getMaxPlayers()) {
            PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Too many players for this map.");
            return;
        }

        String instancePath = MapControl.getInstance().getDataFolder() + "/ActiveMapInstances/" + party.getPartyId() + "_" + selectedMap.getName();
        File originalMap = new File(MapControl.getInstance().getDataFolder() + "/CustomMaps/" + selectedMap.getName());
        File instanceMap = new File(instancePath);

        File activeMapInstancesDir = new File(MapControl.getInstance().getDataFolder(), "ActiveMapInstances");
        if (!activeMapInstancesDir.exists()) {
            if (activeMapInstancesDir.mkdirs()) {
                Bukkit.getLogger().info("ActiveMapInstances directory did not exist. Created new directory: " + activeMapInstancesDir.getPath());
            } else {
                PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Failed to create ActiveMapInstances directory.");
                return;
            }
        }

        try {
            FileUtils.copyDirectory(originalMap, instanceMap);
        } catch (IOException e) {
            PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Failed to copy the map for this instance.");
            return;
        }

        World world = new WorldCreator(instanceMap.getName()).createWorld();
        if (world == null) {
            PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Failed to load the map.");
            return;
        }

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

        Party party = activeInstances.get(map);

        if (party != null) {
            World mainWorld = Bukkit.getWorld("world");

            if (mainWorld == null) {
                Bukkit.getLogger().severe("Main world not found. Cannot teleport players.");
                return;
            }

            party.getPlayers().forEach((playerUUID, role) -> {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.teleport(mainWorld.getSpawnLocation());
                    player.sendMessage(ChatColor.YELLOW + "The map instance has ended. You have been teleported back to the main world.");
                }
            });
        }

        World world = map.getWorld();
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            File worldFolder = world.getWorldFolder();
            try {
                FileUtils.deleteDirectory(worldFolder);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to delete map instance folder: " + worldFolder.getName());
                e.printStackTrace();
            }
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

    public PartyMap getActiveMapForParty(Party party) {
        for (Map.Entry<PartyMap, Party> entry : activeInstances.entrySet()) {
            if (entry.getValue().equals(party)) {
                return entry.getKey();
            }
        }
        return null;
    }

   public static MapManager getInstance() {
        return Instance;
    }
}
