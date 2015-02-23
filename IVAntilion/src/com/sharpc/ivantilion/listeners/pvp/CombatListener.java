package com.sharpc.ivantilion.listeners.pvp;

import java.util.Map;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayer;
import com.sharpc.ivantilion.IVAntilionPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CombatListener implements Listener
{
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		Entity attacker = event.getDamager();
		Entity victim = event.getEntity();
		Player defendingPlayer = null;
		Player attackingPlayer = null;

		if (attacker instanceof Player)
		{
			attackingPlayer = (Player) attacker;
		}

		if (victim instanceof Player)
		{
			defendingPlayer = (Player) victim;
		}

		if (IVAntilionPlugin.vanish != null)
		{
			if (attackingPlayer != null && IVAntilionPlugin.vanish.getManager().isVanished(attackingPlayer))
			{
				return;
			}

			if (defendingPlayer != null && IVAntilionPlugin.vanish.getManager().isVanished(defendingPlayer))
			{
				return;
			}
		}

		// Only check this block if PVE is enabled, as PVE is the only focus of this block.
		if (IVAntilionPlugin.instance.getConfig().getBoolean("tagPVE"))
		{
			if (!this.isPlayerInInvincibleRegion(attackingPlayer))
			{
				// If the attacker is a player and the defender is a mob.
				if (attackingPlayer != null && defendingPlayer == null)
				{
					if (victim instanceof Monster || victim instanceof Wolf)
					{
						putPlayerInCombat(attackingPlayer);
					} else if (victim instanceof Animals)
					{
						if (!IVAntilionPlugin.instance.getConfig().getBoolean("hostileMobsOnly"))
						{
							putPlayerInCombat(attackingPlayer);
						}
					}
				}

				// If the attacker is a mob and the defender is a player.
				if (attackingPlayer == null && defendingPlayer != null)
				{
					if (attacker instanceof Monster)
					{
						putPlayerInCombat(attackingPlayer);
					} else if (attacker instanceof Animals)
					{
						if (attacker instanceof Wolf)
						{
							Wolf wolf = (Wolf) attacker;
							if (!IVAntilionPlugin.instance.getConfig().getBoolean("hostileMobsOnly") && wolf.isAngry())
							{
								putPlayerInCombat(defendingPlayer);
							}
						}
					}
				}
			}

		}

		// Only check this block if PVP is enabled, as PVP is the only focus of this block.
		if (IVAntilionPlugin.instance.getConfig().getBoolean("tagPVP"))
		{
			if (attackingPlayer != null && defendingPlayer != null)
			{
				if (this.canFactionAttack(attackingPlayer, defendingPlayer))
				{
					if (!this.isPlayerInAntiPVPRegion(attackingPlayer) && !this.isPlayerInAntiPVPRegion(defendingPlayer))
					{
						if (attackingPlayer != null && defendingPlayer != null)
						{
							putPlayerInCombat(attackingPlayer);
							putPlayerInCombat(defendingPlayer);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		if (!player.isDead())
		{
			Object value = IVAntilionPlugin.getMetadata(player, "combattag");

			if (value instanceof Boolean)
			{
				boolean combat = (boolean) value;

				if (combat == true)
				{
					if (IVAntilionPlugin.instance != null && IVAntilionPlugin.instance.getConfig().getBoolean("broadcastPvPLogging"))
					{
						IVAntilionPlugin.broadcastPvPLog(player);
					}
					player.setHealth(0.0);
					takePlayerOutOfCombat(player);
				}
			}

		}
	}

	public static void putPlayerInCombat(Player player)
	{
		if (IVAntilionPlugin.getMetadata(player, "combattag") != null)
		{
			if ((boolean) IVAntilionPlugin.getMetadata(player, "combattag") == false)
			{
				player.sendMessage(IVAntilionPlugin.replaceColors(IVAntilionPlugin.instance.getConfig().getString("messagePrefix") + IVAntilionPlugin.instance.getConfig().getString("messagePvPTagged")));
			}
		} else
		{
			player.sendMessage(IVAntilionPlugin.replaceColors(IVAntilionPlugin.instance.getConfig().getString("messagePrefix") + IVAntilionPlugin.instance.getConfig().getString("messagePvPTagged")));
		}

		player.setMetadata("combattag", new FixedMetadataValue(IVAntilionPlugin.instance, true));
		player.setMetadata("combattimeleft", new FixedMetadataValue(IVAntilionPlugin.instance, IVAntilionPlugin.instance.getConfig().getInt("tagDuration")));
	}

	public static void takePlayerOutOfCombat(Player player)
	{
		if (IVAntilionPlugin.getMetadata(player, "combattag") != null)
		{
			if ((boolean) IVAntilionPlugin.getMetadata(player, "combattag") == true)
			{
				player.sendMessage(IVAntilionPlugin.replaceColors(IVAntilionPlugin.instance.getConfig().getString("messagePrefix") + IVAntilionPlugin.instance.getConfig().getString("messagePvPUntagged")));
			}
		} else
		{
			player.sendMessage(IVAntilionPlugin.replaceColors(IVAntilionPlugin.instance.getConfig().getString("messagePrefix") + IVAntilionPlugin.instance.getConfig().getString("messagePvPUntagged")));
		}

		player.setMetadata("combattag", new FixedMetadataValue(IVAntilionPlugin.instance, false));
		player.setMetadata("combattimeleft", new FixedMetadataValue(IVAntilionPlugin.instance, 0));
	}

	private boolean isPlayerInAntiPVPRegion(Player player)
	{
		if (IVAntilionPlugin.worldguard != null)
		{
			for (Map.Entry<String, ProtectedRegion> regionEntry : IVAntilionPlugin.worldguard.getRegionManager(player.getWorld()).getRegions().entrySet())
			{
				if (regionEntry.getValue().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))
				{
					for (Map.Entry<Flag<?>, Object> flagEntry : regionEntry.getValue().getFlags().entrySet())
					{
						if (flagEntry.getKey().getName().equalsIgnoreCase("pvp"))
						{
							if (flagEntry.getValue().toString().equalsIgnoreCase("deny"))
							{
								return true;
							}
						} else if (flagEntry.getKey().getName().equalsIgnoreCase("invincible"))
						{
							if (flagEntry.getValue().toString().equalsIgnoreCase("allow"))
							{
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	private boolean isPlayerInInvincibleRegion(Player player)
	{
		if (IVAntilionPlugin.worldguard != null)
		{
			IVAntilionPlugin.worldguard.getRegionManager(player.getWorld()).getRegions();
			for (Map.Entry<String, ProtectedRegion> regionEntry : IVAntilionPlugin.worldguard.getRegionManager(player.getWorld()).getRegions().entrySet())
			{
				if (regionEntry.getValue().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))
				{
					for (Map.Entry<Flag<?>, Object> flagEntry : regionEntry.getValue().getFlags().entrySet())
					{
						if (flagEntry.getKey().getName().equalsIgnoreCase("invincible"))
						{
							if (flagEntry.getValue().toString().equalsIgnoreCase("allow"))
							{
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	private boolean canFactionAttack(Player attackingPlayer, Player defendingPlayer)
	{
		if (IVAntilionPlugin.factions != null)
		{
			MPlayer attackerM = MPlayer.get(attackingPlayer);
			MPlayer defendingM = MPlayer.get(defendingPlayer);
			Faction attackerFaction = attackerM.getFaction();
			Faction defendingFaction = defendingM.getFaction();

			if (attackerFaction != null && defendingFaction != null)
			{
				if (attackerFaction.getRelationTo(defendingFaction).isAtLeast(Rel.ALLY))
				{
					return false;
				}

				System.out.println(attackerFaction.getName());
				System.out.println(defendingFaction.getName());

				if (attackerFaction.getName().equalsIgnoreCase(defendingFaction.getName()))
				{
					return false;
				}

				for (Map.Entry<MFlag, Boolean> entry : attackerFaction.getFlags().entrySet())
				{
					if (entry.getKey().equals(MFlag.ID_PEACEFUL))
					{
						if (entry.getValue() == true)
						{
							return false;
						}
					}
				}

				for (Map.Entry<MFlag, Boolean> entry : defendingFaction.getFlags().entrySet())
				{
					if (entry.getKey().equals(MFlag.ID_PEACEFUL))
					{
						if (entry.getValue() == true)
						{
							return false;
						}
					}
				}
			}

		}

		return true;
	}

}
