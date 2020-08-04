package com.castlesiege.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.data.guilds.Guild;
import com.castlesiege.extra.UUIDFetcher;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;
import com.castlesiege.player.CSStats;
import com.castlesiege.util.sql.SQLConnection;

public class GuildCommand implements CommandExecutor
{
	private Main plugin;

	private HashMap<String, String> commands;

	public GuildCommand(Main plugin)
	{
		this.plugin = plugin;

		commands = new HashMap<String, String>();
		
		commands.put("create", "Creates a new guild");
		commands.put("remove", "Disbands the guild");
		commands.put("delete", "Disbands the guild");
		commands.put("disband", "Disbands the guild");
		commands.put("invite", "Invites a player to the guild");
		commands.put("uninvite", "Uninvites a player from the guild");
		commands.put("revoke", "Uninvites a player from the guild");
		commands.put("revokeinvite", "Uninvites a player from the guild");
		commands.put("accept", "Accepts an invitation to a guild");
		commands.put("kick", "Kicks a player from the guild");
		commands.put("boot", "Kicks a player from the guild");
		commands.put("leave", "Leaves the guild");
		commands.put("exit", "Leaves the guild");
		commands.put("info", "Lists out guild info");
		commands.put("level", "Lists out guild level info");
		commands.put("lvl", "Lists out guild level info");
		commands.put("exp", "Lists out guild level info");
		commands.put("currentlevel", "Lists out guild level info");
		commands.put("currentexp", "Lists out guild level info");
		commands.put("nextlevel", "Lists out guild level info");
		commands.put("nextlvl", "Lists out guild level info");
		commands.put("experience", "Lists out guild level info");
		commands.put("help", "Lists guild commands");
		
		commands = (HashMap<String, String>) plugin.datahandler.utils.sortByKeys(commands);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			return true;
		}
		final Player p = (Player) sender;

