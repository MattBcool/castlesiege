package com.castlesiege.classes;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.GravityArrow;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassCrossbowman extends CSClass
{
	public ArrayList<Arrow> activeArrows;

	public int cooldownLeft = 0;
	public int maxCooldown = 60;

	public ClassCrossbowman(Main plugin, Player player)
	{
		super(plugin, player, "Crossbowman", CSClassCategory.RANGED, 2);
		activeArrows = new ArrayList<Arrow>();
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.IRON_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(184, 121, 47).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItem(0, new IconDisplay(Material.BOW).setName("Crossbow").addLore("Shoots arrows in a straight line!").setUnbreakable().hideAttributes().create());
		player.getInventory().setItem(1, new IconDisplay(Material.ARROW).setName("Bolt").setAmount(16).create());

		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.LEATHER_BOOTS).setLeatherColor(184, 121, 47).setUnbreakable().hideAttributes();
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
	}

	@Override
	public void gameKit()
	{
		Player player = getPlayer();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false));
	}

	@Override
	public void onPlayerShootBow(EntityShootBowEvent e)
	{
		Player player = getPlayer();
		if(e.getProjectile() instanceof Arrow)
		{
			Arrow arrow = (Arrow) e.getProjectile();
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
			Location loc = player.getLocation();
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc) || (csplayer.isDueling() && csplayer.getDuel().isStarted()))
			{
				player.updateInventory();
				e.setCancelled(true);
				return;
			}
			if(cooldownLeft <= 0 && arrow.isCritical())
			{
				arrow.remove();
				Arrow a = player.launchProjectile(Arrow.class);
				a.setVelocity(player.getLocation().getDirection().multiply(4.1));
				activeArrows.add(a);

				new GravityArrow(plugin, a, a.getVelocity()).start();
				player.getInventory().removeItem(new IconDisplay(Material.ARROW).setName("Bolt").create());
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, .4f, 1.6f);
				cooldownLeft = maxCooldown;
			}
			else
			{
				player.updateInventory();
				e.setCancelled(true);
			}
		}
	}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getDamager() instanceof Player)
		{
			if(player.getInventory().getItemInMainHand().getType() == Material.ARROW)
			{
				e.setDamage(doCustomDamage(player, 3.5));

				if(Math.random() <= 0.50)
				{
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.4f, 1f);
					player.getInventory().removeItem(new IconDisplay(Material.ARROW).setName("Bolt").create());
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
				e.setDamage(e.getDamage() + 5);
				if(headshot)
				{
					e.setDamage(e.getDamage() + 3);
					damager.sendMessage(ChatColor.AQUA + "Headshot! (" + damaged.getName() + ")");
				}
				else
				{
					damager.sendMessage(ChatColor.DARK_AQUA + "Hit! (" + damaged.getName() + ")");
				}
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
