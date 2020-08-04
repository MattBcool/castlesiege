package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
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

public class MuteCommand implements CommandExecutor
{
	private Main plugin;

	public MuteCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		/**
		 * INSERT INTO talesofwar.player_punishments VALUES(DEFAULT, '7df323cf-9eb9-4e92-9f7f-ec1b0e2ff79c', 'KICK', 'Lol', 1467611901, '7df323cf-9eb9-4e92-9f7f-ec1b0e2ff79c', NULL, NULL, NULL);
		 * 
		 * DELETE FROM talesofwar.player_punishments WHERE ID<10;
		 * ALTER TABLE talesofwar.player_punishments AUTO_INCREMENT = 1;
		 * 
		 * SELECT * FROM talesofwar.player_punishments WHERE (offender='7df323cf-9eb9-4e92-9f7f-ec1b0e2ff79c') AND (offense_type='BAN') ORDER BY ID DESC LIMIT 0, 1;
		 */
		if(cmd.getName().equalsIgnoreCase("mute"))
		{
			UUID muterUUID = null;
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				if(plugin.datahandler.utils.getCSPlayer(p).getRank().getID() < CSRank.CHATMOD.getID())
				{
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					return true;
				}
				muterUUID = p.getUniqueId();
			}
			
			if(args.length < 1)
			{
				sender.sendMessage(ChatColor.RED + "/mute <player> [reason]");
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
			
			long date = System.currentTimeMillis() / 1000;
			
			String reason = "";
			if(args.length > 1)
			{
				for(int i = 1; i < args.length; i++)
				{
					reason += args[i] + " ";
				}
				reason = reason.substring(0, reason.length() - 1);
			}
			
			try
			{
				SQLConnection sql = plugin.datahandler.sqlConnection;
				Connection c = sql.connection;
				
				PreparedStatement statement = c.prepareStatement("SELECT * FROM talesofwar.player_punishments WHERE (offender=?) AND (offense_type='MUTE') ORDER BY ID DESC LIMIT 0, 1;");
				statement.setString(1, targetPlayer.toString());
				ResultSet result = statement.executeQuery();
				if(result.next())
				{
					boolean cont = false;
					result.getLong("unpunish_date");
					if(!result.wasNull()) { cont = true; }
					if(!cont)
					{
						long tempUnmute = result.getLong("temp_unpunish_date");
						if(!result.wasNull())
						{
							Date tempUnmuteDate = new Date(tempUnmute * 1000);
							if(tempUnmuteDate.before(new Date())) cont = true;
						}
					}
					
					if(!cont)
					{
						sender.sendMessage(ChatColor.RED + "This player is already muted!");
						return true;
					}
				}
				
				Bukkit.broadcastMessage(ChatColor.GREEN + "" + (muterUUID != null ? Bukkit.getPlayer(muterUUID).getName() : "Console") + ChatColor.GOLD + " has muted " + ChatColor.RED + args[0] + ChatColor.GOLD + " forever." + (reason != null ? " Reason: " + ChatColor.YELLOW + reason : ""));
				
				statement = c.prepareStatement("INSERT INTO talesofwar.player_punishments VALUES(DEFAULT, ?, 'MUTE', ?, ?, ?, NULL, NULL, NULL)");
				statement.setString(1, targetPlayer.toString());
				if(!reason.equals("")) statement.setString(2, reason);
				else statement.setString(2, null);
				statement.setLong(3, date);
				if(muterUUID != null)  statement.setString(4, muterUUID.toString());
				else statement.setString(4, null);
				statement.execute();
				statement.close();
			}
			catch(SQLException ex)
			{
				sender.sendMessage(ChatColor.RED + "An error occurred muting the player.");
				ex.printStackTrace();
			}
			return true;
		}
		return false;
	}
}