package com.castlesiege.classes;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class ClassMaceman extends CSClass
{
	private final int crippleTime = 80;

	public int cooldownLeft = 0;
	public final int maxCooldown = 160;

	public ClassMaceman(Main plugin, Player player)
	{
		super(plugin, player, "Maceman", CSClassCategory.MELEE, 2);
	}

	@Override
	public void itemKit(ArrayList<CSVoteSite> sites)
	{
		Player player = getPlayer();
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (byte) plugin.datahandler.gamemodehandler.getTeam(player).getWoolColor()));
		player.getInventory().setChestplate(new IconDisplay(Material.CHAINMAIL_CHESTPLATE).setUnbreakable().hideAttributes().create());
		player.getInventory().setLeggings(new IconDisplay(Material.CHAINMAIL_LEGGINGS).setUnbreakable().hideAttributes().create());

		player.getInventory().setHeldItemSlot(0);

		IconDisplay mace = new IconDisplay(Material.IRON_SPADE).setName("Mace").setUnbreakable().hideAttributes();
		mace.addLore("Stuns players for 4 seconds!", "");
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
		if(csplayer.hasVoted(CSVoteSite.PMC))
		{
			mace.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
			mace.addLore(ChatColor.BLUE + "+5 Attack Damage", ChatColor.BLUE + "+7 Attack Damage When Stunning");
		}
		else
		{
			mace.addLore(ChatColor.BLUE + "+4 Attack Damage", ChatColor.BLUE + "+6 Attack Damage When Stunning");
		}
		player.getInventory().setItem(0, mace.create());

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

		IconDisplay ladders = new IconDisplay(Material.LADDER, 5);
		if(csplayer.hasVoted(CSVoteSite.MinecraftMP))
		{
			ladders.setAmount(7);
		}
		player.getInventory().setItem(1, ladders.create());
	}

	@Override
	public void gameKit()
	{}

	@Override
	public void onDealDamage(EntityDamageByEntityEvent e)
	{
		final Player player = getPlayer();
		if(e.getDamager() instanceof Player && player.getInventory().getItemInMainHand().getType() == Material.IRON_SPADE)
		{
			e.setDamage(doCustomDamage(player, 4));

			if(cooldownLeft <= 0)
			{
				if(e.getEntity() instanceof Player)
				{
					final Player victim = (Player) e.getEntity();
					if(victim.isSneaking())
					{
						player.sendMessage(ChatColor.DARK_AQUA + victim.getName() + " dodged your stun!");
						victim.sendMessage(ChatColor.DARK_AQUA + "You dodged a stun!");
					}
					else
					{
						e.setDamage(e.getDamage() + 2);
						victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0, true, false));
						victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, crippleTime, 2, true, false));
						victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, crippleTime, 4, true, false));
						victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, crippleTime, 0, true, false));
						victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_HOSTILE_BIG_FALL, 1f, 0.1f);
						player.sendMessage(ChatColor.DARK_AQUA + "You stunned " + victim.getName() + "!");
						victim.sendMessage(ChatColor.DARK_AQUA + "You were stunned by " + player.getName() + "!");
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								if(plugin.datahandler.utils.getCSPlayer(victim) != null && plugin.datahandler.utils.getCSPlayer(player).getKit() != null)
								{
									plugin.datahandler.utils.getCSPlayer(victim).getKit().gameKit();
								}
							}
						}, crippleTime + 5);
					}
					cooldownLeft = maxCooldown;
				}
			}
		}
	}
}