		if(cmd.getName().equalsIgnoreCase("guild"))
		{
			if(args.length > 0)
			{
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				if(args[0].equalsIgnoreCase("create"))
				{
					if(csplayer.getDonorRank().getID() >= CSDonorRank.DUKE.getID() || csplayer.getRank().getID() >= CSRank.ADMIN.getID())
					{
						if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
						{
							if(args.length > 1)
							{
								if(plugin.datahandler.utils.isAlpha(args[1]))
								{
									SQLConnection sql = plugin.datahandler.sqlConnection;
									Connection c = sql.connection;

									try
									{
										PreparedStatement statement = c.prepareStatement("SELECT guild_name FROM guild_data WHERE guild_name=?;");
										statement.setString(1, args[1]);
										ResultSet result = statement.executeQuery();

										if(result.next())
										{
											p.sendMessage(ChatColor.RED + "This guild already exists!");
										}
										else
										{
											csplayer.guild = args[1];
											Guild guild = new Guild(plugin, p, args[1]);
											if(csplayer.guildspawn && plugin.datahandler.gamemodehandler.inSpawnRoom(p.getLocation()))
											{
												p.teleport(guild.getSpawn());
											}
											p.sendMessage(ChatColor.GREEN + "Successfully created the guild " + args[1] + "!");
										}
									}
									catch(SQLException e1)
									{
										e1.printStackTrace();
									}
								}
								else
								{
									p.sendMessage(ChatColor.RED + "Please specify a valid name for the guild.");
								}
							}
							else
							{
								p.sendMessage(ChatColor.RED + "Please specify a name for the guild.");
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + "You are already in a guild!");
							p.sendMessage(ChatColor.RED + "Use " + ChatColor.ITALIC + "/guild leave" + ChatColor.RESET + ChatColor.GREEN.toString() + " to leave your guild.");
						}
					}
					else
					{
						p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RED + "Required Rank: " + CSDonorRank.DUKE.getTag());
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("disband"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						if(guild != null)
						{
							if(guild.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString()))
							{
								guild.disbandGuild();
								csplayer.guild = "";
								p.sendMessage(ChatColor.GREEN + "Successfully disbanded the guild " + csplayer.guild + "!");
							}
							else
							{
								p.sendMessage(ChatColor.RED + "You must be the guild owner to disband the guild!");
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + "An error has occured!");
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("invite"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						if(args.length > 1)
						{
							UUID uuid;
							try
							{
								uuid = UUIDFetcher.getUUIDOf(plugin, args[1]);
							}
							catch(Exception e)
							{
								return false;
							}
							if(uuid == null)
							{
								p.sendMessage(ChatColor.RED + "Please specify a valid player to invite to the guild!");
							}
							else
							{
								Player ply = Bukkit.getPlayer(uuid);

								if(ply != null && ply.isOnline())
								{
									CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
									if(csp.guildInvite.equalsIgnoreCase(csplayer.guild))
									{
										p.sendMessage(ChatColor.RED + "The player " + args[1] + " has already been invited to the guild!");
										return false;
									}
									else
									{
										if(csp.guild.equalsIgnoreCase(csplayer.guild))
										{
											p.sendMessage(ChatColor.RED + "The player " + args[1] + " is already in the guild!");
											return false;
										}
										else
										{
											ply.sendMessage(ChatColor.GREEN + "You were invited to join the guild " + csplayer.guild + " by " + p.getName() + "!");
											ply.sendMessage(ChatColor.GREEN + "Use " + ChatColor.ITALIC + "/guild accept" + ChatColor.RESET + ChatColor.GREEN.toString() + " to accept the invitation.");
											csp.guildInvite = csplayer.guild;
											plugin.datahandler.utils.getGuild(csplayer.guild).initiates.add(uuid);
										}
									}
								}
								else
								{
									SQLConnection sql = plugin.datahandler.sqlConnection;
									Connection c = sql.connection;
									try
									{
										PreparedStatement statement = c.prepareStatement("SELECT uuid FROM player_data WHERE uuid=? AND guild_invite=?");
										statement.setString(1, uuid.toString());
										statement.setString(2, csplayer.guild);
										ResultSet result = statement.executeQuery();
										if(result.next())
										{
											p.sendMessage(ChatColor.RED + "The player " + args[1] + " has already been invited to the guild!");
										}
										else
										{
											PreparedStatement s = c.prepareStatement("SELECT uuid FROM player_data WHERE uuid=? AND guild=?");
											s.setString(1, uuid.toString());
											s.setString(2, csplayer.guild);
											ResultSet r = s.executeQuery();
											if(r.next())
											{
												p.sendMessage(ChatColor.RED + "The player " + args[1] + " is already in the guild!");
												s.close();
												r.close();
												return false;
											}
											else
											{
												PreparedStatement s1 = c.prepareStatement("UPDATE player_data SET guild_invite=? WHERE uuid=?");
												s1.setString(1, csplayer.guild);
												s1.setString(2, uuid.toString());

												s1.execute();
												s1.close();
												s.close();
												r.close();
											}
										}
										statement.close();
									}
									catch(SQLException e)
									{
										p.sendMessage(ChatColor.RED + "Please specify a valid player to invite to the guild!");
										return false;
									}
									plugin.datahandler.utils.getGuild(csplayer.guild).initiates.add(uuid);
								}
								p.sendMessage(ChatColor.GREEN + "You invited " + args[1] + " to the guild!");
								return false;
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + "Please specify a player to invite to the guild!");
							return false;
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("uninvite") || args[0].equalsIgnoreCase("revoke") || args[0].equalsIgnoreCase("revokeinvite"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						if(args.length > 1)
						{
							UUID uuid;
							try
							{
								uuid = UUIDFetcher.getUUIDOf(plugin, args[1]);
							}
							catch(Exception e)
							{
								return false;
							}
							if(uuid == null)
							{
								p.sendMessage(ChatColor.RED + "Please specify a valid player to uninvite from the guild!");
							}
							else
							{
								Player ply = Bukkit.getPlayer(uuid);

								if(ply != null && ply.isOnline())
								{
									CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
									if(csp.guildInvite.equalsIgnoreCase(csplayer.guild))
									{
										ply.sendMessage(ChatColor.GREEN + "You were uninvited to the guild" + csplayer.guild + " by " + p.getName() + "!");
										csp.guildInvite = "";
									}
									else
									{
										p.sendMessage(ChatColor.RED + "The player " + args[1] + " does not have an invite to the guild!");
										return false;
									}
								}
								else
								{
									SQLConnection sql = plugin.datahandler.sqlConnection;
									Connection c = sql.connection;

									try
									{
										PreparedStatement statement = c.prepareStatement("SELECT uuid FROM player_data WHERE uuid=? AND guild_invite=?");
										statement.setString(1, uuid.toString());
										statement.setString(2, csplayer.guild);

										ResultSet result = statement.executeQuery();
										if(result.next())
										{
											PreparedStatement s = c.prepareStatement("UPDATE player_data SET guild_invite=? WHERE uuid=?");
											s.setString(1, "");
											s.setString(2, uuid.toString());

											s.execute();
											s.close();
											statement.close();
											result.close();
										}
										else
										{
											p.sendMessage(ChatColor.RED + "The player " + args[1] + " does not have an invite to the guild!");
											statement.close();
											result.close();
											return false;
										}
									}
									catch(SQLException e)
									{
										p.sendMessage(ChatColor.RED + "Please specify a valid player to invite to the guild!");
										return false;
									}
								}
								p.sendMessage(ChatColor.GREEN + "You uninvited " + args[1] + " from the guild!");
								return false;
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + "Please specify a player to invite to the guild!");
							return false;
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("accept"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						if(csplayer.guildInvite == null || csplayer.guildInvite.equalsIgnoreCase(""))
						{
							p.sendMessage(ChatColor.RED + "You do not have any incoming guild invites!");
						}
						else
						{
							csplayer.guild = csplayer.guildInvite;
							csplayer.guildInvite = "";
							Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
							if(guild == null)
							{
								SQLConnection sql = plugin.datahandler.sqlConnection;
								Connection c = sql.connection;

								try
								{
									PreparedStatement statement = c.prepareStatement("SELECT guild_name FROM guild_data WHERE guild_name=?;");
									statement.setString(1, csplayer.guild);
									ResultSet result = statement.executeQuery();

									if(result.next())
									{
										guild = new Guild(plugin, csplayer.guild);
									}
									else
									{
										csplayer.guild = "";
										p.sendMessage(ChatColor.RED + "Your guild has been disbanded!");
									}
									statement.close();
									result.close();
								}
								catch(SQLException e1)
								{
									e1.printStackTrace();
								}
							}
							if(!csplayer.guild.equalsIgnoreCase(""))
							{
								p.sendMessage(ChatColor.GREEN + "You joined the guild " + csplayer.guild + "!");
							}
							if(csplayer.guildspawn && plugin.datahandler.gamemodehandler.inSpawnRoom(p.getLocation()))
							{
								p.teleport(guild.getSpawn());
								guild.initiates.add(p.getUniqueId());
								final Guild g = guild;
								Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										g.updateGuildSigns(p);
									}
								}, 20L);
							}
						}
					}
					else
					{
						p.sendMessage(ChatColor.RED + "You are already in a guild!");
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("boot"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						if(guild.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString()) || guild.officers.contains(p.getUniqueId().toString()))
						{
							if(args.length > 1)
							{
								if(args[1].equalsIgnoreCase(p.getName()))
								{
									p.sendMessage(ChatColor.RED + "You can not kick yourself from the guild!");
									return false;
								}
								else
								{
									UUID uuid;
									try
									{
										uuid = UUIDFetcher.getUUIDOf(plugin, args[1]);
									}
									catch(Exception e)
									{
										return false;
									}
									if(uuid == null)
									{
										p.sendMessage(ChatColor.RED + "Please specify a valid player to kick from the guild!");
									}
									else
									{
										Player ply = Bukkit.getPlayer(uuid);

										String u = ply.getUniqueId().toString();
										guild.officers.remove(u);
										guild.members.remove(u);
										guild.initiates.remove(u);
										if(ply != null && ply.isOnline())
										{
											CSPlayer csp = plugin.datahandler.utils.getCSPlayer(ply);
											if(csp.guild.equalsIgnoreCase(csplayer.guild))
											{
												if(plugin.datahandler.gamemodehandler.inGuildRoom(ply.getLocation()))
												{
													ply.teleport(plugin.datahandler.gamemodehandler.getTeam(ply).getSpawn());
												}
												csp.guild = "";
												csplayer.guildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
												csplayer.monthGuildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
												ply.sendMessage(ChatColor.GREEN + "You were kicked from the guild " + csplayer.guild + " by " + p.getName() + "!");
											}
											else
											{
												p.sendMessage(ChatColor.RED + "The player " + args[1] + " is not in the guild!");
												return false;
											}
										}
										else
										{
											SQLConnection sql = plugin.datahandler.sqlConnection;
											Connection c = sql.connection;

											try
											{
												PreparedStatement statement = c.prepareStatement("SELECT uuid FROM player_data WHERE uuid=? AND guild=?");
												statement.setString(1, uuid.toString());
												statement.setString(2, csplayer.guild);

												ResultSet result = statement.executeQuery();
												if(result.next())
												{
													PreparedStatement s = c.prepareStatement("UPDATE player_data SET guild=?, guild_score=?, guild_kills=?, guild_deaths=?, guild_assists=?, guild_caps=?, guild_supps=?, guild_mvps=?, guild_streak=? WHERE uuid=?");
													s.setString(1, "");
													s.setInt(2, 0);
													s.setInt(3, 0);
													s.setInt(4, 0);
													s.setInt(5, 0);
													s.setInt(6, 0);
													s.setInt(7, 0);
													s.setInt(8, 0);
													s.setInt(9, 0);
													s.setString(10, uuid.toString());

													s.execute();
													s.close();
													statement.close();
													result.close();
												}
												else
												{
													p.sendMessage(ChatColor.RED + "The player " + args[1] + " is not in the guild!");
													statement.close();
													result.close();
													return false;
												}
											}
											catch(SQLException e)
											{
												p.sendMessage(ChatColor.RED + "Please specify a valid player to kick from the guild!");
											}
										}
										p.sendMessage(ChatColor.GREEN + "You kicked " + args[1] + " from the guild!");
									}
								}
							}
							else
							{
								p.sendMessage(ChatColor.RED + "Please specify a player to kick from the guild!");
							}
						}
						else
						{
							p.sendMessage(ChatColor.RED + "You do not have permission to kick this player from the guild!");
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("exit"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						if(guild.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString()))
						{
							p.sendMessage(ChatColor.RED + "You can not leave your own guild!");
							p.sendMessage(ChatColor.RED + "Use " + ChatColor.ITALIC + "/guild disband" + ChatColor.RESET + ChatColor.GREEN.toString() + " to disband your guild.");
						}
						else
						{
							if(plugin.datahandler.gamemodehandler.inGuildRoom(p.getLocation()))
							{
								p.teleport(plugin.datahandler.gamemodehandler.getTeam(p).getSpawn());
							}
							csplayer.guild = "";
							csplayer.guildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
							csplayer.monthGuildStats = new CSStats(0, 0, 0, 0, 0, 0, 0);
							String uuid = p.getUniqueId().toString();
							guild.officers.remove(uuid);
							guild.members.remove(uuid);
							guild.initiates.remove(uuid);
							p.sendMessage(ChatColor.GREEN + "You left the guild " + csplayer.guild + "!");
						}
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("info"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						p.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Guild Info:");
						p.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.DARK_GRAY + csplayer.guild);
						p.sendMessage(ChatColor.GRAY + "Members: " + ChatColor.DARK_GRAY + guild.getMemberCount());
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("level") || args[0].equalsIgnoreCase("lvl") || args[0].equalsIgnoreCase("exp") || args[0].equalsIgnoreCase("currentlevel") || args[0].equalsIgnoreCase("currentexp") || args[0].equalsIgnoreCase("nextlevel") || args[0].equalsIgnoreCase("nextlvl") || args[0].equalsIgnoreCase("experience"))
				{
					if(csplayer.guild == null || csplayer.guild.equalsIgnoreCase(""))
					{
						p.sendMessage(ChatColor.RED + "You are not in a guild!");
					}
					else
					{
						Guild guild = plugin.datahandler.utils.getGuild(csplayer.guild);
						p.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Guild Level Info:");
						p.sendMessage(ChatColor.GRAY + "Level: " + ChatColor.DARK_GRAY + plugin.datahandler.utils.getLevel(guild.guildStats.getTotalScore(), 3));
						p.sendMessage(ChatColor.GRAY + "Exp: " + ChatColor.DARK_GRAY + guild.guildStats.getTotalScore() + " / " + plugin.datahandler.utils.getMaxExp(guild.guildStats.getTotalScore(), 3));
						String exp = "";
						for(int i = 0; i < 30; i++)
						{
							if((double) guild.guildStats.getTotalScore() / (double) plugin.datahandler.utils.getMaxExp(guild.guildStats.getTotalScore(), 3) >= (double) (1d / 30d) * i)
							{
								exp += ChatColor.DARK_GRAY + "|";
							}
							else
							{
								exp += ChatColor.GRAY + "|";
							}
						}
						p.sendMessage(ChatColor.GRAY + "Progress: " + exp);
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("help"))
				{
					int page = 1;
					if(args.length > 1)
					{
						if(plugin.datahandler.utils.isInteger(args[1]))
						{
							int pa = Integer.parseInt(args[1]);
							if(pa > 0)
							{
								page = pa;
							}
						}
					}
					sendHelp(p, page);
					return false;
				}
				p.sendMessage(ChatColor.RED + "Invalid arguments!");
				p.sendMessage(ChatColor.RED + "Use /guild help for command usage!");
			}
			else
			{
				sendHelp(p, 1);
			}
			return true;
		}
		return false;
	}

	private void sendHelp(Player p, int page)
	{
		int size = commands.size();
		if(size > 0)
		{
			int totalpages = 1;
			if(size > 10)
			{
				totalpages = (int) Math.ceil((double) size / 10.0);
			}
			if(totalpages >= page)
			{
				p.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Guild Help: Page " + ChatColor.RESET + ChatColor.DARK_AQUA + page + "/" + totalpages);

				int sr = 1;
				if(page > 1)
				{
					sr = (page * 10) - 9;
				}
				int er = sr + 9;
				if(size < er)
				{
					er = size;
				}
				for(int i = sr; i <= er; i++)
				{
					if(commands.keySet().toArray().length > i)
					{
						String label = (String) commands.keySet().toArray()[i];
						p.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + i + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "/" + label + ChatColor.DARK_GRAY + " - " + commands.get(label));
					}
				}
			}
			else
			{
				p.sendMessage(ChatColor.RED + "That page of guild commands does not exist.");
			}
		}
		else
		{
			p.sendMessage(ChatColor.RED + "No guild commands were found.");
		}
	}
}
