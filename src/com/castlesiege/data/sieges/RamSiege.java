package com.castlesiege.data.sieges;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;

import com.castlesiege.Main;
import com.castlesiege.data.TeamData;

public class RamSiege extends Siege
{
	private boolean completed = false;
	private boolean reset = true;
	private int stage = 0;
	private int hit = 0;

	public RamSiege(Location base, TeamData usable)
	{
		super(base, usable);
	}

	public boolean isCompleted()
	{
		return completed;
	}

	public boolean isReset()
	{
		return reset;
	}

	public boolean use(Main plugin)
	{
		if(!completed)
		{
			reset = false;
			pasteSchematic("siege_0_" + stage);
			if(stage < 2)
			{
				stage += 1;
			}
			else
			{
				if(hit >= 8)
				{
					completed = true;
					openGate();
					reset();
					base.getWorld().playSound(base, Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 1, 0.5f);
				}
				else
				{
					stage = 1;
					hit += 1;
					base.getWorld().playSound(base, Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, 1, 0.5f);
				}
			}
		}
		return false;
	}

	private void openGate()
	{
		World world = base.getWorld();
		double x = base.getBlockX();
		double y = base.getBlockY();
		double z = base.getBlockZ();
		ArrayList<Location> locs = new ArrayList<Location>();
		int yaw = (int) base.getYaw();
		if(yaw == 0 || yaw == -0)
		{
			locs.add(new Location(world, x, y, z + 8));
			locs.add(new Location(world, x + 1, y, z + 8));
			locs.add(new Location(world, x + 2, y, z + 8));
			locs.add(new Location(world, x, y + 1, z + 8));
			locs.add(new Location(world, x + 1, y + 1, z + 8));
			locs.add(new Location(world, x + 2, y + 1, z + 8));
			locs.add(new Location(world, x + 1, y + 2, z + 8));
		}
		if(yaw == 90)
		{
			locs.add(new Location(world, x - 8, y, z));
			locs.add(new Location(world, x - 8, y, z - 1));
			locs.add(new Location(world, x - 8, y, z - 2));
			locs.add(new Location(world, x - 8, y + 1, z));
			locs.add(new Location(world, x - 8, y + 1, z - 1));
			locs.add(new Location(world, x - 8, y + 1, z - 2));
			locs.add(new Location(world, x - 8, y + 2, z - 1));
		}
		if(yaw == -90)
		{
			locs.add(new Location(world, x + 8, y, z));
			locs.add(new Location(world, x + 8, y, z));
			locs.add(new Location(world, x + 8, y, z + 2));
			locs.add(new Location(world, x + 8, y, z + 2));

			locs.add(new Location(world, x + 8, y, z));
			locs.add(new Location(world, x + 8, y, z - 1));
			locs.add(new Location(world, x + 8, y, z - 2));
			locs.add(new Location(world, x + 8, y + 1, z));
			locs.add(new Location(world, x + 8, y + 1, z - 1));
			locs.add(new Location(world, x + 8, y + 1, z - 2));
			locs.add(new Location(world, x + 8, y + 2, z - 1));
		}
		if(yaw == 180 || yaw == -180)
		{
			locs.add(new Location(world, x, y, z - 8));
			locs.add(new Location(world, x + 1, y, z - 8));
			locs.add(new Location(world, x + 2, y, z - 8));
			locs.add(new Location(world, x, y + 1, z - 8));
			locs.add(new Location(world, x + 1, y + 1, z - 8));
			locs.add(new Location(world, x + 2, y + 1, z - 8));
			locs.add(new Location(world, x + 1, y + 2, z - 8));
		}

		for(Location loc : locs)
		{
			loc.getBlock().setType(Material.AIR);
		}
	}

	public void reset()
	{
		if(!reset)
		{
			pasteSchematic("siege_0_" + 0);
			reset = true;
		}
	}
}
