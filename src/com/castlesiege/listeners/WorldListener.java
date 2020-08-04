package com.castlesiege.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.castlesiege.Main;

public class WorldListener implements Listener
{
	private Main plugin;

	public WorldListener(Main plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	private void onWeatherChange(WeatherChangeEvent e)
	{
		e.setCancelled(true);
	}

	@EventHandler
	private void onChunkUnload(ChunkUnloadEvent e)
	{
		for(Entity en : e.getChunk().getEntities())
		{
			if(en instanceof ArmorStand)
			{
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent e)
	{
		Block b = e.getBlock();
		if(b.getType() != null && b.getType() == Material.LADDER)
		{
			e.setCancelled(true);
		}
	}
}
