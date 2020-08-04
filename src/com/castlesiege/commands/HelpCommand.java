package com.castlesiege.commands;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;

import com.castlesiege.Main;

public class HelpCommand implements CommandExecutor
{
	private Main plugin;

	private HashMap<String, String> commands;

	public HelpCommand(Main plugin)
	{
		this.plugin = plugin;

		commands = new HashMap<String, String>();

		List<Command> cmdList = PluginCommandYamlParser.parse(plugin);
		for(int i = 0; i <= cmdList.size() - 1; i++)
		{
			Command command = cmdList.get(i);
			String description = command.getDescription();
			commands.put(command.getLabel(), description);
			for(String alias : command.getAliases())
			{
				commands.put(alias, description);
			}
		}
		commands = (HashMap<String, String>) plugin.datahandler.utils.sortByKeys(commands);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(label.equalsIgnoreCase("help"))
		{
			int page = 1;
			if(args.length > 0 && plugin.datahandler.utils.isInteger(args[0]))
			{
				int p = Integer.parseInt(args[0]);
				if(p > 0)
				{
					page = p;
				}
			}
			sendHometowns(sender, page);
		}
		return false;
	}

	private void sendHometowns(CommandSender s, int page)
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
				s.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Help: Page " + ChatColor.RESET + ChatColor.DARK_AQUA + page + "/" + totalpages);

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
						s.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + i + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + "/" + label);// + ChatColor.DARK_GRAY + " - " + commands.get(label)); TODO Reenable
					}
				}
			}
			else
			{
				s.sendMessage(ChatColor.RED + "That page of commands does not exist.");
			}
		}
		else
		{
			s.sendMessage(ChatColor.RED + "No commands were found.");
		}
	}
}
