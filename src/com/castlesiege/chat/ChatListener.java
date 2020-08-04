package com.castlesiege.chat;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.castlesiege.Main;
import com.castlesiege.player.CSDonorRank;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;
import com.castlesiege.player.ScoreType;

public class ChatListener implements Listener, CommandExecutor
{

	private Main plugin;

	public ChatListener(Main plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	private void onAsyncPlayerChat(AsyncPlayerChatEvent e)
	{
		Player p = e.getPlayer();
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		CSRank rank = csplayer.getRank();
		ChatColor chatColor = ChatColor.GRAY;

		if(csplayer.isMuted())
		{
			p.sendMessage(ChatColor.RED + "You are currently muted!");
			e.setCancelled(true);
			return;
		}

		e.setMessage(e.getMessage().replaceAll("%", "%%"));
		/*
		 * if(e.getMessage().equals(" ")) { e.setCancelled(true); }
		 */
		if(plugin.datahandler.gamemodehandler.getTeam(p) == null)
		{
			e.setCancelled(true);
			return;
		}
		else
		{
			if(rank.getID() > CSRank.MEDIA.getID())
			{
				chatColor = ChatColor.WHITE;
			}
			if(csplayer.king)
			{
				e.setFormat(ChatColor.RESET + CSDonorRank.KING.getTag() + plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + chatColor + ": ");
			}
			else
			{
				if(rank.getID() == CSRank.DEFAULT.getID())
				{
					e.setFormat(ChatColor.RESET + csplayer.getDonorRank().getTag() + plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + chatColor + ": ");
				}
				else
				{
					e.setFormat(ChatColor.RESET + rank.getTag() + plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName() + chatColor + ": ");
				}
			}
		}

		String message = chatColor + e.getMessage();
		ChatColor levelColor = plugin.datahandler.utils.getLevelColor(csplayer.classStats.getTotalScore(), 3);

		switch(csplayer.channel)
		{
		default:
		case GLOBAL:
			e.setCancelled(true);
			e.setFormat(e.getFormat() + message);
			for(Player target : Bukkit.getOnlinePlayers())
			{
				CSPlayer t = plugin.datahandler.utils.getCSPlayer(target);
				String str = t.asterisk ? "* " : plugin.datahandler.utils.getLevel(csplayer.classStats.getTotalScore(), 3) + " ";
				target.sendMessage(levelColor + str + e.getFormat());
			}
			if(plugin.datahandler.restarting)
			{
				if(e.getMessage().equalsIgnoreCase("gg") && !csplayer.hasGG)
				{
					csplayer.giveScore(ScoreType.SUPPS, 1, false, true, true);
					csplayer.hasGG = true;
					p.sendMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.YELLOW + "+1 support point for being sportsmanlike!");
				}
			}
			break;
		case TEAM:
			e.setCancelled(true);
			e.setFormat(e.getFormat() + ChatColor.DARK_AQUA + "TEAM: " + message);
			for(Player target : Bukkit.getOnlinePlayers())
			{
				CSPlayer csp = plugin.datahandler.utils.getCSPlayer(target);
				if((csp.getRank().getID() >= CSRank.CHATMOD.getID() && csp.teamchatspy) || plugin.datahandler.gamemodehandler.getTeam(p) == plugin.datahandler.gamemodehandler.getTeam(target))
				{
					CSPlayer t = plugin.datahandler.utils.getCSPlayer(target);
					String str = t.asterisk ? "* " : plugin.datahandler.utils.getLevel(csplayer.classStats.getTotalScore(), 3) + " ";
					target.sendMessage(levelColor + str + e.getFormat());
				}
			}
			break;
		case STAFF:
			e.setCancelled(true);
			e.setFormat(e.getFormat() + ChatColor.GREEN + "STAFF: " + message);
			for(Player target : Bukkit.getOnlinePlayers())
			{
				if(plugin.datahandler.utils.getCSPlayer(target).getRank().getID() >= CSRank.CHATMOD.getID())
				{
					CSPlayer t = plugin.datahandler.utils.getCSPlayer(target);
					String str = t.asterisk ? "* " : plugin.datahandler.utils.getLevel(csplayer.classStats.getTotalScore(), 3) + " ";
					target.sendMessage(levelColor + str + e.getFormat());
				}
			}
			break;
		case MOD:
			e.setCancelled(true);
			e.setFormat(e.getFormat() + ChatColor.BLUE + "STAFF: " + message);
			for(Player target : Bukkit.getOnlinePlayers())
			{
				if(plugin.datahandler.utils.getCSPlayer(target).getRank().getID() >= CSRank.MOD.getID())
				{
					CSPlayer t = plugin.datahandler.utils.getCSPlayer(target);
					String str = t.asterisk ? "* " : plugin.datahandler.utils.getLevel(csplayer.classStats.getTotalScore(), 3) + " ";
					target.sendMessage(levelColor + str + e.getFormat());
				}
			}
			break;
		case ADMIN:
			e.setCancelled(true);
			e.setFormat(e.getFormat() + ChatColor.RED + "STAFF: " + message);
			for(Player target : Bukkit.getOnlinePlayers())
			{
				if(plugin.datahandler.utils.getCSPlayer(target).getRank().getID() >= CSRank.ADMIN.getID())
				{
					CSPlayer t = plugin.datahandler.utils.getCSPlayer(target);
					String str = t.asterisk ? "* " : plugin.datahandler.utils.getLevel(csplayer.classStats.getTotalScore(), 3) + " ";
					target.sendMessage(levelColor + str + e.getFormat());
				}
			}
			break;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			return true;
		}
		Player p = (Player) sender;

		if(cmd.getName().equalsIgnoreCase("t"))
		{
			checkChat(p, args, ChatChannel.TEAM);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("g"))
		{
			checkChat(p, args, ChatChannel.GLOBAL);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("s1"))
		{
			checkChat(p, args, ChatChannel.STAFF);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("s2"))
		{
			checkChat(p, args, ChatChannel.MOD);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("s3"))
		{
			checkChat(p, args, ChatChannel.ADMIN);
			return true;
		}
		return false;
	}

	private void checkChat(Player p, String[] args, ChatChannel channel)
	{
		CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
		ChatChannel oldChannel = csplayer.channel;
		csplayer.channel = channel;
		String message = "";
		if(args.length != 0)
		{
			for(String arg : args)
			{
				message += arg + " ";
			}
		}
		if(args.length > 0)
		{
			p.chat(message);
			csplayer.channel = oldChannel;
		}
		else
		{
			if(oldChannel != csplayer.channel)
			{
				p.sendMessage(ChatColor.DARK_AQUA + "You are now talking in " + WordUtils.capitalize(csplayer.channel.toString().toLowerCase()) + " chat!");
			}
		}
	}

	@EventHandler
	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
	{
		Player p = e.getPlayer();
		String[] message = e.getMessage().split(" ");
		String command = message[0].replace("/", "");
		if(command.equalsIgnoreCase("bukkit:pl") || command.equalsIgnoreCase("bukkit:plugins") || command.equalsIgnoreCase("plugins") || command.equalsIgnoreCase("pl"))
		{
			p.sendMessage("Unknown command. Type \"/help\" for help.");
			e.setCancelled(true);
		}
	}
}