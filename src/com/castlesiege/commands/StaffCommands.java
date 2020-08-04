package com.castlesiege.commands;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.player.CSPlayer;

public class StaffCommands implements CommandExecutor
{
	private Main plugin;
	
	public StaffCommands(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("mute")) {
			
		} else if(cmd.getName().equalsIgnoreCase("unmute")) {
			
		} else if(cmd.getName().equalsIgnoreCase("mods")) {
			ArrayList<String> admins = new ArrayList<String>(), devs = new ArrayList<String>(), mods = new ArrayList<String>(), chatMods = new ArrayList<String>();
			if(!Bukkit.getOnlinePlayers().isEmpty()) {
				for(Player p : Bukkit.getServer().getOnlinePlayers()) {
					CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
					switch(csplayer.getRank()) {
					case ADMIN:
						admins.add(plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName());
						break;
					case DEV:
						devs.add(plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName());
						break;
					case MOD:
						mods.add(plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName());
						break;
					case CHATMOD:
						chatMods.add(plugin.datahandler.gamemodehandler.getTeam(p).getChatColor() + p.getName());
						break;
					default:
						break;
					}
				}
			}
			Collections.sort(admins, String.CASE_INSENSITIVE_ORDER);
			Collections.sort(devs, String.CASE_INSENSITIVE_ORDER);
			Collections.sort(mods, String.CASE_INSENSITIVE_ORDER);
			Collections.sort(chatMods, String.CASE_INSENSITIVE_ORDER);
			
			sender.sendMessage("-----------------------------------------------");
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Administrators " + ChatColor.RESET + "" + ChatColor.RED + "online:");
			StringBuilder sb = new StringBuilder("- " + (!admins.isEmpty() ? "" : ChatColor.GRAY + "none"));
			if(!admins.isEmpty()) {
				for(String name : admins) {
					sb.append(name + ChatColor.RESET + ", ");
				}	
			}
			sender.sendMessage(admins.isEmpty() ? sb.toString() : sb.substring(0, sb.toString().length() - 2));
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Developers " + ChatColor.RESET + "" + ChatColor.RED + "online:");
			sb = new StringBuilder("- " + (!devs.isEmpty() ? "" : ChatColor.GRAY + "none"));
			if(!devs.isEmpty()) {
				for(String name : devs) {
					sb.append(name + ChatColor.RESET + ", ");
				}	
			}
			sender.sendMessage(devs.isEmpty() ? sb.toString() : sb.substring(0, sb.toString().length() - 2));
			sender.sendMessage(ChatColor.BLUE  + "" + ChatColor.BOLD + "Moderators " + ChatColor.RESET + "" + ChatColor.BLUE + "online:");
			sb = new StringBuilder("- " + (!mods.isEmpty() ? "" : ChatColor.GRAY + "none"));
			if(!mods.isEmpty()) {
				for(String name : mods) {
					sb.append(name + ChatColor.RESET + ", ");
				}	
			}
			sender.sendMessage(mods.isEmpty() ? sb.toString() : sb.substring(0, sb.toString().length() - 2));
			sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chat Moderators " + ChatColor.RESET + "" + ChatColor.GREEN + "online:");
			sb = new StringBuilder("- " + (!chatMods.isEmpty() ? "" : ChatColor.GRAY + "none"));
			if(!chatMods.isEmpty()) {
				for(String name : chatMods) {
					sb.append(name + ChatColor.RESET + ", ");
				}	
			}
			sender.sendMessage(chatMods.isEmpty() ? sb.toString() : sb.substring(0, sb.toString().length() - 2));
			sender.sendMessage("-----------------------------------------------");
		}
		return false;
	}
}
