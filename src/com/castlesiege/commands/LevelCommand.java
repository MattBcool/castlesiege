package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;

public class LevelCommand implements CommandExecutor
{
	private Main plugin;

	public LevelCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("level") || label.equalsIgnoreCase("lvl") || label.equalsIgnoreCase("exp") || label.equalsIgnoreCase("currentlevel") || label.equalsIgnoreCase("currentexp") || label.equalsIgnoreCase("nextlevel") || label.equalsIgnoreCase("nextlvl") || label.equalsIgnoreCase("experience"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				p.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Level Info:");
				p.sendMessage(ChatColor.GRAY + "Level: " + ChatColor.DARK_GRAY + plugin.datahandler.utils.getLevel(csplayer.classStats.getTotalScore(), 3));
				p.sendMessage(ChatColor.GRAY + "Exp: " + ChatColor.DARK_GRAY + csplayer.classStats.getTotalScore() + " / " + plugin.datahandler.utils.getMaxExp(csplayer.classStats.getTotalScore(), 3));
				String exp = "";
				for(int i = 0; i < 30; i++)
				{
					if((double) csplayer.classStats.getTotalScore() / (double) plugin.datahandler.utils.getMaxExp(csplayer.classStats.getTotalScore(), 3) >= (double) (1d / 30d) * i)
					{
						exp += ChatColor.DARK_GRAY + "|";
					}
					else
					{
						exp += ChatColor.GRAY + "|";
					}
				}
				p.sendMessage(ChatColor.GRAY + "Progress: " + exp);
			}
		}
		return false;
	}
}
