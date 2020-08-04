package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;
import com.castlesiege.player.ScoreType;

public class ClassMedic extends CSClass
{
	public Location cakeLoc;
	public int cooldownLeft = 0;
	public int maxCooldown = 100;

	private Team team1, team2;
	private Objective healthbarbelow;

	public ClassMedic(Main plugin, Player player)
	{
		super(plugin, player, "Medic", CSClassCategory.SUPPORT, 0, true);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.LEATHER_CHESTPLATE).setLeatherColor(224, 224, 224).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(224, 224, 224).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItem(1, new IconDisplay(Material.PAPER).setName("Bandage").create());
		player.getInventory().setItem(3, new ItemStack(Material.CAKE));

		IconDisplay scissors = new IconDisplay(Material.SHEARS).setName("Scissors");
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			scissors.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			scissors.addLore("", ChatColor.BLUE + "+3 Attack Damage");
		}
		else
		{
			scissors.addLore("", ChatColor.BLUE + "+2 Attack Damage");
		}
		player.getInventory().setItem(0, scissors.create());

		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.LEATHER_BOOTS).setUnbreakable().hideAttributes();
		if(csplayer.hasVoted(CSVoteSite.MinecraftServers))
		{
			boots.addEnchantment(Enchantment.PROTECTION_FALL, 1);
		}
		player.getInventory().setBoots(boots.create());

		IconDisplay ladders = new IconDisplay(Material.LADDER, 5);
		if(csplayer.hasVoted(CSVoteSite.MinecraftMP))
		{
			ladders.setAmount(7);
		}
		player.getInventory().setItem(2, ladders.create());

		// setupBelowObj(); TODO Fix
		// doPlayerLoop(); TOXO Fix
	}

	@Override
	public void gameKit()
	{
		Player player = getPlayer();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
	}

	@Override
	public void clearKit()
	{
		removeBelowObj();
		super.clearKit();
	}

	@Override
	public void onDeath(PlayerDeathEvent event)
	{
		if(cakeLoc != null)
		{
			cakeLoc.getWorld().getBlockAt(cakeLoc).setType(Material.AIR);
		}
		cakeLoc = null;
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent e)
	{
		Player player = getPlayer();
		Block b = e.getBlock();
		if(b.getType() != null && b.getType() == Material.CAKE_BLOCK)
		{
			Block bl = e.getBlock().getLocation().clone().add(0, -1, 0).getBlock();
			if(bl.getType() != null && bl.getType().isSolid() && bl.getType() != Material.CAKE_BLOCK)
			{
				if(cakeLoc != null && cakeLoc.getWorld() != null)
				{
					cakeLoc.getWorld().getBlockAt(cakeLoc).setType(Material.AIR);
				}
				cakeLoc = e.getBlockPlaced().getLocation();
				return;
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You cannot place your cake here!");
				e.setCancelled(true);
			}
		}
		e.setCancelled(true);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(e.getClickedBlock() != null && e.getClickedBlock().getType() != Material.AIR && e.getClickedBlock().getType() == Material.CAKE_BLOCK && cakeLoc != null && e.getClickedBlock().getLocation().equals(cakeLoc))
		{
			e.setCancelled(true);
			if(e.getAction() == Action.LEFT_CLICK_BLOCK)
			{
				e.getClickedBlock().setType(Material.AIR);
				cakeLoc = null;
				cooldownLeft = maxCooldown;
			}
		}
	}

	@Override
	public void onTeamDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		Entity en = e.getEntity();
		if(en instanceof Player)
		{
			Player ply = (Player) en;
			if(player.getInventory().getItemInMainHand().getType() == Material.PAPER && !ply.hasPotionEffect(PotionEffectType.REGENERATION) && ply.getHealth() < 20)
			{
				plugin.datahandler.utils.getCSPlayer(player).giveScore(ScoreType.SUPPS, 1, false, true, true);
				// ply.sendMessage(ChatColor.DARK_AQUA + "You were healed by " +
				// player.getName() + "!");
				TitleDisplay.sendActionBar(ply, ChatColor.GOLD + "You were healed by " + player.getName() + "!");
				ply.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false));
				// player.sendMessage(ChatColor.DARK_AQUA +
				// "+1 support point(s)!");
				TitleDisplay.sendActionBar(player, ChatColor.GOLD + "+1 support point(s)!");
			}
		}
	}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(player.getInventory().getItemInMainHand().getType() == Material.SHEARS)
		{
			e.setDamage(doCustomDamage(player, 2));
		}
	}

	@SuppressWarnings("deprecation")
	private void setupBelowObj()
	{
		Player player = getPlayer();
		Scoreboard scoreboard = player.getScoreboard();
		if(scoreboard.getTeam("team1") == null)
		{
			team1 = scoreboard.registerNewTeam("team1");
		}
		else
		{
			team1 = scoreboard.getTeam("team1");
		}
		if(scoreboard.getTeam("team2") == null)
		{
			team2 = scoreboard.registerNewTeam("team2");
		}
		else
		{
			team2 = scoreboard.getTeam("team2");
		}
		team1.addPlayer(player);
		if(scoreboard.getObjective("healthbarbelow") == null)
		{
			healthbarbelow = scoreboard.registerNewObjective("healthbarbelow", "health");
			healthbarbelow.setDisplayName("/ 20");
			healthbarbelow.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
		else
		{
			healthbarbelow = scoreboard.getObjective("healthbarbelow");
		}
	}

	private void removeBelowObj()
	{
		Player player = getPlayer();
		Scoreboard scoreboard = player.getScoreboard();
		if(team1 != null)
		{
			team1.unregister();
		}
		if(team2 != null)
		{
			team2.unregister();
		}
		if(scoreboard.getObjective(DisplaySlot.BELOW_NAME) != null)
		{
			scoreboard.getObjective(DisplaySlot.BELOW_NAME).unregister();
		}
		if(scoreboard.getObjective("healthbarbelow") != null)
		{
			scoreboard.getObjective("healthbarbelow").unregister();
		}
	}

	@SuppressWarnings("deprecation")
	public boolean hasHealthDisplayed(Player player)
	{
		Scoreboard scoreboard = getPlayer().getScoreboard();
		Team team = scoreboard.getPlayerTeam(player);
		if(team.getName().equalsIgnoreCase("team1"))
		{
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void hideHealthBar(Player player)
	{
		team1.removePlayer(player);
		team2.addPlayer(player);
		plugin.datahandler.nametagManager.sendTeams(getPlayer());
	}

	@SuppressWarnings("deprecation")
	public void showHealthBar(Player player)
	{
		team2.removePlayer(player);
		team1.addPlayer(player);
		plugin.datahandler.nametagManager.sendTeams(getPlayer());
	}

	public void doPlayerLoop()
	{
		Player player = getPlayer();
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(plugin.datahandler.gamemodehandler.getTeam(player) == plugin.datahandler.gamemodehandler.getTeam(p))
			{
				showHealthBar(p);
			}
			else
			{
				hideHealthBar(p);
			}
		}
	}
}
