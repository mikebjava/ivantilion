package com.sharpc.ivantilion;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.staticaccess.VanishNoPacket;

import com.massivecraft.factions.Factions;
import com.sharpc.ivantilion.listeners.pvp.CombatListener;
import com.sharpc.ivantilion.timer.PvPTimer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class IVAntilionPlugin extends JavaPlugin
{

	public static final String flavoredPrefix = ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + "IVAntilion" + ChatColor.DARK_RED + "] " + ChatColor.BLUE;
	public static final String unflavoredPrefix = "[IVAntilion] ";
	public static IVAntilionPlugin instance;
	public static WorldGuardPlugin worldguard;
	public static VanishPlugin vanish;
	public static Factions factions;
	public static PvPTimer pvptimer;

	@Override
	public void onEnable()
	{
		instance = this;
		loadConfig();
		PluginManager manager = this.getServer().getPluginManager();
		worldguard = (WorldGuardPlugin) manager.getPlugin("WorldGuard");
		factions = (Factions) manager.getPlugin("Factions");
		vanish = (VanishPlugin) manager.getPlugin("VanishNoPacket");

		if (worldguard == null)
		{
			System.out.println(unflavoredPrefix + "WorldGuard was not found, hooks will not be applied.");
		}

		if (factions == null)
		{
			System.out.println(unflavoredPrefix + "Factions was not found, hooks will not be applied.");
		}

		if (vanish == null)
		{
			System.out.println(unflavoredPrefix + "VanishNoPacket was not found, hooks will not be applied.");
		}

		pvptimer = new PvPTimer();
		pvptimer.start();

		manager.registerEvents(new CombatListener(), this);
	}

	@Override
	public void onDisable()
	{
		pvptimer.stop();
	}

	public void loadConfig()
	{
		this.getConfig().addDefault("tagDuration", 5000);
		this.getConfig().addDefault("tagPVP", true);
		this.getConfig().addDefault("tagPVE", false);
		this.getConfig().addDefault("hostileMobsOnly", true);
		this.getConfig().addDefault("tagGamemode", true);
		this.getConfig().addDefault("messagePrefix", "&4[&2IVAntilion&4]&9");
		this.getConfig().addDefault("messagePvPTagged", "&9 You're now in combat. Logging out will result in death.");
		this.getConfig().addDefault("messagePvPUntagged", "&9 You're no longer in combat.");
		this.getConfig().addDefault("broadcastPvPLogging", true);
		this.getConfig().addDefault("broadcast", "&9 %player tried to PvP Logged and was killed!");
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public static Object getMetadata(Metadatable object, String key)
	{
		List<MetadataValue> values = object.getMetadata(key);
		for (MetadataValue value : values)
		{
			if (instance != null)
			{
				if (value.getOwningPlugin() == instance)
				{
					return value.value();
				}
			}
		}
		return null;
	}

	public static String replaceColors(String message)
	{
		return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");
	}

	public static void broadcastPvPLog(Player playerThatLeft)
	{
		instance.getServer().broadcastMessage(flavoredPrefix + replaceColors(instance.getConfig().getString("broadcast").replaceAll("%player", playerThatLeft.getName())));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = null;
		if (sender instanceof Player)
		{
			player = (Player) sender;

			if (label.equalsIgnoreCase("ivantilion"))
			{
				if (player.isOp())
				{
					if (args.length == 0)
					{
						player.sendMessage(flavoredPrefix + " v" + this.getDescription().getVersion());
						player.sendMessage(ChatColor.BLUE + "/ivantilion reload");
					} else
					{
						if (args.length == 1)
						{
							if (args[0].equalsIgnoreCase("reload"))
							{
								player.sendMessage(flavoredPrefix + "Plugin reloaded.");
								reloadConfig();
							}
						}
					}
				}
			}
		}

		return false;
	}
}
