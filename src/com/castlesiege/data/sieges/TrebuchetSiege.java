package com.castlesiege.data.sieges;

import java.util.ArrayList;

import net.minecraft.server.v1_9_R2.Entity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.FallingSand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.castlesiege.Main;
import com.castlesiege.data.TeamData;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.player.ScoreType;

@SuppressWarnings("deprecation")
public class TrebuchetSiege extends Siege
{
	public ArrayList<Arrow> arrows;
	private boolean firing = false;
	private boolean reset = true;
	private int used = 0;

	private double u, l;

	public TrebuchetSiege(Location base, TeamData usable)
	{
		super(base, usable);

		arrows = new ArrayList<Arrow>();

		u = 0;
		l = 0;
	}

	public boolean use(Main plugin, Player p)
	{
		if(reset && !firing && used <= 9 && plugin.datahandler.gamemodehandler.getTeam(p) == usable)
		{
			plugin.datahandler.utils.getCSPlayer(p).giveScore(ScoreType.SUPPS, 3, false, true, true);
			// player.sendMessage(ChatColor.DARK_AQUA +
			// "+3 support point(s)!");
			TitleDisplay.sendActionBar(p, ChatColor.GOLD + "+3 support point(s)!");
			reset = false;
			firing = true;
			mid(plugin, 0.5);
			return true;
		}
		return false;
	}

	private void up(final Main plugin, double timer)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				base.getWorld().playSound(base, Sound.ENTITY_ENDERDRAGON_SHOOT, 1f, 0.6f);
				pasteSchematic("siege_1_3");
				fire(plugin);
				firing = false;
				mid(plugin, 2);
			}
		}, (int) (20 * (double) timer));
	}

	private void mid(final Main plugin, double timer)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if(firing)
				{
					base.getWorld().playSound(base, Sound.BLOCK_LADDER_STEP, 1f, 0.1f);
					pasteSchematic("siege_1_1");
					up(plugin, 0.5);
				}
				else
				{
					pasteSchematic("siege_1_2");
					down(plugin, 8);
				}
			}
		}, (int) (20 * (double) timer));
	}

	private void down(Main plugin, double timer)
	{
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				pasteSchematic("siege_1_0");
				reset = true;
				firing = false;
			}
		}, (int) (20 * (double) timer));
	}

	private void fire(Main plugin)
	{
		if(firing)
		{
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					base.getWorld().playSound(base, Sound.ENTITY_ENDERDRAGON_FLAP, 1f, 0.1f);
				}
			}, 5L);
			
			FallingSand fallingSand = (FallingSand) base.getWorld().spawnFallingBlock(base.clone().add(0, 5, 0), Material.STONE, (byte) 6);

			Entity entity = ((CraftEntity) fallingSand).getHandle();
			entity.setSize(0f, 0f);
			used += 1;
			double pitch = (((base.getPitch() + 90) + (u * -1)) * Math.PI) / 180;
			double yaw = (((base.getYaw() + 90) + (l * -1)) * Math.PI) / 180;

			double x = (Math.sin(pitch) * Math.cos(yaw)) * 3;
			double y = (Math.sin(pitch) * Math.sin(yaw)) * 3;
			double z = (Math.cos(pitch));

			final Vector vector = new Vector(x, z, y);
			final Arrow arrow = base.getWorld().spawnArrow(base.clone().add(0, 25, 0), new Vector(0, 0, 0), 0.6f, 12);
			arrow.setPassenger(fallingSand);
			arrows.add(arrow);
			arrow.setVelocity(vector);
		}
	}

	public void setUpDown(Main plugin, Sign sign, Player p, boolean up)
	{
		String line = sign.getLine(2);
		if(line.contains("U") && plugin.datahandler.gamemodehandler.getTeam(p) == usable)
		{
			if(up)
			{
				u += 0.5;
			}
			else
			{
				u -= 0.5;
			}
			u = cap(u);
			sign.setLine(2, "U >  " + u + "  < D");
			sign.update(true);
		}
	}

	public void setLeftRight(Main plugin, Sign sign, Player p, boolean left)
	{
		String line = sign.getLine(2);
		if(line.contains("L") && plugin.datahandler.gamemodehandler.getTeam(p) == usable)
		{
			if(left)
			{
				l += 0.5;
			}
			else
			{
				l -= 0.5;
			}
			l = cap(l);
			sign.setLine(2, "L >  " + l + "  < R");
			sign.update(true);
		}
	}

	private double cap(double d)
	{
		if(d > 20)
		{
			d = 20;
		}
		if(d < -20)
		{
			d = -20;
		}
		return d;
	}
}
