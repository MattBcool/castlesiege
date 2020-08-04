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
import com.castlesiege.player.CSRank;
import com.castlesiege.util.sql.SQLConnection;

public class ADACommand implements CommandExecutor
{
	private Main plugin;

	public ADACommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		boolean console = !(sender instanceof Player);
		if(label.equalsIgnoreCase("ada"))
		{
			if(console || (plugin.datahandler.utils.getCSPlayer((Player) sender) != null && plugin.datahandler.utils.getCSPlayer((Player) sender).getRank().getID() >= CSRank.DEV.getID()))
			{
				if(args.length > 0)
				{
					if(args.length > 1)
					{
						if(plugin.datahandler.utils.isDouble(args[1]))
						{
							UUID uuid;
							try
							{
								uuid = UUIDFetcher.getUUIDOf(plugin, args[0]);
							}
							catch(Exception e1)
							{
								sender.sendMessage(ChatColor.RED + "An error occured while adding donation points for " + args[0] + "!");
								return false;
							}

							if(uuid == null)
							{
								sender.sendMessage(ChatColor.RED + "An error occured while adding donation points for " + args[0] + "!");
								return false;
							}
							Double amt = Double.parseDouble(args[1]);
							Player player = Bukkit.getPlayer(uuid);
							if(player == null)
							{
								SQLConnection sql = plugin.datahandler.sqlConnection;
								Connection c = sql.connection;
								try
								{
									PreparedStatement statement = c.prepareStatement("SELECT total_donated FROM player_data WHERE uuid=?");
									ResultSet result = statement.executeQuery();
									if(result.next())
									{
										if(result.getInt("total_donated") + amt < 0)
										{
											PreparedStatement s = c.prepareStatement("UPDATE player_data SET total_donated WHERE uuid=?");
											s.setDouble(1, result.getDouble("total_donated") + amt);
											s.setString(2, uuid.toString());

											s.execute();
											s.close();
											sender.sendMessage(ChatColor.GREEN + "Successfully added donation points for " + args[0] + "!");
										}
										else
										{
											sender.sendMessage(ChatColor.RED + "An error occured while adding donation points for " + args[0] + "! Value would be lower than 0!");
										}
									}
									else
									{
										sender.sendMessage(ChatColor.RED + "An error occured while adding donation points for " + args[0] + "!");
									}
								}
								catch(SQLException e)
								{
									sender.sendMessage(ChatColor.RED + "An error occured while adding donation points for " + args[0] + "!");
								}
							}
							else
							{
								plugin.datahandler.utils.getCSPlayer(player).totalDonated += amt;
								sender.sendMessage(ChatColor.GREEN + "Successfully added donation points for " + args[0] + "!");
								String name = console ? "Console" : sender.getName();
								player.sendMessage(ChatColor.GREEN + "Your donation amount was changed by " + name);
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "Please specify a valid amount of donation points to be added.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "Please specify an amount of donation points to be added.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Please specify a player to add donation points to.");
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