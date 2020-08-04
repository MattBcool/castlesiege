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
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;

public class RankCommand implements CommandExecutor
{
	private Main plugin;

	public RankCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("setrank"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				CSPlayer player = plugin.datahandler.utils.getCSPlayer(p);
				if(player.getRank().getID() < CSRank.ADMIN.getID())
				{
					return false;
				}
			}

			if(args.length != 2)
			{
				sender.sendMessage("/setrank <player> <rank>");
				return true;
			}

			UUID uuid;
			try
			{
				uuid = UUIDFetcher.getUUIDOf(plugin, args[0]);
			}
			catch(Exception e)
			{
				return false;
			}
			Player p = Bukkit.getPlayer(uuid);

			CSRank rank = CSRank.getRank(args[1]);
			if(rank == null)
			{
				sender.sendMessage("Rank " + rank + " does not exist!");
				return true;
			}
			if(p != null && p.isOnline())
			{
				p.sendMessage("Your rank has been set to " + rank.toString() + "!");
				plugin.datahandler.utils.getCSPlayer(p).setRank(rank);
			}
			else
			{
				SQLConnection sql = plugin.datahandler.sqlConnection;
				Connection c = sql.connection;
				try
				{
					PreparedStatement statement = c.prepareStatement("UPDATE player_data SET rank=? WHERE uuid=?");
					statement.setString(1, rank.toString().toLowerCase());
					statement.setString(2, uuid.toString());

					statement.execute();
					statement.close();
				}
				catch(SQLException e)
				{
					sender.sendMessage(ChatColor.RED + "An error occured while setting rank for " + args[0] + "!");
				}
			}

			sender.sendMessage("Successfully set the rank for " + args[0] + " to " + rank.toString());
		}
		return false;
	}
}
