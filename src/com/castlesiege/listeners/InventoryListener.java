package com.castlesiege.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionType;

import com.castlesiege.Main;
import com.castlesiege.classes.CSClasses;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.extra.IconDisplay;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;

public class InventoryListener implements Listener
{
	private Main plugin;

	public enum InventoryPage
	{
		NONE, CLASSES, CLASSES_LIGHT_MELEE, CLASSES_MELEE, CLASSES_HEAVY_MELEE, CLASSES_RANGED, CLASSES_CAVALRY, CLASSES_SUPPORT, OPTIONS, DONOR, HORSE, HORSE_VARIANT, HORSE_COLOR, HORSE_STYLE;
	};

	public HashMap<UUID, InventoryPage> pages;

	public InventoryListener(Main plugin)
	{
		this.plugin = plugin;
		this.pages = new HashMap<UUID, InventoryPage>();
	}

	public void updateInventory(UUID uuid)
	{
		Player p = Bukkit.getPlayer(uuid);
		if(p == null || !p.isOnline())
		{
			return;
		}
		if(!pages.containsKey(p.getUniqueId()))
		{
			pages.put(p.getUniqueId(), InventoryPage.CLASSES);
		}
		for(int i = 9; i <= 35; i++)
		{
			p.getInventory().setItem(i, null);
		}
		switch(pages.get(p.getUniqueId()))
		{
		case CLASSES:
			p.getInventory().setItem(10, new IconDisplay(Material.STICK).setName("Light Melee").setLore(getUnlockedLore(new CSClasses[]
			{ CSClasses.SPEARMAN, CSClasses.SKIRMISHER, CSClasses.HALBERDIER }, p)).create());
			p.getInventory().setItem(13, new IconDisplay(Material.IRON_SWORD).setName("Melee").setLore(getUnlockedLore(new CSClasses[]
			{ CSClasses.SWORDSMAN, CSClasses.VANGUARD, CSClasses.MACEMAN }, p)).hideAttributes().create());
			p.getInventory().setItem(16, new IconDisplay(Material.DIAMOND_CHESTPLATE).setName("Heavy Melee").setLore(getUnlockedLore(new CSClasses[]
			{ CSClasses.KNIGHT, CSClasses.PIKEMAN, CSClasses.AXEMAN }, p)).hideAttributes().create());
			p.getInventory().setItem(19, new IconDisplay(Material.BOW).setName("Ranged").setLore(getUnlockedLore(new CSClasses[]
			{ CSClasses.ARCHER, CSClasses.SIEGE_ARCHER, CSClasses.CROSSBOWMAN }, p)).create());
			p.getInventory().setItem(22, new IconDisplay(Material.GOLD_BARDING).setName("Cavalry").setLore(getUnlockedLore(new CSClasses[]
			{ CSClasses.CAVALRY, CSClasses.RANGED_CAVALRY, CSClasses.LANCER }, p)).create());
			p.getInventory().setItem(25, new IconDisplay(Material.PAPER).setName("Support").setLore(getUnlockedLore(new CSClasses[]
			{ CSClasses.BANNERMAN, CSClasses.MEDIC }, p)).create());
			p.getInventory().setItem(27, new IconDisplay(Material.WOOL).setDurability(14).setName("Page Left").create());
			p.getInventory().setItem(35, new IconDisplay(Material.WOOL).setDurability(5).setName("Page Right").create());
			break;
		case CLASSES_LIGHT_MELEE:
			setupClassItems(plugin, p, CSClasses.SPEARMAN, Material.STICK, 10);
			setupClassItems(plugin, p, CSClasses.SKIRMISHER, Material.STONE_SWORD, 13);
			setupClassItems(plugin, p, CSClasses.HALBERDIER, Material.IRON_AXE, 16);
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case CLASSES_MELEE:
			setupClassItems(plugin, p, CSClasses.SWORDSMAN, Material.IRON_SWORD, 10);
			setupClassItems(plugin, p, CSClasses.VANGUARD, Material.DIAMOND_SWORD, 13);
			setupClassItems(plugin, p, CSClasses.MACEMAN, Material.IRON_SPADE, 16);
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case CLASSES_HEAVY_MELEE:
			setupClassItems(plugin, p, CSClasses.KNIGHT, Material.SHIELD, 10);
			setupClassItems(plugin, p, CSClasses.PIKEMAN, Material.STICK, 13);
			setupClassItems(plugin, p, CSClasses.AXEMAN, Material.IRON_AXE, 16);
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case CLASSES_RANGED:
			setupClassItems(plugin, p, CSClasses.ARCHER, Material.BOW, 10);
			setupClassItems(plugin, p, CSClasses.SIEGE_ARCHER, Material.TIPPED_ARROW, 13);
			setupClassItems(plugin, p, CSClasses.CROSSBOWMAN, Material.ARROW, 16);
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case CLASSES_CAVALRY:
			setupClassItems(plugin, p, CSClasses.CAVALRY, Material.SHIELD, 10);
			setupClassItems(plugin, p, CSClasses.RANGED_CAVALRY, Material.STICK, 13);
			setupClassItems(plugin, p, CSClasses.LANCER, Material.IRON_AXE, 16);
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case CLASSES_SUPPORT:
			setupClassItems(plugin, p, CSClasses.BANNERMAN, Material.BANNER, 11);// 10
			// setupClassItems(plugin, p, CSClasses.PIKEMAN, Material.STICK,
			// 13);
			setupClassItems(plugin, p, CSClasses.MEDIC, Material.PAPER, 15);// 16
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case OPTIONS:
			CSRank rank = plugin.datahandler.utils.getCSPlayer(p).getRank();
			if(rank.getID() >= CSRank.CHATMOD.getID())
			{
				p.getInventory().setItem(12, new IconDisplay(Material.NAME_TAG).setName("Chatspy").create());
				p.getInventory().setItem(14, new IconDisplay(Material.NAME_TAG).setName("Team Chatspy").create());
			}
			p.getInventory().setItem(21, new IconDisplay(Material.WATCH).setName("Switch Scoreboard").create());
			p.getInventory().setItem(22, new IconDisplay(Material.EMERALD).setName("Guild Spawn").create());
			p.getInventory().setItem(23, new IconDisplay(Material.NAME_TAG).setName("Simple Levels").create());
			p.getInventory().setItem(27, new IconDisplay(Material.WOOL).setDurability(5).setName("Page Left").create());
			p.getInventory().setItem(35, new IconDisplay(Material.WOOL).setDurability(5).setName("Page Right").create());
			break;
		case DONOR:
			p.getInventory().setItem(21, new IconDisplay(Material.IRON_SWORD).setName("Duel").hideAttributes().create());
			p.getInventory().setItem(23, new IconDisplay(Material.SADDLE).setName("Horse Customization").create());
			p.getInventory().setItem(27, new IconDisplay(Material.WOOL).setDurability(5).setName("Page Left").create());
			p.getInventory().setItem(35, new IconDisplay(Material.WOOL).setDurability(14).setName("Page Right").create());
			break;
		case HORSE:
			p.getInventory().setItem(10, new IconDisplay(Material.GOLDEN_CARROT).setName("Horse Variant").create());
			p.getInventory().setItem(13, new IconDisplay(Material.INK_SACK).setDurability(8).setName("Horse Color").create());
			p.getInventory().setItem(16, new IconDisplay(Material.RAW_FISH).setDurability(2).setName("Horse Style").create());
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case HORSE_VARIANT:
			p.getInventory().setItem(11, new IconDisplay(Material.WOOL).setDurability(12).setName("Horse").create());
			p.getInventory().setItem(12, new IconDisplay(Material.LEASH).setName("Donkey").create());
			p.getInventory().setItem(13, new IconDisplay(Material.LEASH).setName("Mule").create());
			p.getInventory().setItem(14, new IconDisplay(Material.ROTTEN_FLESH).setName("Zombie").create());
			p.getInventory().setItem(15, new IconDisplay(Material.BONE).setName("Skeleton").create());
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case HORSE_COLOR:
			p.getInventory().setItem(10, new IconDisplay(Material.WOOL).setDurability(12).setName("Brown").create());
			p.getInventory().setItem(11, new IconDisplay(Material.WOOL).setDurability(12).setName("Dark Brown").create());
			p.getInventory().setItem(12, new IconDisplay(Material.WOOL).setDurability(12).setName("Chestnut").create());
			p.getInventory().setItem(13, new IconDisplay(Material.WOOL).setDurability(12).setName("Creamy").create());
			p.getInventory().setItem(14, new IconDisplay(Material.WOOL).setDurability(15).setName("Black").create());
			p.getInventory().setItem(15, new IconDisplay(Material.WOOL).setDurability(8).setName("Gray").create());
			p.getInventory().setItem(16, new IconDisplay(Material.WOOL).setName("White").create());
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		case HORSE_STYLE:
			p.getInventory().setItem(11, new IconDisplay(Material.WOOL).setDurability(8).setName("None").create());
			p.getInventory().setItem(12, new IconDisplay(Material.WOOL).setDurability(15).setName("Black Dots").create());
			p.getInventory().setItem(13, new IconDisplay(Material.WOOL).setName("White").create());
			p.getInventory().setItem(14, new IconDisplay(Material.WOOL).setName("White Dots").create());
			p.getInventory().setItem(15, new IconDisplay(Material.WOOL).setName("Whitefield").create());
			p.getInventory().setItem(31, new IconDisplay(Material.BARRIER).setName("Go Back").create());
			break;
		default:
			break;
		}
		p.updateInventory();
	}

