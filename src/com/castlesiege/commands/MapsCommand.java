package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.castlesiege.Main;
import com.castlesiege.data.WorldData;

public class MapsCommand implements CommandExecutor
{
	private Main plugin;

	public MapsCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("maps") || cmd.getName().equalsIgnoreCase("mapcycle"))
		{
			String map = "";
			ChatColor chatColor = ChatColor.GRAY;
			for(int i = 0; i < plugin.datahandler.gamemodehandler.mapsInOrder.size(); i++)
			{
				WorldData worldData = plugin.datahandler.gamemodehandler.mapsInOrder.get(i);
				if(worldData == plugin.datahandler.currentWorld)
				{
					chatColor = ChatColor.GREEN;
				}
				map += chatColor + worldData.getName() + ChatColor.WHITE + " > ";
				chatColor = ChatColor.GRAY;
			}
			map += ChatColor.GRAY + "restart";
			sender.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Map Cycle:");
			sender.sendMessage(map);
		}
		return false;
	}
}
