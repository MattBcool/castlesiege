package com.castlesiege.commands;

import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class VoteCommand implements CommandExecutor
{
	private Main plugin;

	public VoteCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("vote"))
		{
			if(args.length != 1)
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "/vote [all/1-4]");
				return true;
			}

			if(args[0].equalsIgnoreCase("all"))
			{
				//p.sendMessage(ChatColor.YELLOW + "Bypassing Vote System...");
				for(CSVoteSite vote : CSVoteSite.values())
				{
					if(csplayer.hasVoted(vote))
					{
						p.sendMessage(ChatColor.GRAY + "* " + ChatColor.GREEN + vote.getSite());
						//p.sendMessage(ChatColor.YELLOW + "You have already voted on " + vote.toString() + "!");
					}
					else
					{
						p.sendMessage(ChatColor.GRAY + "* " + ChatColor.RED + vote.getSite());
						//csplayer.voted.put(vote, (int) System.currentTimeMillis() / 1000);
						//p.sendMessage(ChatColor.GREEN + "Thanks! You have successfully voted on " + vote.toString() + "!");
					}
				}
				if(plugin.datahandler.gamemodehandler.inSpawnRoom(p.getLocation()))
				{
					csplayer.getKit().clearKit();
					csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
					csplayer.getKit().gameKit();
				}
			}
			else
			{
				int id = -1;
				try
				{
					id = Integer.parseInt(args[0]);
				}
				catch(NumberFormatException ex)
				{}
				if(id < 1 || id > 4)
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "/vote [all/1-4]");
					return true;
				}

				CSVoteSite site = CSVoteSite.getVoteSite(id - 1);
				if(site == null)
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Vote site not found!");
					return true;
				}

				StringBuilder sb = new StringBuilder(ChatColor.YELLOW + site.toString() + "'s Rewards: ");
				switch(site)
				{
				default:
					sb.append("Unknown");
					break;
				case PMC:
					sb.append("Sharpness Weapon");
					break;
				case Minestatus:
					sb.append("An Extra Heart");
					break;
				case MinecraftServers:
					sb.append("Feather Fall Boots");
					break;
				case MinecraftMP:
					sb.append("2 Extra Ladders");
					break;
				}
				p.sendMessage(sb.toString());
				//p.sendMessage(ChatColor.YELLOW + "Bypassing Vote System...");
				if(csplayer.hasVoted(site))
				{
					p.sendMessage(ChatColor.GREEN + "You already voted on " + WordUtils.capitalize(site.toString().toLowerCase()) + "!");
					//p.sendMessage(ChatColor.YELLOW + "You have already voted on " + site.toString() + "!");
				}
				else
				{
					//csplayer.voted.put(site, (int) System.currentTimeMillis() / 1000);
					//p.sendMessage(ChatColor.GREEN + "Thanks! You have successfully voted on " + site.toString() + "!");
					p.sendMessage(ChatColor.YELLOW + site.getSite());
				}
				return true;
			}
		}
		return false;
	}
}
