package com.castlesiege.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.castlesiege.Main;
import com.castlesiege.data.Duel;
import com.castlesiege.extra.AnvilGUI;
import com.castlesiege.listeners.InventoryListener.InventoryPage;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;

public class DuelCommand implements CommandExecutor
{
	private Main plugin;

	private HashMap<String, String> commands;

	public DuelCommand(Main plugin)
	{
		this.plugin = plugin;

		commands = new HashMap<String, String>();

		commands.put("duel accept", "Accepts a duel request");
		commands.put("duel decline", "Declines a duel request");
		commands.put("duel <player>", "Sends a duel request to the specified player");
		commands.put("duel help", "Lists duel commands");

		commands = (HashMap<String, String>) plugin.datahandler.utils.sortByKeys(commands);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			return true;
		}
		final Player p = (Player) sender;

		if(label.equalsIgnoreCase("duel") || label.equalsIgnoreCase("duels"))
		{
			if(args.length > 0)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				Location loc = p.getLocation();
				if(plugin.datahandler.gamemodehandler.inSpawnRoom(loc) || plugin.datahandler.gamemodehandler.inGuildRoom(loc))
				{
					if(args[0].equalsIgnoreCase("accept"))
					{
						if(csplayer.isDueling())
						{
							p.sendMessage(ChatColor.RED + "You are already dueling another player!");
							return false;
						}
						else
						{
							if(csplayer.hasIncomingDuels())
							{
								if(args.length > 1)
								{
									for(Duel duel : csplayer.getIncomingDuels())
									{
										if(duel.getPlayers()[0].getName().equalsIgnoreCase(args[1]))
										{
											Player[] players = duel.getPlayers();
											p.sendMessage(ChatColor.GREEN + "Duel from " + players[0].getName() + " accepted!");
											if(players[0].isOnline())
											{
												players[0].sendMessage(ChatColor.GREEN + "Your duel request was accepted by " + p.getName() + "!");
												duel.accept();
											}
											else
											{
												duel.endArena();
											}
											return false;
										}
									}
									p.sendMessage(ChatColor.RED + "Please specify a valid duel to accept!");
									return false;
								}
								else
								{
									csplayer.getIncomingDuels().get(0).accept();
									return false;
								}
							}
							else
							{
								p.sendMessage(ChatColor.RED + "You do not have any incoming duels!");
								return false;
							}
						}
					}
					if(args[0].equalsIgnoreCase("decline"))
					{
						for(Duel duel : csplayer.getIncomingDuels())
						{
							if(!duel.isAccepted())
							{
								Player ply = duel.getPlayers()[0];
								if(ply != null && ply.isOnline())
								{
									duel.removeDuel();
									ply.sendMessage(ChatColor.RED + "Your duel request was declined by " + p.getName() + "!");
									p.sendMessage(ChatColor.RED + "Successfully declined a duel request from " + ply.getName() + "!");
								}
							}
						}
						return false;
					}
					if(args[0].equalsIgnoreCase("help"))
					{
						int page = 1;
						if(args.length > 1)
						{
							if(plugin.datahandler.utils.isInteger(args[1]))
							{
								int pa = Integer.parseInt(args[1]);
								if(pa > 0)
								{
									page = pa;
								}
							}
						}
						sendHelp(p, page);
						return false;
					}
					for(Player ply : Bukkit.getOnlinePlayers())
					{
						if(ply.getName().equalsIgnoreCase(args[0]) && !ply.getName().equalsIgnoreCase(p.getName()))
						{
							if(csplayer.getDonorRank().getID() >= CSDonorRank.BARON.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
							{
								if(csplayer.isDueling())
								{
									p.sendMessage(ChatColor.RED + "You are already dueling another player!");
									return false;
								}
								else
								{
									new Duel(plugin, p, ply);
									ply.sendMessage(ChatColor.GREEN + "You have an incoming duel request from " + p.getName() + "!");
									ply.sendMessage(ChatColor.GREEN + "Use " + ChatColor.ITALIC + "/duel accept" + ChatColor.RESET + ChatColor.GREEN.toString() + " to accept the duel.");
									p.sendMessage(ChatColor.GREEN + "You sent a duel request to " + ply.getName() + "!");
									return false;
								}
							}
							else
							{
								p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.BARON.getTag());
								return false;
							}
						}
					}
					p.sendMessage(ChatColor.RED + "Please specify a valid player!");
				}
				else
				{
					final InventoryPage lastInvPage = plugin.datahandler.invListener.pages.get(p.getUniqueId());
					AnvilGUI gui = new AnvilGUI(this.plugin, p, new AnvilGUI.AnvilClickEventHandler()
					{
						@Override
						public void onAnvilClick(AnvilGUI.AnvilClickEvent event)
						{
							if(event.getSlot() != null && event.getSlot().equals((Object) AnvilGUI.AnvilSlot.OUTPUT))
							{
								p.chat("/duel " + event.getName());
							}
							event.setWillClose(true);
							event.setWillDestroy(true);
							if(plugin.datahandler.invListener.pages.get(p.getUniqueId()) != null)
							{
								plugin.datahandler.invListener.pages.put(p.getUniqueId(), lastInvPage);
								plugin.datahandler.invListener.updateInventory(p.getUniqueId());
							}
						}
					});
					ItemStack item = new ItemStack(Material.NAME_TAG);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName("Player");
					item.setItemMeta(meta);
					gui.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, item);
					plugin.datahandler.invListener.pages.put(p.getUniqueId(), InventoryPage.NONE);
					plugin.datahandler.invListener.updateInventory(p.getUniqueId());
					gui.open();
				}
				return false;
			}
			else
			{
				p.sendMessage(ChatColor.RED + "You can only use this command while you are in spawn!");
				return false;
			}
		}
		return false;
	}

	private void sendHelp(Player p, int page)
	{
		int size = commands.size();
		if(size > 0)
		{
			int totalpages = 1;
			if(size > 10)
			{
				totalpages = (int) Math.ceil((double) size / 10.0);
			}
			if(totalpages >= page)
			{
				p.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Duel Help: Page " + ChatColor.RESET + ChatColor.DARK_AQUA + page + "/" + totalpages);

				int sr = 1;
				if(page > 1)
				{
					sr = (page * 10) - 9;
				}
				int er = sr + 9;
				if(size < er)
				{
					er = size;
				}
				for(int i = sr; i <= er; i++)
				{
					if(commands.keySet().toArray().length > i)
					{
						String label = (String) commands.keySet().toArray()[i];
						p.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + i + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "/" + label + ChatColor.DARK_GRAY + " - " + commands.get(label));
					}
				}
			}
			else
			{
				p.sendMessage(ChatColor.RED + "That page of duel commands does not exist.");
			}
		}
		else
		{
			p.sendMessage(ChatColor.RED + "No duel commands were found.");
		}
	}
}
