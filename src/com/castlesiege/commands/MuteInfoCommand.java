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

public class MuteInfoCommand implements CommandExecutor
{
private Main plugin;
	
	public MuteInfoCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("muteinfo"))
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
				sender.sendMessage(ChatColor.RED + "/muteinfo <player/id>");
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
					
					PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (ID=?) AND (offense_type='MUTE');");
					statement.setInt(1, id);
					
					ResultSet result = statement.executeQuery();
					if(result.next())
					{
						ArrayList<UUID> involvedUUIDs = new ArrayList<UUID>();
						UUID mutedUUID = UUID.fromString(result.getString("offender"));
						involvedUUIDs.add(mutedUUID);
						UUID muterUUID = null;
						if(result.getString("punisher") != null)
						{
							muterUUID = UUID.fromString(result.getString("punisher"));
							involvedUUIDs.add(muterUUID);
						}
						UUID unmuterUUID = null;
						if(result.getString("unpunisher") != null)
						{
							unmuterUUID = UUID.fromString(result.getString("unpunisher"));
							involvedUUIDs.add(unmuterUUID);
						}
						NameFetcher fetcher = new NameFetcher(plugin, involvedUUIDs);
						Map<UUID, String> response = fetcher.call();
						
						String reason = result.getString("reason");
						
						Calendar cal = Calendar.getInstance();
						
						Date muteDate = new Date(result.getLong("punish_date") * 1000);
						cal.setTime(muteDate);
						String muteDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
						long tempUnmuteLong = result.getLong("temp_unpunish_date");
						String tempUnmuteDateString = null;
						if(!result.wasNull())
						{
							cal.setTime(new Date(tempUnmuteLong * 1000));
							tempUnmuteDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
						}
						long unmuteLong = result.getLong("unpunish_date");
						String unmuteDateString = null;
						if(!result.wasNull())
						{
							cal.setTime(new Date(unmuteLong * 1000));
							unmuteDateString = ChatColor.BLUE + new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + ChatColor.GRAY + " at " + ChatColor.BLUE + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
						}
						
						sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
						sender.sendMessage(ChatColor.GRAY + "Detailed Information for Mute ID " + ChatColor.LIGHT_PURPLE + id);
						sender.sendMessage(ChatColor.GRAY + "Player " + ChatColor.RED + response.get(mutedUUID) + ChatColor.GRAY + " was muted by " + ChatColor.YELLOW + (muterUUID != null ? response.get(muterUUID) : "Console") + ChatColor.GRAY + " on " + muteDateString + ChatColor.GRAY + ".");
						if(reason != null) sender.sendMessage(ChatColor.GRAY + "Reason: " + ChatColor.AQUA + reason);
						else sender.sendMessage(ChatColor.GRAY + "There is no recorded reason.");
						if(tempUnmuteDateString != null) sender.sendMessage(ChatColor.YELLOW + response.get(mutedUUID) + " was temp-muted until " + tempUnmuteDateString + ChatColor.GRAY + ".");
						if(unmuteDateString != null) sender.sendMessage(ChatColor.RED + response.get(mutedUUID) + ChatColor.GRAY + " was unmuted by " + ChatColor.YELLOW + (unmuterUUID != null ? response.get(unmuterUUID) : "Console") + ChatColor.GRAY + " on " + unmuteDateString + ChatColor.GRAY + ".");
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
			boolean muted = false;
			try
			{
				SQLConnection sql = plugin.datahandler.sqlConnection;
				Connection c = sql.connection;
				
				PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (offender=?) AND (offense_type='MUTE');");
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
						
						if(!cont) { muted = true; }
					}
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
			sender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.GRAY + " is currently " + (muted ? ChatColor.RED + "muted" : ChatColor.GREEN + "unmuted") + ChatColor.GRAY + ", and has been muted " + ChatColor.AQUA + ids.size() + ChatColor.GRAY + " times.");
			if(ids.size() > 0)
			{
				StringBuilder sb = new StringBuilder(ChatColor.GRAY + "Mute IDs: ");
				for(int i = 0; i < ids.size(); i++)
				{
					sb.append(ChatColor.LIGHT_PURPLE + "" + ids.get(i));
					if(ids.size() > i + 1)
					{
						sb.append(ChatColor.GRAY + ", ");
					}
				}
				sender.sendMessage(sb.toString());
				sender.sendMessage(ChatColor.GRAY + "For more information on a specific mute, use " + ChatColor.BLUE + "/muteinfo <id>");
			}
			sender.sendMessage(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 80));
			return true;
		}
		return false;
	}
}