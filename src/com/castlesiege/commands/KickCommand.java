package com.castlesiege.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;
import com.castlesiege.player.CSRank;

public class KickCommand implements CommandExecutor
{
	private Main plugin;
	
	public KickCommand(Main plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("kick"))
		{
			Player kicker = null;
			if(sender instanceof Player) {
				Player p = (Player) sender;
				CSPlayer csp = plugin.datahandler.utils.getCSPlayer(p);
				if(csp.getRank().getID() < CSRank.CHATMOD.getID()) {
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					return true;
				}
				kicker = p;
			}
			
			if(args.length < 1)
			{
				sender.sendMessage(ChatColor.RED + "/kick <player> [reason]");
				return true;
			}
			
			Player targetPlayer = null;
			for(Player p : Bukkit.getServer().getOnlinePlayers())
			{
				if(p.getName().toLowerCase().contains(args[0].toLowerCase()))
				{
					targetPlayer = p;
					break;
				}
			}
			if(targetPlayer == null)
			{
				sender.sendMessage(ChatColor.RED + "Player not found!");
				return true;
			}
			CSPlayer cstarget = plugin.datahandler.utils.getCSPlayer(targetPlayer);
			
			if(kicker != null && plugin.datahandler.utils.getCSPlayer(kicker).getRank().getID() < cstarget.getRank().getID())
			{
				sender.sendMessage(ChatColor.RED + "You cannot kick someone whose a higher rank then you!");
				return true;
			}
			
			String reason = "";
			if(args.length > 1) {
				for(int i = 1; i < args.length; i++) {
					reason += args[i] + " ";
				}
				reason = reason.substring(0, reason.length() - 1);
			}
			
			targetPlayer.kickPlayer("§8You were kicked by " + kicker.getName() + "!" + (!reason.equals("") ? "\n§8Reason: §e" + reason : ""));
		}
		return false;
	}
}