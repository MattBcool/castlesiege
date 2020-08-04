package com.castlesiege.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.castlesiege.Main;
import com.castlesiege.chat.BroadcastHandler;
import com.castlesiege.chat.ChatChannel;
import com.castlesiege.data.flags.MovingFlag;
import com.castlesiege.data.flags.TFlag;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.extra.FileUtil;
import com.castlesiege.extra.MyConfig;
import com.castlesiege.extra.MyConfigManager;
import com.castlesiege.listeners.InventoryListener;
import com.castlesiege.listeners.InventoryListener.InventoryPage;
import com.castlesiege.listeners.MaintenanceListener;
import com.castlesiege.listeners.PlayerListener;
import com.castlesiege.listeners.WorldListener;
import com.castlesiege.nametag.NametagAPI;
import com.castlesiege.nametag.NametagManager;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.util.sql.SQLConnection;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class DataHandler
{
	private Main plugin;

	public SQLConnection sqlConnection;

	public Utils utils;

	public ProtocolManager protocolManager;

	public GameModeHandler gamemodehandler;
	public ServerTick serverTick;

	public MyConfigManager configManager;
	public MyConfig gameData;

	public ArrayList<WorldData> worldData;
	public ArrayList<CSPlayer> playerData;
	public ArrayList<Guild> guildData;

	public ArrayList<Duel> duels;
	
	public HashMap<UUID, String> cachedNames;

	public WorldData currentWorld;
	public int timeLeft;
	public boolean restarting = true;

	public NametagManager nametagManager;
	public NametagAPI nametagAPI;

	public InventoryListener invListener;
	public BroadcastHandler broadcasts;

	public DataHandler(Main plugin)
	{
		this.plugin = plugin;

		sqlConnection = new SQLConnection();
		try
		{
			sqlConnection.connectToServer("talesofwar");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void doEnable()
	{
		protocolManager = ProtocolLibrary.getProtocolManager();
		utils = new Utils(plugin);
		// Register listeners
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(plugin), plugin);
		invListener = new InventoryListener(plugin);
		Bukkit.getServer().getPluginManager().registerEvents(invListener, plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new WorldListener(plugin), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new MaintenanceListener(plugin), plugin);

		timeLeft = 30;
		restarting = false;

		gamemodehandler = new GameModeHandler(plugin);

		serverTick = new ServerTick(plugin);

		worldData = new ArrayList<WorldData>();
		playerData = new ArrayList<CSPlayer>();
		guildData = new ArrayList<Guild>();

		duels = new ArrayList<Duel>();
		
		cachedNames = new HashMap<UUID, String>();

		nametagManager = new NametagManager();
		nametagAPI = new NametagAPI(nametagManager);

		configManager = new MyConfigManager(plugin);
		gameData = configManager.getNewConfig("GameData.yml");

		utils.checkMonth();

		setupWorlds();
		setupConfig();

		gamemodehandler.randomizeMapOrder(worldData);

		// Ensure that SQL is ready
		try
		{
			Connection c = sqlConnection.connection;

			DatabaseMetaData meta = c.getMetaData();
			ResultSet tables = meta.getTables(null, null, "player_data", new String[]
			{ "TABLE" });
			if(!tables.next())
			{
				Bukkit.getLogger().info("Data table not found. Creating SQL table on designated server...");

				// UUID, Name, Rank
				// Coins, Secrets
				Statement statement = c.createStatement();
				statement.executeUpdate("CREATE TABLE player_data (uuid CHAR(36) not NULL, " + "latest_name CHAR(16) not NULL, " + "rank VARCHAR(255) not NULL, " + "PRIMARY KEY ( uuid ));");
				statement.close();
			}
		}
		catch(Exception ex)
		{
			Bukkit.getLogger().severe("MySQL cannot be connected to!");
			ex.printStackTrace();
			Bukkit.getServer().shutdown();
		}
		for(Player p : Bukkit.getOnlinePlayers())
		{
			for(PotionEffect effect : p.getActivePotionEffects())
			{
				p.removePotionEffect(effect.getType());
			}
			CSPlayer csplayer = new CSPlayer(plugin, p.getUniqueId());
			try
			{
				csplayer.downloadData();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			csplayer.channel = ChatChannel.GLOBAL;
			playerData.add(csplayer);
			invListener.pages.put(p.getUniqueId(), InventoryPage.CLASSES);
			invListener.updateInventory(p.getUniqueId());
			ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
			Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
			Objective objective = scoreboard.registerNewObjective("board", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Tales of War");
			p.setScoreboard(scoreboard);
			plugin.datahandler.gamemodehandler.resetScoreboard(p);
		}

		gamemodehandler.changeMap();
		
		broadcasts = new BroadcastHandler();
	}

	@SuppressWarnings("unchecked")
	public void doDisable()
	{
		for(Guild guild : (ArrayList<Guild>) guildData.clone())
		{
			try
			{
				guild.pushData();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			guild.removeGuild();
		}

		for(Duel duel : duels)
		{
			duel.endArena();
		}

		for(Player p : Bukkit.getOnlinePlayers())
		{
			CSPlayer csplayer = utils.getCSPlayer(p);
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().clearKit();
			}
			try
			{
				csplayer.pushData();
			}
			catch(SQLException ex)
			{
				ex.printStackTrace();
			}
			Guild guild = utils.getGuild(csplayer.guild);
			if(guild != null)
			{
				guild.removeGuild();
			}
		}
		playerData.clear();

		// Closing down MySQL
		try
		{
			if(sqlConnection.checkConnection())
			{
				sqlConnection.closeConnections();
			}
		}
		catch(Exception ex)
		{
			// ex.printStackTrace();
		}

		for(WorldData wd : worldData)
		{
			for(ObjectiveData objectiveData : wd.objectiveData)
			{
				if(objectiveData.getFlag() instanceof MovingFlag)
				{
					for(Entity stand : ((MovingFlag) objectiveData.getFlag()).stands.keySet())
					{
						if(stand != null)
						{
							stand.remove();
						}
					}
					((MovingFlag) objectiveData.getFlag()).stands.clear();
				}
				if(objectiveData.getFlag() instanceof TFlag)
				{
					for(Entity stand : ((TFlag) objectiveData.getFlag()).stands.keySet())
					{
						if(stand != null)
						{
							stand.remove();
						}
					}
					((TFlag) objectiveData.getFlag()).stands.clear();
				}
			}
		}
	}

	private void setupWorlds()
	{
		for(String worldName : gameData.getKeys())
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				if(p.getVehicle() != null)
				{
					p.getVehicle().eject();
					p.getVehicle().remove();
				}
				p.teleport(new Location(Bukkit.getWorld("world"), 100, 100, 100));
			}
			World w = Bukkit.getWorld(worldName);
			if(w != null)
			{
				Bukkit.unloadWorld(w, false);
			}

			FileUtil.deleteFile(new File(Bukkit.getWorldContainer(), worldName));

			try
			{
				FileUtil.extractZIP(new File(Bukkit.getWorldContainer(), "worlds/" + worldName + ".zip"), new File(Bukkit.getWorldContainer(), worldName));
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}

			WorldCreator worldCreator = new WorldCreator(worldName);
			worldCreator.createWorld();
		}
	}

	private void setupConfig()
	{
		for(String key : gameData.getKeys())
		{
			String name = "";
			int matchTimer = 0;
			Location[] matchBorder = new Location[2];
			World world = Bukkit.getWorld(key);
			ArrayList<TeamData> teamData = new ArrayList<TeamData>();
			ArrayList<GateData> gateData = new ArrayList<GateData>();
			ArrayList<ObjectiveData> objectiveData = new ArrayList<ObjectiveData>();
			ArrayList<SiegeData> siegeData = new ArrayList<SiegeData>();
			if(world == null)
			{
				System.out.println(ChatColor.DARK_RED + "A fatal error has occured while loading worlds.");
				return;
			}
			teamData.add(new TeamData("None", ChatColor.GRAY, 8, new Location(world, 0, 0, 0)));
			for(Object o : gameData.getList(key))
			{
				String data = o.toString();
				if(data.startsWith("name"))
				{
					String[] d = data.replace("name: ", "").split(",");
					name = d[0];
				}
				if(data.startsWith("match_timer"))
				{
					String[] d = data.replace("match_timer: ", "").split(",");
					matchTimer = Integer.parseInt(d[0]);
				}
				if(data.startsWith("match_border"))
				{
					String[] d = data.replace("match_border: ", "").split(",");
					matchBorder[0] = new Location(world, Double.parseDouble(d[0]), 0, Double.parseDouble(d[1]));
					matchBorder[1] = new Location(world, Double.parseDouble(d[2]), 0, Double.parseDouble(d[3]));
				}
				if(data.startsWith("team"))
				{
					String[] d = data.replace("team: ", "").split(",");
					Location spawn = new Location(world, Double.parseDouble(d[3]), Double.parseDouble(d[4]), Double.parseDouble(d[5]));
					spawn.setYaw(Float.parseFloat(d[6]));
					teamData.add(new TeamData(d[0], ChatColor.getByChar(d[1].charAt(0)), Integer.parseInt(d[2]), spawn));
				}
				if(data.startsWith("gate"))
				{
					String[] d = data.replace("gate: ", "").split(",");
					Location base = new Location(world, Double.parseDouble(d[6]), Double.parseDouble(d[7]), Double.parseDouble(d[8]));
					base.setYaw(Float.parseFloat(d[9]));
					gateData.add(new GateData(d[0], Integer.parseInt(d[1]), Integer.parseInt(d[2]), Sound.valueOf(d[3]), Float.parseFloat(d[4]), Float.parseFloat(d[5]), base, checkLevers(d, 10, world, new ArrayList<Location>())));
				}
				if(data.startsWith("objective"))
				{
					String[] d = data.replace("objective: ", "").split(",");
					Location base = new Location(world, Double.parseDouble(d[7]), Double.parseDouble(d[8]), Double.parseDouble(d[9]));
					base.setYaw(Float.parseFloat(d[10]));
					Location spawn = new Location(world, Double.parseDouble(d[11]), Double.parseDouble(d[12]), Double.parseDouble(d[13]));
					spawn.setYaw(Float.parseFloat(d[14]));
					objectiveData.add(new ObjectiveData(d[0], d[1], Integer.parseInt(d[2]), Integer.parseInt(d[3]), Integer.parseInt(d[4]), Boolean.parseBoolean(d[5]), Boolean.parseBoolean(d[6]), base, spawn, checkWool(d, 15, world, teamData, new HashMap<TeamData, Location>())));
				}
				if(data.startsWith("siege"))
				{
					String[] d = data.replace("siege: ", "").split(",");
					Location base = new Location(world, Double.parseDouble(d[4]), Double.parseDouble(d[5]), Double.parseDouble(d[6]));
					base.setYaw(Float.parseFloat(d[7]));
					siegeData.add(new SiegeData(d[0], d[1], Integer.parseInt(d[2]), Integer.parseInt(d[3]), base));
				}
			}
			worldData.add(new WorldData(name, matchTimer, matchBorder, key, teamData, gateData, objectiveData, siegeData));
		}
		gameData.saveConfig();
	}

	private HashMap<TeamData, Location> checkWool(String[] d, int size, World world, ArrayList<TeamData> teamData, HashMap<TeamData, Location> wool)
	{
		if(d.length > size)
		{
			for(TeamData td : teamData)
			{
				if(td.getName().equals(d[size]))
				{
					wool.put(td, new Location(world, Double.parseDouble(d[size + 1]), Double.parseDouble(d[size + 2]), Double.parseDouble(d[size + 3])));
				}
			}
			size += 4;
			return checkWool(d, size, world, teamData, wool);
		}
		return wool;
	}

	private ArrayList<Location> checkLevers(String[] d, int size, World world, ArrayList<Location> locs)
	{
		if(d.length > size)
		{
			locs.add(new Location(world, Double.parseDouble(d[size]), Double.parseDouble(d[size + 1]), Double.parseDouble(d[size + 2])));
			size += 3;
			return checkLevers(d, size, world, locs);
		}
		return locs;
	}
}
