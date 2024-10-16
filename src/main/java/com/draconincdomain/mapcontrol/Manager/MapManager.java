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
    private final Map<String, PartyMap> allMaps = new HashMap<>();
    private final Map<PartyMap, Party> activeInstances = new HashMap<>();

    public MapManager() {
        Instance = this;
        loadMapsFromDirectory();
    }

    public Map<String, PartyMap> getAllMaps() {
        return allMaps;
    }

    public Optional<PartyMap> getMapByName(String name) {
        return Optional.ofNullable(allMaps.get(name.toLowerCase()));
    }

    public void loadMapsFromDirectory() {

        File mapsDir = new File(MapControl.getInstance().getDataFolder(), "CustomMaps");

        if (!mapsDir.exists() && !mapsDir.mkdirs()) {
            Bukkit.getLogger().severe("Failed to create custom maps directory: " + mapsDir.getPath());
            return;
        }

        if (!mapsDir.isDirectory()) {
            Bukkit.getLogger().severe("Custom maps path is not a directory: " + mapsDir.getPath());
            return;
        }

        for (File file : Objects.requireNonNull(mapsDir.listFiles())) {
            if (file.isDirectory()) {
                String mapName = file.getName();
                PartyMap partyMap = new PartyMap(mapName, 10, null, null);
                allMaps.put(mapName.toLowerCase(), partyMap);
                Bukkit.getLogger().info("Loaded map: " + mapName);
            }
        }
    }

    public void createNewMapInstance(String mapName, Party party) {
        PartyMap selectedMap = getMapByName(mapName).orElse(null);

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

        String mapInstanceName = party.getPartyId() + "_" + selectedMap.getName() + "_" + System.currentTimeMillis();
        String instancePath = MapControl.getInstance().getDataFolder() + "/ActiveMapInstances/" + mapInstanceName;

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
            if (!instanceMap.exists() && !instanceMap.mkdirs()) {
                PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Failed to create instance map directory.");
                return;
            }

            FileUtils.copyDirectory(originalMap, instanceMap);
            Bukkit.getLogger().info("Successfully copied original map to instance directory: " + instanceMap.getPath());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to copy the map for this instance: " + e.getMessage());
            PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Failed to copy the map for this instance.");
            return;
        }

        WorldCreator creator = new WorldCreator(mapInstanceName);
     /*   creator.environment(World.Environment.NORMAL);
        creator.generator(new VoidGenerator());*/
        creator.createWorld();

        World world = Bukkit.getWorld(mapInstanceName);
        if (world == null) {
            PartyManager.getInstance().alertParty(party, PartyNotificationAlert.WARNING, "Failed to load the map.");
            return;
        }

        PartyMap partyMap = new PartyMap(mapInstanceName, 10, party, world);
        selectedMap.setWorld(world);

        party.getPlayers().forEach((playerUUID, role) -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.teleport(world.getSpawnLocation());
                player.sendMessage(ChatColor.AQUA + "You have joined map: " + selectedMap.getName());
            }
        });

        activeInstances.put(partyMap, party);
    }

    public void cleanupMapInstance(PartyMap partyMap) {
        Party party = activeInstances.get(partyMap);

        if (party != null) {
            World world = Bukkit.getWorld(partyMap.getWorld().getName());

            if (world != null) {
                World mainWorld = Bukkit.getWorld("world");
                if (mainWorld != null) {
                    party.getPlayers().forEach((playerUUID, role) -> {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            player.teleport(mainWorld.getSpawnLocation());
                            player.sendMessage(ChatColor.YELLOW + "The map instance has ended. You have been teleported back to the main world.");
                        }
                    });
                }

                Bukkit.getScheduler().runTaskLater(MapControl.getInstance(), () -> {
                    Bukkit.unloadWorld(world, false);
                    if (Bukkit.getWorld(world.getName()) == null) {
                        Bukkit.getLogger().info("World " + world.getName() + " successfully unloaded.");
                    } else {
                        Bukkit.getLogger().severe("Failed to unload world: " + world.getName());
                    }

                    String worldFolderPath = MapControl.getInstance().getDataFolder() + "/ActiveMapInstances/" + partyMap.getName();
                    File worldFolder = new File(worldFolderPath);
                    if (worldFolder.exists()) {
                        try {
                            FileUtils.deleteDirectory(worldFolder);
                            Bukkit.getLogger().info("World folder deleted: " + worldFolder.getName());
                        } catch (IOException e) {
                            Bukkit.getLogger().severe("Failed to delete map instance folder: " + worldFolder.getName());
                            e.printStackTrace();
                        }
                    } else {
                        Bukkit.getLogger().severe("World folder not found for deletion: " + worldFolderPath);
                    }
                }, 20L);
            }
        } else {
            Bukkit.getLogger().warning("No active map instance found for: " + partyMap.getName());
        }
        activeInstances.remove(partyMap);
    }

    public PartyMap getActivePartyMapInstance(Party party) {
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
