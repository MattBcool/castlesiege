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

public class TopCommand implements CommandExecutor
{
	private Main plugin;

	public TopCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		String order = "total_score";
		switch(label)
		{
		case "topkill":
		case "topkills":
			order = "total_kills";
			break;
		case "topdeath":
		case "topdeaths":
			order = "total_deaths";
			break;
		case "topassist":
		case "topassists":
			order = "total_assists";
			break;
		case "topcap":
		case "topcaps":
		case "topcapture":
		case "topcaptures":
			order = "total_caps";
			break;
		case "topsup":
		case "topsups":
		case "topsupp":
		case "topsupps":
		case "topsupport":
		case "topsupports":
			order = "total_supps";
			break;
		case "topmvp":
		case "topmvps":
			order = "total_mvps";
			break;
		case "topstreak":
		case "topstreaks":
		case "topkillstreak":
		case "topkillstreaks":
			order = "highest_streak";
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
						PreparedStatement statement = c.prepareStatement("SELECT uuid FROM player_data ORDER BY " + order + " DESC");
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
			PreparedStatement statement = c.prepareStatement("SELECT latest_name, total_score, total_kills, total_deaths, total_assists, total_caps, total_supps, total_mvps, highest_streak FROM player_data ORDER BY " + order + " DESC LIMIT ? OFFSET ?");
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
				messages.add(chatColor + "" + id + "." + ChatColor.DARK_AQUA + " " + result.getString("latest_name") + ChatColor.WHITE + " " + result.getString("total_score") + ChatColor.DARK_GREEN + " " + result.getString("total_kills") + ChatColor.RED + " " + result.getString("total_deaths") + ChatColor.GREEN + " " + result.getString("total_assists") + ChatColor.GRAY + " " + result.getString("total_caps") + ChatColor.BLUE + " " + result.getString("total_supps") + ChatColor.GOLD + " " + result.getString("total_mvps") + ChatColor.DARK_PURPLE + " " + result.getString("highest_streak"));
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