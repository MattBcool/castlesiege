package com.castlesiege.commands;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;

public class KDRCommand implements CommandExecutor
{
	private Main plugin;

	public KDRCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("kdr"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				double kdr = plugin.datahandler.utils.getCSPlayer(p).classStats.getTotalKDR();
				DecimalFormat df = new DecimalFormat("#.##");      
				kdr = Double.valueOf(df.format(kdr));
				p.sendMessage(ChatColor.DARK_AQUA + "Your KDR: " + ChatColor.GRAY + kdr);
			}
		}
		return false;
	}
}
