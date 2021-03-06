package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.util.sql.SQLConnection;

public class TopGuildCommand implements CommandExecutor
{
	private Main plugin;

	public TopGuildCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer((Player) sender);
			if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
			{
				String order = "guild_score";
				switch(label)
				{
				case "topguildkill":
				case "topguildkills":
					order = "guild_kills";
					break;
				case "topguilddeath":
				case "topguilddeaths":
					order = "guild_deaths";
					break;
				case "topguildassist":
				case "topguildassists":
					order = "guild_assists";
					break;
				case "topguildcap":
				case "topguildcaps":
				case "topguildcapture":
				case "topguildcaptures":
					order = "guild_caps";
					break;
				case "topguildsup":
				case "topguildsups":
				case "topguildsupp":
				case "topguildsupps":
				case "topguildsupport":
				case "topguildsupports":
					order = "guild_supps";
					break;
				case "topguildmvp":
				case "topguildmvps":
					order = "guild_mvps";
					break;
				case "topguildstreak":
				case "topguildstreaks":
				case "topguildkillstreak":
				case "topguildkillstreaks":
					order = "guild_streak";
					break;
				}
				ArrayList<String> messages = new ArrayList<String>();
				messages.add(ChatColor.AQUA + "#. " + ChatColor.DARK_AQUA + "P" + ChatColor.AQUA + "layer " + ChatColor.WHITE + "S" + ChatColor.AQUA + "core " + ChatColor.DARK_GREEN + "K" + ChatColor.AQUA + "ills " + ChatColor.RED + "D" + ChatColor.AQUA + "eaths " + ChatColor.GREEN + "A" + ChatColor.AQUA + "ssists " + ChatColor.GRAY + "C" + ChatColor.AQUA + "aps " + ChatColor.BLUE + "S" + ChatColor.AQUA + "upps " + ChatColor.GOLD + "M" + ChatColor.AQUA + "VPs " + ChatColor.DARK_PURPLE + "S" + ChatColor.AQUA + "treak");
				int limit = 10;
				int offset = 0;
				int used = 0;
				boolean doColor = false;
				SQLConnection sql = plugin.datahandler.sqlConnection;
				Connection c = sql.connection;
				if(args.length > 0)
				{
					int o = -1;
					if(plugin.datahandler.utils.isInteger(args[0]))
					{
						o = Integer.parseInt(args[0]) - 1;
						used = Integer.parseInt(args[0]);
					}
					else
					{
						String uuid;
						try
						{
							uuid = UUIDFetcher.getUUIDOf(plugin, args[0]).toString();
						}
						catch(Exception e1)
						{
							sender.sendMessage(ChatColor.RED + "Player not found, please specify a valid player!");
							return false;
						}
						if(uuid == null)
						{
							sender.sendMessage(ChatColor.RED + "Player not found, please specify a valid player!");
							return false;
						}
						else
						{
							try
							{
								PreparedStatement statement = c.prepareStatement("SELECT uuid FROM player_data WHERE guild=? ORDER BY " + order + " DESC");
								statement.setString(1, csplayer.guild);
								ResultSet result = statement.executeQuery();

								while(result.next())
								{
									if(result.getString("uuid").equalsIgnoreCase(uuid))
									{
										o = result.getRow() - 1;
										used = result.getRow();
									}
								}
								statement.close();
								result.close();
							}
							catch(SQLException e)
							{
								e.printStackTrace();
							}
							if(o < 0)
							{
								sender.sendMessage(ChatColor.RED + "Player not found, please specify a valid player in the guild!");
								return false;
							}
						}
					}
					if(o >= 0)
					{
						offset = capInt(o - 4);
						doColor = true;
					}
				}
				try
				{
					PreparedStatement statement = c.prepareStatement("SELECT latest_name, guild_score, guild_kills, guild_deaths, guild_assists, guild_caps, guild_supps, guild_mvps, guild_streak FROM player_data WHERE guild=? ORDER BY " + order + " DESC LIMIT ? OFFSET ?");
					statement.setString(1, csplayer.guild);
					statement.setInt(2, limit);
					statement.setInt(3, offset);
					ResultSet result = statement.executeQuery();

					int id = offset + 1;
					while(result.next())
					{
						ChatColor chatColor = ChatColor.DARK_GRAY;
						if(doColor)
						{
							chatColor = id == used ? ChatColor.GRAY : ChatColor.DARK_GRAY;
						}
						messages.add(chatColor + "" + id + "." + ChatColor.DARK_AQUA + " " + result.getString("latest_name") + ChatColor.WHITE + " " + result.getString("guild_score") + ChatColor.DARK_GREEN + " " + result.getString("guild_kills") + ChatColor.RED + " " + result.getString("guild_deaths") + ChatColor.GREEN + " " + result.getString("guild_assists") + ChatColor.GRAY + " " + result.getString("guild_caps") + ChatColor.BLUE + " " + result.getString("guild_supps") + ChatColor.GOLD + " " + result.getString("guild_mvps") + ChatColor.DARK_PURPLE + " " + result.getString("guild_streak"));
						id += 1;
					}
					statement.close();
					result.close();
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}

				if(messages.size() > 1)
				{
					for(String message : messages)
					{
						sender.sendMessage(message);
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "There is no data to display!");
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You are not in a guild!");
			}
		}
		return false;
	}

	private int capInt(int i)
	{
		if(i < 0)
		{
			i = 0;
		}
		return i;
	}
}