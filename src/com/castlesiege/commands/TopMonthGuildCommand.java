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

public class TopMonthGuildCommand implements CommandExecutor
{
	private Main plugin;

	public TopMonthGuildCommand(Main plugin)
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
				String order = "month_guild_score";
				switch(label)
				{
				case "topmonthguildkill":
				case "topmonthguildkills":
					order = "month_guild_kills";
					break;
				case "topmonthguilddeath":
				case "topmonthguilddeaths":
					order = "month_guild_deaths";
					break;
				case "topmonthguildassist":
				case "topmonthguildassists":
					order = "month_guild_assists";
					break;
				case "topmonthguildcap":
				case "topmonthguildcaps":
				case "topmonthguildcapture":
				case "topmonthguildcaptures":
					order = "month_guild_caps";
					break;
				case "topmonthguildsup":
				case "topmonthguildsups":
				case "topmonthguildsupp":
				case "topmonthguildsupps":
				case "topmonthguildsupport":
				case "topmonthguildsupports":
					order = "month_guild_supps";
					break;
				case "topmonthguildmvp":
				case "topmonthguildmvps":
					order = "month_guild_mvps";
					break;
				case "topmonthguildstreak":
				case "topmonthguildstreaks":
				case "topmonthguildkillstreak":
				case "topmonthguildkillstreaks":
					order = "month_guild_streak";
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
					PreparedStatement statement = c.prepareStatement("SELECT latest_name, month_guild_score, month_guild_kills, month_guild_deaths, month_guild_assists, month_guild_caps, month_guild_supps, month_guild_mvps, month_guild_streak FROM player_data WHERE guild=? ORDER BY " + order + " DESC LIMIT ? OFFSET ?");
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
						messages.add(chatColor + "" + id + "." + ChatColor.DARK_AQUA + " " + result.getString("latest_name") + ChatColor.WHITE + " " + result.getString("month_guild_score") + ChatColor.DARK_GREEN + " " + result.getString("month_guild_kills") + ChatColor.RED + " " + result.getString("month_guild_deaths") + ChatColor.GREEN + " " + result.getString("month_guild_assists") + ChatColor.GRAY + " " + result.getString("month_guild_caps") + ChatColor.BLUE + " " + result.getString("month_guild_supps") + ChatColor.GOLD + " " + result.getString("month_guild_mvps") + ChatColor.DARK_PURPLE + " " + result.getString("month_guild_streak"));
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