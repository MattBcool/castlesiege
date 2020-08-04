package com.castlesiege.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.castlesiege.Main;
import com.castlesiege.classes.ClassAxeman;
import com.castlesiege.classes.ClassBannerman;
import com.castlesiege.classes.ClassCavalry;
import com.castlesiege.classes.ClassCrossbowman;
import com.castlesiege.classes.ClassKnight;
import com.castlesiege.classes.ClassLancer;
import com.castlesiege.classes.ClassMaceman;
import com.castlesiege.classes.ClassMedic;
import com.castlesiege.classes.ClassPikeman;
import com.castlesiege.classes.ClassRangedCavalry;
import com.castlesiege.classes.ClassSiegeArcher;
import com.castlesiege.classes.ClassSpearman;
import com.castlesiege.data.flags.MovingFlag;
import com.castlesiege.data.flags.TFlag;
import com.castlesiege.data.sieges.RamSiege;
import com.castlesiege.data.sieges.Siege;
import com.castlesiege.data.sieges.TrebuchetSiege;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.ScoreType;
import com.comphenix.packetwrapper.WrapperPlayServerMount;

public class ServerTick
{
	private Main plugin;

	private int tick20;
	private int tick30;
	private int tick60;
	private int tick600;

	public ServerTick(Main plugin)
	{
		this.plugin = plugin;

		tick20 = 0;
		tick30 = 0;
		tick60 = 0;
		tick600 = 0;
	}

	public void doTicks()
	{
		do1Tick();
		do20Tick();
		do30Tick();
		do60Tick();
		do600Tick();
	}

	private void do1Tick()
	{
		doHorseFix();
		doFlagCapturing();
		doCSPlayerLoop();
		plugin.datahandler.broadcasts.tick();
	}

	private void do20Tick()
	{
		if(tick20 >= 20)
		{
			doPlayerLoop();
			handleDuels();
			checkForRestart();
			tick20 = 0; 
		}
		tick20++;
	}

	private void do30Tick()
	{
		if(tick30 >= 30)
		{
			doSiegeUse();
			tick30 = 0;
		}
		tick30++;
	}

	private void do60Tick()
	{
		if(tick60 >= 60)
		{
			doOutOfMapDamage();
			tick60 = 0;
		}
		tick60++;
	}

	private void do600Tick()
	{
		if(tick600 >= 600)
		{
			doMapClickMessage();
			tick600 = 0;
		}
		tick600++;
	}

