package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.data.TeamData;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSMatchStats;

public class MvpCommand implements CommandExecutor
{
	private Main plugin;

	public MvpCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("mvp"))
		{
			TeamData teamData = plugin.datahandler.gamemodehandler.getTeam(p);
			CSPlayer csplayer = plugin.datahandler.gamemodehandler.getMvp(teamData);
			if(csplayer == null)
			{
				p.sendMessage(teamData.getChatColor() + "The " + teamData.getName() + ChatColor.AQUA + " MVP: " + ChatColor.GRAY + "None");
			}
			else
			{
				CSMatchStats stats = csplayer.matchStats;
				p.sendMessage(teamData.getChatColor() + "The " + teamData.getName() + ChatColor.AQUA + " MVP: " + csplayer.getRank().getTag() + teamData.getChatColor() + csplayer.getPlayer().getName());
				p.sendMessage(ChatColor.DARK_AQUA + "Score " + ChatColor.WHITE + stats.getFinalScore() + ChatColor.DARK_AQUA + " | Kills " + ChatColor.WHITE + stats.getKills() + ChatColor.DARK_AQUA + " | Deaths " + ChatColor.WHITE + stats.getDeaths() + ChatColor.DARK_AQUA + " | Assists " + ChatColor.WHITE + stats.getAssists() + ChatColor.DARK_AQUA + " | Captures " + ChatColor.WHITE + stats.getCaps() + ChatColor.DARK_AQUA + " | Supports " + ChatColor.WHITE + stats.getSupps());
			}
		}
		return false;
	}
}
