package com.castlesiege.classes;

import java.util.ArrayList;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassHalberdier extends CSClass
{
	public ClassHalberdier(Main plugin, Player player)
	{
		super(plugin, player, "Halberdier", CSClassCategory.LIGHT_MELEE, 2);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.LEATHER_CHESTPLATE).setLeatherColor(156, 132, 51).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(71, 61, 24).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);

		IconDisplay axe = new IconDisplay(Material.IRON_AXE).setName("Halberd").setUnbreakable().hideAttributes();
		axe.addLore("33% chance to pull players off horses!");
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			axe.addLore("", ChatColor.BLUE + "+8 Attack Damage", ChatColor.BLUE + "+10 Attack Damage Against Horses");
		}
		else
		{
			axe.addLore("", ChatColor.BLUE + "+7 Attack Damage", ChatColor.BLUE + "+9 Attack Damage Against Horses");
		}
		player.getInventory().setItem(0, axe.create());

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

	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getDamager() instanceof Player && player.getInventory().getItemInMainHand().getType() == Material.IRON_AXE)
		{
			e.setDamage(doCustomDamage(player, 7));

			Entity en = e.getEntity();
			if(en instanceof Player && en.getVehicle() != null)
			{
				if(Math.random() <= 0.33)
				{
					player.sendMessage(ChatColor.DARK_AQUA + "You have dismounted " + en.getName() + " from their horse!");
					en.sendMessage(ChatColor.DARK_AQUA + "You have been thrown from your horse by " + player.getName() + "!");
					e.setDamage(e.getDamage() + 2);
					en.getVehicle().eject();
				}
			}
			if(en instanceof Horse)
			{
				e.setDamage(e.getDamage() + 2);
			}
		}
	}
}
