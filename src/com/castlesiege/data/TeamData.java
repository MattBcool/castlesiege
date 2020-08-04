package com.castlesiege.data;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class TeamData
{
	private String name;
	private ChatColor chatColor;
	private int woolColor;
	private Location spawn;
	public ArrayList<UUID> players;

	public TeamData(String name, ChatColor chatColor, int woolColor, Location spawn)
	{
		this.name = name;
		this.chatColor = chatColor;
		this.woolColor = woolColor;
		this.spawn = spawn;
		players = new ArrayList<UUID>();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ChatColor getChatColor()
	{
		return chatColor;
	}

	public void setChatColor(ChatColor chatColor)
	{
		this.chatColor = chatColor;
	}

	public int getWoolColor()
	{
		return woolColor;
	}

	public void setWoolColor(int woolColor)
	{
		this.woolColor = woolColor;
	}

	public Location getSpawn()
	{
		return spawn;
	}

	public void setSpawn(Location spawn)
	{
		this.spawn = spawn;
	}

	public void reset()
	{
		players.clear();
	}
}
