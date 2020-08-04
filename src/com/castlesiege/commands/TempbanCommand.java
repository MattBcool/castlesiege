package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;

public class TempbanCommand implements CommandExecutor
{
	private Main plugin;
	
	public TempbanCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("tempban"))
		{
			UUID bannerUUID = null;
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				if(plugin.datahandler.utils.getCSPlayer(p).getRank().getID() < CSRank.MOD.getID())
				{
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					return true;
				}
				bannerUUID = p.getUniqueId();
			}
			
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + "/tempban <player> <time> [reason]");
				sender.sendMessage(ChatColor.RED + "Time Format: #t, Example: 1d,2m,3s - 1 day 2 minutes 3 seconds");
				return true;
			}
			
			String[] parts = args[1].split(",");
			int days = 0, hours = 0, minutes = 0, seconds = 0;
			for(String part : parts)
			{
				if(part.endsWith("d"))
				{
					days += Integer.parseInt(part.charAt(0) + "");
				}
				else if(part.endsWith("h"))
				{
					hours += Integer.parseInt(part.charAt(0) + "");
				}
				else if(part.endsWith("m"))
				{
					minutes += Integer.parseInt(part.charAt(0) + "");
				}
				else if(part.endsWith("s"))
				{
					seconds += Integer.parseInt(part.charAt(0) + "");
				}
			}
			
			Date date = new Date(new Date().getTime() + (days * 3600*24*1000) + (hours * 3600*1000) + (minutes * 60*1000) + (seconds * 1000));
			
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
			
			long currentDate = System.currentTimeMillis() / 1000;
			
			String reason = "";
			if(args.length > 2)
			{
				for(int i = 2; i < args.length; i++)
				{
					reason += args[i] + " ";
				}
				reason = reason.substring(0, reason.length() - 1);
			}
			
			try
			{
				SQLConnection sql = plugin.datahandler.sqlConnection;
				Connection c = sql.connection;
				
				PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (offender=?) AND (offense_type='BAN') ORDER BY ID DESC LIMIT 0, 1;");
				statement.setString(1, targetPlayer.toString());
				ResultSet result = statement.executeQuery();
				if(result.next())
				{
					boolean cont = false;
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
					
					if(!cont)
					{
						sender.sendMessage(ChatColor.RED + "This player is already banned!");
						return true;
					}
				}
								
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				String tempUnbanDateString = new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + " at " + cal.get(Calendar.HOUR) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)) + ":" + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND) : cal.get(Calendar.SECOND)) + " " + (cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");

				if(Bukkit.getPlayer(targetPlayer) != null) Bukkit.getPlayer(targetPlayer).kickPlayer(ChatColor.GRAY + "You have been banned!\n" + (reason != null ? ChatColor.GRAY + "Reason: " + ChatColor.BLUE + reason + "\n" : "") + ChatColor.GRAY + "You will be unbanned on " + ChatColor.BLUE + tempUnbanDateString + ChatColor.GRAY + ".\n" + ChatColor.GRAY + "Apply to be unbanned at talesofwar.net");
				
				Bukkit.broadcastMessage(ChatColor.GREEN + "" + (bannerUUID != null ? Bukkit.getPlayer(bannerUUID).getName() : "Console") + ChatColor.GOLD + " has banned " + ChatColor.RED + args[0] + " until " + ChatColor.GOLD + tempUnbanDateString + "." + (reason != null ? " Reason: " + ChatColor.YELLOW + reason : ""));
				
				statement = c.prepareStatement("INSERT INTO talesofwar.player_punishments VALUES(DEFAULT, ?, 'BAN', ?, ?, ?, ?, NULL, NULL)");
				statement.setString(1, targetPlayer.toString());
				if(!reason.equals("")) statement.setString(2, reason);
				else statement.setString(2, null);
				statement.setLong(3, currentDate);
				if(bannerUUID != null)  statement.setString(4, bannerUUID.toString());
				else statement.setString(4, null);
				statement.setLong(5, date.getTime() / 1000);
				statement.execute();
				statement.close();
			}
			catch(SQLException ex)
			{
				sender.sendMessage(ChatColor.RED + "An error occurred banning the player.");
				ex.printStackTrace();
			}
			return true;
		}
		return false;
	}
}