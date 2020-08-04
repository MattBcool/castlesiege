package com.castlesiege.nametag;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NametagEvent extends Event implements Cancellable
{

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean cancelled;

	private String value;
	private String player;
	private ChangeType changeType;
	private ChangeReason changeReason;
	private StorageType storageType;

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getPlayer()
	{
		return player;
	}

	public void setPlayer(String player)
	{
		this.player = player;
	}

	public ChangeType getChangeType()
	{
		return changeType;
	}

	public void setChangeType(ChangeType changeType)
	{
		this.changeType = changeType;
	}

	public ChangeReason getChangeReason()
	{
		return changeReason;
	}

	public void setChangeReason(ChangeReason changeReason)
	{
		this.changeReason = changeReason;
	}

	public StorageType getStorageType()
	{
		return storageType;
	}

	public void setStorageType(StorageType storageType)
	{
		this.storageType = storageType;
	}

	public NametagEvent(String player, String value)
	{
		this(player, value, ChangeType.UNKNOWN);
	}

	public NametagEvent(String player, String value, ChangeType changeType)
	{
		this(player, value, changeType, StorageType.MEMORY, ChangeReason.UNKNOWN);
	}

	public NametagEvent(String player, String value, ChangeType changeType, ChangeReason changeReason)
	{
		this(player, value, changeType, StorageType.MEMORY, changeReason);
	}

	public NametagEvent(String player, String value, ChangeType changeType, StorageType storageType, ChangeReason changeReason)
	{
		this.player = player;
		this.value = value;
		this.changeType = changeType;
		this.storageType = storageType;
		this.changeReason = changeReason;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public enum ChangeReason
	{
		API, PLUGIN, UNKNOWN
	}

	public enum ChangeType
	{
		PREFIX, SUFFIX, GROUP, UNKNOWN
	}

	public enum StorageType
	{
		MEMORY, PERSISTENT
	}

}