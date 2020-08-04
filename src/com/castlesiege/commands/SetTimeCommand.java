package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSRank;

public class SetTimeCommand implements CommandExecutor
{
	private Main plugin;

	public SetTimeCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("settime"))
		{
			if(plugin.datahandler.utils.getCSPlayer(p).getRank().getID() == CSRank.DEV.getID())
			{
				if(args.length > 0)
				{
					if(plugin.datahandler.utils.isInteger(args[0]))
					{
						plugin.datahandler.timeLeft = Integer.parseInt(args[0]);
						p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.GREEN + "Time set to " + args[0]);
					}
					else
					{
						p.sendMessage(ChatColor.RED + "Please specify a valid time to be set!");
					}
				}
				else
				{
					p.sendMessage(ChatColor.RED + "Please specify a time to be set!");
				}
			}
			else
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSRank.DEV.getTag());
			}
			return true;
		}
		return false;
	}
}
