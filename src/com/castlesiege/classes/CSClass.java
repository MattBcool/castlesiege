package com.castlesiege.classes;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.MobEffects;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.player.CSVoteSite;

public abstract class CSClass
{
	public enum CSClassCategory
	{
		LIGHT_MELEE, MELEE, HEAVY_MELEE, RANGED, MOUNTED, SUPPORT
	}

	protected Main plugin;
	protected UUID uuid;
	private String name;
	private CSClassCategory category;
	private int categoryStage;
	private boolean voterExclusive;

	protected CSClass(Main plugin, Player player, String name, CSClassCategory category, int categoryStage)
	{
		this.plugin = plugin;
		this.uuid = player.getUniqueId();
		this.name = name;
		this.category = category;
		this.categoryStage = categoryStage;
		this.voterExclusive = false;
	}

	protected CSClass(Main plugin, Player player, String name, CSClassCategory category, int categoryStage, boolean voterExclusive)
	{
		this.plugin = plugin;
		this.uuid = player.getUniqueId();
		this.name = name;
		this.category = category;
		this.categoryStage = categoryStage;
		this.voterExclusive = voterExclusive;
	}

	public void itemKit(ArrayList<CSVoteSite> votedSites)
	{}

	public void gameKit()
	{}

	public void clearKit()
	{
		Player player = getPlayer();
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		for(PotionEffect pe : player.getActivePotionEffects())
		{
			player.removePotionEffect(pe.getType());
		}
		player.setExp(0);
		player.setMaxHealth(20);
	}

	public void onTakeDamage(EntityDamageByEntityEvent event)
	{}

	public void onDealDamage(EntityDamageByEntityEvent event)
	{}

	public void onTeamDamage(EntityDamageByEntityEvent event)
	{}

	public void onDeath(PlayerDeathEvent event)
	{}

	public void onSprint(PlayerToggleSprintEvent event)
	{}

	public void onSneak(PlayerToggleSneakEvent event)
	{}

	public void onBlockPlace(BlockPlaceEvent event)
	{}

	public void onBlockBreak(BlockBreakEvent event)
	{}

	public void onPlayerInteract(PlayerInteractEvent event)
	{}

	public void onProjectileHit(ProjectileHitEvent event)
	{}

	public void onPlayerShootBow(EntityShootBowEvent event)
	{}

	public void onVehicleExit(VehicleExitEvent event)
	{}

	public String getName()
	{
		return name;
	}

	public CSClassCategory getCategory()
	{
		return category;
	}

	public int getCategoryStage()
	{
		return categoryStage;
	}

	public boolean isVoterExclusive()
	{
		return voterExclusive;
	}

	public boolean isCritical(Player p)
	{
		Player player = getPlayer();
		EntityHuman eh = ((CraftPlayer) player).getHandle();
		return eh.fallDistance > 0.0F && !eh.onGround && !eh.n_() && !eh.isInWater() && !eh.hasEffect(MobEffects.BLINDNESS) && !eh.isPassenger() && !eh.isSprinting();
	}

	public double doCustomDamage(Player p, double damage)
	{
		Player player = getPlayer();
		if(player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
		{
			damage += 3;
		}

		if(player.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.DAMAGE_ALL))
		{
			damage += player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL) * 0.5;
		}

		if(isCritical(p))
		{
			damage *= 1.5;
		}

		return damage;
	}
	
	public Player getPlayer()
	{
		return Bukkit.getPlayer(uuid);
	}
}