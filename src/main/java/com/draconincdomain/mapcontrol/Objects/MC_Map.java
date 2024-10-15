package com.draconincdomain.mapcontrol.Objects;

import org.bukkit.World;

import java.io.Serializable;

public class MC_Map implements Serializable {
    private String Name;
    private int MaxPlayers;
    private World world;

    public MC_Map(String name, int maxPlayers, World world) {
        Name = name;
        MaxPlayers = maxPlayers;
        this.world = world;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getMaxPlayers() {
        return MaxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        MaxPlayers = maxPlayers;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public World loadWorld(String worldName) {
        World world = org.bukkit.Bukkit.getServer().getWorld(worldName);
        if (world == null) {
            world = new org.bukkit.WorldCreator(worldName).createWorld();
        }
        return world;
    }
}
