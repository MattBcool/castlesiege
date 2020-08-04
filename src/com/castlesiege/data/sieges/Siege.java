package com.castlesiege.data.sieges;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.castlesiege.Main;
import com.castlesiege.data.TeamData;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

@SuppressWarnings("deprecation")
public class Siege
{
	protected Location base;
	protected TeamData usable;

	public Siege(Location base, TeamData usable)
	{
		this.base = base;
		this.usable = usable;
	}

	public boolean use(Main plugin)
	{
		return false;
	}

	protected void pasteSchematic(String schematic)
	{
		WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		File file = new File("plugins" + File.separator + "WorldEdit" + File.separator + "schematics" + File.separator + schematic + ".schematic");
		EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(base.getWorld()), we.getWorldEdit().getConfiguration().maxChangeLimit);
		try
		{
			CuboidClipboard cc = MCEditSchematicFormat.getFormat(file).load(file);
			cc.rotate2D((int) base.getYaw());
			cc.paste(session, new Vector(base.getBlockX(), base.getBlockY(), base.getBlockZ()), false);
			return;
		}
		catch(MaxChangedBlocksException | com.sk89q.worldedit.data.DataException | IOException e2)
		{
			e2.printStackTrace();
		}
	}
}
