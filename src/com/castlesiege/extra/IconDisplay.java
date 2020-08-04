package com.castlesiege.extra;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class IconDisplay
{
	private ItemStack item;

	public IconDisplay(Material material)
	{
		item = new ItemStack(material);
	}

	public IconDisplay(Material material, int amount)
	{
		item = new ItemStack(material, amount);
	}

	public IconDisplay setAmount(int amount)
	{
		item.setAmount(amount);
		return this;
	}

	public IconDisplay setDurability(int durability)
	{
		item.setDurability((short) durability);
		return this;
	}

	public IconDisplay addEnchantment(Enchantment ench, int level)
	{
		item.addEnchantment(ench, level);
		return this;
	}

	public IconDisplay addUnsafeEnchantment(Enchantment ench, int level)
	{
		item.addUnsafeEnchantment(ench, level);
		return this;
	}

	public IconDisplay setBasePotionData(PotionType potionType)
	{
		if(item.getItemMeta() instanceof PotionMeta)
		{
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			meta.setBasePotionData(new PotionData(potionType));
			item.setItemMeta(meta);
		}
		return this;
	}

	public IconDisplay setName(String name)
	{
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + name);
		item.setItemMeta(meta);
		return this;
	}

	public IconDisplay setLore(String... lore)
	{
		ItemMeta meta = item.getItemMeta();
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		return this;
	}
	
	public IconDisplay addLore(String... newLore)
	{
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if(lore == null)
		{
			setLore(newLore);
			return this;
		}
		else lore.addAll(Arrays.asList(newLore));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return this;
	}
	
	public IconDisplay setUnbreakable()
	{
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		return this;
	}
	
	public IconDisplay hideAttributes()
	{
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		item.setItemMeta(meta);
		return this;
	}
	
	public IconDisplay hidePotionEffects()
	{
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		item.setItemMeta(meta);
		return this;
	}
	
	public IconDisplay setBannerColor(DyeColor color)
	{
		if(item.getType() != Material.BANNER) return this;
		
		BannerMeta meta = (BannerMeta) item.getItemMeta();
		meta.setBaseColor(color);
		item.setItemMeta(meta);
		return this;
	}

	public IconDisplay setLeatherColor(int r, int g, int b)
	{
		if(item.getType() != Material.LEATHER_HELMET && item.getType() != Material.LEATHER_CHESTPLATE && item.getType() != Material.LEATHER_LEGGINGS && item.getType() != Material.LEATHER_BOOTS)
		{
			return this;
		}

		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(r, g, b));
		item.setItemMeta(meta);
		return this;
	}

	public ItemStack create()
	{
		return item;
	}
}