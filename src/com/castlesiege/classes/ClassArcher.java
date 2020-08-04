package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.ChatColor;
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
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassArcher extends CSClass
{
	private IconDisplay arrow;

	public ClassArcher(Main plugin, Player player)
	{
		super(plugin, player, "Archer", CSClassCategory.RANGED, 0);
		arrow = new IconDisplay(Material.ARROW).addLore("", ChatColor.BLUE + "+2 Attack Damage", ChatColor.BLUE + "+50% Chance To Break");
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.LEATHER_CHESTPLATE).setLeatherColor(89, 76, 63).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(89, 76, 63).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItem(0, new IconDisplay(Material.BOW).setUnbreakable().hideAttributes().create());
		player.getInventory().setItem(2, new ItemStack(Material.LADDER, 5));
		player.getInventory().setItem(3, arrow.setAmount(32).create());

		IconDisplay sword = new IconDisplay(Material.WOOD_SWORD).setName("Dagger").setUnbreakable().hideAttributes();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			sword.setLore("", ChatColor.BLUE + "+5 Attack Damage");
		}
		else
		{

			sword.setLore("", ChatColor.BLUE + "+4 Attack Damage");
		}
		player.getInventory().setItem(1, sword.create());

		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.IRON_BOOTS).setUnbreakable().hideAttributes();
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
	public void onProjectileHit(ProjectileHitEvent e)
	{
		if(e.getEntity() instanceof Arrow)
		{
			e.getEntity().remove();
		}
	}

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
	}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getDamager() instanceof Player)
		{
			if(player.getInventory().getItemInMainHand().getType() == Material.ARROW)
			{
				e.setDamage(2);
				if(player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
				{
					e.setDamage(e.getDamage() + 3);
				}
				if(Math.random() <= 0.50)
				{
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.4f, 1f);
					player.getInventory().removeItem(arrow.setAmount(1).create());
				}
			}
		}

		if(e.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) e.getDamager();
			if(arrow.getShooter() instanceof Player)
			{
				Entity damaged = e.getEntity();

				double y = arrow.getLocation().getY();
				double shotY = damaged.getLocation().getY();
				boolean headshot = y - shotY > 1.6d;
				if(arrow.isCritical())
				{
					e.setDamage(e.getDamage() + 2);
				}
				if(headshot)
				{
					e.setDamage(e.getDamage() + 2);
					player.sendMessage(ChatColor.AQUA + "Headshot! (" + damaged.getName() + ")");
				}
				else
				{
					player.sendMessage(ChatColor.DARK_AQUA + "Hit! (" + damaged.getName() + ")");
				}
			}
		}
	}
}
