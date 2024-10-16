package com.draconincdomain.mapcontrol.Events;

import com.draconincdomain.mapcontrol.Annotations.Events;
import com.draconincdomain.mapcontrol.Manager.PartyManager;
import com.draconincdomain.mapcontrol.Objects.Party;
import org.bukkit.ChatColor;
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

        PartyManager.getInstance().removePlayerFromParty(party, playerUUID);
        player.sendMessage(ChatColor.RED + "You have been removed from the party");
    }
}
