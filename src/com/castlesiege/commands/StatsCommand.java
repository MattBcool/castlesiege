package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagString;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.castlesiege.Main;
import com.castlesiege.classes.CSClasses;
import com.castlesiege.extra.TabText;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.player.CSClassStat;
import com.castlesiege.player.CSClassStats;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;

public class StatsCommand implements CommandExecutor
{
	private Main plugin;

	public StatsCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			return true;
		}
		Player p = (Player) sender;

		if(cmd.getName().equalsIgnoreCase("stats") || cmd.getName().equalsIgnoreCase("mystats"))
		{
			if(args.length > 0)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				if(csplayer.getDonorRank().getID() >= CSDonorRank.LORD.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
				{
					String[] pages = getPages(args[0], p.getName());
					if(pages == null)
					{
						p.sendMessage(ChatColor.RED + "Please specify a valid player to get the stats of!");
					}
					else
					{
						openBook(p, book("Stats", p.getName(), pages));
					}
				}
				else
				{
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.LORD.getTag());
				}
			}
			else
			{
				openBook(p, book("Stats", p.getName(), getPages(p.getName(), p.getName())));
			}
		}
		return false;
	}

	private String[] getPages(String name, String sendName)
	{
		UUID uuid = null;
		try
		{
			uuid = UUIDFetcher.getUUIDOf(plugin, name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Player p = Bukkit.getPlayer(uuid);
		String[] pages = new String[1];
		CSClassStats classStats = null;
		int timePlayed = 0;
		if(p != null && p.isOnline())
		{
			CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
			if(csplayer != null)
			{
				classStats = csplayer.classStats;
				timePlayed = csplayer.timePlayed;
			}
		}
		else
		{
			SQLConnection sql = plugin.datahandler.sqlConnection;
			Connection c = sql.connection;
			try
			{
				PreparedStatement statement = c.prepareStatement("SELECT class_stats, total_time_played FROM player_data WHERE uuid=?;");
				statement.setString(1, uuid.toString());
				ResultSet result = statement.executeQuery();

				if(result.next())
				{
					classStats = new CSClassStats(result.getString("class_stats"));
					timePlayed = result.getInt("total_time_played");
					statement.close();
					result.close();
				}
				else
				{
					statement.close();
					result.close();
					return null;
				}
			}
			catch(SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(classStats == null)
		{
			pages[0] = "Could not load player data.";
		}
		else
		{
			pages = new String[CSClasses.values().length + 2];
			double kdr = classStats.getTotalKDR();
			DecimalFormat df = new DecimalFormat("#.##");
			kdr = Double.valueOf(df.format(kdr));
			TabText tt = new TabText(ChatColor.DARK_AQUA + "`Score:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalScore() + "\n" + ChatColor.DARK_AQUA + "`Kills:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalKills() + "\n" + ChatColor.DARK_AQUA + "`Deaths:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalDeaths() + "\n" + ChatColor.DARK_AQUA + "`KDR:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + kdr + "\n" + ChatColor.DARK_AQUA + "`Assists:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalAssists() + "\n" + ChatColor.DARK_AQUA + "`Streak:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getHighestStreak() + "\n" + ChatColor.DARK_AQUA + "`Captures:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalCaps() + "\n" + ChatColor.DARK_AQUA + "`MVPs:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalMvps() + "\n" + ChatColor.DARK_AQUA + "`Supports:"
					+ ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStats.getTotalSupps());
			tt.setPageHeight(12);
			tt.setTabs(0, 10);
			String exp = "";
			for(int i = 0; i < 20; i++)
			{
				if((double) classStats.getTotalScore() / (double) plugin.datahandler.utils.getMaxExp(classStats.getTotalScore(), 3) >= (double) (1d / 20d) * i)
				{
					exp += ChatColor.DARK_GRAY + "|";
				}
				else
				{
					exp += ChatColor.GRAY + "|";
				}
			}
			String str = "Your";
			if(!sendName.equalsIgnoreCase(name))
			{
				String c = name.substring(name.length() - 1);
				str = name;
				if(c.equalsIgnoreCase("z"))
				{
					str += "'";
				}
				else
				{
					str += "'s";
				}
			}
			pages[0] = ChatColor.BLUE + ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString() + str + " Stats:" + "\n\n" + tt.getPage(1, false);
			int day = timePlayed / 60 / 60 / 24;
			int hour = timePlayed / 60 / 60 % 24;
			pages[1] = ChatColor.BLUE + ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString() + "Player Info:" + "\n\n" + ChatColor.DARK_AQUA + "Level: " + ChatColor.DARK_GRAY + plugin.datahandler.utils.getLevel(classStats.getTotalScore(), 3) + "\n" + ChatColor.DARK_AQUA + "Exp: " + ChatColor.DARK_GRAY + classStats.getTotalScore() + " / " + plugin.datahandler.utils.getMaxExp(classStats.getTotalScore(), 3) + "\n" + ChatColor.DARK_AQUA + "Progress: " + ChatColor.DARK_GRAY + exp + "\n" + ChatColor.DARK_AQUA + "Time played: " + ChatColor.DARK_GRAY + plugin.datahandler.utils.fixInt(day) + "d " + plugin.datahandler.utils.fixInt(hour) + "h";
			/*
			 * pages[0] = ChatColor.BLUE + ChatColor.UNDERLINE.toString() +
			 * ChatColor.BOLD.toString() + "Your Stats:" + ChatColor.RESET +
			 * "\n\n" + ChatColor.DARK_AQUA + "Score:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalScore() + "\n" + ChatColor.DARK_AQUA
			 * + "Kills:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalKills() + "\n" + ChatColor.DARK_AQUA
			 * + "Deaths:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalKills() + "\n" + ChatColor.DARK_AQUA
			 * + "KDR:" + ChatColor.DARK_GRAY + kdr + "\n" + ChatColor.DARK_AQUA
			 * + "Assists:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalAssists() + "\n" +
			 * ChatColor.DARK_AQUA + "Streak:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getHighestStreak() + "\n" +
			 * ChatColor.DARK_AQUA + "Captures:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalCaps() + "\n" + ChatColor.DARK_AQUA +
			 * "Heals:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalHeals() + "\n" + ChatColor.DARK_AQUA
			 * + "MVPs:" + ChatColor.DARK_GRAY +
			 * csplayer.classStats.getTotalMvps();
			 */
			int page = 2;
			for(CSClassStat classStat : classStats.classStats)
			{
				TabText t = null;
				kdr = classStat.getKDR();
				kdr = Double.valueOf(df.format(kdr));
				if(classStat.getClassName().contains("medic") || classStat.getClassName().contains("bannerman"))
				{
					t = new TabText(ChatColor.DARK_AQUA + "`Score:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getFinalScore() + "\n" + ChatColor.DARK_AQUA + "`Kills:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getKills() + "\n" + ChatColor.DARK_AQUA + "`Deaths:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getDeaths() + "\n" + ChatColor.DARK_AQUA + "`KDR:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + kdr + "\n" + ChatColor.DARK_AQUA + "`Assists:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getAssists() + "\n" + ChatColor.DARK_AQUA + "`Streak:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getHighestStreak() + "\n" + ChatColor.DARK_AQUA + "`Captures:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getCaps() + "\n" + ChatColor.DARK_AQUA + "`MVPs:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getMvps() + "\n" + ChatColor.DARK_AQUA + "`Supports:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY
							+ classStat.getSupps());
				}
				else
				{
					t = new TabText(ChatColor.DARK_AQUA + "`Score:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getFinalScore() + "\n" + ChatColor.DARK_AQUA + "`Kills:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getKills() + "\n" + ChatColor.DARK_AQUA + "`Deaths:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getDeaths() + "\n" + ChatColor.DARK_AQUA + "`KDR:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + kdr + "\n" + ChatColor.DARK_AQUA + "`Assists:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getAssists() + "\n" + ChatColor.DARK_AQUA + "`Streak:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getHighestStreak() + "\n" + ChatColor.DARK_AQUA + "`Captures:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getCaps() + "\n" + ChatColor.DARK_AQUA + "`MVPs:" + ChatColor.WHITE + "`" + ChatColor.DARK_GRAY + classStat.getMvps());
				}
				t.setPageHeight(12);
				t.setTabs(0, 10);
				pages[page] = ChatColor.BLUE + ChatColor.UNDERLINE.toString() + ChatColor.BOLD.toString() + WordUtils.capitalizeFully(classStat.getClassName().replace("_", " ")) + ":" + "\n\n" + t.getPage(1, false);
				/*
				 * pages[page] = ChatColor.BLUE + ChatColor.UNDERLINE.toString()
				 * + ChatColor.BOLD.toString() +
				 * WordUtils.capitalizeFully(classStat
				 * .getClassName().replace("_", " ")) + ":" + ChatColor.RESET +
				 * "\n\n" + ChatColor.DARK_AQUA + "Score:   " +
				 * ChatColor.DARK_GRAY + classStat.getFinalScore() + "\n" +
				 * ChatColor.DARK_AQUA + "Kills:      " + ChatColor.DARK_GRAY +
				 * classStat.getKills() + "\n" + ChatColor.DARK_AQUA +
				 * "Deaths:   " + ChatColor.DARK_GRAY + classStat.getDeaths() +
				 * "\n" + ChatColor.DARK_AQUA + "KDR:       " +
				 * ChatColor.DARK_GRAY + kdr + "\n" + ChatColor.DARK_AQUA +
				 * "Assists:   " + ChatColor.DARK_GRAY + classStat.getAssists()
				 * + "\n" + ChatColor.DARK_AQUA + "Streak:    " +
				 * ChatColor.DARK_GRAY + classStat.getHighestStreak() + "\n" +
				 * ChatColor.DARK_AQUA + "Captures: " + ChatColor.DARK_GRAY +
				 * classStat.getCaps() + "\n" + ChatColor.DARK_AQUA +
				 * "MVPs:       " + ChatColor.DARK_GRAY + classStat.getMvps() +
				 * "\n";
				 */
				page += 1;
			}
		}
		return pages;
	}

	private void openBook(Player p, ItemStack book)
	{
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);
		try
		{
			PacketContainer pc = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
			pc.getModifier().writeDefaults();
			// NOTICE THE CODE BELOW!
			ByteBuf bf = Unpooled.buffer(256); // #1
			bf.setByte(0, (byte) 0); // #2
			bf.writerIndex(1); // #3
			pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
			// END OF NOTABLE CODE
			pc.getStrings().write(0, "MC|BOpen");
			ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		p.getInventory().setItem(slot, old);
	}

	private static ItemStack book(String title, String author, String... pages)
	{
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
		net.minecraft.server.v1_9_R2.ItemStack nmsis = CraftItemStack.asNMSCopy(is);
		NBTTagCompound bd = new NBTTagCompound();
		bd.setString("title", title);
		bd.setString("author", author);
		NBTTagList bp = new NBTTagList();
		for(String text : pages)
		{
			bp.add(new NBTTagString(text));
		}
		bd.set("pages", bp);
		nmsis.setTag(bd);
		is = CraftItemStack.asBukkitCopy(nmsis);
		return is;
	}
}