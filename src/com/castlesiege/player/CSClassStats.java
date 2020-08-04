package com.castlesiege.player;

import java.util.ArrayList;

import com.castlesiege.classes.CSClass;
import com.castlesiege.classes.CSClasses;

public class CSClassStats
{
	public ArrayList<CSClassStat> classStats;

	public CSClassStats(String stats)
	{
		classStats = new ArrayList<CSClassStat>();

		decryptStats(stats);
	}

	public int getTotalScore()
	{
		int total_score = 0;
		for(CSClassStat classStat : classStats)
		{
			total_score += classStat.getFinalScore();
		}
		return total_score;
	}

	public int getTotalKills()
	{
		int total_kills = 0;
		for(CSClassStat classStat : classStats)
		{
			total_kills += classStat.getKills();
		}
		return total_kills;
	}

	public int getTotalDeaths()
	{
		int total_deaths = 0;
		for(CSClassStat classStat : classStats)
		{
			total_deaths += classStat.getDeaths();
		}
		return total_deaths;
	}

	public int getTotalAssists()
	{
		int total_assists = 0;
		for(CSClassStat classStat : classStats)
		{
			total_assists += classStat.getAssists();
		}
		return total_assists;
	}

	public int getTotalCaps()
	{
		int total_caps = 0;
		for(CSClassStat classStat : classStats)
		{
			total_caps += classStat.getCaps();
		}
		return total_caps;
	}

	public int getTotalSupps()
	{
		int total_supps = 0;
		for(CSClassStat classStat : classStats)
		{
			total_supps += classStat.getSupps();
		}
		return total_supps;
	}

	public int getTotalMvps()
	{
		int total_mvps = 0;
		for(CSClassStat classStat : classStats)
		{
			total_mvps += classStat.getMvps();
		}
		return total_mvps;
	}

	public int getHighestStreak()
	{
		int highest_streak = 0;
		for(CSClassStat classStat : classStats)
		{
			if(highest_streak < classStat.getHighestStreak())
			{
				highest_streak = classStat.getHighestStreak();
			}
		}
		return highest_streak;
	}

	public CSClassStat getClassStat(String name)
	{
		for(CSClassStat classStat : classStats)
		{
			if(classStat.getClassName().replace("_", " ").equalsIgnoreCase(name))
			{
				return classStat;
			}
		}
		return null;
	}

	public CSClassStat getClassStat(CSClass csclass)
	{
		for(CSClassStat classStat : classStats)
		{
			if(classStat.getClassName().replace("_", " ").equalsIgnoreCase(csclass.getName()))
			{
				return classStat;
			}
		}
		return null;
	}

	public double getTotalKDR()
	{
		if(getTotalKills() == 0 && getTotalDeaths() == 0)
		{
			return 0.00;
		}
		if(getTotalKills() != 0 && getTotalDeaths() == 0)
		{
			return (double) getTotalKills() / 1d;
		}
		return (double) getTotalKills() / (double) getTotalDeaths();
	}

	private void decryptStats(String stats)
	{
		String[] data = stats.split(";");
		ArrayList<String> done = new ArrayList<String>();
		for(String s : data)
		{
			String[] d = s.split(",");
			if(d.length > 3)
			{
				classStats.add(new CSClassStat(d));
				done.add(d[0]);
			}
		}

		if(done.size() != CSClasses.values().length)
		{
			for(CSClasses classes : CSClasses.values())
			{
				boolean add = true;
				for(String className : done)
				{
					if(className.equalsIgnoreCase(classes.toString()))
						;
					{
						add = false;
					}
				}
				if(add)
				{
					classStats.add(new CSClassStat(new String[]
					{ classes.toString().toLowerCase(), "0", "0", "0", "0", "0", "0", "0" }));
				}
			}
		}
	}

	public String encryptData()
	{
		String data = "";
		for(CSClassStat classStat : classStats)
		{
			data += classStat.getClassName() + "," + classStat.getKills() + "," + classStat.getDeaths() + "," + classStat.getAssists() + "," + classStat.getCaps() + "," + classStat.getSupps() + "," + classStat.getMvps() + "," + classStat.getHighestStreak() + ";";
		}
		return data;
	}
}
