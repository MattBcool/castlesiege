package com.castlesiege.classes;

import net.md_5.bungee.api.ChatColor;

import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;

public enum CSClasses
{
	SPEARMAN(null, 0, false), SKIRMISHER(SPEARMAN, 10, false), HALBERDIER(SKIRMISHER, 20, false), SWORDSMAN(null, 0, false), VANGUARD(SWORDSMAN, 10, false), MACEMAN(VANGUARD, 20, false), KNIGHT(null, 0, false), PIKEMAN(KNIGHT, 10, false), AXEMAN(PIKEMAN, 20, false), ARCHER(null, 0, false), SIEGE_ARCHER(ARCHER, 10, false), CROSSBOWMAN(SIEGE_ARCHER, 20, false), CAVALRY(null, 0, false), RANGED_CAVALRY(CAVALRY, 10, false), LANCER(RANGED_CAVALRY, 20, false), BANNERMAN(null, 0, true),
	// TBD
	MEDIC(null, 0, true);

	private CSClasses unlocked;
	private int level;
	private boolean voter;

	CSClasses(CSClasses unlocked, int level, boolean voter)
	{
		this.unlocked = unlocked;
		this.level = level;
		this.voter = voter;
	}

	public boolean getVoter()
	{
		return voter;
	}

	public String getName()
	{
		return WordUtils.capitalizeFully(this.toString().replace("_", " "));
	}

	public String[] getLore(Main plugin, Player p)
	{
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		int score = csplayer.classStats.getClassStat(getName()).getFinalScore();
		String level = "", exp = "", progress = "";
		level = ChatColor.GRAY + "Level: " + ChatColor.DARK_GRAY + plugin.datahandler.utils.getLevel(score, 3) + "";
		exp = ChatColor.GRAY + "Exp: " + ChatColor.DARK_GRAY + score + " / " + plugin.datahandler.utils.getMaxExp(score, 3);
		for(int i = 0; i < 20; i++)
		{
			if((double) score / (double) plugin.datahandler.utils.getMaxExp(score, 3) >= (double) (1d / 20d) * i)
			{
				progress += ChatColor.DARK_GRAY + "|";
			}
			else
			{
				progress += ChatColor.GRAY + "|";
			}
		}
		progress = ChatColor.GRAY + "Progress: " + progress;
		return new String[]
		{ level, exp, progress };
	}

	public String[] getUnlockedLore(Main plugin, Player p)
	{
		if(voter)
		{
			if(!unlocked(plugin, p))
			{
				return new String[]
				{ ChatColor.GRAY + "Requirements:", ChatColor.DARK_GRAY + "* Vote on all sites" };
			}
		}
		else
		{
			if(!unlocked(plugin, p))
			{
				return new String[]
				{ ChatColor.GRAY + "Requirements:", ChatColor.DARK_GRAY + "* Reach level " + level, ChatColor.DARK_GRAY + "  with " + unlocked.getName() };
			}
		}
		return new String[]
		{};
	}

	public boolean unlocked(Main plugin, Player p)
	{
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		if(csplayer != null)
		{
			if(unlocked == null)
			{
				if(voter)
				{
					if(csplayer.allVotes())
					{
						return true;
					}
				}
				else
				{
					return true;
				}
			}
			else
			{
				if(plugin.datahandler.utils.getLevel(csplayer.classStats.getClassStat(unlocked.getName()).getFinalScore(), 3) >= level)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static CSClass getClassByName(Main plugin, Player p, String name)
	{
		for(CSClasses classes : values())
		{
			if(name.equalsIgnoreCase(classes.toString().toLowerCase()))
			{
				switch(name)
				{
				case "spearman":
					return new ClassSpearman(plugin, p);
				case "skirmisher":
					return new ClassSkirmisher(plugin, p);
				case "halberdier":
					return new ClassHalberdier(plugin, p);
				case "swordsman":
					return new ClassSwordsman(plugin, p);
				case "vanguard":
					return new ClassVanguard(plugin, p);
				case "maceman":
					return new ClassMaceman(plugin, p);
				case "knight":
					return new ClassKnight(plugin, p);
				case "pikeman":
					return new ClassPikeman(plugin, p);
				case "axeman":
					return new ClassAxeman(plugin, p);
				case "archer":
					return new ClassArcher(plugin, p);
				case "siege_archer":
					return new ClassSiegeArcher(plugin, p);
				case "crossbowman":
					return new ClassCrossbowman(plugin, p);
				case "cavalry":
					return new ClassCavalry(plugin, p);
				case "ranged_cavalry":
					return new ClassRangedCavalry(plugin, p);
				case "lancer":
					return new ClassLancer(plugin, p);
				case "bannerman":
					return new ClassBannerman(plugin, p);
					// case "spearman": return new ClassSpearman(plugin, p); TBD
				case "medic":
					return new ClassMedic(plugin, p);
				}
			}
		}
		return new ClassSwordsman(plugin, p);
	}
}