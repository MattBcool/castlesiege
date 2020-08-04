package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.castlesiege.Main;

public class ForumCommand implements CommandExecutor
{
	private Main plugin;

	public ForumCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("forum") || cmd.getName().equalsIgnoreCase("forums") || cmd.getName().equalsIgnoreCase("board") || cmd.getName().equalsIgnoreCase("boards"))
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "Forum:");
			sender.sendMessage(ChatColor.GRAY + "http://www.talesofwar.net/forum");
			sender.sendMessage(ChatColor.DARK_GRAY + "Check out our forum to get frequent updates about server development, recent news, and more!");
			return true;
		}
		return false;
	}
}
