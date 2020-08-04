package com.castlesiege.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassSpearman extends CSClass
{
	private List<UUID> arrows = new ArrayList<UUID>();
	public int cooldownLeft = 0;
	public final int maxCooldown = 70;

	public ClassSpearman(Main plugin, Player player)
	{
		super(plugin, player, "Spearman", CSClassCategory.LIGHT_MELEE, 0);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.CHAINMAIL_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.CHAINMAIL_LEGGINGS).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);

		IconDisplay spear = new IconDisplay(Material.STICK, 5).setName("Spear");
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			spear.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			spear.addLore("", ChatColor.BLUE + "+7 Attack Damage");
		}
		else
		{
			spear.addLore("", ChatColor.BLUE + "+6 Attack Damage");
		}
		spear.addLore(ChatColor.BLUE + "+10 Spear Damage");
		player.getInventory().setItem(0, spear.create());

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
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		Player player = getPlayer();
		if(e.getDamager() instanceof Player && player.getInventory().getItemInMainHand().getType() == Material.STICK)
		{
			e.setDamage(doCustomDamage(player, 6));
		}
		if(e.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) e.getDamager();
			Entity damaged = e.getEntity();

			double y = arrow.getLocation().getY();
			double shotY = damaged.getLocation().getY();
			boolean headshot = y - shotY > 1.6d;
			e.setDamage(10);
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

	@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().getType() == Material.STICK && cooldownLeft <= 0)
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
			Arrow arrow = (Arrow) player.launchProjectile(Arrow.class);
			arrow.setShooter((ProjectileSource) player);
			arrow.setVelocity(player.getLocation().getDirection().multiply(1.25f));
			arrows.add(arrow.getUniqueId());

			if(player.getInventory().getItemInMainHand().getAmount() == 1)
			{
				player.getInventory().removeItem(player.getInventory().getItemInMainHand());
			}
			else
			{
				player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
			}
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, .4f, 0.1f);
			cooldownLeft = maxCooldown;
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
