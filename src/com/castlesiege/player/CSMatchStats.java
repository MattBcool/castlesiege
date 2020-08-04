package com.castlesiege.player;

public class CSMatchStats
{
	private int kills, deaths, assists, caps, supps, streak;
	
	public CSMatchStats()
	{
		kills = 0;
		deaths = 0;
		assists = 0;
		caps = 0;
		supps = 0;
		streak = 0;
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
	
	public int getStreak()
	{
		return streak;
	}

	public void setStreak(int streak)
	{
		this.streak = streak;
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

	public int getFinalScore()
	{
		return kills + (assists / 2) + (caps / 2) + (supps / 2);
	}
}