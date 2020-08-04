package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;

public class UnbanCommand implements CommandExecutor
{
	private Main plugin;

	public UnbanCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("unban"))
		{
			UUID unbannerUUID = null;
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				CSPlayer csp = plugin.datahandler.utils.getCSPlayer(p);
				if(csp.getRank().getID() < CSRank.MOD.getID())
				{
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					return true;
				}
				unbannerUUID = ((Player) sender).getUniqueId();
			}
			
			if(args.length != 1)
			{
				sender.sendMessage(ChatColor.RED + "/unban <player>");
				return true;
			}
			
			UUID targetPlayer = null;
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
			
			if(targetPlayer == null)
			{
				sender.sendMessage(ChatColor.RED + "Player not found.");
				return true;
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
					long tempUnban = result.getLong("temp_unpunish_date");
					boolean notNull = false;
					if(!result.wasNull())
					{
						notNull = true;
					}
					result.getLong("unpunish_date");
					if(!result.wasNull() || (notNull && new Date(tempUnban * 1000).before(new Date())))
					{
						sender.sendMessage(ChatColor.RED + "This player is not banned!");
						return true;
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "This player is not banned!");
					return true;
				}
				
				if(unbannerUUID != null)
				{
					CSPlayer csp = plugin.datahandler.utils.getCSPlayer((Player) sender);
					String bannerUUID = result.getString("punisher");
					if(csp.getRank().getID() < CSRank.ADMIN.getID() && (bannerUUID == null || !unbannerUUID.equals(UUID.fromString(bannerUUID))))
					{
						sender.sendMessage(ChatColor.RED + "You cannot unban a player you did not ban!");
						return true;
					}
				}
				
				int id = result.getInt("ID");
				result.close();
				statement.close();
				
				statement = c.prepareStatement("UPDATE talesofwar.player_punishments SET unpunish_date=?, unpunisher=? WHERE (ID=?);");
				statement.setLong(1, System.currentTimeMillis() / 1000);
				if(unbannerUUID != null) statement.setString(2, unbannerUUID.toString());
				else statement.setString(2, null);
				statement.setInt(3, id);
				statement.execute();
				statement.close();
			}
			catch(SQLException ex)
			{
				ex.printStackTrace();
			}
		}
		return false;
	}
}