	private void doHorseFix()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(p.getVehicle() != null)
			{
				WrapperPlayServerMount attachPacket = new WrapperPlayServerMount();
				attachPacket.setEntityID(p.getVehicle().getEntityId());
				attachPacket.setPassengerIds(new int[]
				{ p.getEntityId() });
				for(Player ply : Bukkit.getOnlinePlayers())
				{
					if(!p.getName().equalsIgnoreCase(ply.getName()))
					{
						attachPacket.sendPacket(ply);
					}
				}
			}
		}
	}

	private void doMapClickMessage()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(p.getLocation()) && !plugin.datahandler.restarting)
			{
				TitleDisplay.sendTitle(p, 10, 100, 10, "", "Click a flag on the map to begin playing!");
			}
		}
	}

	private void doPlayerLoop()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(plugin.datahandler.utils.getCSPlayer(p) != null)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				csplayer.timePlayed += 1;
				if(csplayer.getKit() instanceof ClassBannerman)
				{
					((ClassBannerman) csplayer.getKit()).buffAllies();
				}
				switch(plugin.datahandler.utils.getCSPlayer(p).scoreboard)
				{
				case 0:
					plugin.datahandler.gamemodehandler.updateTimeScoreboard(p, "Stats");
					break;
				case 1:
					plugin.datahandler.gamemodehandler.updateTimeScoreboard(p, "Flags");
					plugin.datahandler.gamemodehandler.updateFlagScoreboard(p);
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleDuels()
	{
		for(Duel duel : (ArrayList<Duel>) plugin.datahandler.duels.clone())
		{
			if(duel.getTimer() <= 0)
			{
				if(duel.isAccepted())
				{
					if(duel.isStarted())
					{
						duel.endDuel();
					}
					else
					{
						if(duel.isEnded())
						{
							duel.endArena();
						}
						else
						{
							duel.startDuel();
						}
					}
				}
				else
				{
					Player[] players = duel.getPlayers();
					players[0].sendMessage(ChatColor.GRAY + "Your duel request to " + players[1].getName() + " has expired!");
					players[1].sendMessage(ChatColor.GRAY + "Your duel request from " + players[0].getName() + " has expired!");
					duel.removeDuel();
				}
			}
			else
			{
				duel.setTimer(duel.getTimer() - 1);
				if(duel.isAccepted())
				{
					if(!duel.isStarted())
					{
						if(!duel.isEnded())
						{
							Player[] players = duel.getPlayers();
							if(duel.getTimer() == 0)
							{
								if(players[0].isOnline())
								{
									TitleDisplay.sendTitle(players[0], 0, 20, 10, ChatColor.GRAY + "Fight", "");
								}
								if(players[0].isOnline())
								{
									TitleDisplay.sendTitle(players[1], 0, 20, 10, ChatColor.GRAY + "Fight", "");
								}
							}
							else
							{
								if(players[0].isOnline())
								{
									TitleDisplay.sendTitle(players[0], 0, 20, 10, ChatColor.GRAY + "" + duel.getTimer(), ChatColor.DARK_GRAY + "Prepare to fight!");
								}
								if(players[1].isOnline())
								{
									TitleDisplay.sendTitle(players[1], 0, 20, 10, ChatColor.GRAY + "" + duel.getTimer(), ChatColor.DARK_GRAY + "Prepare to fight!");
								}
							}
						}
					}
				}
				BossBar bossBar = duel.getBossBar();
				bossBar.setProgress((double) duel.getTimer() / (double) duel.getMaxTimer());
				bossBar.setTitle(ChatColor.GRAY + "" + duel.getTimer());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void checkForRestart()
	{
		if(plugin.datahandler.timeLeft < 0)
		{
			HashMap<TeamData, Integer> teams = new HashMap<TeamData, Integer>();
			for(TeamData teamData : plugin.datahandler.currentWorld.teamData)
			{
				if(!teamData.getName().equalsIgnoreCase("None"))
				{
					teams.put(teamData, 0);
					for(ObjectiveData objectiveData : plugin.datahandler.currentWorld.objectiveData)
					{
						if(objectiveData.getFlag().getControlling() != null && teamData == objectiveData.getFlag().getControlling())
						{
							teams.put(teamData, teams.get(teamData) + 1);
						}
					}
				}
			}

			TeamData lastTeam = null;
			int objs = 0;

			for(TeamData teamData : ((HashMap<TeamData, Integer>) teams.clone()).keySet())
			{
				if(lastTeam == null)
				{
					lastTeam = teamData;
					objs = teams.get(teamData);
				}
				else
				{
					if(objs > teams.get(teamData))
					{
						teams.remove(teamData);
					}
					else
					{
						teams.remove(lastTeam);
						lastTeam = teamData;
						objs = teams.get(teamData);
					}
				}

			}
			if(teams.size() > 1)
			{
				plugin.datahandler.gamemodehandler.doRestart(null);
			}
			else
			{
				plugin.datahandler.gamemodehandler.doRestart(lastTeam);
			}
		}
		plugin.datahandler.timeLeft -= 1;
	}

	private void doOutOfMapDamage()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			Location loc = p.getLocation();
			if(!p.getGameMode().equals(GameMode.CREATIVE) && plugin.datahandler.gamemodehandler.inWorld(loc) && !plugin.datahandler.gamemodehandler.inMap(loc) && !plugin.datahandler.gamemodehandler.inSpawnRoom(loc))
			{
				p.damage(5);
				// p.sendMessage(ChatColor.DARK_RED +
				// "You're losing health. Get back to the map bounds!");
				TitleDisplay.sendActionBar(p, ChatColor.DARK_RED + ChatColor.BOLD.toString() + "You're losing health. Get back to the map bounds!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void doSiegeUse()
	{
		for(SiegeData siegeData : plugin.datahandler.currentWorld.siegeData)
		{
			Siege siege = siegeData.getSiege();
			if(siege != null)
			{
				boolean doUse = true;
				if(siege instanceof RamSiege)
				{
					RamSiege ramSiege = ((RamSiege) siege);
					if(!ramSiege.isCompleted())
					{
						Location base = siegeData.getBase();
						World world = base.getWorld();
						double x = base.getBlockX() + 0.5;
						double y = base.getBlockY();
						double z = base.getBlockZ() + 0.5;
						ArrayList<Location> locs = new ArrayList<Location>();
						int yaw = (int) base.getYaw();
						if(yaw == 0 || yaw == -0)
						{
							locs.add(new Location(world, x, y, z + 3));
							locs.add(new Location(world, x, y, z + 5));
							locs.add(new Location(world, x - 2, y, z + 3));
							locs.add(new Location(world, x - 2, y, z + 5));
						}
						if(yaw == 90)
						{
							locs.add(new Location(world, x - 3, y, z));
							locs.add(new Location(world, x - 5, y, z));
							locs.add(new Location(world, x - 3, y, z - 2));
							locs.add(new Location(world, x - 5, y, z - 2));
						}
						if(yaw == -90)
						{
							locs.add(new Location(world, x + 3, y, z));
							locs.add(new Location(world, x + 5, y, z));
							locs.add(new Location(world, x + 3, y, z + 2));
							locs.add(new Location(world, x + 5, y, z + 2));
						}
						if(yaw == 180 || yaw == -180)
						{
							locs.add(new Location(world, x, y, z - 3));
							locs.add(new Location(world, x, y, z - 5));
							locs.add(new Location(world, x + 2, y, z - 3));
							locs.add(new Location(world, x + 2, y, z - 5));
						}
						ArrayList<Player> players = new ArrayList<Player>();
						for(Player p : Bukkit.getOnlinePlayers())
						{
							if(plugin.datahandler.gamemodehandler.getTeam(p) == siegeData.getUsableTeam())
							{
								ArrayList<Location> locsc = (ArrayList<Location>) locs.clone();
								for(Location loc : locsc)
								{
									if(p.getWorld() == loc.getWorld() && p.getLocation().distance(loc) < 0.8)
									{
										players.add(p);
										locs.remove(loc);
									}
								}
							}
						}
						if(locs.size() > 0)
						{
							doUse = false;
							ramSiege.reset();
						}
						else
						{
							for(Player p : players)
							{
								CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
								if(csplayer != null)
								{
									csplayer.giveScore(ScoreType.SUPPS, 6, false, true, true);
									// player.sendMessage(ChatColor.DARK_AQUA +
									// "+6 support point(s)!");
									TitleDisplay.sendActionBar(p, ChatColor.GOLD + "+6 support point(s)!");
								}
							}
						}
					}
				}
				if(siege instanceof TrebuchetSiege)
				{
					doUse = false;
				}
				if(doUse)
				{
					siegeData.getSiege().use(plugin);
				}
			}
		}
	}

	private void doFlagCapturing()
	{
		boolean reset = true;
		TeamData lastTeam = null;
		for(ObjectiveData od : plugin.datahandler.currentWorld.objectiveData)
		{
			if((od != null && od.getFlag() != null))
			{
				if(od.getFlag().getControlling() == null)
				{
					reset = false;
				}
				else
				{
					TeamData teamData = od.getFlag().getControlling();
					if(lastTeam != null && lastTeam != teamData)
					{
						reset = false;
					}
					lastTeam = teamData;
				}
				if(od.isCapturable())
				{
					if(od.getFlag().getControlling() == null || od.getFlag().getControlling().getName().equalsIgnoreCase("None"))
					{
						boolean doCap = true;
						TeamData lt = null;
						int count = 0;
						int bannermanCount = 0;
						for(Player p : Bukkit.getOnlinePlayers())
						{
							Location loc = p.getLocation();
							if(plugin.datahandler.gamemodehandler.inWorld(loc) && loc.distance(od.getBase()) <= od.getRadius())
							{
								if(lt == null)
								{
									lt = plugin.datahandler.gamemodehandler.getTeam(p);
									if(plugin.datahandler.utils.getCSPlayer(p).getKit() instanceof ClassBannerman)
									{
										bannermanCount += 1;
									}
									count += 1;
								}
								else
								{
									if(lt != plugin.datahandler.gamemodehandler.getTeam(p))
									{
										doCap = false;
									}
								}
							}
						}
						if(doCap)
						{
							od.getFlag().doCapture(plugin, lt, count, bannermanCount);
						}
					}
					else
					{
						int allyCount = 0;
						int enemyCount = 0;
						int bannermanCount = 0;
						TeamData lt = null;
						if(od.getFlag().getControlling() != null)
						{
							lt = od.getFlag().getControlling();
						}
						for(Player p : Bukkit.getOnlinePlayers())
						{
							if(p.getVehicle() == null && !p.isDead())
							{
								Location loc = p.getLocation();
								if(plugin.datahandler.gamemodehandler.inWorld(loc) && loc.distance(od.getBase()) <= od.getRadius())
								{
									if(plugin.datahandler.gamemodehandler.getTeam(p) != null)
									{
										lt = plugin.datahandler.gamemodehandler.getTeam(p);
										if(plugin.datahandler.utils.getCSPlayer(p).getKit() instanceof ClassBannerman)
										{
											bannermanCount += 1;
										}
										if(lt == od.getFlag().getControlling())
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
						}
						if(allyCount != 0 || enemyCount != 0)
						{
							if(enemyCount == 0 && allyCount > 0)
							{
								od.getFlag().doCapture(plugin, lt, allyCount, bannermanCount);
							}
							if(allyCount == 0 && enemyCount > 0)
							{
								od.getFlag().doCapture(plugin, lt, enemyCount, bannermanCount);
							}
						}
					}
				}
				if(od.getFlag() instanceof MovingFlag)
				{
					((MovingFlag) od.getFlag()).doIdleAnimation();
				}
				if(od.getFlag() instanceof TFlag)
				{
					((TFlag) od.getFlag()).doIdleAnimation();
				}
			}
		}
		if(reset)
		{
			plugin.datahandler.gamemodehandler.doRestart(lastTeam);
		}
	}

	private void doCSPlayerLoop()
	{
		for(CSPlayer csplayer : plugin.datahandler.playerData)
		{
			doEnderchestCooldown(csplayer);
			doClassCooldown(Bukkit.getPlayer(csplayer.getUniqueId()), csplayer);
		}
	}

	private void doEnderchestCooldown(CSPlayer csplayer)
	{
		if(csplayer.getEnderchestCooldown() > 0)
		{
			csplayer.setEnderchestCooldown(csplayer.getEnderchestCooldown() - 1);
		}
	}

	@SuppressWarnings("unchecked")
	private void doClassCooldown(Player p, CSPlayer csplayer)
	{
		if(p == null)
		{
			return;
		}
		if(csplayer.getKit() instanceof ClassSpearman)
		{
			ClassSpearman kit = (ClassSpearman) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
			}
		}
		else if(csplayer.getKit() instanceof ClassKnight)
		{
			ClassKnight kit = (ClassKnight) csplayer.getKit();
			if(kit.cooldownTime > 0)
			{
				kit.cooldownTime--;
				if(kit.cooldownTime <= 0)
				{
					p.setFoodLevel(20);
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassPikeman)
		{
			ClassPikeman kit = (ClassPikeman) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft == 0)
				{
					kit.inStance = true;
					p.setWalkSpeed(0f);
					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 1.3f);
				}
			}
			if(kit.cancelKnockback > 0)
			{
				kit.cancelKnockback--;

				if(kit.cancelKnockback <= 0)
				{
					p.setVelocity(new Vector(0, 0, 0));
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassMaceman)
		{
			ClassMaceman kit = (ClassMaceman) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
			}
		}
		else if(csplayer.getKit() instanceof ClassAxeman)
		{
			ClassAxeman kit = (ClassAxeman) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
			}
		}
		else if(csplayer.getKit() instanceof ClassCrossbowman)
		{
			ClassCrossbowman kit = (ClassCrossbowman) csplayer.getKit();
			for(Arrow arrow : (ArrayList<Arrow>) ((ClassCrossbowman) csplayer.getKit()).activeArrows.clone())
			{
				if(!arrow.getLocation().getChunk().isLoaded())
				{
					arrow.remove();
					((ClassCrossbowman) csplayer.getKit()).activeArrows.remove(arrow);
				}
			}
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft % 10 == 0)
				{
					p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LADDER_FALL, .3f, 1.7f);
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassCavalry)
		{
			ClassCavalry kit = (ClassCavalry) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft <= 0)
				{
					if(!p.getInventory().contains(Material.MONSTER_EGG))
					{
						p.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 100));
					}
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassRangedCavalry)
		{
			ClassRangedCavalry kit = (ClassRangedCavalry) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft <= 0)
				{
					if(!p.getInventory().contains(Material.MONSTER_EGG))
					{
						p.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 100));
					}
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassLancer)
		{
			ClassLancer kit = (ClassLancer) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft <= 0)
				{
					if(!p.getInventory().contains(Material.MONSTER_EGG))
					{
						p.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 100));
					}
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassSiegeArcher)
		{
			ClassSiegeArcher kit = (ClassSiegeArcher) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
			}
			else
			{
				if(kit.addCauldron)
				{
					kit.addCauldron = false;
					if(!p.getInventory().contains(Material.CAULDRON_ITEM))
					{
						p.getInventory().addItem(new ItemStack(Material.CAULDRON_ITEM));
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, false));
				}
			}
			if(kit.addArrow)
			{
				p.getInventory().addItem(new ItemStack(Material.ARROW));
				kit.addArrow = false;
			}
			if(kit.removeArrow)
			{
				p.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
				kit.removeArrow = false;
			}
		}
		else if(csplayer.getKit() instanceof ClassBannerman)
		{
			ClassBannerman kit = (ClassBannerman) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft <= 0)
				{
					ItemStack itemStack = kit.getBannerItemStack();
					if(!p.getInventory().contains(itemStack))
					{
						p.getInventory().addItem(itemStack);
					}
				}
			}
		}
		else if(csplayer.getKit() instanceof ClassMedic)
		{
			ClassMedic kit = (ClassMedic) csplayer.getKit();
			if(kit.cooldownLeft > 0)
			{
				kit.cooldownLeft--;
				p.setExp((kit.cooldownLeft / (float) kit.maxCooldown));
				if(kit.cooldownLeft <= 0)
				{
					ItemStack cake = new ItemStack(Material.CAKE);
					if(!p.getInventory().contains(cake))
					{
						p.getInventory().addItem(cake);
					}
				}
			}
		}
	}
}
