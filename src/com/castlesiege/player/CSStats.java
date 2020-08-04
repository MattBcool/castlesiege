package com.castlesiege.player;

public class CSStats
{
	private int kills, deaths, assists, caps, supps, mvps, highestStreak;

	public CSStats(int kills, int deaths, int assists, int caps, int supps, int mvps, int highestStreak)
	{
		this.kills = kills;
		this.deaths = deaths;
		this.assists = assists;
		this.caps = caps;
		this.supps = supps;
		this.mvps = mvps;
		this.highestStreak = highestStreak;
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

	public int getTotalScore()
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