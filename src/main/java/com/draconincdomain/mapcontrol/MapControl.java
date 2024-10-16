package com.draconincdomain.mapcontrol;

import com.draconincdomain.mapcontrol.Annotations.Commands;
import com.draconincdomain.mapcontrol.Annotations.Events;
import com.draconincdomain.mapcontrol.Commands.CommandCore;
import com.draconincdomain.mapcontrol.Manager.MapManager;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.util.Set;

public final class MapControl extends JavaPlugin {

    private static MapControl Instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Instance = this;
        new MapManager();
        new PartyManager();
        registerPluginCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerPluginCommands() {
        String packageName = getClass().getPackage().getName();

        Reflections reflections = new Reflections(packageName + ".Commands", new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<Class<?>> customCommandClasses = reflections.getTypesAnnotatedWith(Commands.class);

        for (Class<?> commandClass : customCommandClasses) {
            try {
                Commands commandAnnotation = commandClass.getAnnotation(Commands.class);
                String commandName = commandAnnotation.name();
                System.out.println("CommandName: " + commandName);
                if (getCommand(commandName) == null) {
                    System.out.println("Command " + commandName + " not found in paper-plugin.yml");
                }

                String permission = commandAnnotation.permission();
                boolean requiresPlayer = commandAnnotation.requiresPlayer();
                boolean hasCooldown = commandAnnotation.hasCooldown();
                int cooldownValue = commandAnnotation.cooldownDuration();

                CommandCore commandInstance = (CommandCore) commandClass.getDeclaredConstructor().newInstance();
                commandInstance.register(commandName, permission, requiresPlayer, hasCooldown, cooldownValue);
                getCommand(commandName).setExecutor(commandInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerEvents() {
        String packageName = getClass().getPackage().getName();

        Reflections reflections = new Reflections(packageName + ".Events", new TypeAnnotationsScanner(), new SubTypesScanner());
        Set<Class<?>> customEventClasses = reflections.getTypesAnnotatedWith(Events.class);

        for (Class<?> eventClass : customEventClasses) {
            try {
                Listener listener = (Listener) eventClass.getDeclaredConstructor().newInstance();
                Bukkit.getServer().getPluginManager().registerEvents( listener, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static MapControl getInstance() {
        return Instance;
    }
}
