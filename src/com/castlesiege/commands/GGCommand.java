package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.ScoreType;

public class GGCommand implements CommandExecutor
{
	private Main plugin;

	public GGCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player p = (Player) sender;
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);

			if(plugin.datahandler.restarting)
			{
				if(csplayer.hasGG)
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "You have already said good game!");
					return true;
				} else
				{
					if(label.equalsIgnoreCase("gg") || label.equalsIgnoreCase("goodgame"))
					{
						p.chat("Good game!");
					}
					if(label.equalsIgnoreCase("wp") || label.equalsIgnoreCase("wellplayed"))
					{
						p.chat("Well played!");
					}
					if(label.equalsIgnoreCase("ggwp") || label.equalsIgnoreCase("goodgamewellplayed"))
					{
						p.chat("Good game, well played!");
					}
					csplayer.giveScore(ScoreType.SUPPS, 1, false, true, true);
					csplayer.hasGG = true;
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "+1 support point for being sportsmanlike!");
					return true;
				}
			}
			else
			{
				p.sendMessage(ChatColor.RED + "The game is not over yet!");
			}
		}
		return false;
	}
}
