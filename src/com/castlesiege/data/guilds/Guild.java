package com.castlesiege.data.guilds;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.data.WorldData;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSStats;
import com.castlesiege.util.sql.SQLConnection;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

@SuppressWarnings("deprecation")
public class Guild
{
	private Main plugin;
	private String name;
	private UUID owner;
	public ArrayList<UUID> officers, members, initiates;
	public int id, slots;
	public GuildTheme guildTheme;
	public CSStats guildStats, monthGuildStats;

	public Guild(Main plugin, String name)
	{
		this.plugin = plugin;
		this.name = name;

		plugin.datahandler.guildData.add(this);
		id = plugin.datahandler.guildData.size();
		createGuild();
	}

	public Guild(Main plugin, Player player, String name)
	{
		this.plugin = plugin;
		this.name = name;
		owner = player.getUniqueId();

		plugin.datahandler.guildData.add(this);
		id = plugin.datahandler.guildData.size();
		createGuild();
	}

	public String getName()
	{
		return name;
	}

	public UUID getOwner()
	{
		return owner;
	}

	public void downloadData() throws SQLException
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		PreparedStatement statement = c.prepareStatement("SELECT guild_name, owner, officers, members, initiates, slots, guild_theme, guild_score, guild_kills, guild_deaths, guild_assists, guild_caps, guild_supps, guild_mvps, guild_streak, month_guild_score, month_guild_kills, month_guild_deaths, month_guild_assists, month_guild_caps, month_guild_supps, month_guild_mvps, month_guild_streak FROM guild_data WHERE guild_name=?;");
		statement.setString(1, name);
		ResultSet result = statement.executeQuery();

