package com.castlesiege.data;

import org.bukkit.Location;

import com.castlesiege.Main;
import com.castlesiege.data.sieges.RamSiege;
import com.castlesiege.data.sieges.Siege;
import com.castlesiege.data.sieges.TrebuchetSiege;

public class SiegeData
{
	private String name, usableTeamName;
	private TeamData usableTeam;
	private int type, timer;
	private Location base;
	private Siege siege;

	public SiegeData(String name, String defaultTeamName, int type, int timer, Location base)
	{
		this.name = name;
		this.usableTeamName = defaultTeamName;
		usableTeam = null;
		this.type = type;
		this.timer = timer;
		this.base = base;
	}

	public String getName()
	{
		return name;
	}
	
	public TeamData getUsableTeam()
	{
		return usableTeam;
	}

	public int getType()
	{
		return type;
	}

	public int getTimer()
	{
		return timer;
	}

	public Location getBase()
	{
		return base;
	}

	public Siege getSiege()
	{
		return siege;
	}

	public void setup(Main plugin)
	{
		usableTeam = plugin.datahandler.gamemodehandler.getTeamFromName(usableTeamName);
		if(usableTeam != null)
		{
			switch(type)
			{
			case 0:
				siege = new RamSiege(base, usableTeam);
				break;
			case 1:
				siege = new TrebuchetSiege(base, usableTeam);
				break;
			}
		}
	}
}
