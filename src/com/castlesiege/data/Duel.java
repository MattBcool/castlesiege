package com.castlesiege.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.classes.CSClass;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.listeners.InventoryListener.InventoryPage;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

@SuppressWarnings("deprecation")
public class Duel
{
	private Main plugin;
	private UUID[] players;
	private UUID winner;
	private BossBar bossBar;
	private int id, maxTimer, timer;
	private boolean accepted, started, ended;

	public Duel(Main plugin, Player p1, Player p2)
	{
		this.plugin = plugin;
		players = new UUID[]
		{ p1.getUniqueId(), p2.getUniqueId() };
		bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
		bossBar.setVisible(true);
		plugin.datahandler.duels.add(this);
		id = plugin.datahandler.duels.size();
		maxTimer = 30;
		timer = 30;
		accepted = false;
		started = false;
		ended = false;
	}

	public Player[] getPlayers()
	{
		return new Player[]
		{ Bukkit.getPlayer(players[0]), Bukkit.getPlayer(players[1]) };
	}

	public Player getWinner()
	{
		return Bukkit.getPlayer(winner);
	}

	public void setWinner(Player player)
	{
		winner = player.getUniqueId();
	}

	public BossBar getBossBar()
	{
		return bossBar;
	}

	public int getId()
	{
		return id;
	}

	public int getMaxTimer()
	{
		return maxTimer;
	}

	public int getTimer()
	{
		return timer;
	}

	public void setTimer(int timer)
	{
		this.timer = timer;
	}

	public boolean isAccepted()
	{
		return accepted;
	}

	public boolean isStarted()
	{
		return started;
	}

	public boolean isEnded()
	{
		return ended;
	}

	public Player getOtherPlayer(Player player)
	{
		Player[] players = getPlayers();
		if(players[0].getName().equalsIgnoreCase(player.getName()))
		{
			return players[0];
		}
		return players[1];
	}

	public void accept()
	{
		accepted = true;
		initiateArena();
	}

	private void initiateArena()
	{
		pasteSchematic("duel_1");
		World world = Bukkit.getWorld("world");
		Player[] players = getPlayers();
		if(players[0].isOnline() && players[1].isOnline())
		{
			plugin.datahandler.invListener.pages.put(this.players[0], InventoryPage.NONE);
			plugin.datahandler.invListener.updateInventory(this.players[0]);
			players[0].getInventory().setHelmet(null);
			players[0].getInventory().remove(Material.LADDER);
			players[0].setGameMode(GameMode.SURVIVAL);
			players[0].setHealth(players[0].getMaxHealth());
			bossBar.addPlayer(players[0]);
			players[0].teleport(new Location(world, id * 100, 10, id * 100));
			plugin.datahandler.invListener.pages.put(this.players[1], InventoryPage.NONE);
			plugin.datahandler.invListener.updateInventory(this.players[1]);
			players[1].getInventory().setHelmet(null);
			players[1].getInventory().remove(Material.LADDER);
			players[1].setGameMode(GameMode.SURVIVAL);
			players[1].setHealth(players[1].getMaxHealth());
			bossBar.addPlayer(players[1]);
			players[1].teleport(new Location(world, id * 100, 10, id * 100));
		}
		else
		{
			endArena();
		}
		maxTimer = 10;
		timer = 10;
	}

	public void startDuel()
	{
		started = true;
		maxTimer = 120;
		timer = 120;
	}

	public void endDuel()
	{
		if(started)
		{
			ended = true;
			started = false;
			maxTimer = 10;
			timer = 10;
			Player[] players = getPlayers();
			if(players[0].isOnline() && players[1].isOnline())
			{
				if(winner == null)
				{
					TitleDisplay.sendTitle(players[0], 20, 20 * 10, 20, ChatColor.GRAY + "Tie!", "");
					TitleDisplay.sendTitle(players[1], 20, 20 * 10, 20, ChatColor.GRAY + "Tie!", "");
				}
				else
				{
					if(this.players[0].toString().equalsIgnoreCase(winner.toString()))
					{
						TitleDisplay.sendTitle(players[0], 20, 20 * 10, 20, ChatColor.RED + "Defeat!", "");
						TitleDisplay.sendTitle(players[1], 20, 20 * 10, 20, ChatColor.GREEN + "Victory!", "");
					}
					else
					{
						TitleDisplay.sendTitle(players[0], 20, 20 * 10, 20, ChatColor.GREEN + "Victory!", "");
						TitleDisplay.sendTitle(players[1], 20, 20 * 10, 20, ChatColor.RED + "Defeat!", "");
					}
				}
			}
			else
			{
				endArena();
			}
		}
		else
		{
			endArena();
		}
	}

	public void endArena()
	{
		pasteSchematic("duel_0");
		if(accepted)
		{
			bossBar.removeAll();
			Player[] players = getPlayers();
			resetPlayer(players[0]);
			resetPlayer(players[1]);
		}
		removeDuel();
	}

	public void removeDuel()
	{
		plugin.datahandler.duels.remove(this);
	}

	private void resetPlayer(Player player)
	{
		if(player.isOnline())
		{
			plugin.datahandler.invListener.pages.put(this.players[0], InventoryPage.CLASSES);
			plugin.datahandler.invListener.updateInventory(player.getUniqueId());
			TeamData teamData = plugin.datahandler.gamemodehandler.getTeam(player);
			if(teamData == null)
			{
				teamData = plugin.datahandler.gamemodehandler.randomizeTeam(player);
			}
			CSPlayer csplayer =plugin.datahandler.utils.getCSPlayer(player);
			CSClass csclass = csplayer.getKit();
			csclass.clearKit();
			csclass.itemKit(new ArrayList<CSVoteSite>(csplayer.voted.keySet()));
			csclass.gameKit();
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setSaturation(1);
			player.setFireTicks(0);
			player.setExp(0);
			player.setLevel(0);
			player.setGameMode(GameMode.SURVIVAL);
			if(player.getVehicle() != null)
			{
				player.getVehicle().eject();
				player.getVehicle().remove();
			}
			player.teleport(teamData.getSpawn());
		}
	}

	private void pasteSchematic(String schematic)
	{
		WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		File file = new File("plugins" + File.separator + "WorldEdit" + File.separator + "schematics" + File.separator + schematic + ".schematic");
		EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(Bukkit.getWorld("world")), we.getWorldEdit().getConfiguration().maxChangeLimit);
		try
		{
			CuboidClipboard cc = MCEditSchematicFormat.getFormat(file).load(file);
			cc.paste(session, new Vector(id * 100, 8, id * 100), false);
			return;
		}
		catch(MaxChangedBlocksException | com.sk89q.worldedit.data.DataException | IOException e2)
		{
			e2.printStackTrace();
		}
	}
}
