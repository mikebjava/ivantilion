package com.sharpc.ivantilion.listeners.pvp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class CombatListener implements Listener
{
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent Event)
	{
		Entity attacker = Event.getDamager();
		Entity victim = Event.getEntity();
		Player defendingPlayer = (Player) victim;
		Player attackingPlayer = (Player) attacker;
		if(attacker instanceof Player)
		{
			//TODO Set the player to in combat
		}
		
	}
}
