package com.castlesiege;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.castlesiege.chat.ChatListener;
import com.castlesiege.commands.ADACommand;
import com.castlesiege.commands.BanCommand;
import com.castlesiege.commands.BanInfoCommand;
import com.castlesiege.commands.ChatCommand;
import com.castlesiege.commands.ClassesCommand;
import com.castlesiege.commands.DonorRankCommand;
import com.castlesiege.commands.DuelCommand;
import com.castlesiege.commands.FlyCommand;
import com.castlesiege.commands.ForumCommand;
import com.castlesiege.commands.GGCommand;
import com.castlesiege.commands.GuildCommand;
import com.castlesiege.commands.HelpCommand;
import com.castlesiege.commands.HudCommand;
import com.castlesiege.commands.IpCommand;
import com.castlesiege.commands.KDRCommand;
import com.castlesiege.commands.KTSCommand;
import com.castlesiege.commands.KickCommand;
import com.castlesiege.commands.LevelCommand;
import com.castlesiege.commands.MapCommand;
import com.castlesiege.commands.MapsCommand;
import com.castlesiege.commands.MeCommand;
import com.castlesiege.commands.MessageCommand;
import com.castlesiege.commands.MuteCommand;
import com.castlesiege.commands.MuteInfoCommand;
import com.castlesiege.commands.MvpCommand;
import com.castlesiege.commands.MvpsCommand;
import com.castlesiege.commands.RankCommand;
import com.castlesiege.commands.ResetStatsCommand;
import com.castlesiege.commands.ScoreCommand;
import com.castlesiege.commands.SetTimeCommand;
import com.castlesiege.commands.StaffCommands;
import com.castlesiege.commands.StatsCommand;
import com.castlesiege.commands.SuicideCommand;
import com.castlesiege.commands.SwitchCommand;
import com.castlesiege.commands.TempbanCommand;
import com.castlesiege.commands.TempmuteCommand;
import com.castlesiege.commands.TimeCommand;
import com.castlesiege.commands.TopCommand;
import com.castlesiege.commands.TopDonatorCommand;
import com.castlesiege.commands.TopGuildCommand;
import com.castlesiege.commands.TopGuildsCommand;
import com.castlesiege.commands.TopMonthCommand;
import com.castlesiege.commands.TopMonthGuildCommand;
import com.castlesiege.commands.TopMonthGuildsCommand;
import com.castlesiege.commands.TopTimeCommand;
import com.castlesiege.commands.UnbanCommand;
import com.castlesiege.commands.UnmuteCommand;
import com.castlesiege.commands.VoteCommand;
import com.castlesiege.commands.WebsiteCommand;
import com.castlesiege.data.DataHandler;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class Main extends JavaPlugin
{
	public DataHandler datahandler;

	ProtocolManager protocolManager;

	public void onEnable()
	{
		protocolManager = ProtocolLibrary.getProtocolManager();
		datahandler = new DataHandler(this);
		datahandler.doEnable();

		getCommand("chat").setExecutor(new ChatCommand(this));
		getCommand("class").setExecutor(new ClassesCommand(this));
		getCommand("setrank").setExecutor(new RankCommand(this));
		getCommand("setdonorrank").setExecutor(new DonorRankCommand(this));
		getCommand("map").setExecutor(new MapCommand(this));
		getCommand("maps").setExecutor(new MapsCommand(this));
		getCommand("suicide").setExecutor(new SuicideCommand(this));
		getCommand("kts").setExecutor(new KTSCommand(this));
		getCommand("sw").setExecutor(new SwitchCommand(this));
		getCommand("mods").setExecutor(new StaffCommands(this));
		getCommand("resetstats").setExecutor(new ResetStatsCommand(this));
		getCommand("ada").setExecutor(new ADACommand(this));
		getCommand("hud").setExecutor(new HudCommand(this));
		getCommand("mvp").setExecutor(new MvpCommand(this));
		getCommand("mvps").setExecutor(new MvpsCommand(this));
		getCommand("score").setExecutor(new ScoreCommand(this));
		getCommand("stats").setExecutor(new StatsCommand(this));
		getCommand("level").setExecutor(new LevelCommand(this));
		getCommand("time").setExecutor(new TimeCommand(this));
		getCommand("duel").setExecutor(new DuelCommand(this));
		getCommand("ip").setExecutor(new IpCommand(this));
		getCommand("vote").setExecutor(new VoteCommand(this));
		getCommand("settime").setExecutor(new SetTimeCommand(this));
		getCommand("guild").setExecutor(new GuildCommand(this));
		getCommand("website").setExecutor(new WebsiteCommand(this));
		getCommand("forum").setExecutor(new ForumCommand(this));
		getCommand("me").setExecutor(new MeCommand(this));
		getCommand("help").setExecutor(new HelpCommand(this));

		TopCommand top = new TopCommand(this);
		getCommand("toplist").setExecutor(top);
		getCommand("topkill").setExecutor(top);
		getCommand("topdeath").setExecutor(top);
		getCommand("topassist").setExecutor(top);
		getCommand("topcap").setExecutor(top);
		getCommand("topsup").setExecutor(top);
		getCommand("topmvp").setExecutor(top);
		getCommand("topstreak").setExecutor(top);
		getCommand("toptime").setExecutor(new TopTimeCommand(this));
		getCommand("topdonator").setExecutor(new TopDonatorCommand(this));

		TopMonthCommand topmonth = new TopMonthCommand(this);
		getCommand("topmonthlist").setExecutor(topmonth);
		getCommand("topmonthkill").setExecutor(topmonth);
		getCommand("topmonthdeath").setExecutor(topmonth);
		getCommand("topmonthassist").setExecutor(topmonth);
		getCommand("topmonthcap").setExecutor(topmonth);
		getCommand("topmonthsup").setExecutor(topmonth);
		getCommand("topmonthmvp").setExecutor(topmonth);
		getCommand("topmonthstreak").setExecutor(topmonth);

		TopGuildsCommand topguilds = new TopGuildsCommand(this);
		getCommand("topguildslist").setExecutor(topguilds);
		getCommand("topguildskill").setExecutor(topguilds);
		getCommand("topguildsdeath").setExecutor(topguilds);
		getCommand("topguildsassist").setExecutor(topguilds);
		getCommand("topguildscap").setExecutor(topguilds);
		getCommand("topguildssup").setExecutor(topguilds);
		getCommand("topguildsmvp").setExecutor(topguilds);
		getCommand("topguildsstreak").setExecutor(topguilds);

		TopGuildCommand topguild = new TopGuildCommand(this);
		getCommand("topguildlist").setExecutor(topguild);
		getCommand("topguildkill").setExecutor(topguild);
		getCommand("topguilddeath").setExecutor(topguild);
		getCommand("topguildassist").setExecutor(topguild);
		getCommand("topguildcap").setExecutor(topguild);
		getCommand("topguildsup").setExecutor(topguild);
		getCommand("topguildmvp").setExecutor(topguild);
		getCommand("topguildstreak").setExecutor(topguild);
		
		TopMonthGuildsCommand topmonthguilds = new TopMonthGuildsCommand(this);
		getCommand("topmonthguildslist").setExecutor(topmonthguilds);
		getCommand("topmonthguildskill").setExecutor(topmonthguilds);
		getCommand("topmonthguildsdeath").setExecutor(topmonthguilds);
		getCommand("topmonthguildsassist").setExecutor(topmonthguilds);
		getCommand("topmonthguildscap").setExecutor(topmonthguilds);
		getCommand("topmonthguildssup").setExecutor(topmonthguilds);
		getCommand("topmonthguildsmvp").setExecutor(topmonthguilds);
		getCommand("topmonthguildsstreak").setExecutor(topmonthguilds);

		TopMonthGuildCommand topmonthguild = new TopMonthGuildCommand(this);
		getCommand("topmonthguildlist").setExecutor(topmonthguild);
		getCommand("topmonthguildkill").setExecutor(topmonthguild);
		getCommand("topmonthguilddeath").setExecutor(topmonthguild);
		getCommand("topmonthguildassist").setExecutor(topmonthguild);
		getCommand("topmonthguildcap").setExecutor(topmonthguild);
		getCommand("topmonthguildsup").setExecutor(topmonthguild);
		getCommand("topmonthguildmvp").setExecutor(topmonthguild);
		getCommand("topmonthguildstreak").setExecutor(topmonthguild);

		MessageCommand messageCommand = new MessageCommand(this);
		getCommand("message").setExecutor(messageCommand);
		getCommand("reply").setExecutor(messageCommand);

		ChatListener chat = new ChatListener(this);
		getCommand("g").setExecutor(chat);
		getCommand("t").setExecutor(chat);
		getCommand("s1").setExecutor(chat);
		getCommand("s2").setExecutor(chat);
		getCommand("s3").setExecutor(chat);
		getServer().getPluginManager().registerEvents(chat, this);
		
		getCommand("ban").setExecutor(new BanCommand(this));
		getCommand("tempban").setExecutor(new TempbanCommand(this));
		getCommand("baninfo").setExecutor(new BanInfoCommand(this));

		getCommand("fly").setExecutor(new FlyCommand(this));
		getCommand("kdr").setExecutor(new KDRCommand(this));

		getCommand("unban").setExecutor(new UnbanCommand(this));
		getCommand("kick").setExecutor(new KickCommand(this));
		getCommand("mute").setExecutor(new MuteCommand(this));
		getCommand("unmute").setExecutor(new UnmuteCommand(this));
		getCommand("tempmute").setExecutor(new TempmuteCommand(this));
		getCommand("muteinfo").setExecutor(new MuteInfoCommand(this));
		
		GGCommand ggCommand = new GGCommand(this);
		getCommand("gg").setExecutor(ggCommand);
		getCommand("wp").setExecutor(ggCommand);
		getCommand("ggwp").setExecutor(ggCommand);

		serverTick();
	}

	public void onDisable()
	{
		datahandler.doDisable();
	}

	private void serverTick()
	{
		datahandler.serverTick.doTicks();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				serverTick();
			}
		}, 1L);
	}

	public boolean isInRect(Location loc, Location loc1, Location loc2)
	{
		double[] dim = new double[2];

		dim[0] = loc1.getX();
		dim[1] = loc2.getX();
		Arrays.sort(dim);
		if(loc.getX() > dim[1] || loc.getX() < dim[0])
			return false;

		dim[0] = loc1.getZ();
		dim[1] = loc2.getZ();
		Arrays.sort(dim);
		if(loc.getZ() > dim[1] || loc.getZ() < dim[0])
			return false;
		return true;
	}
}
