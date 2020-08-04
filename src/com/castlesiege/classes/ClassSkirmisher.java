package com.castlesiege.classes;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassSkirmisher extends CSClass
{
	public ClassSkirmisher(Main plugin, Player player)
	{
		super(plugin, player, "Skirmisher", CSClassCategory.LIGHT_MELEE, 1);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.LEATHER_CHESTPLATE).setLeatherColor(77, 99, 69).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(76, 48, 46).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);

		player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));

		IconDisplay sword = new IconDisplay(Material.STONE_SWORD).setName("Short Sword").setUnbreakable().hideAttributes();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			sword.addLore("", ChatColor.BLUE + "+6 Attack Damage");
		}
		else
		{
			sword.addLore("", ChatColor.BLUE + "+5 Attack Damage");
		}
		player.getInventory().setItem(0, sword.create());

		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.LEATHER_BOOTS).setLeatherColor(56, 36, 35).setUnbreakable().hideAttributes();
		if(csplayer.hasVoted(CSVoteSite.MinecraftServers))
		{
			boots.addEnchantment(Enchantment.PROTECTION_FALL, 1);
		}
		player.getInventory().setBoots(boots.create());

		IconDisplay ladders = new IconDisplay(Material.LADDER, 10);
		if(csplayer.hasVoted(CSVoteSite.MinecraftMP))
		{
			ladders.setAmount(12);
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
	public void onTakeDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(player.isSprinting() && Math.random() <= 0.20)
		{
			Entity en = e.getDamager();
			if(en instanceof Arrow)
			{
				en = (Entity) ((Projectile) en).getShooter();
			}
			en.sendMessage(ChatColor.AQUA + "Your attack was dodged by " + player.getName() + "!");
			player.sendMessage(ChatColor.DARK_AQUA + "You dodged an attack!");
			e.setCancelled(true);
		}
	}
}
