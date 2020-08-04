package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.extra.NameFetcher;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;

public class BanInfoCommand implements CommandExecutor
{
private Main plugin;
	
	public BanInfoCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("baninfo"))
		{
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(plugin.datahandler.utils.getCSPlayer(p).getRank().getID() < CSRank.CHATMOD.getID()) {
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					return true;
				}
			}
			
			if(args.length != 1)
			{
				sender.sendMessage(ChatColor.RED + "/baninfo <player/id>");
				return true;
			}
			
			boolean cont = true;
			int id = -1;
			try
			{
				id = Integer.parseInt(args[0]);
			}
			catch(Exception ex)
			{
				cont = false;
			}
			
			if(cont)
			{
				try
				{
					SQLConnection sql = plugin.datahandler.sqlConnection;
					Connection c = sql.connection;
					
					PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (ID=?) AND (offense_type='BAN');");
					statement.setInt(1, id);
					
					ResultSet result = statement.executeQuery();
					if(result.next())
					{
						ArrayList<UUID> involvedUUIDs = new ArrayList<UUID>();
						UUID bannedUUID = UUID.fromString(result.getString("offender"));
						involvedUUIDs.add(bannedUUID);
						UUID bannerUUID = null;
						if(result.getString("punisher") != null)
						{
							bannerUUID = UUID.fromString(result.getString("punisher"));
							involvedUUIDs.add(bannerUUID);
						}
						UUID unbannerUUID = null;
						if(result.getString("unpunisher") != null)
						{
							unbannerUUID = UUID.fromString(result.getString("unpunisher"));
							involvedUUIDs.add(unbannerUUID);
						}
						NameFetcher fetcher = new NameFetcher(plugin, involvedUUIDs);
						Map<UUID, String> response = fetcher.call();
						
						String reason = result.getString("reason");
						
						Calendar cal = Calendar.getInstance();
						
						Date banDate = new Date(result.getLong("punish_date") * 1000);
						cal.setTime(banDate);
						String banDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
						long tempUnbanLong = result.getLong("temp_unpunish_date");
						String tempUnbanDateString = null;
						if(!result.wasNull())
						{
							cal.setTime(new Date(tempUnbanLong * 1000));
							tempUnbanDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
						}
						long unbanLong = result.getLong("unpunish_date");
						String unbanDateString = null;
						if(!result.wasNull())
						{
							cal.setTime(new Date(unbanLong * 1000));
							unbanDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
						}
						
						sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
						sender.sendMessage(ChatColor.GRAY + "Detailed Information for Ban ID " + ChatColor.LIGHT_PURPLE + id);
						sender.sendMessage(ChatColor.GRAY + "Player " + ChatColor.RED + response.get(bannedUUID) + ChatColor.GRAY + " was banned by " + ChatColor.YELLOW + (bannerUUID != null ? response.get(bannerUUID) : "Console") + ChatColor.GRAY + " on " + banDateString + ChatColor.GRAY + ".");
						if(reason != null) sender.sendMessage(ChatColor.GRAY + "Reason: " + ChatColor.AQUA + reason);
						else sender.sendMessage(ChatColor.GRAY + "There is no recorded reason.");
						if(tempUnbanDateString != null) sender.sendMessage(ChatColor.YELLOW + response.get(bannedUUID) + " was temp-banned until " + tempUnbanDateString + ChatColor.GRAY + ".");
						if(unbanDateString != null) sender.sendMessage(ChatColor.YELLOW + response.get(bannedUUID) + ChatColor.GRAY + " was unbanned by " + ChatColor.RED + (unbannerUUID != null ? response.get(unbannerUUID) : "Console") + ChatColor.GRAY + " on " + unbanDateString + ChatColor.GRAY + ".");
						sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
					}
				}
				catch (SQLException ex)
				{
					sender.sendMessage(ChatColor.RED + "An unknown error occurred getting the information for that ID. Perhaps ID " + id + " doesn't exist?");
					ex.printStackTrace();
				}
				catch (Exception ex)
				{
					sender.sendMessage("An unknown error occurred trying to download the names of the players involved.");
					ex.printStackTrace();
				}
				return true;
			}
			
			UUID targetPlayer = null;
			if(Bukkit.getPlayer(args[0]) != null) targetPlayer = Bukkit.getPlayer(args[0]).getUniqueId();
			else
			{
				UUIDFetcher fetcher = new UUIDFetcher(plugin, Arrays.asList(args[0]));
				try
				{
					targetPlayer = fetcher.call().get(args[0]);
				}
				catch(Exception ex)
				{
					sender.sendMessage(ChatColor.RED + "An error occurred getting the offline player's details.");
					ex.printStackTrace();
					return true;
				}
			}
			
			if(targetPlayer == null)
			{
				sender.sendMessage(ChatColor.RED + "That player does not exist!");
				return true;
			}
			
			ArrayList<Integer> ids = new ArrayList<Integer>();
			boolean banned = false;
			try
			{
				SQLConnection sql = plugin.datahandler.sqlConnection;
				Connection c = sql.connection;
				
				PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (offender=?) AND (offense_type='BAN');");
				statement.setString(1, targetPlayer.toString());
				
				ResultSet result = statement.executeQuery();
				int latestId = -1;
				while(result.next())
				{
					int tempId = result.getInt("ID");
					ids.add(tempId);
					if(latestId < tempId)
					{
						latestId = tempId;
					}
				}
				result.close();
				statement.close();
				
				if(latestId != -1)
				{
					cont = false;
					statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE ID=?;");
					statement.setInt(1, latestId);
					result = statement.executeQuery();
					
					if(result.next())
					{
						result.getLong("unpunish_date");
						if(!result.wasNull()) { cont = true; }
						if(!cont)
						{
							long tempUnban = result.getLong("temp_unpunish_date");
							if(!result.wasNull())
							{
								Date tempUnbanDate = new Date(tempUnban * 1000);
								if(tempUnbanDate.before(new Date())) cont = true;
							}
						}
						
						if(!cont) { banned = true; }
					}
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
			sender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.GRAY + " is currently " + (banned ? ChatColor.RED + "banned" : ChatColor.GREEN + "unbanned") + ChatColor.GRAY + ", and has been banned " + ChatColor.AQUA + ids.size() + ChatColor.GRAY + " times.");
			if(ids.size() > 0)
			{
				StringBuilder sb = new StringBuilder(ChatColor.GRAY + "Ban IDs: ");
				for(int i = 0; i < ids.size(); i++)
				{
					sb.append(ChatColor.LIGHT_PURPLE + "" + ids.get(i));
					if(ids.size() > i + 1)
					{
						sb.append(ChatColor.GRAY + ", ");
					}
				}
				sender.sendMessage(sb.toString());
				sender.sendMessage(ChatColor.GRAY + "For more information on a specific ban, use " + ChatColor.BLUE + "/baninfo <id>");
			}
			sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
			return true;
		}
		return false;
	}
}