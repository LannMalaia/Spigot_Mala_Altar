package ma.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import ma.main.Mala_Altar;

public class Altar_Start_Effect implements Runnable
{
	World m_World;
	Location m_Loc;
	
	public Altar_Start_Effect(Location _loc)
	{
		m_World = _loc.getWorld();
		m_Loc = _loc;

		// m_World.playSound(m_Loc, Sound.BLOCK_BELL_USE, 2.0f, 1.5f);
		m_World.playSound(m_Loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
		m_World.playSound(m_Loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 0.5f);
		m_World.spawnParticle(Particle.END_ROD, m_Loc, 600, 0, 0, 0, 0.4);
	}
	
	double radius = 0.5;
	double remained_time = 1.0;
	public void run()
	{
		for (double angle = 0.0; angle <= 360.0; angle += 360.0 / (60.0 * radius))
		{
			double rad = Math.toRadians(angle);
			Location loc = new Location(m_World, Math.cos(rad) * radius, 0, Math.sin(rad) * radius);
			loc.add(m_Loc);
			m_World.spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
		}
		
		radius = radius + ((10.0 - radius) * 0.1);
		if (remained_time > 0.0)
		{
			remained_time -= 0.05;
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
		}
	}
}
