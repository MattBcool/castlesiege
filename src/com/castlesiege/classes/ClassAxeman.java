package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

import net.md_5.bungee.api.ChatColor;

public class ClassAxeman extends CSClass
{
	public int cooldownLeft = 0;
	public final int maxCooldown = 80;

	public ClassAxeman(Main plugin, Player player)
	{
		super(plugin, player, "Axeman", CSClassCategory.HEAVY_MELEE, 2);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.DIAMOND_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.LEATHER_LEGGINGS).setLeatherColor(130, 114, 114).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);

		IconDisplay axe = new IconDisplay(Material.STONE_AXE).setName("Battle Axe").setUnbreakable().hideAttributes();
		axe.setLore("Executes enemies with 5 or less health!");
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			axe.addLore("", ChatColor.BLUE + "+6 Attack Damage");
		}
		else
			axe.addLore("", ChatColor.BLUE + "+5 Attack Damage");
		player.getInventory().setItem(0, axe.create());

		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.LEATHER_BOOTS).setLeatherColor(84, 65, 65).setUnbreakable().hideAttributes();
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
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getEntity() instanceof Player)
		{
			Player victim = (Player) e.getEntity();
			if(player.getInventory().getItemInMainHand().getType() == Material.STONE_AXE)
			{
				e.setDamage(doCustomDamage(player, 5));

				if(victim.getHealth() < 6)
				{
					e.setDamage(100d);
					victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRONGOLEM_DEATH, 0.5f, 0.6f);
					cooldownLeft = maxCooldown;
				}
			}
		}
	}
}
