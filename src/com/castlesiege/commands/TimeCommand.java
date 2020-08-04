package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;

public class TimeCommand implements CommandExecutor
{
	private Main plugin;

	public TimeCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("time") || label.equalsIgnoreCase("timeplayed") || label.equalsIgnoreCase("totaltime") || label.equalsIgnoreCase("totaltimeplayed") || label.equalsIgnoreCase("playtime") || label.equalsIgnoreCase("totalplaytime"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				int day = csplayer.timePlayed / 60 / 60 / 24;
				int hour = csplayer.timePlayed / 60 / 60 % 24;
				int minute = csplayer.timePlayed / 60 % 60;
				int second = csplayer.timePlayed % 60;
				p.sendMessage(ChatColor.DARK_AQUA + "Time played: " + ChatColor.GRAY + plugin.datahandler.utils.fixInt(day) + "d " + plugin.datahandler.utils.fixInt(hour) + "h " + plugin.datahandler.utils.fixInt(minute) + "m " + plugin.datahandler.utils.fixInt(second) + "s");
			}
		}
		return false;
	}
}
