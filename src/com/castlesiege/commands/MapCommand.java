package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.castlesiege.Main;

public class MapCommand implements CommandExecutor
{
	private Main plugin;

	public MapCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("map") || cmd.getName().equalsIgnoreCase("timeleft") || cmd.getName().equalsIgnoreCase("currentmap"))
		{
			String next = "restart";
			try
			{
				next = plugin.datahandler.gamemodehandler.mapsInOrder.get(plugin.datahandler.gamemodehandler.currentMap + 1).getName();
			}
			catch(IndexOutOfBoundsException e)
			{}

			sender.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Map Info:");
			sender.sendMessage(ChatColor.GRAY + "Current map: " + ChatColor.DARK_GRAY + plugin.datahandler.currentWorld.getName());
			sender.sendMessage(ChatColor.GRAY + "This round ends in: " + ChatColor.DARK_GRAY + (plugin.datahandler.timeLeft / 60) + " minutes.");
			sender.sendMessage(ChatColor.GRAY + "Next map: " + ChatColor.DARK_GRAY + next);
		}
		return false;
	}
}
