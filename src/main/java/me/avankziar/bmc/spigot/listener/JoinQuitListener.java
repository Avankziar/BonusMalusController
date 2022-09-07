package main.java.me.avankziar.bmc.spigot.listener;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import main.java.me.avankziar.bmc.spigot.BMC;

public class JoinQuitListener implements Listener
{
	private BMC plugin;
	
	public JoinQuitListener(BMC plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		plugin.getBonusMalusProvider().join(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		final UUID uuid = event.getPlayer().getUniqueId();
		plugin.getBonusMalusProvider().quit(uuid);
	}
}