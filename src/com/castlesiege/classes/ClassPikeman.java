package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

import net.md_5.bungee.api.ChatColor;

public class ClassPikeman extends CSClass
{
	public int cooldownLeft = 0;
	public int maxCooldown = 60;
	public int cancelKnockback = 0;
	public boolean inStance = false;

	public ClassPikeman(Main plugin, Player player)
	{
		super(plugin, player, "Pikeman", CSClassCategory.HEAVY_MELEE, 2);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.DIAMOND_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.CHAINMAIL_LEGGINGS).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));

		IconDisplay pike = new IconDisplay(Material.STICK).setName("Pike");
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			pike.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			pike.addLore("", ChatColor.BLUE + "+7 Attack Damage");
		}
		else
		{
			pike.addLore("", ChatColor.BLUE + "+6 Attack Damage");
		}
		player.getInventory().setItem(0, pike.create());

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
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, true, false));
	}

	@Override
	public void onTakeDamage(final EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(inStance)
		{
			cancelKnockback = 1;
		}
		else
		{
			player.setExp(0f);
			cooldownLeft = 0;
		}
	}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getDamager() instanceof Player && player.getInventory().getItemInMainHand().getType() == Material.STICK)
		{
			e.setDamage(doCustomDamage(player, 6));
		}
	}

	@Override
	public void onSneak(PlayerToggleSneakEvent e)
	{
		Player player = getPlayer();
		if(e.isSneaking() && !plugin.datahandler.gamemodehandler.inSpawnRoom(player.getLocation()))
		{
			cooldownLeft = maxCooldown;
		}
		else
		{
			player.setExp(0f);
			player.setWalkSpeed(0.2f);
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			cooldownLeft = 0;
			inStance = false;
		}
	}
}
