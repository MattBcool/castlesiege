package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
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

public class IpCommand implements CommandExecutor
{
	private Main plugin;

	public IpCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean console = !(sender instanceof Player);
		if(label.equalsIgnoreCase("ip") || label.equalsIgnoreCase("getip"))
		{
			if(console || plugin.datahandler.utils.getCSPlayer((Player) sender) != null && plugin.datahandler.utils.getCSPlayer((Player) sender).getRank().getID() >= CSRank.ADMIN.getID())
			{
				if(args.length > 0)
				{
					UUID uuid;
					try
					{
						uuid = UUIDFetcher.getUUIDOf(plugin, args[0]);
					}
					catch(Exception e1)
					{
						sender.sendMessage(ChatColor.RED + "An error occured while getting ip for " + args[0] + "!");
						return false;
					}

					if(uuid == null)
					{
						sender.sendMessage(ChatColor.RED + "An error occured while getting ip for " + args[0] + "!");
						return false;
					}
					Player player = Bukkit.getPlayer(uuid);
					if(player == null)
					{
						SQLConnection sql = plugin.datahandler.sqlConnection;
						Connection c = sql.connection;
						try
						{
							PreparedStatement statement = c.prepareStatement("SELECT latest_ip, rank FROM player_data WHERE uuid=?");
							statement.setString(1, uuid.toString());

							ResultSet result = statement.executeQuery();
							if(result.next())
							{
								if(console || CSRank.getRank(result.getString("rank")).getID() < CSRank.ADMIN.getID())
								{
									sender.sendMessage(ChatColor.GREEN + "Ip for " + args[0] + ": " + result.getString("latest_ip"));
								}
								else
								{
									sender.sendMessage(ChatColor.RED + "You don't have permission to get this player's ip!");
								}
							}
							else
							{
								sender.sendMessage(ChatColor.RED + "An error occured while getting ip for " + args[0] + "!");
							}
							statement.close();
							result.close();
						}
						catch(SQLException e)
						{
							sender.sendMessage(ChatColor.RED + "An error occured while getting ip for " + args[0] + "!");
						}
					}
					else
					{
						CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(player);
						if(console || csplayer.getRank().getID() < CSRank.ADMIN.getID())
						{
							sender.sendMessage(ChatColor.GREEN + "Ip for " + player.getName() + ": " + csplayer.ip);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You don't have permission to get this player's ip!");
						}
						
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Please specify a player to get their ip.");
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "You don't have permission to run this command!");
			}
		}
		return false;
	}
}