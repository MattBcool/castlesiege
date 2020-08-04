package com.castlesiege.data.guilds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;

public enum GuildMap
{
	RATSNEST(new String[]
	{ "the Market", "the Gallows", "the Port", "the Church", "the Alley", "the Town Hall", "the Garrison" }, new double[]
	{ -1, 2, 1, 2, 8, 1, 5, 4, 1, 1, 4, 1, -4, 2, 1, -4, 5, 1, -4, 11, 1 }), LAKEBOROUGH(new String[]
	{ "the Military Camp", "the Village", "the East Tower", "the North East Tower", "the South Tower", "the West Tower", "the North West Tower", "the Dining Hall", "the Kings Tower" }, new double[]
	{ 0, 11, 1, 0, 9, 1, -1, 4, 1, -2, 1, 1, -2, 6, 1, -4, 5, 1, -5, 1, 1, -3, 4, 1, -3, 2, 1 }), FALKIRK(new String[]
	{ "the Scottish Camp", "No Mans Land", "the English Camp" }, new double[]
	{ 0, 2, 1, 0, 6, 1, 0, 10, 1 }), SOLDIERSPEAK(new String[]
	{ "the Legions Way", "the Main Gate", "the Courtyard", "the Keeps Entrance", "the Library", "the Throne Room", "the War Room", "the Upper Courtyard", "the Bridge", "the Arcane Tower" }, new double[]
	{ -4, 1, 1, -2, 4, 1, -1, 5, 1, 0, 6, 1, 2, 6, 1, 1, 8, 1, 3, 8, 1, 2, 9, 1, 3, 10, 1, -3, 10, 1 }), ANCIENTRUINS(new String[]
	{ "the Spanish Camp", "the Mayan Camp", "the West Cave", "the Upper Halls", "the Ruined Street", "the Temple Throne", "the Courtyard", "the Lower Halls", "the Ruined Monument", "the East Cave", "the Portuguese Camp" }, new double[]
	{ 4, 4, 1, -5, 4, 1, 1, 5, 1, 0, 6, 1, -4, 6, 1, 2, 7, 1, -2, 7, 1, 0, 8, 1, -4, 8, 1, -1, 9, 1, 2, 10, 1 });

	private String[] objectiveName;
	private double[] objectiveLoc;

	GuildMap(String[] objectiveName, double[] objectiveLoc)
	{
		this.objectiveName = objectiveName;
		this.objectiveLoc = objectiveLoc;
	}

	public ObjectiveData getObjectiveDataByLoc(Main plugin, Guild guild, Location loc)
	{
		double[] maps = guild.guildTheme.getMap();
		Location map = guild.getSpawn().add(maps[0], maps[1], maps[2]);
		for(int i = 0; i < objectiveName.length; i++)
		{
			if(loc.distance(map.clone().getBlock().getLocation().add(objectiveLoc[i * 3], objectiveLoc[i * 3 + 1], objectiveLoc[i * 3 + 2])) <= 1)
			{
				for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
				{
					if(objectiveName[i].equalsIgnoreCase(objectiveData.getName()))
					{
						return objectiveData;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public void updateWool(Main plugin, Guild guild)
	{
		double[] maps = guild.guildTheme.getMap();
		Location map = guild.getSpawn().add(maps[0], maps[1], maps[2]);
		for(int i = 0; i < objectiveName.length; i++)
		{
			for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
			{
				if(objectiveData != null)
				{
					if(objectiveName[i].equalsIgnoreCase(objectiveData.getName()))
					{
						byte color = 0;
						if(objectiveData.getFlag() == null || objectiveData.getFlag().getControlling() == null)
						{
							color = 8;
						}
						else
						{
							color = (byte) objectiveData.getFlag().getControlling().getWoolColor();
						}
						map.clone().add(objectiveLoc[i * 3], objectiveLoc[i * 3 + 1], objectiveLoc[i * 3 + 2]).getBlock().setData(color);
					}
				}
			}
		}
	}

	public void updateSigns(Main plugin, Guild guild)
	{
		double[] maps = guild.guildTheme.getMap();
		Location map = guild.getSpawn().add(maps[0], maps[1], maps[2]);
		for(int i = 0; i < objectiveName.length; i++)
		{
			for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
			{
				if(objectiveName[i].equalsIgnoreCase(objectiveData.getName()))
				{
					Block b = map.clone().add(objectiveLoc[i * 3], objectiveLoc[i * 3 + 1], objectiveLoc[i * 3 + 2] - 1).getBlock();
					Material mat = b.getType();
					if(mat == Material.SIGN || mat == Material.SIGN_POST || mat == Material.WALL_SIGN)
					{
						BlockState bs = b.getState();
						Sign sign = (Sign) bs;
						sign.setLine(1, objectiveData.getName().replace("the ", ""));
						sign.update(true);
					}
				}
			}
		}
	}
}
