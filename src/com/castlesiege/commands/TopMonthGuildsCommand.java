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
import com.castlesiege.util.sql.SQLConnection;

public class TopMonthGuildsCommand implements CommandExecutor
{
	private Main plugin;

	public TopMonthGuildsCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		String order = "month_guild_score";
		switch(label)
		{
		case "topmonthguildskill":
		case "topmonthguildskills":
			order = "month_guild_kills";
			break;
		case "topmonthguildsdeath":
		case "topmonthguildsdeaths":
			order = "month_guild_deaths";
			break;
		case "topmonthguildsassist":
		case "topmonthguildsassists":
			order = "month_guild_assists";
			break;
		case "topmonthguildscap":
		case "topmonthguildscaps":
		case "topmonthguildscapture":
		case "topmonthguildscaptures":
			order = "month_guild_caps";
			break;
		case "topmonthguildssup":
		case "topmonthguildssups":
		case "topmonthguildssupp":
		case "topmonthguildssupps":
		case "topmonthguildssupport":
		case "topmonthguildssupports":
			order = "month_guild_supps";
			break;
		case "topmonthguildsmvp":
		case "topmonthguildsmvps":
			order = "month_guild_mvps";
			break;
		case "topmonthguildsstreak":
		case "topmonthguildsstreaks":
		case "topmonthguildskillstreak":
		case "topmonthguildskillstreaks":
			order = "month_guild_streak";
			break;
		}
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(ChatColor.AQUA + "#. " + ChatColor.DARK_AQUA + "G" + ChatColor.AQUA + "uild " + ChatColor.WHITE + "S" + ChatColor.AQUA + "core " + ChatColor.DARK_GREEN + "K" + ChatColor.AQUA + "ills " + ChatColor.RED + "D" + ChatColor.AQUA + "eaths " + ChatColor.GREEN + "A" + ChatColor.AQUA + "ssists " + ChatColor.GRAY + "C" + ChatColor.AQUA + "aps " + ChatColor.BLUE + "S" + ChatColor.AQUA + "upps " + ChatColor.GOLD + "M" + ChatColor.AQUA + "VPs " + ChatColor.DARK_PURPLE + "S" + ChatColor.AQUA + "treak");
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
				try
				{
					PreparedStatement statement = c.prepareStatement("SELECT guild_name FROM guild_data ORDER BY " + order + " DESC");
					ResultSet result = statement.executeQuery();

					while(result.next())
					{
						if(result.getString("guild_name").equalsIgnoreCase(args[0]))
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
					sender.sendMessage(ChatColor.RED + "Guild not found, please specify a valid player!");
					return false;
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
			PreparedStatement statement = c.prepareStatement("SELECT guild_name, month_guild_score, month_guild_kills, month_guild_deaths, month_guild_assists, month_guild_caps, month_guild_supps, month_guild_mvps, month_guild_streak FROM guild_data ORDER BY " + order + " DESC LIMIT ? OFFSET ?");
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
				messages.add(chatColor + "" + id + "." + ChatColor.DARK_AQUA + " " + result.getString("guild_name") + ChatColor.WHITE + " " + result.getString("month_guild_score") + ChatColor.DARK_GREEN + " " + result.getString("month_guild_kills") + ChatColor.RED + " " + result.getString("month_guild_deaths") + ChatColor.GREEN + " " + result.getString("month_guild_assists") + ChatColor.GRAY + " " + result.getString("month_guild_caps") + ChatColor.BLUE + " " + result.getString("month_guild_supps") + ChatColor.GOLD + " " + result.getString("month_guild_mvps") + ChatColor.DARK_PURPLE + " " + result.getString("month_guild_streak"));
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