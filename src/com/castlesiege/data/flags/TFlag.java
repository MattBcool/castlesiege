package com.castlesiege.data.flags;

import java.util.HashMap;

import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;

public class TFlag extends Flag
{
	public HashMap<Entity, Location> stands;
	private int t1 = 0;
	private int t2 = 0;

	public TFlag(Main plugin, ObjectiveData objectiveData)
	{
		super(plugin, objectiveData);

		stands = new HashMap<Entity, Location>();
		controlling = objectiveData.getDefaultTeam();

		generateFlag();
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
						setFlagWool(teamData);
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
				}

				if(capped <= 0)
				{
					switchTeam(plugin, teamData);
					setFlagWool(teamData);
				}
			}
			doCapMessages(plugin, teamData);
		}
	}

	private void setFlagWool(TeamData teamData)
	{
		for(Entity stand : stands.keySet())
		{
			((ArmorStand) stand).setHelmet(new ItemStack(Material.WOOL, 1, (byte) teamData.getWoolColor()));
		}
	}

	private void generateFlag()
	{
		for(Entity e : stands.keySet())
		{
			if(e != null)
			{
				e.remove();
			}
		}
		stands.clear();
		for(int i = 0; i < 10; i++)
		{
			Location loc = base.clone().add(0.5, 0, 0.5);
			loc.getChunk().load();
			ArmorStand stand = ((ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND));
			stand.setGravity(false);
			stand.setVisible(false);

			EntityArmorStand nmsarmorstand = ((CraftArmorStand) stand).getHandle();

			NBTTagCompound compoundTag = new NBTTagCompound();
			nmsarmorstand.c(compoundTag);
			compoundTag.setBoolean("Marker", true);
			compoundTag.setBoolean("NoBasePlate", true);
			nmsarmorstand.f(compoundTag);

			if(controlling == null)
			{
				stand.setHelmet(new ItemStack(Material.WOOL, 1, (byte) 8));
			}
			else
			{
				stand.setHelmet(new ItemStack(Material.WOOL, 1, (byte) controlling.getWoolColor()));
			}
			stands.put(stand, loc);
		}
	}

	public void doIdleAnimation()
	{
		if(t1 <= 50)
		{
			t1++;
		}
		else
		{
			t1 = 0;
		}

		t2++;

		if(t2 > 3600)
		{
			t2 = 0;
		}

		int id = 0;
		int row = 0;
		for(Entity stand : stands.keySet())
		{
			double val = warpValue(t1 + (id), 50);
			double val2 = warpValue(t1 + (id + 1), 50);
			double offset2 = (Math.cos(((double) val / 25d) * Math.PI) * 0.75) * ((double) (id) / 10d);
			double offsetFirst = (Math.cos(((double) val2 / 25d) * Math.PI) * 0.75) * ((double) (id + 1) / 10d);

			double offset1 = Math.abs(offsetFirst * 0.25d);
			// offset1-=((double)row*10d) * 0.2;
			double angle = base.getYaw();
			double x = (lengthdir_x(offset1, angle) - lengthdir_y(offset2, angle)) * (capped / 100);
			double y = 2 + (-(id * ((-10 - (-capped)) * 0.00515)) * 1.35d);
			double z = (lengthdir_y(offset1, angle) + lengthdir_x(offset2, angle)) * (capped / 100);

			if(y + stands.get(stand).getY() > base.getBlockY() + 2)
			{
				y = base.getBlockY() + 2;
			}
			
			float yaw = base.getYaw();
			Location loc;
			if(yaw == 90 || yaw == -90)
			{
				if(row == 0)
				{
					
					loc = stands.get(stand).clone().add(x, y, z - 2);
				}
				else
				{
					loc = stands.get(stand).clone().add(x, y, z + 2);
				}

			}
			else
			{
				if(row == 0)
				{
					loc = stands.get(stand).clone().add(x - 2, y, z);
				}
				else
				{
					loc = stands.get(stand).clone().add(x + 2, y, z);
				}
			}
			loc.setYaw((float) (angle + ((offsetFirst * 15d))));
			loc.getChunk().load();
			if(stand != null)
			{
				stand.teleport(loc);
			}
			if(id < 4)
			{
				id += 1;
			}
			else
			{
				id = 0;
				row++;
			}
		}
	}

	private double lengthdir_x(double i, double angle)
	{
		return Math.cos(angle * (Math.PI / 180d)) * i;
	}

	private double lengthdir_y(double i, double angle)
	{
		return Math.sin(angle * (Math.PI / 180d)) * i;
	}

	private int warpValue(int d, int m)
	{
		int val = d;
		if(d > m)
		{
			val = d - m;
		}
		return val;
	}
}
