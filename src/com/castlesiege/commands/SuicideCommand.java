package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.extra.TitleDisplay;

public class SuicideCommand implements CommandExecutor
{
	private Main plugin;

	public SuicideCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			return true;
		}
		Player p = (Player) sender;

		if(cmd.getName().equalsIgnoreCase("suicide") || cmd.getName().equalsIgnoreCase("sui") || cmd.getName().equalsIgnoreCase("kill") || cmd.getName().equalsIgnoreCase("k"))
		{
			Location loc = p.getLocation();
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				//p.sendMessage(ChatColor.RED + "You cannot kill yourself while in your spawn room!");
				TitleDisplay.sendActionBar(p, ChatColor.RED + "You cannot kill yourself while in your spawn room!");
				return false;
			}
			//p.sendMessage(ChatColor.GRAY + "You killed yourself.");
			TitleDisplay.sendActionBar(p, ChatColor.GRAY + "You killed yourself.");
			p.damage(100D);
		}
		return false;
	}
}
