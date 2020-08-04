package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.extra.TitleDisplay;

public class KTSCommand implements CommandExecutor
{
	private Main plugin;

	public KTSCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("kts") || cmd.getName().equalsIgnoreCase("killthyself"))
		{
			Location loc = p.getLocation();
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				//p.sendMessage(ChatColor.RED + "You cannot kill thyself while in thy spawn room!");
				TitleDisplay.sendActionBar(p, ChatColor.RED + "You cannot kill thyself while in thy spawn room!");
				return false;
			}
			//p.sendMessage(ChatColor.GRAY + "You have killed thy self.");
			TitleDisplay.sendActionBar(p, ChatColor.GRAY + "You have killed thy self.");
			p.damage(100D);
		}
		return false;
	}
}
