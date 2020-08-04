package com.castlesiege.data;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;

import com.castlesiege.Main;
import com.castlesiege.data.gates.Gate;
import com.castlesiege.data.gates.NormalGate;

public class GateData
{
	private String name;
	private int type, timer;
	private Sound sound;
	private Float volume, pitch;
	private Location base;
	public ArrayList<Location> locs;
	private Gate gate;

	public GateData(String name, int type, int timer, Sound sound, Float volume, Float pitch, Location base, ArrayList<Location> locs)
	{
		this.name = name;
		this.type = type;
		this.timer = timer;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.base = base;
		this.locs = locs;
	}

	public String getName()
	{
		return name;
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

	public Gate getGate()
	{
		return gate;
	}

	public void setup(Main plugin)
	{
		gate = new NormalGate(base, type, timer, sound, volume, pitch);
		/*switch(type)
		{
		case 0:
			gate = new NormalGate(base, type, timer, sound, volume, pitch);
			break;
		}*/
	}
}
