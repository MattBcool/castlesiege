package com.castlesiege.data.flags;

import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.player.ScoreType;

public class Flag
{
	protected String name;
	protected Location base;
	protected int timer;
	protected int radius;

	protected double capped;
	protected TeamData controlling;

	protected HashMap<TeamData, Location> wool;

	protected boolean didMessage;
	protected TeamData pointGiveTeam;
	protected int lastPointGive;

	public Flag(Main plugin, ObjectiveData objectiveData)
	{
		name = objectiveData.getName();
		base = objectiveData.getBase();
		timer = objectiveData.getTimer();
		radius = objectiveData.getRadius();

		capped = 100;

		wool = objectiveData.wool;

		didMessage = true;
		pointGiveTeam = null;
		lastPointGive = 5;

		if(objectiveData.getDefaultTeam() != null)
		{
			controlling = objectiveData.getDefaultTeam();
		}
		updateWool(plugin);
	}

	public TeamData getControlling()
	{
		return controlling;
	}

	public void doCapture(Main plugin, TeamData teamData, int playerCount, int bannermanCount)
	{}

	protected void switchTeam(Main plugin, TeamData teamData)
	{
		controlling = teamData;
		updateWool(plugin);
		for(Player p : Bukkit.getOnlinePlayers())
		{
			p.sendMessage(teamData.getChatColor() + "\u2691 The " + teamData.getName() + " have captured " + name + "! \u2691");
		}
	}

	protected void doCapMessages(Main plugin, TeamData teamData)
	{
		int capamt = 20;
		if(!didMessage && (pointGiveTeam == null || (pointGiveTeam != null && pointGiveTeam == teamData && lastPointGive != (int) (capped / capamt)) || (pointGiveTeam != null && pointGiveTeam != teamData)))
		{
			pointGiveTeam = teamData;
			lastPointGive = (int) (capped / capamt);
			int rounded = (int) Math.round(capped);
			if((rounded != 100 && controlling != teamData) || (controlling == teamData))
			{
				boolean doMessage = rounded >= 100 && !didMessage;
				for(Player p : Bukkit.getOnlinePlayers())
				{
					if(p.getVehicle() == null && !p.isDead() && plugin.datahandler.gamemodehandler.getTeam(p) == teamData && plugin.datahandler.gamemodehandler.inWorld(p.getLocation()) && p.getLocation().distance(base) < radius)
					{
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1f);
						// p.sendMessage(ChatColor.DARK_AQUA + "Capping " +
						// (int)Math.round(capped) + "%");
						//p.sendMessage(ChatColor.DARK_AQUA + "+1 flagcapping point(s)!");
						plugin.datahandler.utils.getCSPlayer(p).giveScore(ScoreType.CAPS, 1, false, true, true);
						if(doMessage)
						{
							//p.sendMessage(ChatColor.DARK_AQUA + "Fully captured.");
							TitleDisplay.sendActionBar(p, ChatColor.GOLD + "+1 flagcapping point(s)!" + ChatColor.BOLD + " Fully captured!");
						}
						else
						{
							TitleDisplay.sendActionBar(p, ChatColor.GOLD + "+1 flagcapping point(s)!");
						}
					}
				}
				if(doMessage)
				{
					didMessage = true;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected void updateWool(Main plugin)
	{
		for(TeamData td : wool.keySet())
		{
			if(controlling == null)
			{
				wool.get(td).getBlock().setData((byte) 8);
			}
			else
			{
				wool.get(td).getBlock().setData((byte) controlling.getWoolColor());
			}
		}
		for(Guild guild : plugin.datahandler.guildData)
		{
			guild.updateMap();
		}
	}
	
	protected double capDouble(double d)
	{
		if(d > 100)
		{
			d = 100;
		}
		if(d < 0)
		{
			d = 0;
		}
		return d;
	}
}
