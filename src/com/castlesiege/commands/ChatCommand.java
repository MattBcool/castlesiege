package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;

public class ChatCommand implements CommandExecutor
{
	private Main plugin;
	
	public ChatCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You must be a player to perform this command!");
			return true;
		}
		Player p = (Player) sender;
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		
		if(cmd.getName().equalsIgnoreCase("chat"))
		{
			if(args.length == 1)
			{
				String arg = args[0];
				boolean enable = true;
				if(arg.startsWith("-"))
				{
					enable = false;
					arg = arg.substring(1, arg.length());
				}
				
				if(arg.equalsIgnoreCase("d"))
				{
					csplayer.deathMessages = enable;
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + (enable ? "Enabled" : "Disabled") + " death messages.");
				}
				else if(arg.equalsIgnoreCase("j"))
				{
					csplayer.joinMessages = enable;
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + (enable ? "Enabled" : "Disabled") + " joining and quitting messages.");
				}
				else if(arg.equalsIgnoreCase("g"))
				{
					csplayer.guildJoinMessages = enable;
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + (enable ? "Enabled" : "Disabled") + " guild joining and quitting messages.");
				}
				else if(arg.equalsIgnoreCase("m"))
				{
					csplayer.pms = enable;
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + (enable ? "Enabled" : "Disabled") + " private messages.");
				}
				else
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Invalid option. Please use /chat to see all the options.");
				}
				return true;
			}
			else
			{
				p.sendMessage(ChatColor.DARK_AQUA + "Usage: /chat [-]<option>");
				p.sendMessage(ChatColor.DARK_AQUA + "Example: " + ChatColor.AQUA + "/chat d" + ChatColor.DARK_AQUA + " shows death msgs, " + ChatColor.AQUA + "/chat -d" + ChatColor.DARK_AQUA + " hides them.");
				p.sendMessage(ChatColor.DARK_AQUA + "Options:");
				p.sendMessage(ChatColor.DARK_AQUA + "d: show/hide death messages");
				p.sendMessage(ChatColor.DARK_AQUA + "j: show/hide join/quit messages");
				p.sendMessage(ChatColor.DARK_AQUA + "g: show/hide guild join/quit messages");
				p.sendMessage(ChatColor.DARK_AQUA + "m: show/hide private messages");
				return true;
			}
		}
		return false;
	}
}
