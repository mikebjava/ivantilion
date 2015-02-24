package com.sharpc.ivantilion.timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import com.sharpc.ivantilion.IVAntilionPlugin;
import com.sharpc.ivantilion.listeners.pvp.CombatListener;

@SuppressWarnings("deprecation")
public class PvPTimer extends BukkitRunnable implements ActionListener
{

	public static final int checkDelay = 250;
	public Timer timer;

	public PvPTimer()
	{
		this.timer = new Timer(checkDelay, this);
	}

	private void checkPlayers()
	{
		Player[] players = IVAntilionPlugin.instance.getServer().getOnlinePlayers();
		for (Player player : players)
		{
			if (IVAntilionPlugin.getMetadata(player, "combattag") != null)
			{
				if (Boolean.parseBoolean(IVAntilionPlugin.getMetadata(player, "combattag").toString()) == true)
				{
					if (IVAntilionPlugin.getMetadata(player, "combattimeleft") != null)
					{
						int timeleft = (int) IVAntilionPlugin.getMetadata(player, "combattimeleft");
						if (timeleft > 0)
						{
							player.setMetadata("combattimeleft", new FixedMetadataValue(IVAntilionPlugin.instance, (timeleft - checkDelay)));
						}

						if (timeleft <= 0)
						{
							CombatListener.takePlayerOutOfCombat(player);
						}
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource().equals(timer))
		{
			if (IVAntilionPlugin.instance != null)
			{
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				scheduler.scheduleSyncDelayedTask(IVAntilionPlugin.instance, new Runnable()
				{
					@Override
					public void run()
					{
						checkPlayers();
					}
				}, 20L);

			}
		}
	}

	public void stop()
	{
		if (timer != null && timer.isRunning())
		{
			timer.stop();
		}
	}

	public void start()
	{
		if (IVAntilionPlugin.instance != null)
		{
			this.runTask(IVAntilionPlugin.instance);
		}
	}

	@Override
	public void run()
	{
		if (timer != null)
		{
			timer.start();
		}
	}

}
