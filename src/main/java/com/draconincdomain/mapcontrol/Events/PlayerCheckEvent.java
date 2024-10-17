package com.draconincdomain.mapcontrol.Events;

import com.draconincdomain.mapcontrol.Annotations.Events;
import com.draconincdomain.mapcontrol.Manager.MapManager;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import com.draconincdomain.mapcontrol.Objects.Party;
import com.draconincdomain.mapcontrol.Objects.PartyMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@Events
public class PlayerCheckEvent implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!PartyManager.getInstance().isPlayerInParty(event.getPlayer().getUniqueId()))
            return;

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        Party party = PartyManager.getInstance().findPlayerParty(playerUUID);
        if (party == null) {
            return;
        }

        PartyMap partyMap = MapManager.getInstance().getActivePartyMapInstance(party);
        if (partyMap != null) {
            World mainWorld = Bukkit.getWorld("world");
            if (mainWorld != null) {
                player.teleport(mainWorld.getSpawnLocation());
                player.sendMessage(ChatColor.YELLOW + "You have been teleported back to the main world.");
            } else {
                player.sendMessage(ChatColor.RED + "Main world not found.");
            }
        }

        PartyManager.getInstance().removePlayerFromParty(party, playerUUID);
        player.sendMessage(ChatColor.RED + "You have been removed from the party");
    }
}
