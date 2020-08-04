package com.castlesiege.data;

import java.util.HashMap;

import org.bukkit.Location;

import com.castlesiege.Main;
import com.castlesiege.data.flags.Flag;
import com.castlesiege.data.flags.InvisibleFlag;
import com.castlesiege.data.flags.MovingFlag;
import com.castlesiege.data.flags.PillarFlag;
import com.castlesiege.data.flags.StreetFlag;
import com.castlesiege.data.flags.TFlag;
import com.castlesiege.data.flags.TableFlag;
import com.castlesiege.data.flags.TownHallFlag;
import com.castlesiege.data.flags.WallFlag;

public class ObjectiveData
{
	private String name, defaultTeamName;
	private TeamData defaultTeam;
	private int type, radius, timer;
	private boolean capturable, spawnable;
	private Location base, spawn;
	public HashMap<TeamData, Location> wool;
	private Flag flag;

	public ObjectiveData(String name, String defaultTeamName, int type, int radius, int timer, boolean capturable, boolean spawnable, Location base, Location spawn, HashMap<TeamData, Location> wool)
	{
		this.name = name;
		this.defaultTeamName = defaultTeamName;
		this.defaultTeam = null;
		this.type = type;
		this.radius = radius;
		this.timer = timer;
		this.capturable = capturable;
		this.spawnable = spawnable;
		this.base = base;
		this.spawn = spawn;
		this.wool = wool;
	}

	public String getName()
	{
		return name;
	}

	public TeamData getDefaultTeam()
	{
		return defaultTeam;
	}

	public int getType()
	{
		return type;
	}

	public int getRadius()
	{
		return radius;
	}

	public int getTimer()
	{
		return timer;
	}

	public boolean isCapturable()
	{
		return capturable;
	}
	
	public boolean isSpawnable()
	{
		return spawnable;
	}

	public Location getBase()
	{
		return base;
	}

	public Location getSpawn()
	{
		return spawn;
	}

	public Flag getFlag()
	{
		return flag;
	}

	public void setup(Main plugin)
	{
		defaultTeam = plugin.datahandler.gamemodehandler.getTeamFromName(defaultTeamName);

		if(defaultTeam != null)
		{
			switch(type)
			{
			case 0:
				flag = new InvisibleFlag(plugin, this);
				break;
			case 1:
				flag = new MovingFlag(plugin, this);
				break;
			case 2:
				flag = new StreetFlag(plugin, this);
				break;
			case 3:
				flag = new TownHallFlag(plugin, this);
				break;
			case 4:
				flag = new TFlag(plugin, this);
				break;
			case 5:
				flag = new WallFlag(plugin, this);
				break;
			case 6:
				flag = new TableFlag(plugin, this);
				break;
			case 7:
				flag = new PillarFlag(plugin, this);
				break;
			}
		}
	}
}