	private String[] getUnlockedLore(CSClasses[] classes, Player p)
	{
		String[] lore = null;
		ArrayList<String> unlocked = new ArrayList<String>();
		ArrayList<String> locked = new ArrayList<String>();
		for(CSClasses c : classes)
		{
			if(c.unlocked(plugin, p))
			{
				unlocked.add(c.getName());
			}
			else
			{
				locked.add(c.getName());
			}
		}
		if(unlocked.size() < 1)
		{
			if(locked.size() == 2)
			{
				lore = new String[]
				{ ChatColor.RED + "Locked: ", ChatColor.DARK_GRAY + "* " + locked.get(0), ChatColor.DARK_GRAY + "* " + locked.get(1) };
			}
			else
			{
				lore = new String[]
				{ ChatColor.RED + "Locked: ", ChatColor.DARK_GRAY + "* " + locked.get(0), ChatColor.DARK_GRAY + "* " + locked.get(1), ChatColor.DARK_GRAY + "* " + locked.get(2) };
			}
		}
		else if(locked.size() < 1)
		{
			if(unlocked.size() == 2)
			{
				lore = new String[]
				{ ChatColor.GREEN + "Unlocked: ", ChatColor.DARK_GRAY + "* " + unlocked.get(0), ChatColor.DARK_GRAY + "* " + unlocked.get(1) };
			}
			else
			{
				lore = new String[]
				{ ChatColor.GREEN + "Unlocked: ", ChatColor.DARK_GRAY + "* " + unlocked.get(0), ChatColor.DARK_GRAY + "* " + unlocked.get(1), ChatColor.DARK_GRAY + "* " + unlocked.get(2) };
			}
		}
		else
		{
			int last = 1;
			if(unlocked.size() < 1)
			{
				lore = new String[classes.length + 1];
			}
			else
			{
				lore = new String[classes.length + 2];
			}
			lore[0] = ChatColor.GREEN + "Unlocked";
			for(String str : unlocked)
			{
				if(lore.length > last)
				{
					lore[last] = ChatColor.DARK_GRAY + "* " + str;
					last += 1;
				}
			}
			lore[unlocked.size() + 1] = ChatColor.RED + "Locked";
			last += 1;
			for(String str : locked)
			{
				if(lore.length > last)
				{
					lore[last] = ChatColor.DARK_GRAY + "* " + str;
					last += 1;
				}
			}
		}
		return lore;
	}

