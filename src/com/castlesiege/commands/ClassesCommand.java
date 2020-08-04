package com.castlesiege.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.castlesiege.Main;
import com.castlesiege.classes.CSClasses;
import com.castlesiege.player.CSPlayer;

public class ClassesCommand implements CommandExecutor
{
	private Main plugin;

	public ClassesCommand(Main plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("classes") || label.equalsIgnoreCase("class") || label.equalsIgnoreCase("c") || label.equalsIgnoreCase("kit"))
		{
			if(!(sender instanceof Player))
				return true;
			Player p = (Player) sender;
			if(args.length == 0)
			{
				p.sendMessage(ChatColor.GRAY + "Classes: " + ChatColor.DARK_GRAY + "Spearman, Skirmisher, Knight, Swordsman, Archer, Vanguard, Maceman, Axeman, Crossbowman, SiegeArcher, Cavalry, RangedCavalry, Lancer, Bannerman, Medic, Halberdier, Pikeman");
			}
			else if(args.length == 1)
			{
				if(!plugin.datahandler.gamemodehandler.inSpawnRoom(p.getLocation()) && !plugin.datahandler.gamemodehandler.inGuildRoom(p.getLocation()))
				{
					p.sendMessage(ChatColor.RED + "You can only change classes in your spawn room!");
					return false;
				}
				CSPlayer csplayer = plugin.datahandler.utils.getCSPlayer(p);
				if("Spearman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.SPEARMAN);
				}
				else if("Skirmisher".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.SKIRMISHER);
				}
				else if("Swordsman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.SWORDSMAN);
				}
				else if("Knight".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.KNIGHT);
				}
				else if("Archer".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.ARCHER);
				}
				else if("Vanguard".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.VANGUARD);
				}
				else if("Maceman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.MACEMAN);
				}
				else if("Axeman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.AXEMAN);
				}
				else if("Crossbowman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.CROSSBOWMAN);
				}
				else if("SiegeArcher".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.SIEGE_ARCHER);
				}
				else if("Cavalry".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.CAVALRY);
				}
				else if("RangedCavalry".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.RANGED_CAVALRY);
				}
				else if("Lancer".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.LANCER);
				}
				else if("Bannerman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.BANNERMAN);
				}
				else if("Halberdier".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.HALBERDIER);
				}
				else if("Pikeman".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.PIKEMAN);
				}
				else if("Medic".equalsIgnoreCase(args[0]))
				{
					csplayer.setClass(CSClasses.MEDIC);
				}
			}
		}
		return false;
	}
}
