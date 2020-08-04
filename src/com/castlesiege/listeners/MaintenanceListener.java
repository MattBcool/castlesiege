package com.castlesiege.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.castlesiege.Main;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;

public class MaintenanceListener implements Listener
{
	public MaintenanceListener(Main plugin)
	{
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params(plugin, new PacketType[]
		{ PacketType.Status.Server.OUT_SERVER_INFO }).optionAsync())
		{
			public void onPacketSending(PacketEvent event)
			{
				if(plugin.getServer().hasWhitelist())
				{
					WrappedServerPing ping = (WrappedServerPing) event.getPacket().getServerPings().read(0);
					ping.setVersionName("Maintenance");
					ping.setVersionProtocol(-1);
				}
			}
		});
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e)
	{
		if(e.getResult() == Result.KICK_WHITELIST)
		{
			e.setKickMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Tales of War\n\n" + ChatColor.RESET + "The server is currently down for maintenance.\nIt will be back up soon!\nPlease visit talesofwar.net for more information.");
		}
	}
}
