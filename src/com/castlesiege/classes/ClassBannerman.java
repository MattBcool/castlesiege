package com.castlesiege.classes;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.extra.TitleDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;
import com.castlesiege.player.ScoreType;

public class ClassBannerman extends CSClass
{
	public Location bannerLoc;
	public int cooldownLeft = 0;
	public int maxCooldown = 200;

	public ArrayList<String> buffed = new ArrayList<String>();

	public ClassBannerman(Main plugin, Player player)
	{
		super(plugin, player, "Bannerman", CSClassCategory.SUPPORT, 1, true);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);

		player.getInventory().setItem(2, getBannerItemStack());

		IconDisplay dagger = new IconDisplay(Material.WOOD_SWORD).setName("Dagger").setUnbreakable().hideAttributes();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			dagger.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			dagger.setLore("", ChatColor.BLUE + "+3 Attack Damage");
		}
		else
		{
			dagger.setLore("", ChatColor.BLUE + "+2 Attack Damage");
		}
		player.getInventory().setItem(0, dagger.create());

		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.LEATHER_BOOTS).setLeatherColor(89, 76, 63).setUnbreakable().hideAttributes();
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
		player.getInventory().setItem(1, ladders.create());
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
		buffed.clear();
		cooldownLeft = 0;
		if(bannerLoc != null && bannerLoc.getWorld() != null)
		{
			bannerLoc.getWorld().getBlockAt(bannerLoc).setType(Material.AIR);
		}
		bannerLoc = null;
		super.clearKit();
	}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getDamager() instanceof Player && player.getInventory().getItemInMainHand().getType() == Material.WOOD_SWORD)
		{
			e.setDamage(doCustomDamage(player, 2));
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent e)
	{
		Player player = getPlayer();
		Block b = e.getBlock();
		if(b.getType() != null && b.getType() == Material.STANDING_BANNER)
		{
			Block bl = e.getBlock().getLocation().clone().add(0, -1, 0).getBlock();
			if(bl.getType() != null && bl.getType().isSolid() && bl.getType() != Material.STANDING_BANNER)
			{
				if(bannerLoc != null && bannerLoc.getWorld() != null)
				{
					bannerLoc.getWorld().getBlockAt(bannerLoc).setType(Material.AIR);
				}
				bannerLoc = e.getBlockPlaced().getLocation();
				return;
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You cannot place your banner here!");
				e.setCancelled(true);
			}
		}
		e.setCancelled(true);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(e.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			Block b = e.getClickedBlock();
			if(b.getType() == Material.STANDING_BANNER && bannerLoc != null && bannerLoc.getWorld() == b.getWorld() && bannerLoc.distance(b.getLocation()) < 1)
			{
				if(bannerLoc != null && bannerLoc.getWorld() != null)
				{
					bannerLoc.getWorld().getBlockAt(bannerLoc).setType(Material.AIR);
				}
				buffed.clear();
				bannerLoc = null;
				cooldownLeft = maxCooldown;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public ItemStack getBannerItemStack()
	{
		Player player = getPlayer();
		DyeColor dyeColor = DyeColor.getByData((byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor());
		ItemStack itemStack = new ItemStack(Material.BANNER);
		BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
		bannerMeta.setBaseColor(dyeColor);
		bannerMeta.setLore(Arrays.asList(new String[]
		{ "Buffs all teammates within a 5 block radius!" }));
		itemStack.setItemMeta(bannerMeta);
		return itemStack;
	}

	public void buffAllies()
	{
		Player player = getPlayer();
		if(bannerLoc != null)
		{
			boolean b = false;
			for(Player ply : Bukkit.getOnlinePlayers())
			{
				Location l = ply.getLocation();
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
				boolean samePly = ply.getName().equalsIgnoreCase(player.getName());
				if(plugin.datahandler.gamemodehandler.getTeam(player) == plugin.datahandler.gamemodehandler.getTeam(ply) && !samePly || (csplayer.isDueling() && samePly))
				{
					if(l.getWorld() == bannerLoc.getWorld() && l.distance(bannerLoc) <= 6)
					{
						if(!ply.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
						{
							ply.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 0, true, true));
						}
						if(!ply.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
						{
							ply.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 0, true, true));
						}
						String name = ply.getName();
						if(!b && !buffed.contains(name) && !samePly && !csplayer.isDueling())
						{
							csplayer.giveScore(ScoreType.SUPPS, 1, false, true, true);
							// player.sendMessage(ChatColor.DARK_AQUA +
							// "+1 support point(s)!");
							TitleDisplay.sendActionBar(player, ChatColor.GOLD + "+1 support point(s)!");
							buffed.add(name);
							b = true;
						}
					}
					else
					{
						final String name = ply.getName();
						if(buffed.contains(name))
						{
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
							{
								@Override
								public void run()
								{
									buffed.remove(name);
								}
							}, 20L * 5);
						}
					}
				}
			}
		}
	}
}
