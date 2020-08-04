package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.castlesiege.Main;
import com.castlesiege.data.TeamData;

public class SwitchCommand implements CommandExecutor
{
	private Main plugin;

	public SwitchCommand(Main plugin)
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

		if(cmd.getName().equalsIgnoreCase("sw"))
		{
			if(plugin.datahandler.currentWorld != null)
			{
				Location loc = p.getLocation();
				if(!plugin.datahandler.gamemodehandler.inSpawnRoom(loc) && !plugin.datahandler.gamemodehandler.inGuildRoom(loc))
				{
					p.sendMessage(ChatColor.RED + "You can only change teams in your spawn room!");
					return false;
				}
				TeamData teamData = plugin.datahandler.gamemodehandler.getTeam(p);
				if(teamData == null)
				{
					plugin.datahandler.gamemodehandler.randomizeTeam(p);
					return false;
				}
				if(plugin.datahandler.currentWorld.teamData.size() > 3)
				{
					if(args.length > 0)
					{
						for(TeamData td : plugin.datahandler.currentWorld.teamData)
						{
							if(teamData.players.size() >= td.players.size() && td.getName().toLowerCase().contains(args[0].toLowerCase()))
							{
								plugin.datahandler.gamemodehandler.setTeam(p, td);
								p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) td.getWoolColor()));
								p.teleport(td.getSpawn());
								return false;
							}
						}
						p.sendMessage(ChatColor.RED + "You can not switch teams right now as teams would become too unbalanced!");
						return false;
					}
					int i = 0;
					String teams = "";
					for(TeamData td : plugin.datahandler.currentWorld.teamData)
					{
						if(!td.getName().equalsIgnoreCase("None"))
						{
							if(plugin.datahandler.currentWorld.teamData.size() - 1 > i)
							{
								teams += td.getChatColor() + td.getName() + ChatColor.GRAY + ", ";
							}
							else
							{
								teams += td.getChatColor() + td.getName();
							}
						}
						i++;
					}
					p.sendMessage(ChatColor.RED + "Please provide a team to switch to. " + ChatColor.GRAY + "(" + teams + ")");
					return false;
				}
				else
				{
					if(args.length > 0)
					{
						for(TeamData td : plugin.datahandler.currentWorld.teamData)
						{
							if(teamData.players.size() >= td.players.size() && td.getName().toLowerCase().contains(args[0].toLowerCase()))
							{
								plugin.datahandler.gamemodehandler.setTeam(p, td);
								p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) td.getWoolColor()));
								if(!plugin.datahandler.gamemodehandler.inGuildRoom(loc))
								{
									p.teleport(td.getSpawn());
								}
								return false;
							}
						}
						p.sendMessage(ChatColor.RED + "You can not switch teams right now as teams would become too unbalanced!");
						return false;
					}
					else
					{
						for(TeamData td : plugin.datahandler.currentWorld.teamData)
						{
							if(td != null && !td.getName().equalsIgnoreCase("None") && td != teamData)
							{
								if(teamData.players.size() >= td.players.size())
								{
									plugin.datahandler.gamemodehandler.setTeam(p, td);
									p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) td.getWoolColor()));
									p.teleport(td.getSpawn());
									return false;
								}
							}
						}
						p.sendMessage(ChatColor.RED + "You can not switch teams right now as teams would become too unbalanced!");
						return false;
					}
				}
			}
		}
		return false;
	}
}
