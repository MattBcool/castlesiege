package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSRank;

public class FlyCommand implements CommandExecutor
{
	private Main plugin;

	public FlyCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("fly"))
		{
			if(plugin.datahandler.utils.getCSPlayer(p).getRank().getID() >= CSRank.MOD.getID() || plugin.datahandler.utils.getCSPlayer(p).getRank() == CSRank.MEDIA)
			{
				p.setAllowFlight(!p.getAllowFlight());
				p.sendMessage(ChatColor.GREEN + "Turned " + (p.getAllowFlight() ? "on" : "off") + " flight.");
			}
			else
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSRank.MOD.getTag() + ChatColor.RED + "or " + CSRank.MEDIA.getTag());
			}
			return true;
		}
		return false;
	}
}
