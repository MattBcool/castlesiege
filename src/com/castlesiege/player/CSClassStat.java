package com.castlesiege.player;

public class CSClassStat
{
	private String className;

	public int kills, deaths, assists, caps, supps, mvps, highestStreak;

	public CSClassStat(String[] d)
	{
		className = d[0];
		kills = Integer.parseInt(d[1]);
		deaths = Integer.parseInt(d[2]);
		assists = Integer.parseInt(d[3]);
		caps = Integer.parseInt(d[4]);
		supps = Integer.parseInt(d[5]);
		mvps = Integer.parseInt(d[6]);
		highestStreak = Integer.parseInt(d[7]);
	}

	public String getClassName()
	{
		return className;
	}

	public int getKills()
	{
		return kills;
	}

	public void setKills(int kills)
	{
		this.kills = kills;
	}

	public int getDeaths()
	{
		return deaths;
	}

	public void setDeaths(int deaths)
	{
		this.deaths = deaths;
	}

	public int getAssists()
	{
		return assists;
	}

	public void setAssists(int assists)
	{
		this.assists = assists;
	}

	public int getCaps()
	{
		return caps;
	}

	public void setCaps(int caps)
	{
		this.caps = caps;
	}

	public int getSupps()
	{
		return supps;
	}

	public void setSupps(int supps)
	{
		this.supps = supps;
	}

	public int getMvps()
	{
		return mvps;
	}

	public void setMvps(int mvps)
	{
		this.mvps = mvps;
	}

	public int getHighestStreak()
	{
		return highestStreak;
	}

	public void setHighestStreak(int highestStreak)
	{
		this.highestStreak = highestStreak;
	}

	public int getFinalScore()
	{
		return kills + (assists / 2) + (caps / 2) + (supps / 2);
	}

	public double getKDR()
	{
		if(kills == 0 && deaths == 0)
		{
			return 0.00;
		}
		if(kills != 0 && deaths == 0)
		{
			return (double) kills / 1d;
		}
		return (double) kills / (double) deaths;
	}
}