	private void setupClassItems(Main plugin, Player p, CSClasses classes, Material mat, int slot)
	{
		p.getInventory().setItem(slot, new IconDisplay(mat).setName(classes.getName()).hideAttributes().setBasePotionData(PotionType.FIRE_RESISTANCE).hidePotionEffects().setLore(classes.getLore(plugin, p)).create());
		if(classes.unlocked(plugin, p))
		{
			p.getInventory().setItem(slot + 9, new IconDisplay(Material.WOOL).setName(ChatColor.GREEN + "Unlocked").setLore(classes.getUnlockedLore(plugin, p)).setDurability(5).create());
		}
		else
		{
			p.getInventory().setItem(slot + 9, new IconDisplay(Material.WOOL).setName(ChatColor.RED + "Locked").setLore(classes.getUnlockedLore(plugin, p)).setDurability(14).create());
		}
	}

	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e)
	{
		Player p = (Player) e.getPlayer();
		if(e.getInventory().getHolder() instanceof Player)
		{
			if(plugin.datahandler.gamemodehandler.inSpawnRoom(p.getLocation()) || plugin.datahandler.gamemodehandler.inGuildRoom(p.getLocation()))
			{
				pages.put(p.getUniqueId(), InventoryPage.CLASSES);
			}
			else
			{
				pages.put(p.getUniqueId(), InventoryPage.NONE);
			}
			updateInventory(p.getUniqueId());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e)
	{
		if(e.getWhoClicked() instanceof Player)
		{
			final Player p = (Player) e.getWhoClicked();
			if(p.getGameMode() != GameMode.CREATIVE && (e.getInventory().getName().contains("Horse") || e.getInventory().getName().contains("Chest")))
			{
				e.setCancelled(true);
				return;
			}
			if(e.getInventory().getHolder() instanceof Player)
			{
				if(p.getGameMode() != GameMode.CREATIVE && e.getSlotType() == SlotType.ARMOR || (e.getSlotType() == SlotType.QUICKBAR && e.getSlot() == 9))
				{
					if(e.getCursor() != null)
					{
						p.getInventory().addItem(e.getCursor());
						e.setCursor(null);
					}
					e.setCancelled(true);
				}
				if(e.getSlot() >= 9 && e.getSlot() <= 35 && pages.get(p.getUniqueId()) != InventoryPage.NONE)
				{
					e.setCancelled(true);
					if(pages.get(p.getUniqueId()) == null)
					{
						pages.put(p.getUniqueId(), InventoryPage.CLASSES);
						updateInventory(p.getUniqueId());
					}
					CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
					switch(pages.get(p.getUniqueId()))
					{
					default:
						break;
					case CLASSES:
						switch(e.getSlot())
						{
						case 10:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES_LIGHT_MELEE);
							updateInventory(p.getUniqueId());
							break;
						case 13:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES_MELEE);
							updateInventory(p.getUniqueId());
							break;
						case 16:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES_HEAVY_MELEE);
							updateInventory(p.getUniqueId());
							break;
						case 19:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES_RANGED);
							updateInventory(p.getUniqueId());
							break;
						case 22:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES_CAVALRY);
							updateInventory(p.getUniqueId());
							break;
						case 25:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES_SUPPORT);
							updateInventory(p.getUniqueId());
							break;
						case 35:
							pages.put(p.getUniqueId(), InventoryPage.OPTIONS);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case CLASSES_LIGHT_MELEE:
						switch(e.getSlot())
						{
						case 10:
							csplayer.setClass(CSClasses.SPEARMAN);
							break;
						case 13:
							csplayer.setClass(CSClasses.SKIRMISHER);
							break;
						case 16:
							csplayer.setClass(CSClasses.HALBERDIER);
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case CLASSES_MELEE:
						switch(e.getSlot())
						{
						case 10:
							csplayer.setClass(CSClasses.SWORDSMAN);
							break;
						case 13:
							csplayer.setClass(CSClasses.VANGUARD);
							break;
						case 16:
							csplayer.setClass(CSClasses.MACEMAN);
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case CLASSES_HEAVY_MELEE:
						switch(e.getSlot())
						{
						case 10:
							csplayer.setClass(CSClasses.KNIGHT);
							break;
						case 13:
							csplayer.setClass(CSClasses.PIKEMAN);
							break;
						case 16:
							csplayer.setClass(CSClasses.AXEMAN);
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case CLASSES_RANGED:
						switch(e.getSlot())
						{
						case 10:
							csplayer.setClass(CSClasses.ARCHER);
							break;
						case 13:
							csplayer.setClass(CSClasses.SIEGE_ARCHER);
							break;
						case 16:
							csplayer.setClass(CSClasses.CROSSBOWMAN);
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case CLASSES_CAVALRY:
						switch(e.getSlot())
						{
						case 10:
							csplayer.setClass(CSClasses.CAVALRY);
							break;
						case 13:
							csplayer.setClass(CSClasses.RANGED_CAVALRY);
							break;
						case 16:
							csplayer.setClass(CSClasses.LANCER);
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case CLASSES_SUPPORT:
						switch(e.getSlot())
						{
						case 11:
							csplayer.setClass(CSClasses.BANNERMAN);
							break;
						case 15:
							csplayer.setClass(CSClasses.MEDIC);
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case OPTIONS:
						switch(e.getSlot())
						{
						case 12:
							if(csplayer.getRank().getID() >= CSRank.CHATMOD.getID())
							{
								if(csplayer.chatspy)
								{
									p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Turning off Chatspy");
								}
								else
								{
									p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Turning on Chatspy");
								}
								csplayer.chatspy = !csplayer.chatspy;
							}
							else
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSRank.CHATMOD.getTag());
							}
							break;
						case 14:
							if(csplayer.getRank().getID() >= CSRank.CHATMOD.getID())
							{
								if(csplayer.teamchatspy)
								{
									p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Turning off Team Chatspy");
								}
								else
								{
									p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Turning on Team Chatspy");
								}
								csplayer.teamchatspy = !csplayer.teamchatspy;
							}
							else
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSRank.CHATMOD.getTag());
							}
							break;
						case 21:
							csplayer.scoreboard = (csplayer.scoreboard == 0 ? 1 : 0);
							switch(csplayer.scoreboard)
							{
							case 0:
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing scoreboard to Stat Display");
								break;
							case 1:
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing scoreboard to Flag Display");
								break;
							}
							plugin.datahandler.gamemodehandler.resetScoreboard(p);
							break;
						case 22:
							Location loc = p.getLocation();
							if(csplayer.guildspawn)
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Turning on Guild Spawn");
								if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase("") && plugin.datahandler.gamemodehandler.inSpawnRoom(loc))
								{
									final Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
									p.teleport(guild.getSpawn());
									Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
									{
										@Override
										public void run()
										{
											guild.updateGuildSigns(p);
										}
									}, 20L);
								}
							}
							else
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Turning off Guild Spawn");
								if(csplayer.guild != null && !csplayer.guild.equalsIgnoreCase("") && plugin.datahandler.gamemodehandler.inGuildRoom(loc))
								{
									p.teleport(plugin.datahandler.gamemodehandler.getTeam(p).getSpawn());
								}
							}
							csplayer.guildspawn = !csplayer.guildspawn;
							break;
						case 23:
							if(csplayer.asterisk)
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Dynamic Levels");
							}
							else
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Simple Levels");
							}
							csplayer.asterisk = !csplayer.asterisk;
							break;
						case 27:
							pages.put(p.getUniqueId(), InventoryPage.CLASSES);
							updateInventory(p.getUniqueId());
							break;
						case 35:
							pages.put(p.getUniqueId(), InventoryPage.DONOR);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case DONOR:
						switch(e.getSlot())
						{
						case 21:
							p.chat("/duel");

							break;
						case 23:
							pages.put(p.getUniqueId(), InventoryPage.HORSE);
							updateInventory(p.getUniqueId());
							break;
						case 27:
							pages.put(p.getUniqueId(), InventoryPage.OPTIONS);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case HORSE:
						if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
						{
							switch(e.getSlot())
							{
							case 10:
								pages.put(p.getUniqueId(), InventoryPage.HORSE_VARIANT);
								updateInventory(p.getUniqueId());
								break;
							case 13:
								pages.put(p.getUniqueId(), InventoryPage.HORSE_COLOR);
								updateInventory(p.getUniqueId());
								break;
							case 16:
								pages.put(p.getUniqueId(), InventoryPage.HORSE_STYLE);
								updateInventory(p.getUniqueId());
								break;
							case 31:
								pages.put(p.getUniqueId(), InventoryPage.DONOR);
								updateInventory(p.getUniqueId());
								break;
							}
						}
						else
						{
							p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
						}
						break;
					case HORSE_VARIANT:
						switch(e.getSlot())
						{
						case 11:
							csplayer.horse_variant = 0;
							p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Horse horse variant");
							break;
						case 12:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 2;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Donkey horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
							}
							break;
						case 13:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 2;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Mule horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
							}
							break;
						case 14:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.DUKE.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 3;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Zombie horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.DUKE.getTag());
							}
							break;
						case 15:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.PRINCE.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 4;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Skeleton horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.PRINCE.getTag());
							}
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.HORSE);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case HORSE_COLOR:
						switch(e.getSlot())
						{
						case 10:
							csplayer.horse_color = 0;
							p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Brown horse color");
						case 11:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 1;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Dark Brown horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
							}
							break;
						case 12:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 2;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Chestnut horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
							}
							break;
						case 13:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 3;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Creamy horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
							}
							break;
						case 14:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 4;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Black horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
							}
							break;
						case 15:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 5;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Gray horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
							}
							break;
						case 16:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_variant = 6;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to White horse variant");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
							}
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.HORSE);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					case HORSE_STYLE:
						switch(e.getSlot())
						{
						case 11:
							csplayer.horse_style = 0;
							p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to None horse style");
							break;
						case 12:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_style = 1;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Black Dots horse style");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
							}
							break;
						case 13:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_style = 2;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to White horse style");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
							}
							break;
						case 14:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_style = 3;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to White Dots horse style");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
							}
							break;
						case 15:
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								csplayer.horse_style = 4;
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "Changing to Whitefield horse style");
							}
							else
							{
								p.sendMessage(ChatColor.DARK_BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
							}
							break;
						case 31:
							pages.put(p.getUniqueId(), InventoryPage.HORSE);
							updateInventory(p.getUniqueId());
							break;
						}
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		pages.put(e.getPlayer().getUniqueId(), InventoryPage.CLASSES);
		updateInventory(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		pages.remove(e.getPlayer().getUniqueId());
	}
}
