package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

import net.md_5.bungee.api.ChatColor;

public class ClassVanguard extends CSClass
{
	public ClassVanguard(Main plugin, Player player)
	{
		super(plugin, player, "Vanguard", CSClassCategory.MELEE, 1);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.IRON_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(76, 48, 46).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);

		IconDisplay sword = new IconDisplay(Material.DIAMOND_SWORD).setUnbreakable().hideAttributes();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			sword.addLore("", ChatColor.BLUE + "+8 Attack Damage");
		}
		else
		{
			sword.addLore("", ChatColor.BLUE + "+7 Attack Damage");
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

		IconDisplay ladders = new IconDisplay(Material.LADDER, 5);
		if(csplayer.hasVoted(CSVoteSite.MinecraftMP))
		{
			ladders.setAmount(7);
		}
		player.getInventory().setItem(1, ladders.create());
	}
}
