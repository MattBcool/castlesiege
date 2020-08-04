package com.castlesiege.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;

public class MessageCommand implements CommandExecutor
{
	private Main plugin;

	public MessageCommand(Main plugin)
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
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);

		if(cmd.getName().equalsIgnoreCase("message") || cmd.getName().equalsIgnoreCase("msg") || cmd.getName().equalsIgnoreCase("m") || cmd.getName().equalsIgnoreCase("tell") || cmd.getName().equalsIgnoreCase("privatemessage") || cmd.getName().equalsIgnoreCase("pm") || cmd.getName().equalsIgnoreCase("whisper") || cmd.getName().equalsIgnoreCase("w"))
		{
			if(!csplayer.isMuted())
			{
				if(args.length > 1)
				{
					if(Bukkit.getPlayer(args[0]) != null)
					{
						Player pl = Bukkit.getPlayer(args[0]);
						if(p.getName() != pl.getName())
						{
							String message = "";
							for(int i = 0; i < args.length; i++)
							{
								if(i > 0)
								{
									message += args[i] + " ";
								}
							}
							message(p, pl, message);
						}
						else
						{
							p.sendMessage(ChatColor.RED + "You can not message yourself!");
						}
					}
					else
					{
						p.sendMessage(ChatColor.RED + "That player count not be found!");
					}
				}
				else
				{
					p.sendMessage(ChatColor.RED + "Invalid arguments! /message <player> <message>");
				}
			}
			else
			{
				p.sendMessage(ChatColor.RED + "You are currently muted!");
			}
		}

		if(cmd.getName().equalsIgnoreCase("reply") || cmd.getName().equalsIgnoreCase("r") || cmd.getName().equalsIgnoreCase("respond") || cmd.getName().equalsIgnoreCase("re"))
		{
			if(!csplayer.isMuted())
			{
				if(args.length > 0)
				{
					if(plugin.datahandler.utils.getCSPlayer(p) != null && Bukkit.getPlayer(plugin.datahandler.utils.getCSPlayer(p).getReply()) != null)
					{
						Player pl = Bukkit.getPlayer(plugin.datahandler.utils.getCSPlayer(p).getReply());
						if(pl.isOnline())
						{
							if(p.getName() != pl.getName())
							{
								String message = "";
								for(String arg : args)
								{
									message += arg + " ";
								}
								message(p, pl, message);
								return true;
							}
							else
							{
								p.sendMessage(ChatColor.RED + "You can not message yourself!");
								return true;
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + "The player you were messaging is no longer online!");
							return true;
						}
					}
					else
					{
						p.sendMessage(ChatColor.RED + "There is nobody to reply to!");
						return true;
					}
				}
				else
				{
					p.sendMessage(ChatColor.RED + "Invalid arguments! /reply <message>");
					return true;
				}
			}
			else
			{
				p.sendMessage(ChatColor.RED + "You are currently muted!");
			}
		}
		return false;
	}

	private void message(Player p, Player pl, String message)
	{
		String ctp = "";
		String ctpl = "";
		if(plugin.datahandler.utils.getCSPlayer(p) != null)
		{
			CSPlayer csp = plugin.datahandler.utils.getCSPlayer(p);
			if(!csp.pms)
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "You have disabled private messaging.");
				return;
			}
			ctp = plugin.datahandler.utils.getCSPlayer(p).getRank().getTag() + ChatColor.GOLD;
			plugin.datahandler.utils.getCSPlayer(p).setReply(pl.getUniqueId());
		}
		if(plugin.datahandler.utils.getCSPlayer(pl) != null)
		{
			CSPlayer cspl = plugin.datahandler.utils.getCSPlayer(pl);
			if(!cspl.pms)
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + pl.getName() + " has disabled private messaging.");
				return;
			}
			ctpl = cspl.getRank().getTag() + ChatColor.GOLD;
			cspl.setReply(p.getUniqueId());
		}
		p.sendMessage(ChatColor.GOLD + "[me \u27A1 " + ctpl + pl.getName() + "] " + ChatColor.WHITE + message);
		pl.sendMessage(ChatColor.GOLD + "[" + ctp + p.getName() + " \u27A1 me] " + ChatColor.WHITE + message);
		for(Player ply : Bukkit.getOnlinePlayers())
		{
			if(ply != p && ply != pl)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(ply);
				if(csplayer != null && csplayer.getRank().getID() >= CSRank.CHATMOD.getID() && csplayer.chatspy)
				{
					ctp = ChatColor.RESET.toString() + ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + ChatColor.stripColor(ctp) + ChatColor.RESET + ChatColor.DARK_GRAY;
					ctpl = ChatColor.RESET.toString() + ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + ChatColor.stripColor(ctpl) + ChatColor.RESET + ChatColor.DARK_GRAY;
					ply.sendMessage(ChatColor.DARK_GRAY + "[" + ctp + p.getName() + ChatColor.RESET + ChatColor.DARK_GRAY + " \u27A1 " + ctpl + pl.getName() + "] " + message);
				}
			}
		}
	}
}
