package com.sharpc.ivantilion;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Factions;
import com.sharpc.ivantilion.listeners.pvp.CombatListener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class IVAntilionPlugin extends JavaPlugin
{

	public static final String flavoredPrefix = ChatColor.DARK_RED + "[" + ChatColor.DARK_GREEN + "IVAntilion" + ChatColor.DARK_RED + "]" + ChatColor.BLUE;
	public static final String unflavoredPrefix = "[IVAntilion] ";

	public static WorldGuardPlugin worldguard;
	public static Factions factions;

	@Override
	public void onEnable()
	{

		PluginManager manager = this.getServer().getPluginManager();
		worldguard = (WorldGuardPlugin) manager.getPlugin("WorldGuard");
		factions = (Factions) manager.getPlugin("Factions");

		if (worldguard == null)
		{
			System.out.println(unflavoredPrefix + "WorldGuard was not found, hooks will not be applied.");
		}

		if (factions == null)
		{
			System.out.println(unflavoredPrefix + "Factions was not found, hooks will not be applied.");
		}

		manager.registerEvents(new CombatListener(), this);
	}

	@Override
	public void onDisable()
	{

	}

	public void loadConfig()
	{
		this.getConfig().addDefault("tagDuration", 5000);
		this.getConfig().addDefault("messagePrefix", "&4[&2IVAntilion&4]&9");
		this.getConfig().addDefault("messagePvPTagged", "&9 You're now in combat. Logging out will result in death.");
		this.getConfig().addDefault("messagePvPUntagged", "&9 You're no longer in combat.");
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}
}
