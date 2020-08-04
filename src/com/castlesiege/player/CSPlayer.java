package com.castlesiege.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.castlesiege.Main;
import com.castlesiege.chat.ChatChannel;
import com.castlesiege.classes.CSClass;
import com.castlesiege.classes.CSClasses;
import com.castlesiege.data.Duel;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.listeners.InventoryListener.InventoryPage;
import com.castlesiege.util.sql.SQLConnection;

public class CSPlayer
{
	private Main plugin;
	private UUID playerUUID;
	private String playerName;

	private CSDonorRank donorRank;
	public boolean king;
	private CSRank rank;
	private CSClass kit;
	public ChatChannel channel;
	public CSClassStats classStats;
	public CSMatchStats matchStats;
	public CSStats monthStats, guildStats, monthGuildStats;
	public boolean hasGG;
	public int timePlayed;
	public String ip;
	public List<CSPunishRecord> bans, mutes;

	public ArrayList<PermissionAttachment> permissionAttachments;
	public ArrayList<UUID> ignored;

	private int enderchestCooldown = 0;
	private UUID reply = UUID.randomUUID();
	public int scoreboard, horse_variant, horse_color, horse_style;
	public boolean asterisk, guildspawn, chatspy, teamchatspy;
	public double totalDonated;
	public String guild, guildInvite;
	public int guildPage;

	public ArrayList<UUID> assisting;
	public HashMap<CSVoteSite, Long> voted;

	public boolean deathMessages, joinMessages, guildJoinMessages, pms;

	public CSPlayer(Main plugin, UUID playerUUID)
	{
		// Initialize basic variables
		this.plugin = plugin;
		this.playerUUID = playerUUID;
		if(getPlayer() != null)
			playerName = getPlayer().getName();
		permissionAttachments = new ArrayList<PermissionAttachment>();
		ignored = new ArrayList<UUID>();
		// this.stats = new CSStats(plugin, playerUUID);
		hasGG = false;
		matchStats = new CSMatchStats();
		assisting = new ArrayList<UUID>();
		voted = new HashMap<CSVoteSite, Long>();
		channel = ChatChannel.GLOBAL;
		bans = new ArrayList<CSPunishRecord>();
		mutes = new ArrayList<CSPunishRecord>();
		king = false;
	}

	public void downloadBans() throws SQLException
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		PreparedStatement statement = c.prepareStatement("SELECT bans, mutes FROM player_data WHERE uuid=?;");
		statement.setString(1, playerUUID.toString());
		ResultSet result = statement.executeQuery();

