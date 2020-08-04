package com.castlesiege.player;

import java.util.Date;
import java.util.UUID;

public class CSPunishRecord
{
	private Date punishDate, tempUnpunishDate, unpunishDate;
	private UUID punisherUUID, unpunisherUUID;
	private String reason;

	public CSPunishRecord(String serializedString)
	{
		String[] parts = serializedString.split(";");
		if(parts[0] != null && !parts[0].equals(""))
		{
			punisherUUID = UUID.fromString(parts[0]);
		}
		if(punisherUUID != null)
		{
			reason = parts[1];
			long date;
			try
			{
				date = Long.parseLong(parts[2]);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return;
			}
			punishDate = new Date(date);

			if(parts.length < 4)
				return;
			if(parts[3] != null && !parts[3].equals(""))
			{
				try
				{
					date = Long.parseLong(parts[3]);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					return;
				}
				tempUnpunishDate = new Date(date);
			}

			if(parts.length < 5)
				return;
			if(parts[4] != null && !parts[4].equals(""))
			{
				try
				{
					date = Long.parseLong(parts[4]);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					return;
				}
				unpunishDate = new Date(date);
			}

			if(parts.length < 6)
				return;
			if(parts[5] != null && !parts[5].equals(""))
			{
				unpunisherUUID = UUID.fromString(parts[5]);
			}
		}
	}

	public CSPunishRecord(UUID bannerUUID, Date banDate, String reason)
	{
		this.punishDate = banDate;
		this.punisherUUID = bannerUUID;
		this.reason = reason.replaceAll(";", "").replaceAll("=", "");
	}

	public CSPunishRecord(UUID bannerUUID, Date banDate, String reason, Date tempUnbanDate)
	{
		this.punishDate = banDate;
		this.tempUnpunishDate = tempUnbanDate;
		this.punisherUUID = bannerUUID;
		this.reason = reason.replaceAll(";", "").replaceAll("=", "");
	}

	public String serialize()
	{
		StringBuilder sb = new StringBuilder();
		if(punisherUUID != null)
		{
			sb.append(punisherUUID.toString());
		}
		sb.append(";" + reason + ";");
		sb.append(punishDate.getTime() + ";");
		if(tempUnpunishDate != null)
		{
			sb.append(tempUnpunishDate.getTime());
		}
		sb.append(";");
		if(unpunishDate != null)
		{
			sb.append(unpunishDate.getTime());
		}
		sb.append(";");
		if(unpunisherUUID != null)
		{
			sb.append(unpunisherUUID.toString());
		}
		return sb.toString();
	}

	public UUID getPunisherUUID()
	{
		return punisherUUID;
	}

	public Date getPunishDate()
	{
		return punishDate;
	}

	public String getReason()
	{
		return reason;
	}

	public Date getTempUnpunishDate()
	{
		return tempUnpunishDate;
	}

	public Date getUnpunishDate()
	{
		return unpunishDate;
	}

	public void setUnpunishDate(Date unpunishDate)
	{
		this.unpunishDate = unpunishDate;
	}

	public UUID getUnpunisherUUID()
	{
		return unpunisherUUID;
	}

	public void setUnpunisherUUID(UUID unpunisherUUID)
	{
		this.unpunisherUUID = unpunisherUUID;
	}
}