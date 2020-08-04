package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import com.castlesiege.player.CSClassStats;
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;

public class ResetStatsCommand implements CommandExecutor
{
	private Main plugin;

	public ResetStatsCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean console = !(sender instanceof Player);
		if(label.equalsIgnoreCase("resetstats"))
		{
			if(console || plugin.datahandler.utils.getCSPlayer((Player) sender) != null && plugin.datahandler.utils.getCSPlayer((Player) sender).getRank().getID() >= CSRank.DEV.getID())
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
						sender.sendMessage(ChatColor.RED + "An error occured while resetting stats for " + args[0] + "!");
						return false;
					}

					if(uuid == null)
					{
						sender.sendMessage(ChatColor.RED + "An error occured while resetting stats for " + args[0] + "!");
						return false;
					}
					Player player = Bukkit.getPlayer(uuid);
					if(player == null)
					{
						SQLConnection sql = plugin.datahandler.sqlConnection;
						Connection c = sql.connection;
						try
						{
							PreparedStatement statement = c.prepareStatement("UPDATE player_data SET current_class=?, total_score=?, total_kills=?, total_deaths=?, total_assists=?, total_caps=?, total_supps=?, total_mvps=?, highest_streak=?, class_stats=? WHERE uuid=?");
							statement.setString(1, "swordsman");
							statement.setInt(2, 0);
							statement.setInt(3, 0);
							statement.setInt(4, 0);
							statement.setInt(5, 0);
							statement.setInt(6, 0);
							statement.setInt(7, 0);
							statement.setInt(8, 0);
							statement.setInt(9, 0);
							statement.setString(10, new CSClassStats("").encryptData());
							statement.setString(11, uuid.toString());

							statement.execute();
							statement.close();
							sender.sendMessage(ChatColor.GREEN + "Successfully reset stats for " + args[0] + "!");
						}
						catch(SQLException e)
						{
							sender.sendMessage(ChatColor.RED + "An error occured while resetting stats for " + args[0] + "!");
						}
					}
					else
					{
						plugin.datahandler.utils.getCSPlayer(player).classStats = new CSClassStats("");
						sender.sendMessage(ChatColor.GREEN + "Successfully reset stats for " + player.getName() + "!");
						String name = console ? "Console" : sender.getName();
						player.sendMessage(ChatColor.GREEN + "Your stats were reset by " + name);
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Please specify a player to reset stats for.");
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