		if(result.next())
		{
			name = result.getString("guild_name");
			owner = UUID.fromString(result.getString("owner"));
			ArrayList<UUID> officer = new ArrayList<UUID>();
			for(String s : result.getString("officers").split(","))
			{
				if(s != null && !s.equalsIgnoreCase(""))
				{
					officer.add(UUID.fromString(s));
				}
			}
			officers = officer;
			ArrayList<UUID> member = new ArrayList<UUID>();
			for(String s : result.getString("members").split(","))
			{
				if(s != null && !s.equalsIgnoreCase(""))
				{
					member.add(UUID.fromString(s));
				}
			}
			members = member;
			ArrayList<UUID> initiate = new ArrayList<UUID>();
			for(String s : result.getString("initiates").split(","))
			{
				if(s != null && !s.equalsIgnoreCase(""))
				{
					initiate.add(UUID.fromString(s));
				}
			}
			initiates = initiate;
			guildTheme = GuildTheme.valueOf(result.getString("guild_theme").toUpperCase());
			guildStats = new CSStats(Integer.parseInt(result.getString("guild_kills")), Integer.parseInt(result.getString("guild_deaths")), Integer.parseInt(result.getString("guild_assists")), Integer.parseInt(result.getString("guild_caps")), Integer.parseInt(result.getString("guild_supps")), Integer.parseInt(result.getString("guild_mvps")), Integer.parseInt(result.getString("guild_streak")));
			monthGuildStats = new CSStats(result.getInt("month_guild_kills"), result.getInt("month_guild_deaths"), result.getInt("month_guild_assists"), result.getInt("month_guild_caps"), result.getInt("month_guild_supps"), result.getInt("month_guild_mvps"), result.getInt("month_guild_streak"));
		}
		else
		{
			statement = c.prepareStatement("INSERT INTO guild_data values(?, ?, ?, ?, ?, 0, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
			statement.setString(1, name);
			statement.setString(2, owner.toString());
			statement.setString(3, "");
			statement.setString(4, "");
			statement.setString(5, "");
			statement.setString(6, GuildTheme.DEFAULT.toString().toLowerCase());
			officers = new ArrayList<UUID>();
			members = new ArrayList<UUID>();
			initiates = new ArrayList<UUID>();
			guildTheme = GuildTheme.DEFAULT;
			guildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
			monthGuildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);

			statement.execute();
		}
		statement.close();
		result.close();
	}

	public void pushData() throws SQLException
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		PreparedStatement statement = c.prepareStatement("UPDATE guild_data SET guild_name=?, owner=?, guild_theme=?, officers=?, members=?, initiates=?, guild_score=?, guild_kills=?, guild_deaths=?, guild_assists=?, guild_caps=?, guild_supps=?, guild_mvps=?, guild_streak=?, month_guild_score=?, month_guild_kills=?, month_guild_deaths=?, month_guild_assists=?, month_guild_caps=?, month_guild_supps=?, month_guild_mvps=?, month_guild_streak=? WHERE guild_name=?");
		statement.setString(1, name);
		statement.setString(2, owner.toString());
		statement.setString(3, guildTheme.toString().toLowerCase());
		String officer = "";
		for(UUID uuid : officers)
		{
			officer += uuid.toString() + ",";
		}
		statement.setString(4, officer);
		String member = "";
		for(UUID uuid : members)
		{
			member += uuid.toString() + ",";
		}
		statement.setString(5, member);
		String initiate = "";
		for(UUID uuid : initiates)
		{
			initiate += uuid.toString() + ",";
		}
		statement.setString(6, initiate);
		statement.setInt(7, guildStats.getTotalScore());
		statement.setInt(8, guildStats.getKills());
		statement.setInt(9, guildStats.getDeaths());
		statement.setInt(10, guildStats.getAssists());
		statement.setInt(11, guildStats.getCaps());
		statement.setInt(12, guildStats.getSupps());
		statement.setInt(13, guildStats.getMvps());
		statement.setInt(14, guildStats.getHighestStreak());
		statement.setInt(15, monthGuildStats.getTotalScore());
		statement.setInt(16, monthGuildStats.getKills());
		statement.setInt(17, monthGuildStats.getDeaths());
		statement.setInt(18, monthGuildStats.getAssists());
		statement.setInt(19, monthGuildStats.getCaps());
		statement.setInt(20, monthGuildStats.getSupps());
		statement.setInt(21, monthGuildStats.getMvps());
		statement.setInt(22, monthGuildStats.getHighestStreak());
		statement.setString(23, name);

		statement.execute();
		statement.close();
	}

	public void createGuild()
	{
		try
		{
			downloadData();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		pasteSchematic("guild_" + (guildTheme.getId() + 1), getSpawn());
		updateMap();
	}

	public void removeGuild()
	{
		pasteSchematic("guild_0", getSpawn());

		try
		{
			pushData();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		plugin.datahandler.guildData.remove(this);
	}

	public void disbandGuild()
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		try
		{
			PreparedStatement s = c.prepareStatement("SELECT uuid FROM player_data WHERE guild=?");
			s.setString(1, name);
			ResultSet r = s.executeQuery();
			Player ply = Bukkit.getPlayer(UUID.fromString(r.getString("uuid")));
			if(ply == null || !ply.isOnline())
			{
				PreparedStatement statement = c.prepareStatement("UPDATE guild=? FROM player_data WHERE uuid=?");
				statement.setString(1, "");
				statement.setString(2, r.getString("uuid"));

				statement.execute();
				statement.close();
			}
			else
			{
				if(plugin.datahandler.gamemodehandler.inGuildRoom(ply.getLocation()))
				{
					ply.teleport(plugin.datahandler.gamemodehandler.getTeam(ply).getSpawn());
				}
				CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
				csp.guild = "";
				csp.guildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
				csp.monthGuildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
				ply.sendMessage(ChatColor.RED + "Your guild has been disbanded!");
			}

			s.close();
			r.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		try
		{
			PreparedStatement s = c.prepareStatement("SELECT uuid FROM player_data WHERE guild_invite=?");
			s.setString(1, name);
			ResultSet r = s.executeQuery();
			Player ply = Bukkit.getPlayer(UUID.fromString(r.getString("uuid")));
			if(ply == null || !ply.isOnline())
			{
				PreparedStatement statement = c.prepareStatement("UPDATE guild_invite=? FROM player_data WHERE uuid=?");
				statement.setString(1, "");
				statement.setString(2, r.getString("uuid"));

				statement.execute();
				statement.close();
			}
			else
			{
				CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
				csp.guildInvite = "";
				ply.sendMessage(ChatColor.RED + "Your guild has been disbanded!");
			}

			s.close();
			r.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		try
		{
			PreparedStatement statement = c.prepareStatement("DELETE FROM guild_data WHERE guild_name=?");
			statement.setString(1, name);

			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		removeGuild();
	}

	@SuppressWarnings("static-access")
	public void updateMap()
	{
		double[] map = guildTheme.getMap();
		WorldData worldData = plugin.datahandler.currentWorld;
		pasteSchematic("map_" + worldData.getName().toLowerCase().replace(" ", ""), new Location(Bukkit.getWorld("world"), (id * -100) + map[0], 10 + map[1], (id * -100) + map[2]));
		GuildMap guildMap = plugin.datahandler.currentWorld.guildMap.valueOf(worldData.getName().replace(" ", "").toUpperCase());
		guildMap.updateWool(plugin, this);
		guildMap.updateSigns(plugin, this);
	}

	public void updateGuildSigns(Player p)
	{
		switch(plugin.datahandler.utils.getCSPlayer(p).guildPage)
		{
		case 0:
			p.sendSignChange(getSignLocs(guildTheme.getTitleLoc()).get(0), new String[]
			{ "", ChatColor.BLUE + ChatColor.BOLD.toString() + "Top Guilds", "", "" });
			SQLConnection sql = plugin.datahandler.sqlConnection;
			Connection c = sql.connection;

			ArrayList<String> guilds = new ArrayList<String>();
			try
			{
				PreparedStatement statement = c.prepareStatement("SELECT guild_name FROM guild_data ORDER BY guild_score DESC LIMIT ? OFFSET ?");
				statement.setInt(1, 60);
				statement.setInt(2, 0);
				ResultSet result = statement.executeQuery();

				while(result.next())
				{
					guilds.add(result.getString("guild_name"));
				}
				statement.close();
				result.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			ArrayList<Location> locs = getSignLocs(guildTheme.getBoardLocs());
			ArrayList<String> strings = new ArrayList<String>();
			for(int i = 0; i < 60; i++)
			{
				String s = (i + 1) + ".";
				if(guilds.size() >= i + 1)
				{
					s += ChatColor.DARK_GRAY + " " + guilds.get(i);
				}
				strings.add(s + "                    ");
			}
			for(int i = 0; i < locs.size(); i++)
			{
				p.sendSignChange(locs.get(i), new String[]
				{ strings.get(i * 4), strings.get(i * 4 + 1), strings.get(i * 4 + 2), strings.get(i * 4 + 3) });
			}
			p.sendSignChange(getSignLocs(guildTheme.getLeftPageLoc()).get(0), new String[]
			{ "", ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Page Left", ChatColor.DARK_RED + "<", "" });
			p.sendSignChange(getSignLocs(guildTheme.getRightPageLoc()).get(0), new String[]
			{ "", ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Page Right", ChatColor.DARK_RED + ">", "" });
			break;
		}
	}

	public ArrayList<Location> getSignLocs(double[] doubles)
	{
		ArrayList<Location> locs = new ArrayList<Location>();
		int loops = 0;
		double x = 0, y = 0;
		for(double d : doubles)
		{
			if(loops == 0)
			{
				x = d;
			}
			if(loops == 1)
			{
				y = d;
			}
			if(loops == 2)
			{
				locs.add(new Location(Bukkit.getWorld("world"), (id * -100) + x, 10 + y, (id * -100) + d));
				x = 0;
				y = 0;
				loops = 0;
			}
			else
			{
				loops++;
			}
		}
		return locs;
	}

	public Location getSpawn()
	{
		double[] spawn = guildTheme.getSpawn();
		return new Location(Bukkit.getWorld("world"), (id * -100) + spawn[0], 10 + spawn[1], (id * -100) + spawn[2]);
	}

	public int getMemberCount()
	{
		return initiates.size() + members.size() + officers.size() + 1;
	}

	protected void pasteSchematic(String schematic, Location base)
	{
		WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		File file = new File("plugins" + File.separator + "WorldEdit" + File.separator + "schematics" + File.separator + schematic + ".schematic");
		EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(base.getWorld()), we.getWorldEdit().getConfiguration().maxChangeLimit);
		try
		{
			CuboidClipboard cc = MCEditSchematicFormat.getFormat(file).load(file);
			cc.rotate2D((int) base.getYaw());
			cc.paste(session, new Vector(base.getBlockX(), base.getBlockY(), base.getBlockZ()), false);
			return;
		}
		catch(MaxChangedBlocksException | com.sk89q.worldedit.data.DataException | IOException e2)
		{
			e2.printStackTrace();
		}
	}
}
