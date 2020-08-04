package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;

public class HudCommand implements CommandExecutor
{
	private Main plugin;

	public HudCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("hud"))
		{
			if(plugin.datahandler.utils.getCSPlayer(p) != null)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				csplayer.scoreboard = (csplayer.scoreboard == 0 ? 1 : 0);
				plugin.datahandler.gamemodehandler.resetScoreboard(p);
				switch(csplayer.scoreboard)
				{
				case 0:
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing scoreboard to Stat Display");
					break;
				case 1:
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing scoreboard to Flag Display");
					break;
				}
			}
		}
		return false;
	}
}
