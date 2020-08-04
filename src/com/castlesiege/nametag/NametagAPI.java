package com.castlesiege.nametag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class NametagAPI
{
	private NametagManager manager;

	public NametagAPI(NametagManager manager)
	{
		this.manager = manager;
	}

	public void setPrefix(Player player, String prefix)
	{
		setPrefix(player.getName(), prefix);
	}

	public void setSuffix(Player player, String suffix)
	{
		setSuffix(player.getName(), suffix);
	}

	public void setPrefix(String player, String prefix)
	{
		NametagEvent event = new NametagEvent(player, prefix, NametagEvent.ChangeType.PREFIX, NametagEvent.ChangeReason.API);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			manager.updateNametag(player, prefix, null);
		}
	}

	public void setSuffix(String player, String suffix)
	{
		NametagEvent event = new NametagEvent(player, suffix, NametagEvent.ChangeType.SUFFIX, NametagEvent.ChangeReason.API);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled())
		{
			manager.updateNametag(player, null, suffix);
		}
	}

	public void setNametag(Player player, String prefix, String suffix)
	{
		manager.overlapNametag(player.getName(), prefix, suffix);
	}

	public void setNametag(String player, String prefix, String suffix)
	{
		manager.overlapNametag(player, prefix, suffix);
	}

}