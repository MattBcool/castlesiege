package com.castlesiege.data.flags;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;

public class WallFlag extends Flag
{
	private ArrayList<Location> blocks = new ArrayList<Location>();

	@SuppressWarnings("deprecation")
	public WallFlag(Main plugin, ObjectiveData objectiveData)
	{
		super(plugin, objectiveData);

		World world = base.getWorld();
		double x = base.getBlockX();
		double y = base.getBlockY();
		double z = base.getBlockZ();
		if(base.getYaw() == 90 || base.getYaw() == -90)
		{
			blocks.add(new Location(world, x, y + 6, z + 1));
			blocks.add(new Location(world, x, y + 6, z - 1));
			blocks.add(new Location(world, x, y + 5, z + 1));
			blocks.add(new Location(world, x, y + 5, z - 1));
			blocks.add(new Location(world, x, y + 4, z + 1));
			blocks.add(new Location(world, x, y + 4, z - 1));
			blocks.add(new Location(world, x, y + 3, z + 1));
			blocks.add(new Location(world, x, y + 3, z - 1));
			blocks.add(new Location(world, x, y + 2, z + 1));
			blocks.add(new Location(world, x, y + 2, z - 1));
		}
		if(base.getYaw() == 0 || base.getYaw() == 180 || base.getYaw() == -180)
		{
			blocks.add(new Location(world, x + 2, y + 6, z));
			blocks.add(new Location(world, x - 2, y + 6, z));
			blocks.add(new Location(world, x + 2, y + 5, z));
			blocks.add(new Location(world, x - 2, y + 5, z));
			blocks.add(new Location(world, x + 2, y + 4, z));
			blocks.add(new Location(world, x - 2, y + 4, z));
			blocks.add(new Location(world, x + 2, y + 3, z));
			blocks.add(new Location(world, x - 2, y + 3, z));
			blocks.add(new Location(world, x + 2, y + 2, z));
			blocks.add(new Location(world, x - 2, y + 2, z));
		}

		for(Location loc : blocks)
		{
			if(controlling == null)
			{
				loc.getBlock().setData((byte) 8);
			}
			else
			{
				loc.getBlock().setData((byte) controlling.getWoolColor());
			}
		}
	}

	public void doCapture(Main plugin, TeamData teamData, int playerCount, int bannermanCount)
	{
		if(teamData != null)
		{
			if(playerCount > 6)
			{
				playerCount = 6;
			}
			double cap = (((double) (playerCount + bannermanCount) * 0.2) + 0.8) * (200d / timer / 20d);
			if(controlling != null)
			{
				if(!controlling.equals(teamData))
				{
					if(capped > 0)
					{
						capped = capDouble(capped - cap);
						didMessage = false;
					}

					if(capped <= 0)
					{
						switchTeam(plugin, teamData);
					}
				}
				else
				{
					if(capped < 100)
					{
						capped = capDouble(capped + cap);
					}
				}
			}
			else
			{
				if(capped > 0)
				{
					capped = capDouble(capped - cap);
					didMessage = false;
				}

				if(capped <= 0)
				{
					switchTeam(plugin, teamData);
				}
			}
			doCapMessages(plugin, teamData);
			changeWool();
		}
	}

	@SuppressWarnings("deprecation")
	public void changeWool()
	{
		int blockLoops = (int) (capped / 10d);
		int i = 0;
		for(Location loc : blocks)
		{
			if(i < blockLoops)
			{
				loc.getBlock().setType(Material.WOOL);
				loc.getBlock().setData((byte) controlling.getWoolColor());
			}
			else
			{
				loc.getBlock().setData((byte) 8);
			}
			i++;
		}
	}
}
