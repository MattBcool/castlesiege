package com.castlesiege.data.gates;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

import com.castlesiege.Main;

public class NormalGate extends Gate
{
	private Sound sound;
	private float volume;
	private float pitch;

	private boolean running;

	public NormalGate(Location base, int type, int timer, Sound sound, float volume, float pitch)
	{
		super(base, type, timer);

		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	public boolean use(Main plugin)
	{
		if(!running)
		{
			running = true;
			mid(plugin);
			return true;
		}
		return false;
	}

	protected void open(Main plugin)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				pasteSchematic("gate_" + type + "_2");
				base.getWorld().playSound(base, sound, volume, pitch);
				opened = true;
				running = false;
			}
		}, (int)(20 * ((double)timer / 2d)));
	}

	private void mid(final Main plugin)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				pasteSchematic("gate_" + type + "_1");
				base.getWorld().playSound(base, sound, volume, pitch);
				if(opened)
				{
					close(plugin);
				}
				else
				{
					open(plugin);
				}
			}
		}, (int)(20 * ((double)timer / 2d)));
	}

	protected void close(Main plugin)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				pasteSchematic("gate_" + type + "_0");
				base.getWorld().playSound(base, sound, volume, pitch);
				opened = false;
				running = false;
			}
		}, (int)(20 * ((double)timer / 2d)));
	}
}
