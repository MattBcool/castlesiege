package com.castlesiege.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class BroadcastHandler
{
	private final int TICKS = 12000;
	private final String[] MESSAGES = { ChatColor.YELLOW + "Remember to vote for in-game perks! /vote", ChatColor.YELLOW + "Check out the forums for games, updates, and friends! www.talesofwar.net" };
	
	private int ticks;
	private ArrayList<String> messages;
	
	public BroadcastHandler()
	{
		ticks = TICKS;
		messages = new ArrayList<String>();
		messages.addAll(Arrays.asList(MESSAGES));
	}
	
	public void tick()
	{
		ticks--;
		if(ticks <= 0)
		{
			ticks = TICKS;
			if(messages.isEmpty())
			{
				messages.addAll(Arrays.asList(MESSAGES));
			}
			String msg = messages.get(new Random().nextInt(messages.size()));
			messages.remove(msg);
			
			Bukkit.broadcastMessage(ChatColor.BLUE + "Tales of War > " + ChatColor.RESET + msg);
		}
	}
}
