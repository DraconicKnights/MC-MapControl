package com.draconincdomain.mapcontrol;

import com.draconincdomain.mapcontrol.Annotations.Commands;
import com.draconincdomain.mapcontrol.Commands.CommandCore;
import com.draconincdomain.mapcontrol.Manager.MapManager;
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
        registerPluginCommands();
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
                String permission = commandAnnotation.permission();
                boolean requiresPlayer = commandAnnotation.requiresPlayer();
                boolean hasCooldown = commandAnnotation.hasCooldown();
                int cooldownValue = commandAnnotation.cooldownDuration();

                CommandCore commandInstance = (CommandCore) commandClass.getDeclaredConstructor().newInstance();
                commandInstance.register(commandName, permission, requiresPlayer, hasCooldown, cooldownValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static MapControl getInstance() {
        return Instance;
    }
}
