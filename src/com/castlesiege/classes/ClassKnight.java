package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

import net.md_5.bungee.api.ChatColor;

public class ClassKnight extends CSClass
{
	public int cooldownTime = 0;

	public ClassKnight(Main plugin, Player player)
	{
		super(plugin, player, "Knight", CSClassCategory.HEAVY_MELEE, 0);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.DIAMOND_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.IRON_LEGGINGS).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));

		IconDisplay sword = new IconDisplay(Material.STONE_SWORD).setName("Longsword").setUnbreakable().hideAttributes();
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
		player.getInventory().setItem(1, ladders.create());
	}

	@Override
	public void onSprint(PlayerToggleSprintEvent e)
	{
		Player p = e.getPlayer();
		if(e.isSprinting())
		{
			p.setFoodLevel(6);
			cooldownTime = 20;
		}
	}

	@Override
	public void onTakeDamage(EntityDamageByEntityEvent e)
	{
		Entity en = e.getDamager();
		if(en instanceof Player)
		{
			((Player) en).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1, false, false));
		}
	}
}
