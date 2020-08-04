package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.castlesiege.Main;

public class WebsiteCommand implements CommandExecutor
{
	private Main plugin;

	public WebsiteCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("website") || cmd.getName().equalsIgnoreCase("site"))
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "Website:");
			sender.sendMessage(ChatColor.GRAY + "http://www.talesofwar.net");
			sender.sendMessage(ChatColor.DARK_GRAY + "Check out our website to get frequent updates about server development, recent news, and more!");
			return true;
		}
		return false;
	}
}
