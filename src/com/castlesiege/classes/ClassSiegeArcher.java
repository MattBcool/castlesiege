package com.castlesiege.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassSiegeArcher extends CSClass
{
	public int cooldownLeft = 0;
	public final int maxCooldown = 20;
	public boolean addArrow, removeArrow, addCauldron;
	private List<UUID> fireArrows = new ArrayList<UUID>();
	public Location cauldronLoc;

	public ClassSiegeArcher(Main plugin, Player player)
	{
		super(plugin, player, "Siege Archer", CSClassCategory.RANGED, 1);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.LEATHER_CHESTPLATE).setLeatherColor(66, 31, 20).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(54, 32, 26).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItem(0, new IconDisplay(Material.BOW).setUnbreakable().hideAttributes().create());
		player.getInventory().setItem(2, new ItemStack(Material.ARROW, 24));
		player.getInventory().setItem(3, new IconDisplay(Material.CAULDRON_ITEM).setLore("Right click on this with arrows", "to create flaming arrows!").create());

		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.LEATHER_BOOTS).setLeatherColor(66, 31, 20).setUnbreakable().hideAttributes();
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
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, false));
	}

	@Override
	public void clearKit()
	{
		if(cauldronLoc != null)
		{
			cauldronLoc.getWorld().getBlockAt(cauldronLoc).setType(Material.AIR);
		}
		cauldronLoc = null;
		super.clearKit();
	}

	@Override
	public void onDeath(PlayerDeathEvent event)
	{
		if(cauldronLoc != null)
		{
			cauldronLoc.getWorld().getBlockAt(cauldronLoc).setType(Material.AIR);
		}
		cauldronLoc = null;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(e.getClickedBlock() != null && e.getClickedBlock().getType() != Material.AIR && e.getClickedBlock().getType() == Material.CAULDRON && cauldronLoc != null && e.getClickedBlock().getLocation().equals(cauldronLoc))
		{
			Player player = getPlayer();
			e.setCancelled(true);
			if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() != null && e.getItem().getType() == Material.ARROW)
			{
				int count = 0;
				for(ItemStack itemStack : player.getInventory().getContents())
				{
					if(itemStack != null && itemStack.getType() != null && itemStack.getType() == Material.TIPPED_ARROW)
					{
						count += itemStack.getAmount();
					}
				}
				if(cooldownLeft <= 0 && count < 5)
				{
					if(e.getItem().getAmount() > 1)
					{
						e.getItem().setAmount(e.getItem().getAmount() - 1);
					}
					else
					{
						player.getInventory().removeItem(e.getItem());
					}
					player.getInventory().addItem(new IconDisplay(Material.TIPPED_ARROW).setName("Flame-Tipped Arrow").setBasePotionData(PotionType.FIRE_RESISTANCE).create());
					player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, .4f, 1.2f);
					cooldownLeft = maxCooldown;
				}
			}
			else if(e.getAction() == Action.LEFT_CLICK_BLOCK)
			{
				e.getClickedBlock().setType(Material.AIR);
				player.getInventory().addItem(new ItemStack(Material.CAULDRON_ITEM));
				player.playSound(player.getLocation(), Sound.BLOCK_METAL_BREAK, .7f, 1f);
				player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, .2f, 1f);
				cauldronLoc = null;
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
			}
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent e)
	{
		if(e.getItemInHand().getType() == Material.CAULDRON_ITEM)
		{
			Player player = getPlayer();
			Block b = e.getBlock().getLocation().clone().add(0, -1, 0).getBlock();
			if(b.getType() != null && b.getType().isSolid() && b.getType() != Material.CAULDRON)
			{
				addCauldron = false;
				cauldronLoc = e.getBlockPlaced().getLocation();
				player.removePotionEffect(PotionEffectType.SLOW);
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You cannot place your cauldron here!");
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void onPlayerShootBow(EntityShootBowEvent e)
	{
		Player player = getPlayer();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		Location loc = player.getLocation();
		if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc) || (csplayer.isDueling() && csplayer.getDuel().isStarted()))
		{
			player.updateInventory();
			e.setCancelled(true);
			return;
		}
		if(player.getInventory().contains(Material.TIPPED_ARROW))
		{
			Entity proj = e.getProjectile();
			proj.setFireTicks(Integer.MAX_VALUE);
			if(e.getProjectile().getType() == EntityType.TIPPED_ARROW)
			{
				Arrow arrow = player.launchProjectile(Arrow.class);
				arrow.setVelocity(proj.getVelocity());
				arrow.setFireTicks(proj.getFireTicks());
				proj.remove();
			}
			fireArrows.add(e.getProjectile().getUniqueId());
			addArrow = true;
			player.getInventory().removeItem(new IconDisplay(Material.TIPPED_ARROW).setName("Flame-Tipped Arrow").setBasePotionData(PotionType.FIRE_RESISTANCE).create());
		}
	}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		if(e.getDamager() instanceof Player)
		{
			Player player = getPlayer();
			if(player.getInventory().getItemInMainHand().getType() == Material.ARROW || player.getInventory().getItemInMainHand().getType() == Material.TIPPED_ARROW)
			{
				e.setDamage(doCustomDamage(player, 2));

				if(Math.random() <= 0.50)
				{
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.4f, 1f);
					player.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
				}
			}
			if(player.getInventory().getItemInMainHand().getType() == Material.TIPPED_ARROW)
			{
				e.setDamage(doCustomDamage(player, 2));

				Entity en = e.getEntity();
				en.setFireTicks(en.getFireTicks() + (20 * 3));
				if(Math.random() <= 0.75)
				{
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.4f, 1f);
					player.getInventory().removeItem(new IconDisplay(Material.TIPPED_ARROW).setName("Flame-Tipped Arrow").setBasePotionData(PotionType.FIRE_RESISTANCE).create());
				}
			}
		}

		if(e.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) e.getDamager();
			if(arrow.getShooter() instanceof Player)
			{
				Player damager = (Player) arrow.getShooter();
				Entity damaged = e.getEntity();

				double y = arrow.getLocation().getY();
				double shotY = damaged.getLocation().getY();
				boolean headshot = y - shotY > 1.6d;
				e.setDamage(e.getDamage() + 2);
				if(headshot)
				{
					e.setDamage(e.getDamage() + 2);
					damager.sendMessage(ChatColor.DARK_AQUA + "Headshot! (" + damaged.getName() + ")");
				}
				else
				{
					damager.sendMessage(ChatColor.DARK_AQUA + "Hit! (" + damaged.getName() + ")");
				}
			}

			if(fireArrows.contains(e.getDamager().getUniqueId()))
			{
				fireArrows.remove(e.getDamager().getUniqueId());
				arrow.setFireTicks(80);
			}
		}
	}

	@Override
	public void onProjectileHit(ProjectileHitEvent e)
	{
		if(e.getEntity() instanceof Arrow)
		{
			e.getEntity().remove();
		}
	}
}
