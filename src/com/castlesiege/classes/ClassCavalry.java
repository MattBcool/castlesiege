package com.castlesiege.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.GenericAttributes;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.castlesiege.Main;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSVoteSite;

public class ClassCavalry extends CSClass
{
	private Horse horse;
	public int cooldownLeft = 0;
	public final int maxCooldown = 200;

	public ClassCavalry(Main plugin, Player player)
	{
		super(plugin, player, "Cavalry", CSClassCategory.MOUNTED, 0);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		cooldownLeft = 0;
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.LEATHER_CHESTPLATE).setLeatherColor(194, 168, 77).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.CHAINMAIL_LEGGINGS).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItem(1, new ItemStack(Material.MONSTER_EGG, 1, (short) 100));

		IconDisplay sword = new IconDisplay(Material.IRON_SWORD).setName("Sword").setUnbreakable().hideAttributes();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			sword.setLore("", ChatColor.BLUE + "+7 Attack Damage");
		}
		else
		{
			sword.setLore("", ChatColor.BLUE + "+6 Attack Damage");
		}
		player.getInventory().setItem(0, sword.create());

		if(csplayer.hasVoted(CSVoteSite.Minestatus) && player.getMaxHealth() < 22)
		{
			player.setMaxHealth(22);
		}

		IconDisplay boots = new IconDisplay(Material.CHAINMAIL_BOOTS).setUnbreakable().hideAttributes();
		if(csplayer.hasVoted(CSVoteSite.MinecraftServers))
		{
			boots.addEnchantment(Enchantment.PROTECTION_FALL, 1);
		}
		player.getInventory().setBoots(boots.create());

		if(csplayer.hasVoted(CSVoteSite.MinecraftMP))
		{
			player.getInventory().setItem(2, new ItemStack(Material.LADDER, 2));
		}
	}

	@Override
	public void gameKit()
	{
		Player player = getPlayer();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, false));
	}

	@Override
	public void clearKit()
	{
		Player player = getPlayer();
		if(player.getVehicle() != null)
		{
			player.leaveVehicle();
		}
		if(horse != null)
		{
			horse.remove();
		}
		super.clearKit();
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		Player player = getPlayer();
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() != null && e.getItem().getType() == Material.MONSTER_EGG)
		{
			Location loc = player.getLocation();
			if(!plugin.datahandler.gamemodehandler.inSpawnRoom(loc) && !plugin.datahandler.gamemodehandler.inGuildRoom(loc))
			{
				player.removePotionEffect(PotionEffectType.SLOW);
				horse = (Horse) e.getPlayer().getWorld().spawn(e.getPlayer().getLocation(), Horse.class);
				horse.setAdult();
				horse.setTamed(true);
				horse.setOwner(e.getPlayer());
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
				switch(csplayer.horse_variant)
				{
				case 0:
					horse.setVariant(Variant.HORSE);
					break;
				case 1:
					horse.setVariant(Variant.DONKEY);
					horse.setCarryingChest(true);
					break;
				case 2:
					horse.setVariant(Variant.MULE);
					horse.setCarryingChest(true);
					break;
				case 3:
					horse.setVariant(Variant.UNDEAD_HORSE);
					break;
				case 4:
					horse.setVariant(Variant.SKELETON_HORSE);
					break;
				}
				switch(csplayer.horse_color)
				{
				case 0:
					horse.setColor(Horse.Color.BROWN);
					break;
				case 1:
					horse.setColor(Horse.Color.DARK_BROWN);
					break;
				case 2:
					horse.setColor(Horse.Color.CHESTNUT);
					break;
				case 3:
					horse.setColor(Horse.Color.CREAMY);
					break;
				case 4:
					horse.setColor(Horse.Color.BLACK);
					break;
				case 5:
					horse.setColor(Horse.Color.GRAY);
					break;
				case 6:
					horse.setColor(Horse.Color.WHITE);
					break;
				}
				switch(csplayer.horse_style)
				{
				case 0:
					horse.setStyle(Style.NONE);
					break;
				case 1:
					horse.setStyle(Style.BLACK_DOTS);
					break;
				case 2:
					horse.setStyle(Style.WHITE);
					break;
				case 3:
					horse.setStyle(Style.WHITE_DOTS);
					break;
				case 4:
					horse.setStyle(Style.WHITEFIELD);
					break;
				}
				horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
				horse.setMaxHealth(30);
				horse.setHealth(30);
				horse.setJumpStrength(0.63);
				((EntityLiving) ((CraftEntity) horse).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.2);
				horse.setPassenger(e.getPlayer());
				e.getPlayer().getInventory().remove(Material.MONSTER_EGG);
			}
			e.setCancelled(true);
		}
	}

	@Override
	public void onVehicleExit(VehicleExitEvent e)
	{
		Player player = getPlayer();
		if(e.getVehicle() instanceof Horse)
		{
			if(!((Horse) e.getVehicle()).isDead())
			{
				e.getVehicle().remove();
				cooldownLeft = maxCooldown;
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false));
		}
	}

	@Override
	public void onDeath(PlayerDeathEvent e)
	{
		cooldownLeft = 0;
		if(e.getEntity().getVehicle() != null)
		{
			e.getEntity().leaveVehicle();
		}
	}
}
