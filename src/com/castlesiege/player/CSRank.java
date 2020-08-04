package com.castlesiege.player;

import org.bukkit.ChatColor;

public enum CSRank
{
	DEV(5, ChatColor.RED + ChatColor.BOLD.toString() + "Dev ", new String[]
	{}), ADMIN(4, ChatColor.RED + ChatColor.BOLD.toString() + "Admin ", new String[]
	{"command.bypass"}), MOD(3, ChatColor.BLUE + ChatColor.BOLD.toString() + "Mod ", new String[]
	{}), CHATMOD(2, ChatColor.GREEN + ChatColor.BOLD.toString() + "ChatMod ", new String[]
	{}), MEDIA(1, ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Media ", new String[]
	{}), DEFAULT(0, ChatColor.RED + ChatColor.BOLD.toString() + "", new String[]
	{});

	private int ID;
	private String tag;
	private String[] permissions;

	CSRank(int ID, String tag, String[] permissions)
	{
		this.ID = ID;
		this.tag = tag;
		this.permissions = permissions;
	}

	public int getID()
	{
		return ID;
	}

	public String getTag()
	{
		return tag;
	}

	public String[] getPermissions()
	{
		return permissions;
	}

	public static CSRank getRank(int id)
	{
		for(CSRank rank : values())
		{
			if(rank.getID() == id)
			{
				return rank;
			}
		}
		return null;
	}

	public static CSRank getRank(String name)
	{
		for(CSRank rank : values())
		{
			if(rank.toString().equalsIgnoreCase(name))
			{
				return rank;
			}
		}
		return null;
	}
}