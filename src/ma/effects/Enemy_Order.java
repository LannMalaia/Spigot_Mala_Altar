package ma.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import ma.altar.Altar;
import ma.altar.Altar_Manager;
import ma.main.Mala_Altar;

public class Enemy_Order implements Runnable
{
	public boolean is_Mythicmob = false;
	public String m_MobName = "";
	Location m_Loc;
	public double m_WaitingTime;
	public boolean is_NoItemDrop = false;
	
	Entity m_Entity = null;
	ActiveMob m_MythicEntity = null;
	public boolean Spawned = false;
	double m_SpawnTime = 1.0;

	public Enemy_Order(boolean _mythic, String _name, Location _loc, double _waiting_time, boolean _is_NoItemDrop)
	{
		is_Mythicmob = _mythic;
		m_MobName = _name;
		m_Loc = _loc.clone().add(0.5, 2.0, 0.5);
		m_WaitingTime = _waiting_time;
		is_NoItemDrop = _is_NoItemDrop;
	}
	
	double radius = 4.0;
	public void run()
	{
		if (!Spawned)
		{
			// 몇 초 기다리기
			if (m_WaitingTime > 0.0)
			{
				m_WaitingTime -= 0.05;
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
				return;
			}
			
			// 다 기다렸으면 파티클 그리기
			for (double angle = 0.0; angle <= 360.0; angle += 45.0)//360.0 / (60.0 * radius))
			{
				double rad = Math.toRadians(angle);
				Location loc = m_Loc.clone().add(Math.cos(rad) * radius, -0.75, Math.sin(rad) * radius);
				m_Loc.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
			}
			// m_Loc.getWorld().playSound(m_Loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.5f);
			
			// 3초 세고 스폰함
			if (m_SpawnTime > 0)
			{
				m_SpawnTime -= 0.25;
				radius -= 0.25;
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 5);
			}
			else // 이 부분에서 스폰
			{
				Spawned = true;
				if (is_Mythicmob)
					Spawn_Mythic_Mob();
				else
					Spawn_Vanilla_Mob();
				m_Entity.setMetadata("malaAltar.monster", new FixedMetadataValue(Mala_Altar.plugin, true));
				if (is_NoItemDrop)
					m_Entity.setMetadata("malaAltar.noItemDrop", new FixedMetadataValue(Mala_Altar.plugin, true));
			}
		}
		
		return;
	}
	
	public void Spawn_Vanilla_Mob()
	{
		EntityType type = EntityType.valueOf(m_MobName);
		m_Entity = m_Loc.getWorld().spawnEntity(m_Loc, type);
		LivingEntity le = null;
		if (m_Entity instanceof LivingEntity)
		{
			le = (LivingEntity)m_Entity;
			if (le.getEquipment().getHelmet() == null)
				le.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		}
		Random rand = new Random();
		switch (type)
		{
		case DROWNED:
			if (rand.nextInt(4) == 1)
				le.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
			break;
		case SLIME:
		case MAGMA_CUBE:
			Slime slime = (Slime)m_Entity;
			slime.setSize(3);
			break;
		case VINDICATOR:
			le.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
			break;
		case PILLAGER:
			le.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
			break;
		case SKELETON:
		case STRAY:
			le.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
			break;
		case WITHER_SKELETON:
			le.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
			break;
		case ZOMBIFIED_PIGLIN:
			le.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
			break;
		case PIGLIN:
		case PIGLIN_BRUTE:
			if (rand.nextInt(2) == 1)
				le.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
			else
				le.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
			break;
		case VEX:
			le.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
			break;
		case RABBIT:
			Rabbit rabbit = (Rabbit)m_Entity;
			rabbit.setRabbitType(Type.THE_KILLER_BUNNY);
			break;
		}
		if (le instanceof Mob)
		{
			Altar altar = Altar_Manager.Instance.Get_Altar(this);
			if (altar == null)
				return;
			if (altar.m_Players.size() > 0)
				((Mob)le).setTarget(altar.m_Players.get(rand.nextInt(altar.m_Players.size())));
		}
	}
	
	public void Spawn_Mythic_Mob()
	{
		m_MythicEntity = MythicBukkit.inst().getMobManager().spawnMob(m_MobName, m_Loc);
		m_Entity = m_MythicEntity.getEntity().getBukkitEntity();
	}
	
	public Entity Get_Entity()
	{
		if (!Spawned)
			return null;
		
		if (is_Mythicmob)
		{
			if (m_MythicEntity == null)
				return null;
			if (m_MythicEntity.getEntity() == null)
				return null;
			return m_MythicEntity.getEntity().getBukkitEntity();
		}
		
		return m_Entity;
	}

	public void TP_Mob_to_Original_Location()
	{
		if (Spawned)
		{
			if (is_Mythicmob)
			{
				m_MythicEntity.getEntity().getBukkitEntity().teleport(m_Loc);
			}
			else
			{
				m_Entity.teleport(m_Loc);
			}
		}
	}
	public void Kill_Mob()
	{
		if (Spawned)
		{
			if (is_Mythicmob)
			{
				AbstractLocation _loc = m_MythicEntity.getLocation();
				m_Loc.getWorld().loadChunk(_loc.getBlockX(), _loc.getBlockZ());
				m_MythicEntity.getEntity().remove();
			}
			else
			{
				m_Loc = m_Entity.getLocation();
				m_Loc.getWorld().loadChunk(m_Loc.getBlockX(), m_Loc.getBlockZ());
				m_Entity.remove();
			}
		}
	}
	
	public boolean Check_Entity_Dead()
	{
		if (!Spawned) // 엔티티 죽음?
			return false;
		
		if (is_Mythicmob)
		{
			if (m_MythicEntity == null)
				return true;
			if (!m_MythicEntity.getEntity().getBukkitEntity().isValid())
				return true;
			if (m_MythicEntity.isDead())
				return true;
		}
		else
		{
			if (m_Entity == null)
				return true;
			if (m_Entity.isDead())
				return true;
		}
		
		return false;
	}
}
