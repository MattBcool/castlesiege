package com.castlesiege.data.flags;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;

public class PillarFlag extends Flag
{
	private ArrayList<Location> blocks = new ArrayList<Location>();

	@SuppressWarnings("deprecation")
	public PillarFlag(Main plugin, ObjectiveData objectiveData)
	{
		super(plugin, objectiveData);

		World world = base.getWorld();
		double x = base.getBlockX();
		double y = base.getBlockY();
		double z = base.getBlockZ();
		// if(base.getYaw() == 0)// TODO Add rotation
		{
			blocks.add(new Location(world, x + 3, y + 1, z - 2));
			blocks.add(new Location(world, x + 3, y + 2, z - 2));
			blocks.add(new Location(world, x + 3, y + 1, z + 3));
			blocks.add(new Location(world, x + 3, y + 2, z + 3));
			blocks.add(new Location(world, x - 2, y + 1, z + 3));
			blocks.add(new Location(world, x - 2, y + 2, z + 3));
			blocks.add(new Location(world, x - 2, y + 1, z - 2));
			blocks.add(new Location(world, x - 2, y + 2, z - 2));
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
		int blockLoops = (int) (capped / (25 / 2));
		int i = 0;
		for(Location loc : blocks)
		{
			if(i < blockLoops)
			{
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
