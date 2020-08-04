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

import com.castlesiege.Main;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.util.sql.SQLConnection;

public class TopTimeCommand implements CommandExecutor
{
	private Main plugin;

	public TopTimeCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("toptime") || label.equalsIgnoreCase("toptimeplayed") || label.equalsIgnoreCase("toptotaltime") || label.equalsIgnoreCase("toptotaltimeplayed") || label.equalsIgnoreCase("topplaytime") || label.equalsIgnoreCase("toptotalplaytime"))
		{
			ArrayList<String> messages = new ArrayList<String>();
			messages.add(ChatColor.AQUA + "#. " + ChatColor.DARK_AQUA + "P" + ChatColor.AQUA + "layer " + ChatColor.WHITE + "D" + ChatColor.AQUA + "ays " + ChatColor.DARK_GREEN + "H" + ChatColor.AQUA + "ours " + ChatColor.RED + "M" + ChatColor.AQUA + "inutes " + ChatColor.GREEN + "S" + ChatColor.AQUA + "econds");
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
							PreparedStatement statement = c.prepareStatement("SELECT uuid FROM player_data ORDER BY total_time_played DESC");
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
							sender.sendMessage(ChatColor.RED + "Player not found, please specify a valid player!");
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
				PreparedStatement statement = c.prepareStatement("SELECT latest_name, total_time_played FROM player_data ORDER BY total_time_played DESC LIMIT ? OFFSET ?");
				statement.setInt(1, limit);
				statement.setInt(2, offset);
				ResultSet result = statement.executeQuery();

				int id = offset + 1;
				while(result.next())
				{
					ChatColor chatColor = ChatColor.DARK_GRAY;
					if(doColor)
					{
						chatColor = id == used ? ChatColor.GRAY : ChatColor.DARK_GRAY;
					}
					int timePlayed = result.getInt("total_time_played");
					int day = timePlayed / 60 / 60 / 24;
					int hour = timePlayed / 60 / 60 % 24;
					int minute = timePlayed / 60 % 60;
					int second = timePlayed % 60;
					messages.add(chatColor + "" + id + "." + ChatColor.DARK_AQUA + " " + result.getString("latest_name") + ChatColor.WHITE + " " + day + ChatColor.DARK_GREEN + " " + hour + ChatColor.RED + " " + minute + ChatColor.GREEN + " " + second);
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