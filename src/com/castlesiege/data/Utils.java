package com.castlesiege.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.util.sql.SQLConnection;

public class Utils
{
	Main plugin;

	public Utils(Main plugin)
	{
		this.plugin = plugin;
	}

	public CSPlayer getCSPlayer(Player p)
	{
		for(CSPlayer player : plugin.datahandler.playerData)
		{
			if(player.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString()))
			{
				return player;
			}
		}
		return null;
	}

	public Guild getGuild(String name)
	{
		for(Guild guild : plugin.datahandler.guildData)
		{
			if(guild.getName().equalsIgnoreCase(name))
			{
				return guild;
			}
		}
		return null;
	}

	public void checkMonth()
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;
		int month = getMonth();
		try
		{
			PreparedStatement statement = c.prepareStatement("SELECT uuid, month FROM player_data");
			ResultSet result = statement.executeQuery();
			while(result.next())
			{
				if(result.getInt("month") != month)
				{
					PreparedStatement s = c.prepareStatement("UPDATE player_data SET month=?, month_score=?, month_kills=?, month_deaths=?, month_assists=?, month_caps=?, month_supps=?, month_mvps=?, month_streak=?, month_guild_score=?, month_guild_kills=?, month_guild_deaths=?, month_guild_assists=?, month_guild_caps=?, month_guild_supps=?, month_guild_mvps=?, month_guild_streak=? WHERE uuid=?");
					s.setInt(1, month);
					s.setInt(2, 0);
					s.setInt(3, 0);
					s.setInt(4, 0);
					s.setInt(5, 0);
					s.setInt(6, 0);
					s.setInt(7, 0);
					s.setInt(8, 0);
					s.setInt(9, 0);
					s.setInt(10, 0);
					s.setInt(11, 0);
					s.setInt(12, 0);
					s.setInt(13, 0);
					s.setInt(14, 0);
					s.setInt(15, 0);
					s.setInt(16, 0);
					s.setInt(17, 0);
					s.setString(18, result.getString("uuid"));
					s.execute();
					s.close();
				}
			}
			statement.close();
			result.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			PreparedStatement statement = c.prepareStatement("SELECT guild_name, month FROM guild_data");
			ResultSet result = statement.executeQuery();
			while(result.next())
			{
				if(result.getInt("month") != month)
				{
					PreparedStatement s = c.prepareStatement("UPDATE guild_data SET month=?, month_guild_score=?, month_guild_kills=?, month_guild_deaths=?, month_guild_assists=?, month_guild_caps=?, month_guild_supps=?, month_guild_mvps=?, month_guild_streak=? WHERE guild_name=?");
					s.setInt(1, month);
					s.setInt(2, 0);
					s.setInt(3, 0);
					s.setInt(4, 0);
					s.setInt(5, 0);
					s.setInt(6, 0);
					s.setInt(7, 0);
					s.setInt(8, 0);
					s.setInt(9, 0);
					s.setString(10, result.getString("guild_name"));
					s.execute();
					s.close();
				}
			}
			statement.close();
			result.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public boolean isInteger(String s)
	{
		try
		{
			Integer.parseInt(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		catch(NullPointerException e)
		{
			return false;
		}
		return true;
	}
	
	public boolean isDouble(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		catch(NullPointerException e)
		{
			return false;
		}
		return true;
	}

	public boolean isAlpha(String s)
	{
		s = s.replace(" ", "");
		for(Character c : s.toCharArray())
		{
			if(!Character.isLetter(c))
				return false;
		}
		return true;
	}

	public int getMonth()
	{
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MONTH);
	}

	public String fixInt(int fix)
	{
		String fixed = fix + "";
		if(!(fixed.length() > 1))
		{
			fixed = "0" + fixed;
		}
		return fixed;
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public <K extends Comparable, V extends Comparable> Map<K, V> sortByKeys(Map<K, V> map)
	{
		List<K> keys = new LinkedList<K>(map.keySet());
		Collections.sort(keys);

		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();
		for(K key : keys)
		{
			sortedMap.put(key, map.get(key));
		}
		return sortedMap;
	}

	public int getLevel(int score, int pow)
	{
		return checkLevel(score, pow, 0);
	}

	public int getMaxExp(int score, int pow)
	{
		return (int) (Math.pow(getLevel(score, pow), pow) / 2) + 1;
	}

	private int checkLevel(int score, int pow, int toCheck)
	{
		if(score >= (Math.pow(toCheck, pow) / 2) && toCheck + 1 <= 100)
		{
			return checkLevel(score, pow, toCheck + 1);
		}
		return toCheck;
	}

	public ChatColor getLevelColor(int score, int pow)
	{
		int group = (int) getLevel(score, pow) / 10;
		ChatColor chatColor = ChatColor.GRAY;
		switch(group)
		{
		case 2:
		case 3:
			chatColor = ChatColor.BLUE;
			break;
		case 4:
		case 5:
			chatColor = ChatColor.DARK_GREEN;
			break;
		case 6:
		case 7:
			chatColor = ChatColor.GOLD;
			break;
		case 8:
		case 9:
			chatColor = ChatColor.RED;
		case 10:
			chatColor = ChatColor.DARK_RED;
		}
		return chatColor;
	}
}
