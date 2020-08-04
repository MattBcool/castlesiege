package com.castlesiege.data.flags;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;

public class StreetFlag extends Flag
{
	private ArrayList<Location> blocks = new ArrayList<Location>();

	@SuppressWarnings("deprecation")
	public StreetFlag(Main plugin, ObjectiveData objectiveData)
	{
		super(plugin, objectiveData);

		name = objectiveData.getName();
		wool = objectiveData.wool;

		if(objectiveData.getDefaultTeam() != null)
		{
			controlling = objectiveData.getDefaultTeam();
		}

		World world = objectiveData.getBase().getWorld();
		blocks.add(new Location(world, -287, 16, -1295));
		blocks.add(new Location(world, -289, 16, -1294));
		blocks.add(new Location(world, -291, 17, -1292));
		blocks.add(new Location(world, -293, 19, -1291));
		blocks.add(new Location(world, -296, 18, -1292));
		blocks.add(new Location(world, -299, 18, -1293));
		blocks.add(new Location(world, -299, 17, -1297));
		blocks.add(new Location(world, -296, 16, -1300));
		blocks.add(new Location(world, -290, 15, -1303));
		blocks.add(new Location(world, -288, 16, -1301));

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
