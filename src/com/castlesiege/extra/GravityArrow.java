package com.castlesiege.extra;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.castlesiege.Main;

public class GravityArrow
{
	private Main plugin;
	private int id;
	private Entity e;
	private Vector v;

	public GravityArrow(Main plugin, Entity e, Vector v)
	{
		this.plugin = plugin;
		this.e = e;
		this.v = v;
	}

	public void start()
	{
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if(e.isValid() && !e.isOnGround())
				{
					e.setVelocity(v);
				}
				else
				{
					Bukkit.getScheduler().cancelTask(id);
				}
			}

		}, 0, 2);
	}
}
