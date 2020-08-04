package com.castlesiege.data.flags;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;

public class TableFlag extends Flag
{
	private ArrayList<Location> blocks = new ArrayList<Location>();

	@SuppressWarnings("deprecation")
	public TableFlag(Main plugin, ObjectiveData objectiveData)
	{
		super(plugin, objectiveData);

		World world = base.getWorld();
		double x = base.getBlockX();
		double y = base.getBlockY();
		double z = base.getBlockZ();
		// if(base.getYaw() == 90 || base.getYaw() == -90)
		{
			blocks.add(new Location(world,x+5,y,z-1));
			blocks.add(new Location(world,x+5,y,z));
			blocks.add(new Location(world,x+5,y,z+1));
			blocks.add(new Location(world,x+5,y,z+2));

			blocks.add(new Location(world,x+4,y,z+2));
			blocks.add(new Location(world,x+4,y,z+1));
			blocks.add(new Location(world,x+4,y,z));
			blocks.add(new Location(world,x+4,y,z-1));

			blocks.add(new Location(world,x+3,y,z-1));
			blocks.add(new Location(world,x+3,y,z));
			blocks.add(new Location(world,x+3,y,z+1));
			blocks.add(new Location(world,x+3,y,z+2));

			blocks.add(new Location(world,x+2,y,z+2));
			blocks.add(new Location(world,x+2,y,z+1));
			blocks.add(new Location(world,x+2,y,z));
			blocks.add(new Location(world,x+2,y,z-1));

			blocks.add(new Location(world,x+1,y,z-1));
			blocks.add(new Location(world,x+1,y,z));
			blocks.add(new Location(world,x+1,y,z+1));
			blocks.add(new Location(world,x+1,y,z+2));

			blocks.add(new Location(world,x,y,z+2));
			blocks.add(new Location(world,x,y,z+1));
			blocks.add(new Location(world,x,y,z));
			blocks.add(new Location(world,x,y,z-1));

			blocks.add(new Location(world,x-1,y,z-1));
			blocks.add(new Location(world,x-1,y,z));
			blocks.add(new Location(world,x-1,y,z+1));
			blocks.add(new Location(world,x-1,y,z+2));

			blocks.add(new Location(world,x-2,y,z+2));
			blocks.add(new Location(world,x-2,y,z+1));
			blocks.add(new Location(world,x-2,y,z));
			blocks.add(new Location(world,x-2,y,z-1));

			blocks.add(new Location(world,x-3,y,z-1));
			blocks.add(new Location(world,x-3,y,z));
			blocks.add(new Location(world,x-3,y,z+1));
			blocks.add(new Location(world,x-3,y,z+2));

			blocks.add(new Location(world,x-4,y,z+2));
			blocks.add(new Location(world,x-4,y,z+1));
			blocks.add(new Location(world,x-4,y,z));
			blocks.add(new Location(world,x-4,y,z-1));

			blocks.add(new Location(world,x-5,y,z-1));
			blocks.add(new Location(world,x-5,y,z));
			blocks.add(new Location(world,x-5,y,z+1));
			blocks.add(new Location(world,x-5,y,z+2));
		}
		/*
		 * if(base.getYaw() == 0 || base.getYaw() == 180 || base.getYaw() ==
		 * -180) {}
		 */
		for(Location loc : blocks)
		{
			if(controlling == null)
			{
				loc.getBlock().setData((byte) 8);
				Block bl = loc.clone().add(0, 1, 0).getBlock();
				if(bl.getType() == Material.CARPET)
				{
					bl.setData((byte) 8);
				}
			}
			else
			{
				loc.getBlock().setData((byte) controlling.getWoolColor());
				Block bl = loc.clone().add(0, 1, 0).getBlock();
				if(bl.getType() == Material.CARPET)
				{
					bl.setData((byte) controlling.getWoolColor());
				}
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
		int blockLoops = (int) (capped / (25 / 11));
		int i = 0;
		for(Location loc : blocks)
		{
			if(i < blockLoops)
			{
				Block b = loc.getBlock();
				if(b.getType() == Material.WOOL)
				{
					b.setData((byte) controlling.getWoolColor());
				}
				Block bl = loc.clone().add(0, 1, 0).getBlock();
				if(bl.getType() == Material.CARPET)
				{
					bl.setData((byte) controlling.getWoolColor());
				}
			}
			else
			{
				Block b = loc.getBlock();
				if(b.getType() == Material.WOOL)
				{
					b.setData((byte) 8);
				}
				Block bl = loc.clone().add(0, 1, 0).getBlock();
				if(bl.getType() == Material.CARPET)
				{
					bl.setData((byte) 8);
				}
			}
			i++;
		}
	}
}
