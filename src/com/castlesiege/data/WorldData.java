package com.castlesiege.data;

import java.util.ArrayList;

import org.bukkit.Location;

import com.castlesiege.data.guilds.GuildMap;

public class WorldData
{
	private String name;
	private int matchTimer;
	private Location[] matchBorder;
	private String world;
	public ArrayList<TeamData> teamData;
	public ArrayList<GateData> gateData;
	public ArrayList<ObjectiveData> objectiveData;
	public ArrayList<SiegeData> siegeData;
	public GuildMap guildMap;

	public WorldData(String name, int matchTimer, Location[] matchBorder, String world, ArrayList<TeamData> teamData, ArrayList<GateData> gateData, ArrayList<ObjectiveData> objectiveData, ArrayList<SiegeData> siegeData)
	{
		this.name = name;
		this.matchTimer = matchTimer;
		this.matchBorder = matchBorder;
		this.world = world;
		this.teamData = teamData;
		this.gateData = gateData;
		this.objectiveData = objectiveData;
		this.siegeData = siegeData;
		guildMap = GuildMap.valueOf(name.replace(" ", "").toUpperCase());
	}

	public String getName()
	{
		return name;
	}
	
	public int getMatchTimer()
	{
		return matchTimer;
	}
	
	public Location[] getMatchBorder()
	{
		return matchBorder;
	}

	public String getWorldName()
	{
		return world;
	}
}
