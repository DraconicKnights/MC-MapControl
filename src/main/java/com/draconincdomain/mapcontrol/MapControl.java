package com.draconincdomain.mapcontrol;

import com.draconincdomain.mapcontrol.Manager.MapManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MapControl extends JavaPlugin {

    private MapControl Instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.Instance = this;
        new MapManager();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public MapControl getInstance() {
        return Instance;
    }
}
