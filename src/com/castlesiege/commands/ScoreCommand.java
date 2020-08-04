package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSMatchStats;

public class ScoreCommand implements CommandExecutor
{
	private Main plugin;

	public ScoreCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("score"))
		{
			p.sendMessage(ChatColor.DARK_AQUA + "Your score:");
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
			if(csplayer == null)
			{
				p.sendMessage(ChatColor.GRAY + "None");
			}
			else
			{
				CSMatchStats stats = csplayer.matchStats;
				p.sendMessage(ChatColor.DARK_AQUA + "Score " + ChatColor.WHITE + stats.getFinalScore() + ChatColor.DARK_AQUA + " | Kills " + ChatColor.WHITE + stats.getKills() + ChatColor.DARK_AQUA + " | Deaths " + ChatColor.WHITE + stats.getDeaths() + ChatColor.DARK_AQUA + " | Assists " + ChatColor.WHITE + stats.getAssists() + ChatColor.DARK_AQUA + " | Captures " + ChatColor.WHITE + stats.getCaps() + ChatColor.DARK_AQUA + " | Supports " + ChatColor.WHITE + stats.getSupps());
			}
		}
		return false;
	}
}