		if(result.next())
		{
			String serializedBans = result.getString("bans");
			if(serializedBans != null && !serializedBans.isEmpty() && !serializedBans.equals("") && !serializedBans.equals("0"))
			{
				String[] parts = serializedBans.split("=");
				if(parts.length == 1)
				{
					bans.add(new CSPunishRecord(parts[0]));
				}
				else
				{
					for(String part : parts)
					{
						bans.add(new CSPunishRecord(part));
					}
					sortBans();
				}
			}

			String serializedMutes = result.getString("mutes");
			if(serializedMutes != null && !serializedMutes.isEmpty() && !serializedMutes.equals(""))
			{
				String[] parts = serializedBans.split("=");
				if(parts.length == 1)
				{
					mutes.add(new CSPunishRecord(parts[0]));
				}
				else
				{
					for(String part : parts)
					{
						mutes.add(new CSPunishRecord(part));
					}
					sortBans();
				}
			}
		}
		statement.close();
	}

	public void pushBans() throws SQLException
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		// Basic data
		PreparedStatement statement = c.prepareStatement("UPDATE player_data SET bans=?, mutes=? WHERE uuid=?;");
		String serializedBans = "";
		if(!bans.isEmpty())
		{
			for(CSPunishRecord ban : bans)
			{
				serializedBans += ban.serialize() + "=";
			}
			serializedBans.substring(0, serializedBans.length() - 1);
		}

		String serializedMutes = "";
		if(!mutes.isEmpty())
		{
			for(CSPunishRecord mute : mutes)
			{
				serializedMutes += mute.serialize() + "=";
			}
			serializedMutes.substring(0, serializedMutes.length() - 1);
		}

		statement.setString(1, serializedBans);
		statement.setString(2, serializedMutes);
		statement.setString(3, playerUUID.toString());
		statement.execute();
		statement.close();
	}

	public void downloadData() throws SQLException
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		// Basic data
		PreparedStatement statement = c.prepareStatement("SELECT latest_ip, rank, donor_rank, current_class, vote, class_stats, ignored, scoreboard, asterisk, guildspawn, chatspy, teamchatspy, chat_options, bans, mutes, horses, total_time_played, total_donated, month, month_kills, month_deaths, month_assists, month_caps, month_supps, month_mvps, month_streak, guild, guild_invite, guild_page, guild_score, guild_kills, guild_deaths, guild_assists, guild_caps, guild_supps, guild_mvps, guild_streak, month_guild_score, month_guild_kills, month_guild_deaths, month_guild_assists, month_guild_caps, month_guild_supps, month_guild_mvps, month_guild_streak FROM player_data WHERE uuid=?;");
		statement.setString(1, playerUUID.toString());
		ResultSet result = statement.executeQuery();

		if(result.next())
		{
			// UUID was already in the database
			ip = result.getString("latest_ip");
			CSRank tr = CSRank.valueOf(result.getString("rank").toUpperCase());
			if(tr == null)
			{
				rank = CSRank.DEFAULT;
			}
			else
			{
				rank = tr;
			}
			CSDonorRank dr = CSDonorRank.getRank(result.getString("donor_rank"));
			if(dr == null)
			{
				donorRank = CSDonorRank.DEFAULT;
			}
			else
			{
				donorRank = dr;
			}
			donorRank = CSDonorRank.getRank(result.getString("donor_rank"));
			classStats = new CSClassStats(result.getString("class_stats"));
			timePlayed = result.getInt("total_time_played");
			totalDonated = result.getDouble("total_donated");
			String v = result.getString("vote");
			if(v != null && !v.equalsIgnoreCase(""))
			{
				for(String str : v.split(";"))
				{
					String[] s = str.split(",");
					if(s.length > 1)
					{
						if(!((Long.parseLong(s[1]) / 60 / 24) - (System.currentTimeMillis() / 1000 / 60 / 24) > 0))
						{
							voted.put(CSVoteSite.getVoteSite(Integer.parseInt(s[0])), Long.parseLong(s[1]));
						}
					}
				}
			}
			String i = result.getString("ignored");
			if(i != null && !i.equalsIgnoreCase("") && !i.equalsIgnoreCase("0"))
			{
				for(String str : i.split(","))
				{
					UUID uuid = UUID.fromString(str);
					if(uuid != null)
					{
						ignored.add(uuid);
					}
				}
			}
			scoreboard = result.getInt("scoreboard");
			asterisk = result.getInt("asterisk") == 1;
			guildspawn = result.getInt("guildspawn") == 1;
			chatspy = result.getInt("chatspy") == 1;
			teamchatspy = result.getInt("teamchatspy") == 1;

			if(result.getString("chat_options") != null)
			{
				char[] chatOptions = result.getString("chat_options").toCharArray();
				if(chatOptions.length > 3)
				{
					deathMessages = Integer.parseInt(chatOptions[0] + "") == 1;
					joinMessages = Integer.parseInt(chatOptions[1] + "") == 1;
					guildJoinMessages = Integer.parseInt(chatOptions[2] + "") == 1;
					pms = Integer.parseInt(chatOptions[3] + "") == 1;
				}
				else
				{
					deathMessages = false;
					joinMessages = false;
					guildJoinMessages = false;
					pms = false;
				}
			}
			else
			{
				deathMessages = false;
				joinMessages = false;
				guildJoinMessages = false;
				pms = true;
			}

			String serializedBans = result.getString("bans");
			if(serializedBans != null && !serializedBans.isEmpty() && !serializedBans.equals(""))
			{
				String[] parts = serializedBans.split("=");
				if(parts.length == 1)
				{
					bans.add(new CSPunishRecord(parts[0]));
				}
				else
				{
					for(String part : parts)
					{
						bans.add(new CSPunishRecord(part));
					}
					sortBans();
				}
			}

			String serializedMutes = result.getString("mutes");
			if(serializedMutes != null && !serializedMutes.isEmpty() && !serializedMutes.equals(""))
			{
				String[] parts = serializedBans.split("=");
				if(parts.length == 1)
				{
					mutes.add(new CSPunishRecord(parts[0]));
				}
				else
				{
					for(String part : parts)
					{
						mutes.add(new CSPunishRecord(part));
					}
					sortBans();
				}
			}

			char[] horses = result.getString("horses").toCharArray();
			horse_variant = Integer.parseInt(horses[0] + "");
			horse_color = Integer.parseInt(horses[1] + "");
			horse_style = Integer.parseInt(horses[2] + "");

			monthStats = new CSStats(result.getInt("month_kills"), result.getInt("month_deaths"), result.getInt("month_assists"), result.getInt("month_caps"), result.getInt("month_supps"), result.getInt("month_mvps"), result.getInt("month_streak"));
			guildStats = new CSStats(result.getInt("guild_kills"), result.getInt("guild_deaths"), result.getInt("guild_assists"), result.getInt("guild_caps"), result.getInt("guild_supps"), result.getInt("guild_mvps"), result.getInt("guild_streak"));
			monthGuildStats = new CSStats(result.getInt("month_guild_kills"), result.getInt("month_guild_deaths"), result.getInt("month_guild_assists"), result.getInt("month_guild_caps"), result.getInt("month_guild_supps"), result.getInt("month_guild_mvps"), result.getInt("month_guild_streak"));
			String g = result.getString("guild");
			if(g == null)
			{
				guild = "";
			}
			else
			{
				guild = g;
			}
			String gi = result.getString("guild_invite");
			if(gi == null)
			{
				guildInvite = "";
			}
			else
			{
				guildInvite = gi;
			}
			guildPage = result.getInt("guild_page");

			PreparedStatement s = c.prepareStatement("SELECT uuid FROM player_data ORDER BY total_donated DESC LIMIT 1 OFFSET 0;");
			ResultSet r = s.executeQuery();
			if(r.next())
			{
				if(r.getString("uuid").equalsIgnoreCase(playerUUID.toString()))
				{
					king = true;
					for(CSPlayer csplayer : plugin.datahandler.playerData)
					{
						if(!csplayer.getUniqueId().toString().equalsIgnoreCase(getUniqueId().toString()))
						{
							if(csplayer.king)
							{
								csplayer.king = false;
							}
						}
					}
				}
			}
			kit = CSClasses.getClassByName(plugin, getPlayer(), result.getString("current_class"));
		}
		else
		{
			// UUID was not in the database
			statement = c.prepareStatement("INSERT INTO player_data values(?, ?, ?, ?, ?, ?, 0, ?, ?, 0, 0, 0, 0, 0, ?, ?, ?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
			String name = playerName;
			String ip = "127.0.0.1";
			Player p = Bukkit.getPlayer(playerUUID);
			if(p != null && p.isOnline())
			{
				name = p.getName();
				ip = p.getAddress().getHostString();
			}
			statement.setString(1, playerUUID.toString());
			statement.setString(2, name);
			statement.setString(3, ip);
			statement.setFloat(4, System.currentTimeMillis() / 1000);
			statement.setString(5, "default");
			statement.setString(6, "default");
			statement.setString(7, "0,0;1,0;2,0;3,0;");
			statement.setString(8, "");
			statement.setString(9, "0001");
			statement.setString(10, "");
			statement.setString(11, "");
			statement.setString(12, "000");
			rank = CSRank.DEFAULT;
			donorRank = CSDonorRank.DEFAULT;
			classStats = new CSClassStats("");
			monthStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
			guildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
			monthGuildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
			guild = "";
			guildInvite = "";
			guildPage = 0;
			timePlayed = 0;
			totalDonated = 0;
			scoreboard = 0;
			asterisk = false;
			guildspawn = false;
			chatspy = false;
			teamchatspy = false;
			horse_variant = 0;
			horse_color = 0;
			horse_style = 0;
			deathMessages = false;
			joinMessages = false;
			guildJoinMessages = false;
			pms = true;
			this.ip = ip;
			statement.execute();
		}
		statement.close();
		result.close();
	}

	public void pushData() throws SQLException
	{
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;

		// Basic data
		PreparedStatement statement = c.prepareStatement("UPDATE player_data SET latest_name=?, latest_ip=?, latest_seen=?, rank=?, donor_rank=?, current_class=?, vote=?, ignored=?, scoreboard=?, asterisk=?, guildspawn=?, chatspy=?, teamchatspy=?, chat_options=?, bans=?, mutes=?, horses=?, total_time_played=?, total_donated=?, total_score=?, total_kills=?, total_deaths=?, total_assists=?, total_caps=?, total_supps=?, total_mvps=?, highest_streak=?, class_stats=?, month=?, month_score=?, month_kills=?, month_deaths=?, month_assists=?, month_caps=?, month_supps=?, month_mvps=?, month_streak=?, guild=?, guild_invite=?, guild_page=?, guild_score=?, guild_kills=?, guild_deaths=?, guild_assists=?, guild_caps=?, guild_supps=?, guild_mvps=?, guild_streak=?, month_guild_score=?, month_guild_kills=?, month_guild_deaths=?, month_guild_assists=?, month_guild_caps=?, month_guild_supps=?, month_guild_mvps=?, month_guild_streak=? WHERE uuid=?");
		String name = playerName;
		String ip = "127.0.0.1";
		Player p = Bukkit.getPlayer(playerUUID);
		if(p != null && p.isOnline())
		{
			name = p.getName();
			ip = p.getAddress().getHostString();
		}
		statement.setString(1, name);
		statement.setString(2, ip);
		statement.setLong(3, System.currentTimeMillis() / 1000);
		statement.setString(4, rank.toString().toLowerCase());
		statement.setString(5, donorRank.toString().toLowerCase());

		statement.setString(6, kit.getName().replace(" ", "_").toLowerCase());
		String vote = "";
		for(CSVoteSite voteSite : voted.keySet())
		{
			vote += voteSite.getId() + "," + voted.get(voteSite) + ";";
		}
		statement.setString(7, vote);
		String ignore = "";
		for(UUID str : ignored)
		{
			ignore += str.toString() + ",";
		}
		statement.setString(8, ignore);
		statement.setInt(9, scoreboard);
		statement.setInt(10, boolToInt(asterisk));
		statement.setInt(11, boolToInt(guildspawn));
		statement.setInt(12, boolToInt(chatspy));
		statement.setInt(13, boolToInt(teamchatspy));
		statement.setString(14, boolToInt(deathMessages) + "" + boolToInt(joinMessages) + "" + boolToInt(guildJoinMessages) + "" + boolToInt(pms));
		String serializedBans = "";
		if(!bans.isEmpty())
		{
			for(CSPunishRecord ban : bans)
			{
				serializedBans += ban.serialize() + "=";
			}
			serializedBans.substring(0, serializedBans.length() - 1);
		}
		statement.setString(15, serializedBans);
		String serializedMutes = "";
		if(!mutes.isEmpty())
		{
			for(CSPunishRecord mute : mutes)
			{
				serializedMutes += mute.serialize() + "=";
			}
			serializedMutes.substring(0, serializedMutes.length() - 1);
		}
		statement.setString(16, serializedMutes);
		statement.setString(17, horse_variant + "" + horse_color + "" + horse_style);
		statement.setInt(18, timePlayed);
		statement.setDouble(19, totalDonated);
		statement.setInt(20, classStats.getTotalScore());
		statement.setInt(21, classStats.getTotalKills());
		statement.setInt(22, classStats.getTotalDeaths());
		statement.setInt(23, classStats.getTotalAssists());
		statement.setInt(24, classStats.getTotalCaps());
		statement.setInt(25, classStats.getTotalSupps());
		statement.setInt(26, classStats.getTotalMvps());
		statement.setInt(27, classStats.getHighestStreak());
		statement.setString(28, classStats.encryptData());
		statement.setInt(29, plugin.datahandler.utils.getMonth());
		statement.setInt(30, monthStats.getTotalScore());
		statement.setInt(31, monthStats.getKills());
		statement.setInt(32, monthStats.getDeaths());
		statement.setInt(33, monthStats.getAssists());
		statement.setInt(34, monthStats.getCaps());
		statement.setInt(35, monthStats.getSupps());
		statement.setInt(36, monthStats.getMvps());
		statement.setInt(37, monthStats.getHighestStreak());
		statement.setString(38, guild);
		statement.setString(39, guildInvite);
		statement.setInt(40, guildPage);
		statement.setInt(41, guildStats.getTotalScore());
		statement.setInt(42, guildStats.getKills());
		statement.setInt(43, guildStats.getDeaths());
		statement.setInt(44, guildStats.getAssists());
		statement.setInt(45, guildStats.getCaps());
		statement.setInt(46, guildStats.getSupps());
		statement.setInt(47, guildStats.getMvps());
		statement.setInt(48, guildStats.getHighestStreak());
		statement.setInt(49, monthGuildStats.getTotalScore());
		statement.setInt(50, monthGuildStats.getKills());
		statement.setInt(51, monthGuildStats.getDeaths());
		statement.setInt(52, monthGuildStats.getAssists());
		statement.setInt(53, monthGuildStats.getCaps());
		statement.setInt(54, monthGuildStats.getSupps());
		statement.setInt(55, monthGuildStats.getMvps());
		statement.setInt(56, monthGuildStats.getHighestStreak());
		statement.setString(57, playerUUID.toString());
		statement.execute();
		statement.close();
	}

	private int boolToInt(boolean bool)
	{
		return(bool == true ? 1 : 0);
	}

	public String getPlayerName()
	{
		return playerName;
	}

	public UUID getUniqueId()
	{
		return playerUUID;
	}

	public CSRank getRank()
	{
		return rank;
	}

	public void setRank(CSRank rank)
	{
		this.rank = rank;
		updatePerms();
	}

	public CSDonorRank getDonorRank()
	{
		return donorRank;
	}

	public void setDonorRank(CSDonorRank donorRank)
	{
		this.donorRank = donorRank;
		updatePerms();
	}

	public void sortBans()
	{
		if(!bans.isEmpty())
		{
			Collections.sort(bans, new Comparator<CSPunishRecord>()
			{
				@Override
				public int compare(CSPunishRecord c1, CSPunishRecord c2)
				{
					return c2.getPunishDate().compareTo(c1.getPunishDate());
				}
			});
		}
		if(!mutes.isEmpty())
		{
			Collections.sort(bans, new Comparator<CSPunishRecord>()
			{
				@Override
				public int compare(CSPunishRecord c1, CSPunishRecord c2)
				{
					return c2.getPunishDate().compareTo(c1.getPunishDate());
				}
			});
		}
	}

	public void updatePerms()
	{
		Player p = Bukkit.getPlayer(playerUUID);
		if(p == null || !p.isOnline())
		{
			return;
		}
		if(!permissionAttachments.isEmpty())
		{
			for(PermissionAttachment attachment : permissionAttachments)
			{
				p.removeAttachment(attachment);
			}
			permissionAttachments.clear();
		}
		if(rank != null)
		{
			for(int i = 0; i <= rank.getID(); i++)
			{
				for(String permission : CSRank.getRank(i).getPermissions())
				{
					permissionAttachments.add(p.addAttachment(plugin, permission, true));
				}
			}
		}
		if(donorRank != null)
		{
			for(int i = 0; i <= donorRank.getID(); i++)
			{
				for(String permission : CSDonorRank.getRank(i).getPermissions())
				{
					permissionAttachments.add(p.addAttachment(plugin, permission, true));
				}
			}
		}
	}

	public void removePerms()
	{
		Player p = Bukkit.getPlayer(playerUUID);
		if(p == null)
		{
			return;
		}
		if(!permissionAttachments.isEmpty())
		{
			for(PermissionAttachment attachment : permissionAttachments)
			{
				p.removeAttachment(attachment);
			}
			permissionAttachments.clear();
		}
	}

	public CSClass getKit()
	{
		return kit;
	}

	public void setKit(CSClass kit)
	{
		this.kit = kit;
	}

	public int getEnderchestCooldown()
	{
		return enderchestCooldown;
	}

	public void setEnderchestCooldown(int enderchestCooldown)
	{
		this.enderchestCooldown = enderchestCooldown;
	}

	public UUID getReply()
	{
		return reply;
	}

	public void setReply(UUID reply)
	{
		this.reply = reply;
	}

	public Player getPlayer()
	{
		return Bukkit.getPlayer(playerUUID);
	}
	
	public boolean isMuted()
	{
		try
		{
			SQLConnection sql = plugin.datahandler.sqlConnection;
			Connection c = sql.connection;
			
			PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (offender=?) AND (offense_type='MUTE') ORDER BY ID DESC LIMIT 0, 1;");
			statement.setString(1, playerUUID.toString());
			ResultSet result = statement.executeQuery();
			
			if(result.next())
			{
				boolean cont = false;
				result.getLong("unpunish_date");
				if(!result.wasNull()) { cont = true; }
				if(!cont)
				{
					long tempUnban = result.getLong("temp_unpunish_date");
					if(!result.wasNull())
					{
						Date tempUnbanDate = new Date(tempUnban * 1000);
						if(tempUnbanDate.before(new Date())) cont = true;
					}
				}
				
				result.close();
				statement.close();
				
				if(cont) return false;
				else return true;
			}
			return false;
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasIncomingDuels()
	{
		for(Duel duel : (ArrayList<Duel>) plugin.datahandler.duels.clone())
		{
			if((duel.getPlayers()[1].getName().equalsIgnoreCase(playerName)) && !duel.isAccepted() && !isDueling())
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Duel> getIncomingDuels()
	{
		ArrayList<Duel> duels = new ArrayList<Duel>();
		for(Duel duel : (ArrayList<Duel>) plugin.datahandler.duels.clone())
		{
			Player[] players = duel.getPlayers();
			if((players[1] != null && players[1].isOnline() && players[1].getName().equalsIgnoreCase(playerName)) && !duel.isAccepted())
			{
				duels.add(duel);
			}
		}
		return duels;
	}

	@SuppressWarnings("unchecked")
	public boolean isDueling()
	{
		for(Duel duel : (ArrayList<Duel>) plugin.datahandler.duels.clone())
		{
			Player[] players = duel.getPlayers();
			if(players[0] != null && players[0].isOnline() && players[0].getName().equalsIgnoreCase(playerName) || players[1] != null && players[1].isOnline() && players[1].getName().equalsIgnoreCase(playerName) && duel.isAccepted())
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Duel getDuel()
	{
		for(Duel duel : (ArrayList<Duel>) plugin.datahandler.duels.clone())
		{
			Player[] players = duel.getPlayers();
			if((players[0] != null && players[0].isOnline() && players[0].getName().equalsIgnoreCase(playerName) || players[1] != null && players[1].isOnline() && players[1].getName().equalsIgnoreCase(playerName)) && duel.isAccepted())
			{
				return duel;
			}
		}
		return null;
	}

	public void setClass(CSClasses classes)
	{
		Player p = getPlayer();
		if(classes.getVoter())
		{
			if(!allVotes())
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "You need to vote on all our sites in order to use this class! Please use /vote to get the links.");
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5, 1);
			}
			else
			{
				kit.clearKit();
				kit = CSClasses.getClassByName(plugin, p, classes.toString().toLowerCase());
				kit.itemKit(new ArrayList<CSVoteSite>(voted.keySet()));
				kit.gameKit();
				plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.CLASSES);
				p.closeInventory();
			}
			return;
		}
		else
		{
			if(classes.unlocked(plugin, p))
			{
				kit.clearKit();
				kit = CSClasses.getClassByName(plugin, p, classes.toString().toLowerCase());
				kit.itemKit(new ArrayList<CSVoteSite>(voted.keySet()));
				kit.gameKit();
				plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.CLASSES);
				p.closeInventory();
			}
			else
			{
				p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "You can not equip this class!");
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 5, 1);
			}
		}
	}

	public boolean allVotes()
	{
		if(voted.size() == CSVoteSite.values().length)
		{
			for(CSVoteSite voteSite : voted.keySet())
			{
				if(!hasVoted(voteSite))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean hasVoted(CSVoteSite voteSite)
	{
		if(voted.containsKey(voteSite) && (System.currentTimeMillis() - voted.get(voteSite)*1000) < 86400000)
		{
			return true;
		}
		return false;
	}

	public void giveScore(ScoreType scoreType, int amount, boolean set, boolean levelUpCheck, boolean resetScoreboard)
	{
		int preLevel = plugin.datahandler.utils.getLevel(classStats.getTotalScore(), 3);
		CSClassStat classStat = classStats.getClassStat(kit);
		Guild guild = plugin.datahandler.utils.getGuild(this.guild);
		switch(scoreType)
		{
		case KILLS:
			if(set)
			{
				classStat.setKills(amount);
				monthStats.setKills(amount);
				if(guild != null)
				{
					guildStats.setKills(amount);
					monthGuildStats.setKills(amount);
					// guild.guildStats.setKills(amount);
				}
				matchStats.setKills(amount);
			}
			else
			{
				classStat.setKills(classStat.getKills() + amount);
				monthStats.setKills(monthStats.getKills() + amount);
				if(guild != null)
				{
					guildStats.setKills(guildStats.getKills() + amount);
					monthGuildStats.setKills(monthGuildStats.getKills() + amount);
					guild.guildStats.setKills(guild.guildStats.getKills() + amount);
					guild.monthGuildStats.setKills(guild.monthGuildStats.getKills() + amount);
				}
				matchStats.setKills(matchStats.getKills() + amount);
				matchStats.setStreak(matchStats.getStreak() + amount);
				if(classStat.getHighestStreak() < matchStats.getStreak() || monthStats.getHighestStreak() < matchStats.getStreak() || guildStats.getHighestStreak() < matchStats.getStreak())
				{
					giveScore(ScoreType.HIGHESTSTREAK, matchStats.getStreak(), true, false, false);
				}
			}
			break;
		case DEATHS:
			if(set)
			{
				classStat.setDeaths(amount);
				monthStats.setDeaths(amount);
				if(guild != null)
				{
					guildStats.setDeaths(amount);
					monthGuildStats.setDeaths(amount);
					// guild.guildStats.setDeaths(amount);
				}
				matchStats.setDeaths(amount);
			}
			else
			{
				classStat.setDeaths(classStat.getDeaths() + amount);
				monthStats.setDeaths(monthStats.getDeaths() + amount);
				if(guild != null)
				{
					guildStats.setDeaths(guildStats.getDeaths() + amount);
					monthGuildStats.setDeaths(monthGuildStats.getDeaths() + amount);
					guild.guildStats.setDeaths(guild.guildStats.getDeaths() + amount);
					guild.monthGuildStats.setDeaths(guild.monthGuildStats.getDeaths() + amount);
				}
				matchStats.setDeaths(matchStats.getDeaths() + amount);
			}
			break;
		case ASSISTS:
			if(set)
			{
				classStat.setAssists(amount);
				monthStats.setAssists(amount);
				if(guild != null)
				{
					guildStats.setAssists(amount);
					monthGuildStats.setAssists(amount);
					// guild.guildStats.setAssists(amount);
				}
				matchStats.setAssists(amount);
			}
			else
			{
				classStat.setAssists(classStat.getAssists() + amount);
				monthStats.setAssists(monthStats.getAssists() + amount);
				if(guild != null)
				{
					guildStats.setAssists(guildStats.getAssists() + amount);
					monthGuildStats.setAssists(monthGuildStats.getAssists() + amount);
					guild.guildStats.setAssists(guild.guildStats.getAssists() + amount);
					guild.monthGuildStats.setAssists(guild.monthGuildStats.getAssists() + amount);
				}
				matchStats.setAssists(matchStats.getAssists() + amount);
			}
			break;
		case CAPS:
			if(set)
			{
				classStat.setCaps(amount);
				monthStats.setCaps(amount);
				if(guild != null)
				{
					guildStats.setCaps(amount);
					monthGuildStats.setCaps(amount);
					// guild.guildStats.setCaps(amount);
				}
				matchStats.setCaps(amount);
			}
			else
			{
				classStat.setCaps(classStat.getCaps() + amount);
				monthStats.setCaps(monthStats.getCaps() + amount);
				if(guild != null)
				{
					guildStats.setCaps(guildStats.getCaps() + amount);
					monthGuildStats.setCaps(monthGuildStats.getCaps() + amount);
					guild.guildStats.setCaps(guild.guildStats.getCaps() + amount);
					guild.monthGuildStats.setCaps(guild.monthGuildStats.getCaps() + amount);
				}
				matchStats.setCaps(matchStats.getCaps() + amount);
			}
			break;
		case SUPPS:
			if(set)
			{
				classStat.setSupps(amount);
				monthStats.setSupps(amount);
				if(guild != null)
				{
					guildStats.setSupps(amount);
					monthGuildStats.setSupps(amount);
					// guild.guildStats.setSupps(amount);
				}
				matchStats.setSupps(amount);
			}
			else
			{
				classStat.setSupps(classStat.getSupps() + amount);
				monthStats.setSupps(monthStats.getSupps() + amount);
				if(guild != null)
				{
					guildStats.setSupps(guildStats.getSupps() + amount);
					monthGuildStats.setSupps(monthGuildStats.getSupps() + amount);
					guild.guildStats.setSupps(guild.guildStats.getSupps() + amount);
					guild.monthGuildStats.setSupps(guild.monthGuildStats.getSupps() + amount);
				}
				matchStats.setSupps(matchStats.getSupps() + amount);
			}
			break;
		case STREAK:
			if(set)
			{
				matchStats.setStreak(amount);
			}
			else
			{
				matchStats.setStreak(matchStats.getStreak() + amount);
			}
			break;
		case MVPS:
			if(set)
			{
				monthStats.setMvps(amount);
				if(guild != null)
				{
					guildStats.setMvps(amount);
					monthGuildStats.setMvps(amount);
					// guild.guildStats.setMvps(amount);
				}
				classStat.setMvps(amount);
			}
			else
			{
				monthStats.setMvps(monthStats.getMvps() + amount);
				if(guild != null)
				{
					guildStats.setMvps(guildStats.getMvps() + amount);
					monthGuildStats.setMvps(monthGuildStats.getMvps() + amount);
					guild.guildStats.setMvps(guild.guildStats.getMvps() + amount);
					guild.monthGuildStats.setMvps(guild.monthGuildStats.getMvps() + amount);
				}
				classStat.setMvps(classStat.getMvps() + amount);
			}
			break;
		case HIGHESTSTREAK:
			if(set)
			{
				if(amount > monthStats.getHighestStreak())
				{
					monthStats.setHighestStreak(amount);
				}
				if(guild != null)
				{
					if(amount > guildStats.getHighestStreak())
					{
						guildStats.setHighestStreak(amount);
					}
					if(amount > monthGuildStats.getHighestStreak())
					{
						monthGuildStats.setHighestStreak(amount);
					}
					if(amount > guild.guildStats.getHighestStreak())
					{
						guild.guildStats.setHighestStreak(amount);
					}
					if(amount > guild.monthGuildStats.getHighestStreak())
					{
						guild.monthGuildStats.setHighestStreak(amount);
					}
				}
				if(amount > classStat.getHighestStreak())
				{
					classStat.setHighestStreak(amount);
				}
			}
			else
			{
				monthStats.setHighestStreak(monthStats.getHighestStreak() + amount);
				if(guild != null)
				{
					guildStats.setHighestStreak(guildStats.getHighestStreak() + amount);
					monthGuildStats.setHighestStreak(monthGuildStats.getHighestStreak() + amount);
				}
				classStat.setHighestStreak(classStat.getHighestStreak() + amount);
			}
			break;
		}
		Player p = getPlayer();
		if(resetScoreboard || scoreboard == 0)
		{
			plugin.datahandler.gamemodehandler.resetScoreboard(p);
		}
		if(preLevel < plugin.datahandler.utils.getLevel(classStats.getTotalScore(), 3))
		{
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1f);
			p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.GREEN + "You leveled up to level " + (preLevel + 1) + "!");
		}
	}

	public void update() throws SQLException
	{
		PreparedStatement statement = plugin.datahandler.sqlConnection.connection.prepareStatement("INSERT INTO player_data values(?, ?, ?);");

		statement.setString(1, playerUUID.toString());
		statement.setString(2, playerName);
		statement.setString(3, rank.toString());

		statement.execute();
	}
}