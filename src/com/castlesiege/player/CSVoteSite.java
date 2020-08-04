package com.castlesiege.player;

public enum CSVoteSite
{
	PMC(0, "http://www.planetminecraft.com/server/tales-of-war/"), Minestatus(1, "https://www.minestatus.net/147275-tales-of-war"), MinecraftServers(2, "http://minecraftservers.org/server/363279"), MinecraftMP(3, "http://minecraft-mp.com/server-s126382");
	
	private int id;
	private String site;
	
	CSVoteSite(int id, String site)
	{
		this.id = id;
		this.site = site;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getSite()
	{
		return site;
	}
	
	public static CSVoteSite getVoteSite(int id)
	{
		for(CSVoteSite voteSite : values())
		{
			if(voteSite.getId() == id)
			{
				return voteSite;
			}
		}
		return null;
	}
}
