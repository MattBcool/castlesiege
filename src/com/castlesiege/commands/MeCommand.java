package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSRank;

public class MeCommand implements CommandExecutor
{
	private Main plugin;

	public MeCommand(Main plugin)
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

		if(label.equalsIgnoreCase("me") || label.equalsIgnoreCase("emote") || label.equalsIgnoreCase("emotes"))
		{
			if(plugin.datahandler.utils.getCSPlayer(p).getDonorRank().getID() >= CSDonorRank.LORD.getID() || plugin.datahandler.utils.getCSPlayer(p).getRank().getID() >= CSRank.ADMIN.getID())
			{
				if(args.length > 0)
				{
					String message = "";
					for(String arg : args)
					{
						message += arg + " ";
					}
					p.chat(ChatColor.YELLOW + "*" + message);
				}
				else
				{
					p.sendMessage(ChatColor.RED + "Invalid arguments!");
				}
			}
			else
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
			}
			return true;
		}
		return false;
	}
}
