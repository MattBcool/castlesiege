package com.castlesiege.listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.castlesiege.Main;
import com.castlesiege.chat.ChatChannel;
import com.castlesiege.classes.ClassBannerman;
import com.castlesiege.classes.ClassMedic;
import com.castlesiege.classes.ClassSiegeArcher;
import com.castlesiege.classes.ClassSwordsman;
import com.castlesiege.data.Duel;
import com.castlesiege.data.GateData;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.SiegeData;
import com.castlesiege.data.TeamData;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.data.sieges.Siege;
import com.castlesiege.data.sieges.TrebuchetSiege;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.listeners.InventoryListener.InventoryPage;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;
import com.castlesiege.player.ScoreType;
import com.castlesiege.util.sql.SQLConnection;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class PlayerListener implements Listener
{
	private Main plugin;

	public PlayerListener(Main plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	private void onPlayerLogin(PlayerLoginEvent e)
	{
		Player p = e.getPlayer();

		try
		{
			SQLConnection sql = plugin.datahandler.sqlConnection;
			Connection c = sql.connection;
			
			PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (offender=?) AND (offense_type='BAN') ORDER BY ID DESC LIMIT 0, 1;");
			statement.setString(1, p.getUniqueId().toString());
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
				
				if(!cont) {
					e.setResult(Result.KICK_BANNED);
					String reason = result.getString("reason");
					
					Calendar cal = Calendar.getInstance();
					long tempUnbanLong = result.getLong("temp_unpunish_date");
					String tempUnbanDateString = null;
					if(!result.wasNull())
					{
						cal.setTime(new Date(tempUnbanLong * 1000));
						tempUnbanDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
					}
					
					e.setKickMessage(ChatColor.GRAY + "You have been banned!\n" + (reason != null ? ChatColor.GRAY + "Reason: " + ChatColor.BLUE + reason + "\n" : "") + (tempUnbanDateString != null ? ChatColor.GRAY + "You will be unbanned on " + tempUnbanDateString + ChatColor.GRAY + ".\n" : "") + ChatColor.GRAY + "Apply to be unbanned at talesofwar.net");
				}
			}
			
			result.close();
			statement.close();

			statement = c.prepareStatement("SELECT donor_rank from player_data WHERE uuid=?;");
			statement.setString(1, p.getUniqueId().toString());
			result = statement.executeQuery();

			if(result.next())
			{
				if(e.getResult() == Result.KICK_FULL)
				{
					CSDonorRank donorRank = CSDonorRank.valueOf(result.getString("donor_rank"));
					if(donorRank != null && donorRank.getID() >= CSDonorRank.LORD.getID())
					{
						e.allow();
					}
				}
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player p = e.getPlayer();
		final CSPlayer csplayer;
		if(plugin.datahandler.utils.getCSPlayer(p) == null)
		{
			csplayer = new CSPlayer(plugin, p.getUniqueId());
			plugin.datahandler.playerData.add(csplayer);

			try
			{
				csplayer.downloadData();
			}
			catch(SQLException ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			csplayer = plugin.datahandler.utils.getCSPlayer(p);
		}

		TeamData teamData = plugin.datahandler.gamemodehandler.randomizeTeam(p);
		plugin.datahandler.nametagManager.sendTeams(p);

		if(p.getVehicle() != null)
		{
			p.getVehicle().eject();
			p.getVehicle().remove();
		}

		e.setJoinMessage(null);

		p.setWalkSpeed(0.2f);
		p.setFoodLevel(20);
		p.setSaturation(1);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);
		p.setGameMode(GameMode.SURVIVAL);
		p.setAllowFlight(false);

		p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier("generic.attackSpeed", 9.9999999E7D, AttributeModifier.Operation.ADD_NUMBER));
		p.setMaximumNoDamageTicks(20);

		csplayer.updatePerms();
		csplayer.channel = ChatChannel.GLOBAL;

		if(plugin.datahandler.restarting)
		{
			p.sendMessage(ChatColor.GRAY + "A new match will begin shortly.");
		}

		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("board", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Tales of War");
		p.setScoreboard(scoreboard);
		plugin.datahandler.gamemodehandler.resetScoreboard(p);

		TitleDisplay.sendTabTitle(p, ChatColor.GOLD + "Tales of War", ChatColor.GOLD + "Forums: talesofwar.net");

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
		else
		{
			for(CSPlayer csp : plugin.datahandler.playerData)
			{
				Player ply = csp.getPlayer();
				if(ply != null && ply.isOnline())
				{
					if(csplayer.getDonorRank().getID() >= CSDonorRank.PRINCE.getID())
					{
						ply.sendMessage(csplayer.getDonorRank().getTag() + ChatColor.YELLOW + p.getName() + " joined the game.");
					}
					else
					{
						if(csp.joinMessages)
						{
							ply.sendMessage(ChatColor.YELLOW + p.getName() + " joined the game.");
						}
						else
						{
							if(!csp.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString()) && csp.guild.equalsIgnoreCase(csplayer.guild))
							{
								if(ply != null && ply.isOnline())
								{
									ply.sendMessage(ChatColor.BLUE + "Guild > " + ChatColor.YELLOW + p.getName() + " joined the game.");// TODO
																																		// Implement
																																		// guild
																																		// chat
																																		// toggle
																																		// option
								}
							}
						}
					}
				}
			}
		}

		if(csplayer.guildspawn || csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
		{
			p.teleport(teamData.getSpawn());
		}
		else
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

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if(csplayer.getKit() == null || csplayer.getKit() instanceof ClassBannerman || csplayer.getKit() instanceof ClassBannerman && !csplayer.allVotes())
				{
					csplayer.setKit(new ClassSwordsman(plugin, p));
				}
				csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
				csplayer.getKit().gameKit();
				p.updateInventory();
			}
		}, 10L);
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();

		for(Player pl : Bukkit.getServer().getOnlinePlayers())
		{
			CSPlayer cspl = plugin.datahandler.utils.getCSPlayer(pl);
			if(cspl.joinMessages)
			{
				pl.sendMessage(ChatColor.YELLOW + p.getName() + " left the game.");
			}
		}

		if(plugin.datahandler.utils.getCSPlayer(p) != null)
		{
			if(plugin.datahandler.gamemodehandler.inMap(p.getLocation()) && (p.getMaxHealth() - p.getHealth()) > 0)
			{
				p.damage(100D);
			}
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
			if(csplayer.isDueling())
			{
				Duel duel = csplayer.getDuel();
				duel.setWinner(duel.getOtherPlayer(p));
				csplayer.getDuel().endArena();
			}
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().clearKit();
				p.updateInventory();
			}
			if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
			{
				Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
				if(guild != null)
				{
					for(CSPlayer csp : plugin.datahandler.playerData)
					{
						if(!csp.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString()) && csp.guild.equalsIgnoreCase(csplayer.guild))
						{
							Player ply = csp.getPlayer();
							if(ply != null && ply.isOnline())
							{
								ply.sendMessage(ChatColor.BLUE + "Guild > " + ChatColor.YELLOW + p.getName() + " left the game.");// TODO
																																	// Implement
																																	// guild
																																	// chat
																																	// toggle
																																	// option
							}
						}
					}
				}
			}
			csplayer.matchStats.setStreak(0);
			csplayer.removePerms();
			plugin.datahandler.gamemodehandler.leaveTeam(p);
			e.setQuitMessage(null);
		}

		for(Player ply : Bukkit.getOnlinePlayers())
		{
			CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
			if(csp.getKit() instanceof ClassBannerman)
			{
				((ClassBannerman) csp.getKit()).buffed.remove(p.getName());
			}
		}
	}

	@EventHandler
	private void onFoodLevelChange(FoodLevelChangeEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			if(e.getFoodLevel() < ((Player) e.getEntity()).getFoodLevel())
			{
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{
		if(plugin.datahandler.restarting)
		{
			e.setCancelled(true);
			return;
		}
		Player damager = null;
		if(e.getDamager() instanceof Player)
		{
			damager = (Player) e.getDamager();
		}
		else if(e.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) e.getDamager();
			if(arrow.getShooter() instanceof Player)
			{
				damager = (Player) arrow.getShooter();
			}
		}
		if(damager == null)
		{
			return;
		}

		if(e.getEntity() instanceof Horse)
		{
			if(e.getEntity().getPassenger() instanceof Player)
			{
				if(plugin.datahandler.gamemodehandler.getTeam(damager).equals(plugin.datahandler.gamemodehandler.getTeam((Player) e.getEntity().getPassenger())) && !plugin.datahandler.utils.getCSPlayer(damager).isDueling())
				{
					e.setCancelled(true);
					return;
				}
			}
		}

		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(damager);
		if(e.getEntity() instanceof Player)
		{
			boolean dueling = csplayer.isDueling();
			if(dueling)
			{
				Duel duel = csplayer.getDuel();
				if(!duel.isStarted() || duel.isEnded())
				{
					e.setCancelled(true);
				}
				else
				{
					plugin.datahandler.utils.getCSPlayer((Player) e.getEntity()).getKit().onTakeDamage(e);
					csplayer.getKit().onDealDamage(e);
				}
				return;
			}
			if(plugin.datahandler.gamemodehandler.getTeam(damager).equals(plugin.datahandler.gamemodehandler.getTeam((Player) e.getEntity())) && !dueling)
			{
				if(csplayer.getKit() != null)
				{
					csplayer.getKit().onTeamDamage(e);
				}
				e.setCancelled(true);
				return;
			}

			Player damaged = (Player) e.getEntity();
			CSPlayer dcsplayer = plugin.datahandler.utils.getCSPlayer((Player) damaged);

			if(damaged.getHealth() >= 20)
			{
				dcsplayer.assisting.clear();
			}
			if(!dcsplayer.assisting.contains(damager.getUniqueId()))
			{
				dcsplayer.assisting.add(damager.getUniqueId());
			}

			dcsplayer.getKit().onTakeDamage(e);
		}

		if(csplayer.getKit() != null)
		{
			csplayer.getKit().onDealDamage(e);
		}
	}

	@EventHandler
	private void onEntityDamage(EntityDamageEvent e)
	{
		Entity en = e.getEntity();
		Location loc = en.getLocation();
		if(en instanceof Player && (plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc)))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onPlayerDeath(PlayerDeathEvent e)
	{
		Player p = e.getEntity();
		Player pl = p.getKiller();

		e.setDroppedExp(0);
		e.getDrops().clear();
		e.setDeathMessage(null);

		final Spigot spigot = p.getPlayer().spigot();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				if(spigot != null)
				{
					spigot.respawn();
				}
			}
		}, 10L);

		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		if(pl == null)
		{
			for(Player target : Bukkit.getServer().getOnlinePlayers())
			{
				CSPlayer cstarget = plugin.datahandler.utils.getCSPlayer(target);
				if(cstarget.deathMessages)
				{
					target.sendMessage(plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + ChatColor.RESET + " has died.");
				}
			}
		}
		else
		{
			if(csplayer.isDueling())
			{
				Duel duel = csplayer.getDuel();
				duel.setWinner(duel.getOtherPlayer(p));
				duel.endDuel();
				return;
			}
			csplayer.giveScore(ScoreType.DEATHS, 1, false, false, true);
			p.sendMessage("You were killed by " + plugin.datahandler.gamemodehandler.getTeam(pl).getChatColor() + pl.getName() + ChatColor.RESET + "!");
			CSPlayer killer = plugin.datahandler.utils.getCSPlayer(pl);
			killer.giveScore(ScoreType.KILLS, 1, false, true, true);
			plugin.datahandler.gamemodehandler.resetScoreboard(pl);
			pl.sendMessage("You killed " + plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + ChatColor.RESET + "!" + ChatColor.GRAY + " (" + killer.matchStats.getStreak() + ")");

			for(Player target : Bukkit.getServer().getOnlinePlayers())
			{
				CSPlayer cstarget = plugin.datahandler.utils.getCSPlayer(pl);
				if(cstarget.deathMessages)
				{
					target.sendMessage(plugin.datahandler.gamemodehandler.getTeam(pl).getChatColor() + pl.getName() + ChatColor.RESET + " has killed " + plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + ChatColor.RESET + "!");
				}
			}
		}

		csplayer.matchStats.setStreak(0);
		if(csplayer.assisting != null)
		{
			for(UUID uuid : csplayer.assisting)
			{
				if(Bukkit.getPlayer(uuid) != null)
				{
					Player assisted = Bukkit.getPlayer(uuid);
					if(assisted != null && assisted.isOnline() && pl != null && !assisted.getName().equals(pl.getName()) && plugin.datahandler.utils.getCSPlayer(assisted) != null)
					{
						plugin.datahandler.utils.getCSPlayer(assisted).giveScore(ScoreType.ASSISTS, 1, false, true, true);
						assisted.sendMessage("Assisted in killing " + plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + ChatColor.RESET + "!");
					}
				}
			}
		}
		csplayer.assisting.clear();
		if(csplayer.getKit() != null)
		{
			csplayer.getKit().onDeath(e);
			csplayer.getKit().clearKit();
		}

		for(Player ply : Bukkit.getOnlinePlayers())
		{
			CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
			if(csp.getKit() instanceof ClassBannerman)
			{
				((ClassBannerman) csp.getKit()).buffed.remove(p.getName());
			}
		}
	}

	@EventHandler
	private void onPlayerRespawn(PlayerRespawnEvent e)
	{
		final Player p = e.getPlayer();
		final CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		if(plugin.datahandler.gamemodehandler.getTeam(p) != null)
		{
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						csplayer.getKit().gameKit();
					}
				}, 10L);

			}

			if(csplayer.isDueling())
			{
				Duel duel = csplayer.getDuel();
				e.setRespawnLocation(new Location(Bukkit.getWorld("World"), duel.getId() * 100, 10, duel.getId() * 100));
				p.getInventory().setHelmet(null);
				p.getInventory().remove(Material.LADDER);
			}
			else
			{
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

				if(csplayer.guildspawn || csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
				{
					e.setRespawnLocation(plugin.datahandler.gamemodehandler.getTeam(p).getSpawn());
				}
				else
				{
					e.setRespawnLocation(guild.getSpawn());
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
				plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.CLASSES);
				plugin.datahandler.invListener.updateInventory(p.getUniqueId());
			}
		}
	}

	@EventHandler
	private void onPlayerDropItem(PlayerDropItemEvent e)
	{
		Player p = e.getPlayer();
		if(p.getGameMode() != GameMode.CREATIVE)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onPlayerPickupItem(PlayerPickupItemEvent e)
	{
		Player p = e.getPlayer();
		if(p.getGameMode() != GameMode.CREATIVE)
		{
			e.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void onPlayerInteract(PlayerInteractEvent e)
	{
		Player p = e.getPlayer();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		if(csplayer.getKit() != null)
		{
			Action a = e.getAction();
			Block b = e.getClickedBlock();
			e.setCancelled(doContainerClick(p, b));
			if(a == Action.LEFT_CLICK_AIR || a == Action.RIGHT_CLICK_AIR)
			{
				doFlagCheck(p, p.getTargetBlock((HashSet<Byte>) null, 100));
			}
			if(a == Action.LEFT_CLICK_BLOCK || a == Action.RIGHT_CLICK_BLOCK)
			{
				doFlagCheck(p, b);
				if(doResupply(p, b) || doGateClick(b))
				{
					e.setCancelled(true);
					return;
				}
			}
			if(a == Action.LEFT_CLICK_BLOCK)
			{
				doCauldronRemove(p, b);
				doSignClick(b, p, true);
				if(doFireClick(p))
				{
					e.setCancelled(true);
					return;
				}
			}
			if(a == Action.RIGHT_CLICK_BLOCK)
			{
				doSignClick(b, p, false);
				doCakeClick(p, b);
				if(doTrebuchetLeverClick(p, b))
				{
					e.setCancelled(true);
					return;
				}
			}
			csplayer.getKit().onPlayerInteract(e);
		}
	}

	private void doFlagCheck(Player p, Block b)
	{
		if(!plugin.datahandler.restarting)
		{
			if(b.getType() == Material.WOOL || b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN)
			{
				Location loc = b.getLocation();
				if(plugin.datahandler.gamemodehandler.inGuildRoom(loc))
				{
					CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
					if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
					{
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						ObjectiveData objectiveData = plugin.datahandler.currentWorld.guildMap.getObjectiveDataByLoc(plugin, guild, loc);
						if(objectiveData != null)
						{
							doFlagTeleport(p, objectiveData);
						}
					}
				}
				else if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc))
				{
					for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
					{
						for(TeamData teamData : objectiveData.wool.keySet())
						{
							if(loc.getWorld() == objectiveData.wool.get(teamData).getWorld() && (loc == objectiveData.wool.get(teamData) || loc.distance(objectiveData.wool.get(teamData)) <= 1))
							{
								doFlagTeleport(p, objectiveData);
							}
						}
					}
				}
			}
		}
	}

	private void doFlagTeleport(Player p, ObjectiveData objectiveData)
	{
		if(objectiveData.getFlag().getControlling() != null && objectiveData.getFlag().getControlling() == plugin.datahandler.gamemodehandler.getTeam(p))
		{
			int allyCount = 0;
			int enemyCount = 0;
			TeamData lt = objectiveData.getFlag().getControlling();
			for(Player pl : Bukkit.getOnlinePlayers())
			{
				Location lo = pl.getLocation();
				if(plugin.datahandler.gamemodehandler.inWorld(lo) && lo.distance(objectiveData.getBase()) <= objectiveData.getRadius())
				{
					if(plugin.datahandler.gamemodehandler.getTeam(pl) != null && !pl.isDead())
					{
						lt = plugin.datahandler.gamemodehandler.getTeam(pl);
						if(lt == objectiveData.getFlag().getControlling())
						{
							allyCount += 1;
						}
						else
						{
							enemyCount += 1;
						}
					}

				}
			}

			if(!objectiveData.isCapturable() || enemyCount == 0 || (enemyCount != 0 && allyCount != 0))
			{
				if(objectiveData.isSpawnable())
				{
					p.teleport(objectiveData.getSpawn());
					// p.sendMessage(ChatColor.GREEN +
					// "Spawning at " +
					// objectiveData.getName());
					TitleDisplay.sendActionBar(p, ChatColor.GREEN + "Spawning at " + objectiveData.getName());
					plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.NONE);
					plugin.datahandler.invListener.updateInventory(p.getUniqueId());
					TitleDisplay.clearTitle(p);
				}
				else
				{
					p.sendMessage(ChatColor.RED + "You cannot spawn at this flag!");
				}
				return;
			}
			if(enemyCount != 0 && allyCount == 0 && objectiveData.isCapturable())
			{
				p.sendMessage(ChatColor.RED + "This flag is being captured by another team!");
				return;
			}
		}
		else
		{
			p.sendMessage(ChatColor.RED + "Your team doesn't control this flag!");
		}
	}

	private boolean doResupply(Player p, Block b)
	{
		if(b.getType() == Material.ENDER_CHEST)
		{
			if(plugin.datahandler.utils.getCSPlayer(p) != null)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				if(csplayer.getEnderchestCooldown() <= 0)
				{
					csplayer.getKit().clearKit();
					csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
					csplayer.getKit().gameKit();
					// p.sendMessage(ChatColor.GREEN +
					// "Your equipment has been replenished!");
					TitleDisplay.sendActionBar(p, ChatColor.GREEN + "Your equipment has been replenished!");
					return true;
				}
			}
			return true;
		}
		return false;
	}

	private boolean doGateClick(Block b)
	{
		if(b.getType() == Material.LEVER || b.getType() == Material.STONE_BUTTON || b.getType() == Material.WOOD_BUTTON)
		{
			for(GateData gateData : plugin.datahandler.currentWorld.gateData)
			{
				for(Location loc : gateData.locs)
				{
					if(b.getLocation().getWorld() == loc.getWorld() && b.getLocation().distance(loc) < 1)
					{
						boolean d = !gateData.getGate().use(plugin);
						if(d)
						{
							return d;
						}
						else
						{
							for(Location locs : gateData.locs)
							{
								if(locs.distance(loc) > 1)
								{
									BlockState state = locs.getBlock().getState();
									Lever lever = (Lever) state.getData();
									lever.setPowered(gateData.getGate().getOpened());
									state.setData(lever);
									state.update();
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean doContainerClick(Player p, Block b)
	{
		if((p.getGameMode() != GameMode.CREATIVE && b != null && b.getType() != null) && (b.getType() == Material.CHEST || b.getType() == Material.HOPPER || b.getType() == Material.FURNACE || b.getType() == Material.WORKBENCH || b.getType() == Material.ENCHANTMENT_TABLE || b.getType() == Material.ANVIL || b.getType() == Material.BREWING_STAND || b.getType() == Material.TRAPPED_CHEST))
		{
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean doFireClick(Player p)
	{
		if(!p.getGameMode().equals(GameMode.CREATIVE) && p.getTargetBlock((HashSet<Byte>) null, 6).getType() == Material.FIRE)
		{
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void doCakeClick(Player p, Block b)
	{
		for(Player ply : Bukkit.getOnlinePlayers())
		{
			if(plugin.datahandler.utils.getCSPlayer(ply) != null)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(ply);
				if(csplayer.getKit() instanceof ClassMedic && plugin.datahandler.gamemodehandler.getTeam(ply) == plugin.datahandler.gamemodehandler.getTeam(p) && !csplayer.isDueling())
				{
					ClassMedic medic = (ClassMedic) csplayer.getKit();
					if(medic.cakeLoc != null && medic.cakeLoc.getWorld() == b.getWorld() && medic.cakeLoc.distance(b.getLocation()) < 1 && !p.hasPotionEffect(PotionEffectType.REGENERATION) && ply.getHealth() < 20)
					{
						byte data = b.getData();
						if(data == 6)
						{
							medic.cooldownLeft = medic.maxCooldown;
							medic.cakeLoc = null;
							b.setType(Material.AIR);
						}
						else
						{
							data += 1;
							b.setData(data);
						}
						p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1, false, false));
						if(p.getName().equalsIgnoreCase(ply.getName()))
						{
							p.sendMessage(ChatColor.DARK_AQUA + "You healed yourself!");
						}
						else
						{
							csplayer.giveScore(ScoreType.SUPPS, 1, false, true, true);
							// p.sendMessage(ChatColor.DARK_AQUA +
							// "You were healed by " + ply.getName() + "!");
							// ply.sendMessage(ChatColor.DARK_AQUA +
							// "+1 support point(s)!");
							TitleDisplay.sendActionBar(p, ChatColor.GOLD + "You were healed by " + ply.getName() + "!");
							TitleDisplay.sendActionBar(ply, ChatColor.GOLD + "+1 support point(s)!");
						}
					}
				}
			}
		}
	}

	private void doSignClick(Block b, Player p, boolean up)
	{
		if(b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN)
		{
			Location loc = b.getLocation();
			if(plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
				{
					Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
					if(guild.getSignLocs(guild.guildTheme.getLeftPageLoc()).get(0).distance(loc) < 1)
					{
						if(csplayer.guildPage > 0)
						{
							csplayer.guildPage -= 1;
							guild.updateGuildSigns(p);
						}
						return;
					}
					if(guild.getSignLocs(guild.guildTheme.getRightPageLoc()).get(0).distance(loc) < 1)
					{
						if(csplayer.guildPage < 1)
						{
							csplayer.guildPage += 1;
							guild.updateGuildSigns(p);
						}
						return;
					}
				}
			}
			for(SiegeData siegeData : plugin.datahandler.currentWorld.siegeData)
			{
				Siege siege = siegeData.getSiege();
				if(siege instanceof TrebuchetSiege)
				{
					TrebuchetSiege trebuchetSiege = (TrebuchetSiege) siege;
					Sign sign = (Sign) b.getState();
					trebuchetSiege.setLeftRight(plugin, sign, p, up);
					trebuchetSiege.setUpDown(plugin, sign, p, up);
					return;
				}
			}
		}
	}

	private boolean doTrebuchetLeverClick(Player p, Block b)
	{
		if(b.getType() == Material.LEVER)
		{
			for(SiegeData siegeData : plugin.datahandler.currentWorld.siegeData)
			{
				Siege siege = siegeData.getSiege();
				if((siege instanceof TrebuchetSiege && b.getLocation().distance(siegeData.getBase()) < 20))
				{
					return(!((TrebuchetSiege) siege).use(plugin, p));
				}
			}
		}
		return false;
	}

	private void doCauldronRemove(Player p, Block b)
	{
		if(b.getType() != null && b.getType().equals(Material.CAULDRON))
		{
			for(Player ply : Bukkit.getOnlinePlayers())
			{
				if(plugin.datahandler.utils.getCSPlayer(ply) != null)
				{
					CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(ply);
					if(csplayer.getKit() instanceof ClassSiegeArcher)
					{
						Location loc = b.getLocation();
						ClassSiegeArcher siegeArcher = (ClassSiegeArcher) csplayer.getKit();
						if(siegeArcher.cauldronLoc != null && siegeArcher.cauldronLoc.getWorld() == loc.getWorld() && siegeArcher.cauldronLoc.distance(loc) < 1)
						{
							if(p.getName() != ply.getName())
							{
								ply.sendMessage(ChatColor.DARK_AQUA + "Your cauldron was destroyed by " + p.getName() + "!");
								p.sendMessage(ChatColor.DARK_AQUA + "You destroyed a cauldron placed by " + ply.getName() + "!");
							}
							siegeArcher.cauldronLoc = null;
							siegeArcher.cooldownLeft = siegeArcher.maxCooldown;
							siegeArcher.addCauldron = true;
							b.setType(Material.AIR);
							return;
						}
					}
				}
			}
		}
	}

	@EventHandler
	private void onPlayerInteractEntity(PlayerInteractEntityEvent e)
	{
		if(e.getRightClicked() instanceof Horse)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e)
	{
		if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onEntityDeath(EntityDeathEvent e)
	{
		e.setDroppedExp(0);
		e.getDrops().clear();
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	private void onProjectileHit(ProjectileHitEvent e)
	{
		if(e.getEntity().getShooter() instanceof Player)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer((Player) e.getEntity().getShooter());
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().onProjectileHit(e);
			}
		}
		else
		{
			if(e.getEntity() instanceof Arrow)
			{
				Arrow arrow = (Arrow) e.getEntity();
				for(SiegeData siegeData : plugin.datahandler.currentWorld.siegeData)
				{
					Siege siege = siegeData.getSiege();
					if(siege instanceof TrebuchetSiege)
					{
						TrebuchetSiege trebuchetSiege = (TrebuchetSiege) siege;
						for(Arrow a : (ArrayList<Arrow>) trebuchetSiege.arrows.clone())
						{
							if(arrow.getUniqueId() == a.getUniqueId())
							{
								Location loc = a.getLocation();
								a.getPassenger().remove();
								a.remove();
								trebuchetSiege.arrows.remove(a);
								loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 3f, false, true);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	private void onPlayerToggleSprint(PlayerToggleSprintEvent e)
	{
		if(plugin.datahandler.utils.getCSPlayer(e.getPlayer()) != null)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(e.getPlayer());
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().onSprint(e);
			}
		}
	}

	@EventHandler
	private void onPlayerToggleSneak(PlayerToggleSneakEvent e)
	{
		if(plugin.datahandler.utils.getCSPlayer(e.getPlayer()) != null)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(e.getPlayer());
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().onSneak(e);
			}
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent e)
	{
		if(e.getExited() instanceof Player)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer((Player) e.getExited());
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().onVehicleExit(e);
			}
		}
	}

	@EventHandler
	private void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e)
	{
		Player p = e.getPlayer();
		if(plugin.datahandler.gamemodehandler.inWorld(p.getLocation()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onEntityShootBow(EntityShootBowEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer((Player) e.getEntity());
			if(csplayer.getKit() != null)
			{
				csplayer.getKit().onPlayerShootBow(e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void onBlockPlace(BlockPlaceEvent e)
	{
		Player p = e.getPlayer();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		Block b = e.getBlock();
		Material mat = b.getType();
		Location loc = b.getLocation();
		if(p.getGameMode() != GameMode.CREATIVE)
		{
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				e.setCancelled(true);
				return;
			}
			if(plugin.datahandler.gamemodehandler.inMap(loc) || csplayer.isDueling())
			{
				if(mat.equals(Material.LADDER))
				{
					loc.add(0, -1, 0);
					if(loc.getBlock().getType().equals(Material.AIR))
					{
						ArrayList<Location> flocs = new ArrayList<Location>();

						for(double y = loc.getY(); y >= loc.getY() - 9; y--)
						{
							flocs.add(new Location(loc.getWorld(), loc.getX(), y, loc.getZ()));
						}
						ArrayList<Location> locs = new ArrayList<Location>();
						boolean breaks = false;
						for(Location l : flocs)
						{
							Block block = l.getBlock();
							if(block.getType().equals(Material.AIR) || block.getType().equals(null))
							{
								locs.add(l);
							}
							else
							{
								breaks = true;
								break;
							}
						}
						ItemStack is = p.getItemInHand();
						if(is.getAmount() >= locs.size() + 1)
						{
							int calculated;
							if(breaks)
							{
								calculated = is.getAmount() - (locs.size() + 1);
								if(calculated <= 0)// IF YOUR AMOUNT IS LESS
													// THAN OR
													// EQUAL TO 0
								{
									p.setItemInHand(null);
								}
								else
								{
									if(breaks)
									{
										is.setAmount(is.getAmount() - locs.size() - 1);
									}
								}
							}
							else
							{
								calculated = is.getAmount() - locs.size();
								if(calculated == 0)
								{
									p.setItemInHand(null);
								}
								else
								{
									is.setAmount(is.getAmount() - locs.size());
								}
							}

							for(Location l : locs)
							{
								Block block = l.getBlock();
								block.setType(Material.LADDER);
								block.setData(b.getData());
							}
						}
						else
						{
							e.setCancelled(true);
							p.sendMessage(ChatColor.DARK_RED + "You don't have enough ladders to setup a ladder of this size!");
						}
					}
				}
				else
				{
					if(csplayer.getKit() instanceof ClassSiegeArcher || csplayer.getKit() instanceof ClassBannerman || csplayer.getKit() instanceof ClassMedic)
					{
						csplayer.getKit().onBlockPlace(e);
					}
					else
					{
						e.setCancelled(true);
					}
				}
			}
		}
	}

	// TODO Check if banners or cauldrons are destroyed in explosions
	// (Trebuchets)

	@EventHandler
	private void onBlockBreak(BlockBreakEvent e)
	{
		final Player p = e.getPlayer();
		Block b = e.getBlock();
		Material mat = b.getType();
		Location loc = b.getLocation();
		if(p.getGameMode() != GameMode.CREATIVE)
		{
			e.setExpToDrop(0);
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				if(mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN))
				{
					final CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
					if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase(""))
					{
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								plugin.datahandler.utils.getGuild(csplayer.guild).updateGuildSigns(p);
							}
						}, 20L);
					}
				}
				e.setCancelled(true);
				return;
			}
			if(plugin.datahandler.gamemodehandler.inMap(loc) || plugin.datahandler.utils.getCSPlayer(p).isDueling())
			{
				if(mat.equals(Material.STANDING_BANNER))
				{
					for(Player ply : Bukkit.getOnlinePlayers())
					{
						if(plugin.datahandler.utils.getCSPlayer(ply) != null)
						{
							CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(ply);
							if(csplayer.getKit() instanceof ClassBannerman)
							{
								ClassBannerman bannerman = (ClassBannerman) csplayer.getKit();
								if(bannerman.bannerLoc != null && bannerman.bannerLoc.getWorld() == loc.getWorld() && bannerman.bannerLoc.distance(loc) < 1)
								{
									if(ply.getName() != p.getName())
									{
										ply.sendMessage(ChatColor.DARK_AQUA + "Your banner was destroyed by " + p.getName() + "!");
										p.sendMessage(ChatColor.DARK_AQUA + "You destroyed a banner placed by " + ply.getName() + "!");
									}
									bannerman.buffed.clear();
									bannerman.bannerLoc = null;
									bannerman.cooldownLeft = bannerman.maxCooldown;
									return;
								}
							}
						}
					}
				}
				if(mat.equals(Material.CAKE_BLOCK))
				{
					for(Player ply : Bukkit.getOnlinePlayers())
					{
						if(plugin.datahandler.utils.getCSPlayer(ply) != null)
						{
							CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(ply);
							if(csplayer.getKit() instanceof ClassMedic)
							{
								ClassMedic medic = (ClassMedic) csplayer.getKit();
								if(medic.cakeLoc != null && medic.cakeLoc.getWorld() == loc.getWorld() && medic.cakeLoc.distance(loc) < 1)
								{
									if(ply.getName().equalsIgnoreCase(p.getName()))
									{
										medic.cooldownLeft = medic.maxCooldown;
										medic.cakeLoc = null;
									}
									else
									{
										if(ply.getName() != p.getName())
										{
											ply.sendMessage(ChatColor.DARK_AQUA + "Your cake was destroyed by " + p.getName() + "!");
											p.sendMessage(ChatColor.DARK_AQUA + "You destroyed a cake placed by " + ply.getName() + "!");
										}
										medic.cakeLoc = null;
										medic.cooldownLeft = medic.maxCooldown;
									}
									return;
								}
							}
						}
					}
				}
				if(mat.equals(Material.LADDER))
				{
					b.setType(Material.AIR);
					loc.add(0, 1, 0);
					if(loc.getBlock().getType().equals(Material.LADDER))
					{
						ArrayList<Location> flocs = new ArrayList<Location>();

						for(double y = loc.getY(); y <= loc.getY() + 63; y++)
						{
							flocs.add(new Location(loc.getWorld(), loc.getX(), y, loc.getZ()));
						}
						ArrayList<Location> locs = new ArrayList<Location>();
						for(Location l : flocs)
						{
							Block block = l.getBlock();
							if(block.getType().equals(Material.LADDER))
							{
								locs.add(l);
							}
							else
							{
								break;
							}
						}
						p.getWorld().playSound(b.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, .5F, 5F);
						for(Location l : locs)
						{
							l.getBlock().setType(Material.AIR);
						}
					}
				}
				else
				{
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	private void onHangingBreakByEntity(HangingBreakByEntityEvent e)
	{
		e.setCancelled(true);
	}

	@EventHandler
	private void onPlayerVote(VotifierEvent e)
	{
		Vote v = e.getVote();
		String name = v.getUsername();
		if(name == null) return;
		UUID uuid = null;
		try
		{
			uuid = UUIDFetcher.getUUIDOf(plugin, name);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		if(uuid != null)
		{
			String service = v.getServiceName();
			/*
			 * for(Player p : Bukkit.getOnlinePlayers()) {
			 * p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.GRAY
			 * + name + " voted for the server on " + service + "!"); }
			 */
			switch(service.replace(".com", "").replace(".net", "").replace(".org", "").toLowerCase())
			{
			case "planetminecraft":
				vote(uuid, CSVoteSite.PMC);
				break;
			case "minestatus":
				vote(uuid, CSVoteSite.Minestatus);
				break;
			case "minecraftservers":
				vote(uuid, CSVoteSite.MinecraftServers);
				break;
			case "minecraft-mp":
				vote(uuid, CSVoteSite.MinecraftMP);
				break;
			}
		}
	}

	private void vote(UUID uuid, CSVoteSite voteSite)
	{
		Player p = Bukkit.getPlayer(uuid);
		long now = System.currentTimeMillis() / 1000;
		if(p != null && p.isOnline())
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
			Location loc = p.getLocation();
			csplayer.voted.put(voteSite, now);
			p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.GREEN + "Thank you for voting on " + WordUtils.capitalize(voteSite.toString()) + "!");
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				csplayer.getKit().clearKit();
				csplayer.getKit().itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
				csplayer.getKit().gameKit();
			}
		}
		SQLConnection sql = plugin.datahandler.sqlConnection;
		Connection c = sql.connection;
		try
		{
			PreparedStatement statement = c.prepareStatement("SELECT vote FROM player_data WHERE uuid=?");
			statement.setString(1, uuid.toString());
			ResultSet result = statement.executeQuery();
			if(result.next())
			{
				String vote = "";
				String[] voting = result.getString("vote").split(";");
				if(voting.length > 0)
				{
					for(String str : voting)
					{
						String[] s = str.split(",");
						if(s.length > 1)
						{
							int id = Integer.parseInt(s[0]);
							long time = Long.parseLong(s[1]);
							if(id == voteSite.getId())
							{
								time = now;
							}
							vote += id + "," + time + ";";
						}
					}
				}
				else
				{
					vote += voteSite.getId() + "," + now + ";";
				}
				PreparedStatement s = c.prepareStatement("UPDATE player_data SET vote=? WHERE uuid=?;");
				s.setString(1, vote);
				s.setString(2, uuid.toString());

				s.execute();
				s.close();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
