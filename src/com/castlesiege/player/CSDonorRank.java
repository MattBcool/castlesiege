package com.castlesiege.player;

import org.bukkit.ChatColor;

public enum CSDonorRank
{
	KING(6, 0, new String[]
	{ ChatColor.GOLD + "*King* ", ChatColor.GOLD + "*Queen* " }, new String[]
	{}), PRINCE(5, 100, new String[]
	{ ChatColor.YELLOW + "Prince ", ChatColor.YELLOW + "Princess " }, new String[]
	{}), VICEROY(4, 80, new String[]
	{ ChatColor.LIGHT_PURPLE + "Viceroy ", ChatColor.LIGHT_PURPLE + "Viceroy" }, new String[]
	{}), DUKE(3, 50, new String[]
	{ ChatColor.DARK_PURPLE + "Duke ", ChatColor.DARK_PURPLE + "Duchess" }, new String[]
	{}), BARON(2, 20, new String[]
	{ ChatColor.DARK_GREEN + "Baron ", ChatColor.DARK_GREEN + "Baroness" }, new String[]
	{}), LORD(1, 10, new String[]
	{ ChatColor.DARK_AQUA + "Lord ", ChatColor.DARK_AQUA + "Lady" }, new String[]
	{}), DEFAULT(0, 0, new String[]
	{ ChatColor.RED + ChatColor.BOLD.toString() + "", ChatColor.RED + ChatColor.BOLD.toString() + "" }, new String[]
	{});

	private int ID, amount;
	private String[] tag;
	private String[] permissions;

	CSDonorRank(int ID, int amount, String[] tag, String[] permissions)
	{
		this.ID = ID;
		this.amount = amount;
		this.tag = tag;
		this.permissions = permissions;
	}

	public int getID()
	{
		return ID;
	}

	public String getTag()
	{
		return tag[0];
	}

	public String[] getPermissions()
	{
		return permissions;
	}

	public static CSDonorRank getRank(int id)
	{
		for(CSDonorRank rank : values())
		{
			if(rank.getID() == id)
			{
				return rank;
			}
		}
		return null;
	}

	public static CSDonorRank getRank(String name)
	{
		for(CSDonorRank rank : values())
		{
			if(rank.toString().equalsIgnoreCase(name))
			{
				return rank;
			}
		}
		return null;
	}

	public static CSDonorRank getDonorAmountRank(int amount)
	{
		CSDonorRank donorRank = CSDonorRank.DEFAULT;
		int last = 0;
		for(CSDonorRank rank : values())
		{
			if(rank != CSDonorRank.KING && rank.amount <= amount && rank.amount > last)
			{
				donorRank = rank;
				last = rank.amount;
			}
		}
		return donorRank;
	}
}