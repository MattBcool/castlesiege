package com.castlesiege.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;

public class TpCommand implements CommandExecutor
{
	private Main plugin;
	
	public TpCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
			return true;
		}
		Player p = (Player) sender;
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		if(csplayer.getRank().getID() < CSRank.MOD.getID())
		{
			p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "You do not have permission to use this command!");
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("tp"))
		{
			if(args.length == 1)
			{
				Player target = Bukkit.getPlayer(args[0]);
				if(target == null)
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Player not found!");
					return true;
				}
				
				p.teleport(target.getLocation());
			}
			else if(args.length == 3)
			{
				int x = 0, y = 0, z = 0;
				try
				{
					if(args[0].startsWith("~"))
					{
						args[0].replaceAll("~", "");
						x += p.getLocation().getBlockX();
					}
					x += Integer.parseInt(args[0]);
					
					if(args[1].startsWith("~"))
					{
						args[1].replaceAll("~", "");
						y += p.getLocation().getBlockY();
					}
					y += Integer.parseInt(args[1]);
					
					if(args[2].startsWith("~"))
					{
						args[2].replaceAll("~", "");
						z += p.getLocation().getBlockZ();
					}
					z += Integer.parseInt(args[2]);
				}
				catch(NumberFormatException ex)
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Invalid coordinates! /tp <x> <y> <z>");
					return true;
				}
				
				p.teleport(new Location(p.getWorld(), x, y, z));
			}
			else
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "/tp <player> OR /tp <x> <y> <z>");
			}
		} else if(cmd.getName().equalsIgnoreCase("tphere"))
		{
			if(args.length != 1)
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "/tphere <player>");
				return true;
			}
			
			Player target = Bukkit.getPlayer(args[0]);
			if(target == null)
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Player not found!");
				return true;
			}
			
			target.teleport(p.getLocation());
		}
		return false;
	}
}
