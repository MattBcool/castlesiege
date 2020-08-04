package com.castlesiege.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.server.v1_9_R2.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.castlesiege.Main;
import com.castlesiege.classes.ClassSwordsman;
import com.castlesiege.data.flags.MovingFlag;
import com.castlesiege.data.flags.TFlag;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.listeners.InventoryListener.InventoryPage;
import com.castlesiege.player.CSClassStat;
import com.castlesiege.player.CSMatchStats;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;
import com.castlesiege.util.sql.SQLConnection;

public class GameModeHandler
{
	private Main plugin;
	public ArrayList<WorldData> mapsInOrder;
	public int currentMap;

	public GameModeHandler(Main plugin)
	{
		this.plugin = plugin;
		mapsInOrder = new ArrayList<WorldData>();
		currentMap = -1;
	}

	public void randomizeMapOrder(ArrayList<WorldData> worlds)
	{
		if(!worlds.isEmpty())
		{
			mapsInOrder.clear();
			mapsInOrder.addAll(worlds);
			// Collections.shuffle(mapsInOrder, new Random());
		}
	}

	@SuppressWarnings("unchecked")
	public void changeMap()
	{
		if(!plugin.datahandler.worldData.isEmpty())
		{
			if(plugin.datahandler.currentWorld != null)
			{
				for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
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
			currentMap++;
			if(mapsInOrder.size() - 1 < currentMap)
			{
				for(CSPlayer csplayer : plugin.datahandler.playerData)
				{
					Player p = csplayer.getPlayer();
					try
					{
						csplayer.pushData();
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
					{
						boolean doRemoval = true;
						for(CSPlayer csp : plugin.datahandler.playerData)
						{
							if(!csp.getUniqueId().toString().equalsIgnoreCase(csplayer.getUniqueId().toString()) && csp.guild.equalsIgnoreCase(csplayer.guild))
							{
								doRemoval = false;
							}
						}
						if(doRemoval)
						{
							Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
							if(guild != null)
							{
								guild.removeGuild();
							}
						}
					}
					if(p != null && p.isOnline())
					{
						p.kickPlayer("The server is restarting!");
					}
				}
				plugin.datahandler.playerData.clear();
				Bukkit.getServer().shutdown();
			}
			else
			{
				plugin.datahandler.currentWorld = mapsInOrder.get(currentMap);
				Bukkit.broadcastMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Changing maps to " + plugin.datahandler.currentWorld.getName());
				plugin.datahandler.timeLeft = plugin.datahandler.currentWorld.getMatchTimer();
				plugin.datahandler.restarting = false;
				for(GateData gateData : plugin.datahandler.currentWorld.gateData)
				{
					gateData.setup(plugin);
				}
				for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
				{
					objectiveData.setup(plugin);
				}
				for(SiegeData siegeData : plugin.datahandler.currentWorld.siegeData)
				{
					siegeData.setup(plugin);
				}
				plugin.datahandler.nametagManager.reset();
				for(CSPlayer csplayer : (ArrayList<CSPlayer>) plugin.datahandler.playerData.clone())
				{
					final Player p = csplayer.getPlayer();
					try
					{
						csplayer.pushData();
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}
					if(p != null && p.isOnline())
					{
						boolean dueling = false;
						plugin.datahandler.nametagManager.sendTeams(p);
						TeamData teamData = plugin.datahandler.gamemodehandler.randomizeTeam(p);
						if(csplayer != null)
						{
							if(csplayer.isDueling())
							{
								dueling = true;
								p.getInventory().setHelmet(null);
								p.getInventory().remove(Material.LADDER);
							}
							csplayer.matchStats = new CSMatchStats();
							csplayer.hasGG = false;
						}
						resetScoreboard(p);

						if(!dueling)
						{
							p.getInventory().clear();
							for(PotionEffect pe : p.getActivePotionEffects())
							{
								p.removePotionEffect(pe.getType());
							}
							if(csplayer.getKit() == null)
							{
								csplayer.setKit(new ClassSwordsman(plugin, p));
								csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
								csplayer.getKit().gameKit();
							}
							else
							{
								csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
								csplayer.getKit().gameKit();
							}
							plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							plugin.datahandler.invListener.updateInventory(p.getUniqueId());
							if(!p.isDead())
							{
								p.setHealth(p.getMaxHealth());
							}
							p.setFoodLevel(20);
							p.setSaturation(1);
							p.setFireTicks(0);
							p.setExp(0);
							p.setLevel(0);
							p.setGameMode(GameMode.SURVIVAL);
							if(p.getVehicle() != null)
							{
								p.getVehicle().eject();
								if(p.getVehicle() instanceof EntityLiving)
								{
									p.getVehicle().remove();
								}
							}
							TitleDisplay.clearTitle(p);

							Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
							if(guild == null)
							{
								SQLConnection sql = plugin.datahandler.sqlConnection;
								Connection c = sql.connection;

								try
								{
									PreparedStatement statement = c.prepareStatement("SELECT guild_name FROM guild_data WHERE guild_name=?;");
									statement.setString(1, csplayer.guild);
									ResultSet result = statement.executeQuery();

									if(result.next())
									{
										guild = new Guild(plugin, csplayer.guild);
									}
									else
									{
										if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
										{
											csplayer.guild = "";
											p.sendMessage(ChatColor.RED + "Your guild has been disbanded!");
										}
									}
									statement.close();
									result.close();
								}
								catch(SQLException e1)
								{
									e1.printStackTrace();
								}
							}
							if(guild != null && !csplayer.guild.equalsIgnoreCase(""))
							{
								try
								{
									guild.pushData();
								}
								catch(SQLException e)
								{
									e.printStackTrace();
								}
								guild.updateMap();
							}

							if(csplayer.guildspawn || csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
							{
								p.teleport(teamData.getSpawn());
							}
							else
							{
								if(!inGuildRoom(p.getLocation()))
								{
									p.teleport(guild.getSpawn());
									final Guild g = guild;
									Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
									{
										@Override
										public void run()
										{
											g.updateGuildSigns(p);
										}
									}, 20L);
								}
							}
						}
					}
					else
					{
						if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
						{
							boolean doRemoval = true;
							for(CSPlayer csp : plugin.datahandler.playerData)
							{
								if(!csp.getUniqueId().toString().equalsIgnoreCase(csplayer.getUniqueId().toString()) && csp.guild.equalsIgnoreCase(csplayer.guild))
								{
									doRemoval = false;
								}
							}
							if(doRemoval)
							{
								Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
								if(guild != null)
								{
									guild.removeGuild();
								}
							}
						}
						plugin.datahandler.playerData.remove(csplayer);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void doRestart(TeamData teamData)
	{
		if(!plugin.datahandler.restarting)
		{
			int seconds = 15;
			ArrayList<String> mvps = new ArrayList<String>();
			for(TeamData td : plugin.datahandler.currentWorld.teamData)
			{
				if(!td.getName().equalsIgnoreCase("None"))
				{
					CSPlayer csplayer = getMvp(td);
					if(csplayer == null)
					{
						mvps.add(td.getChatColor() + "The " + td.getName() + ChatColor.AQUA + " MVP: " + ChatColor.GRAY + "None");
					}
					else
					{
						CSMatchStats stats = csplayer.matchStats;
						mvps.add(td.getChatColor() + "The " + td.getName() + ChatColor.AQUA + " MVP: " + csplayer.getRank().getTag() + td.getChatColor() + csplayer.getPlayer().getName());
						mvps.add(ChatColor.DARK_AQUA + "Score " + ChatColor.WHITE + stats.getFinalScore() + ChatColor.DARK_AQUA + " | Kills " + ChatColor.WHITE + stats.getKills() + ChatColor.DARK_AQUA + " | Deaths " + ChatColor.WHITE + stats.getDeaths() + ChatColor.DARK_AQUA + " | Assists " + ChatColor.WHITE + stats.getAssists() + ChatColor.DARK_AQUA + " | Captures " + ChatColor.WHITE + stats.getCaps() + ChatColor.DARK_AQUA + " | Supports " + ChatColor.WHITE + stats.getSupps());
						CSClassStat classStat = csplayer.classStats.getClassStat(csplayer.getKit());
						classStat.setMvps(classStat.getMvps() + 1);
					}
				}
			}
			for(final Player p : Bukkit.getOnlinePlayers())
			{
				if(getTeam(p) != null)
				{
					if(!plugin.datahandler.utils.getCSPlayer(p).isDueling())
					{
						plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.CLASSES);
						plugin.datahandler.invListener.updateInventory(p.getUniqueId());
						if(p.getVehicle() != null)
						{
							p.getVehicle().eject();
						}
						CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						if(guild == null)
						{
							SQLConnection sql = plugin.datahandler.sqlConnection;
							Connection c = sql.connection;

							try
							{
								PreparedStatement statement = c.prepareStatement("SELECT guild_name FROM guild_data WHERE guild_name=?;");
								statement.setString(1, csplayer.guild);
								ResultSet result = statement.executeQuery();

								if(result.next())
								{
									guild = new Guild(plugin, csplayer.guild);
								}
								else
								{
									if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
									{
										csplayer.guild = "";
										p.sendMessage(ChatColor.RED + "Your guild has been disbanded!");
									}
								}
								statement.close();
								result.close();
							}
							catch(SQLException e1)
							{
								e1.printStackTrace();
							}
						}

						Location loc = p.getLocation();
						if(csplayer.guildspawn || csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
						{
							if(!inSpawnRoom(loc))
							{
								p.teleport(getTeam(p).getSpawn());
							}
						}
						else
						{
							if(!inGuildRoom(loc))
							{
								p.teleport(guild.getSpawn());
								final Guild g = guild;
								Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										g.updateGuildSigns(p);
									}
								}, 20L);
							}
						}
						plugin.datahandler.invListener.updateInventory(p.getUniqueId());
					}
				}
				if(teamData == null)
				{
					p.sendMessage(ChatColor.GOLD + "The match ended in a tie!");
				}
				else
				{
					p.sendMessage(ChatColor.GOLD + "The " + teamData.getName() + " have won the match!");
				}
				if(getTeam(p) != null)
				{
					if(teamData == getTeam(p))
					{
						TitleDisplay.sendTitle(p, 20, 600, 20, ChatColor.GREEN + "Victory!", "Type /gg to congratulate your opponents");
					}
					else
					{
						TitleDisplay.sendTitle(p, 20, 600, 20, ChatColor.RED + "Defeat!", "Type /gg to congratulate your opponents");
					}
				}
				for(String message : mvps)
				{
					p.sendMessage(message);
				}
				if(mapsInOrder.size() - 1 < currentMap + 1)
				{
					p.sendMessage(ChatColor.GRAY + "The server will be restarting in " + seconds + " seconds.");

					for(Duel duel : (ArrayList<Duel>) plugin.datahandler.duels.clone())
					{
						duel.endDuel();
					}
				}
				else
				{
					p.sendMessage(ChatColor.GRAY + "A new match will begin in " + seconds + " seconds.");
				}
			}
			plugin.datahandler.restarting = true;
			plugin.datahandler.timeLeft = seconds;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					changeMap();
				}
			}, 20 * plugin.datahandler.timeLeft);
		}
	}

	public TeamData randomizeTeam(Player p)
	{
		TeamData lastTeam = null;
		if(plugin.datahandler.currentWorld != null)
		{
			for(TeamData team : plugin.datahandler.currentWorld.teamData)
			{
				if(team != null && !team.getName().equalsIgnoreCase("None"))
				{
					if(lastTeam == null || team.players.size() < lastTeam.players.size())
					{
						lastTeam = team;
					}
				}
			}
			setTeam(p, lastTeam);
		}
		return lastTeam;
	}

	public void setTeam(Player p, TeamData teamData)
	{
		if(getTeam(p) != null)
		{
			leaveTeam(p);
		}
		teamData.players.add(p.getUniqueId());
		if(plugin.datahandler.utils.getCSPlayer(p) != null)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().clearKit();
				csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
				csplayer.getKit().gameKit();
			}
			else
			{
				csplayer.setKit(new ClassSwordsman(plugin, p));
				csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
				csplayer.getKit().gameKit();
			}
		}
		/*
		 * for(Player ply : Bukkit.getOnlinePlayers()) { CSPlayer csplayer =
		 * plugin.datahandler.getCSPlayer(ply); if(csplayer != null &&
		 * csplayer.getKit() instanceof ClassMedic) {
		 * if(getTeam(ply).getName().equalsIgnoreCase(teamData.toString())) {
		 * ((ClassMedic) csplayer.getKit()).showHealthBar(p); } else {
		 * ((ClassMedic) csplayer.getKit()).hideHealthBar(p); } } }
		 */// TODO Fix
		plugin.datahandler.nametagAPI.setPrefix(p, teamData.getChatColor().toString());// TODO
																						// Check
																						// for
																						// VIP
		p.sendMessage(teamData.getChatColor() + "You have joined the " + teamData.getName() + "!");
	}

	public TeamData getTeam(Player p)
	{
		for(TeamData team : plugin.datahandler.currentWorld.teamData)
		{
			if(team.players.contains(p.getUniqueId()))
			{
				return team;
			}
		}
		return null;
	}

	public void leaveTeam(Player p)
	{
		if(getTeam(p) != null)
		{
			getTeam(p).players.remove(p.getUniqueId());
			plugin.datahandler.nametagManager.reset(p.getName());
		}
	}

	public TeamData getTeamFromName(String name)
	{
		for(TeamData teamData : plugin.datahandler.currentWorld.teamData)
		{
			if(teamData.getName().equals(name))
			{
				return teamData;
			}
		}
		return null;
	}

	public boolean inMap(Location loc)
	{
		if(plugin.datahandler.currentWorld.getMatchBorder()[0] != null && plugin.datahandler.currentWorld.getMatchBorder()[0].getWorld() == loc.getWorld())
		{
			return plugin.isInRect(loc, plugin.datahandler.currentWorld.getMatchBorder()[0], plugin.datahandler.currentWorld.getMatchBorder()[1]);
		}
		return false;
	}

	public boolean inSpawnRoom(Location loc)
	{
		for(TeamData teamData : plugin.datahandler.currentWorld.teamData)
		{
			if(teamData.getSpawn().getWorld() == loc.getWorld() && loc.distance(teamData.getSpawn()) < 70)
			{
				return true;
			}
		}
		return false;
	}

	public boolean inGuildRoom(Location loc)
	{
		for(Guild guild : plugin.datahandler.guildData)
		{
			if(guild.getSpawn().getWorld() == loc.getWorld() && loc.distance(guild.getSpawn()) < 70)
			{
				return true;
			}
		}
		return false;
	}

	public boolean inWorld(Location loc)
	{
		return loc.getWorld().getName().equals(plugin.datahandler.currentWorld.getWorldName());
	}

	public CSPlayer getMvp(TeamData teamData)
	{
		ArrayList<CSPlayer> players = new ArrayList<CSPlayer>();
		for(UUID uuid : teamData.players)
		{
			Player p = Bukkit.getPlayer(uuid);
			if(plugin.datahandler.utils.getCSPlayer(p) != null && p.isOnline())
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				players.add(csplayer);
			}
		}
		if(players.size() == 0)
		{
			return null;
		}

		CSPlayer topPlayer = players.get(0);
		for(CSPlayer player : players)
		{
			if(player.matchStats.getFinalScore() > topPlayer.matchStats.getFinalScore())
			{
				topPlayer = player;
			}
		}
		return topPlayer;
	}

	public void updateTimeScoreboard(Player p, String name)
	{
		Scoreboard scoreboard = p.getScoreboard();
		if(scoreboard != null)
		{
			Objective objective = scoreboard.getObjective("board");
			if(objective != null)
			{
				int min = plugin.datahandler.timeLeft / 60;
				int sec = plugin.datahandler.timeLeft % 60;
				String minutes = min + "";
				String seconds = sec + "";
				if(!(minutes.length() > 1))
				{
					minutes = "0" + minutes;
				}
				if(!(seconds.length() > 1))
				{
					seconds = "0" + seconds;
				}
				objective.setDisplayName(name + " " + ChatColor.RED + minutes + ":" + seconds);
			}
		}
	}

	public void updateFlagScoreboard(Player p)
	{
		if(p.getScoreboard() != null)
		{
			Scoreboard scoreboard = p.getScoreboard();
			Objective objective = scoreboard.getObjective("board");
			if(plugin.datahandler.currentWorld != null)
			{
				for(WorldData worldData : plugin.datahandler.worldData)
				{
					for(TeamData teamData : worldData.teamData)
					{
						for(ObjectiveData objectiveData : worldData.objectiveData)
						{
							if(plugin.datahandler.currentWorld.objectiveData.contains(objectiveData))
							{
								if(objectiveData.getFlag() != null)
								{
									if(objectiveData.getFlag().getControlling() == teamData)
									{
										String name = objectiveData.getName().substring(0, 1).toUpperCase() + objectiveData.getName().substring(1);
										if(objective != null)
										{
											Score score = objective.getScore(objectiveData.getFlag().getControlling().getChatColor() + name);
											if(score != null)
											{
												score.setScore(1);
											}
										}
									}
									else
									{
										scoreboard.resetScores(teamData.getChatColor() + objectiveData.getName().substring(0, 1).toUpperCase() + objectiveData.getName().substring(1));
									}
								}
							}
							else
							{
								scoreboard.resetScores(teamData.getChatColor() + objectiveData.getName().substring(0, 1).toUpperCase() + objectiveData.getName().substring(1));
							}
						}
					}
				}
			}
		}
	}

	public void updateStatScoreboard(Player p)
	{
		if(p.getScoreboard() != null)
		{
			Scoreboard scoreboard = p.getScoreboard();
			Objective objective = scoreboard.getObjective("board");
			if(plugin.datahandler.utils.getCSPlayer(p) != null)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				objective.getScore(ChatColor.WHITE + "Score: ").setScore(csplayer.matchStats.getFinalScore());
				objective.getScore(ChatColor.GRAY + "Captures: ").setScore(csplayer.matchStats.getCaps());
				objective.getScore(ChatColor.DARK_GREEN + "Kills: ").setScore(csplayer.matchStats.getKills());
				objective.getScore(ChatColor.AQUA + "Assists: ").setScore(csplayer.matchStats.getAssists());
				objective.getScore(ChatColor.RED + "Deaths: ").setScore(csplayer.matchStats.getDeaths());
				objective.getScore(ChatColor.BLUE + "Supports: ").setScore(csplayer.matchStats.getSupps());
			}
		}
	}

	public void resetScoreboard(Player p)
	{
		if(p.getScoreboard() != null)
		{
			Scoreboard scoreboard = p.getScoreboard();
			if(plugin.datahandler.currentWorld != null)
			{
				for(WorldData worldData : plugin.datahandler.worldData)
				{
					for(TeamData teamData : worldData.teamData)
					{
						for(ObjectiveData objectiveData : worldData.objectiveData)
						{
							scoreboard.resetScores(teamData.getChatColor() + objectiveData.getName().substring(0, 1).toUpperCase() + objectiveData.getName().substring(1));
						}
					}
				}
			}
			scoreboard.resetScores(ChatColor.WHITE + "Score: ");
			scoreboard.resetScores(ChatColor.GRAY + "Captures: ");
			scoreboard.resetScores(ChatColor.DARK_GREEN + "Kills: ");
			scoreboard.resetScores(ChatColor.AQUA + "Assists: ");
			scoreboard.resetScores(ChatColor.RED + "Deaths: ");
			scoreboard.resetScores(ChatColor.BLUE + "Supports: ");
			if(plugin.datahandler.utils.getCSPlayer(p) != null)
			{
				switch(plugin.datahandler.utils.getCSPlayer(p).scoreboard)
				{
				case 0:
					updateTimeScoreboard(p, "Stats");
					updateStatScoreboard(p);
					break;
				case 1:
					updateTimeScoreboard(p, "Flags");
					updateFlagScoreboard(p);
					break;
				}
			}
		}
	}
}
