package com.castlesiege.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.castlesiege.Main;
import com.castlesiege.data.TeamData;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSMatchStats;

public class MvpsCommand implements CommandExecutor
{
	private Main plugin;

	public MvpsCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("mvps"))
		{
			ArrayList<String> mvps = new ArrayList<String>();
			for(TeamData td : plugin.datahandler.currentWorld.teamData)
			{
				if(!td.getName().equalsIgnoreCase("None"))
				{
					CSPlayer csplayer = plugin.datahandler.gamemodehandler.getMvp(td);
					if(csplayer == null)
					{
						mvps.add(td.getChatColor() + "The " + td.getName() + ChatColor.AQUA + " MVP: " + ChatColor.GRAY + "None");
					}
					else
					{
						CSMatchStats stats = csplayer.matchStats;
						mvps.add(td.getChatColor() + "The " + td.getName() + ChatColor.AQUA + " MVP: " + csplayer.getRank().getTag() + td.getChatColor() + csplayer.getPlayer().getName());
						mvps.add(ChatColor.DARK_AQUA + "Score " + ChatColor.WHITE + stats.getFinalScore() + ChatColor.DARK_AQUA + " | Kills " + ChatColor.WHITE + stats.getKills() + ChatColor.DARK_AQUA + " | Deaths " + ChatColor.WHITE + stats.getDeaths() + ChatColor.DARK_AQUA + " | Assists " + ChatColor.WHITE + stats.getAssists() + ChatColor.DARK_AQUA + " | Captures " + ChatColor.WHITE + stats.getCaps() + ChatColor.DARK_AQUA + " | Supports " + ChatColor.WHITE + stats.getSupps());
					}
				}
			}
			for(String message : mvps)
			{
				sender.sendMessage(message);
			}
		}
		return false;
	}
}
