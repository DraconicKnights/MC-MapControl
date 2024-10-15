package com.draconincdomain.mapcontrol.Commands;

import com.draconincdomain.mapcontrol.Annotations.Commands;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

@Commands(name = "party", permission = "mapControl.admin", hasCooldown = true, cooldownDuration = 10)
public class PartyCommand extends CommandCore{

    @Override
    protected void execute(Player player, String[] args) {

    }

    @Override
    protected List<String> commandCompletion(Player player, Command command, String[] strings) {
        return null;
    }
}
