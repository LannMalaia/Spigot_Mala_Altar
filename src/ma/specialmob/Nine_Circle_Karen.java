package ma.specialmob;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.core.mobs.ActiveMob;
import laylia_core.main.TitleManager;
import ma.main.Mala_Altar;
import mala.mmoskill.skills.Lightning_Bolt;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.TRS;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;


// 1
// 구륜의 카렌 - 성전의 개시자
//  보스 공격 피격시 플레이어에게 고리가 생기고, 이후에는 보스에게 고리가 생김
//  고리가 겹쳐지면 마나 및 스태미나 증발하고 2의 고정 피해를 지속으로 입음
// 1. 사방에서 날아오는 고정 피해 광선
// 2. 원점에서 한바퀴 돌리면서 쏘는 죽음 광선
// 3. 모든 파티원 유도 새틀라이트 캐논
// 4. 죽었을 때 멀리 튕겨보내는 몬스터 소환
// 5. 패턴이 끝날 때마다 고리가 하나씩 생김 - 고리가 아홉 개 생기면 필살기 쓰고 파티 전멸


// 2
// 군단장 아스크라멘트 - 성전의 파괴자
//  군단장은 무적, 소환하는 몹을 잡아야만 체력을 떨어트릴 수 있음


// 3
// 사도 렉스 - 성검 찬탈자	
//  즉사기 휙휙 날리는 쌍넘
// 마도인형 시트린 - 성역의 배반자
//  한 놈만 패는 쌍년

// 4
// 아리에타 - 최후의 수호자
//  무적은 아닌데 되려 무적인 몬스터를 개많이 소환함

// 5
// 탄생의 알
//  피해량 자랑대회

// 구륜의 카렌
enum KAREN_STATE { BEGIN, PHASE_1, PHASE_2, ENDING }

public class Nine_Circle_Karen implements Runnable
{
	KAREN_STATE m_State = KAREN_STATE.BEGIN;
	ActiveMob m_Boss;
	Entity m_Boss_Entity;
	Location m_First_Loc;
	
	double m_HP_percentage = 0.0;
	int m_Teleport_Cooldown = 0;
	int m_ThunderShower_Cooldown = 0;
	boolean m_Phase_2 = false;
	
	public Nine_Circle_Karen(ActiveMob _mob)
	{
		m_Boss = _mob;
		m_Boss_Entity = m_Boss.getEntity().getBukkitEntity();
		m_First_Loc = m_Boss_Entity.getLocation();
		
		Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Intro_Effect(this));		
	}
	
	void Say(String _msg)
	{
		for (Entity e : m_Boss.getEntity().getBukkitEntity().getNearbyEntities(50, 50, 50))
			e.sendMessage(m_Boss.getDisplayName() + "§f: " + _msg);
	}
	void Title(String _title, String _subtitle)
	{
		for (Entity e : m_Boss.getEntity().getBukkitEntity().getNearbyEntities(80, 80, 80))
		{
			if (e instanceof Player)
			{
				Player player = (Player)e;
				player.sendTitle(_title, _subtitle, 0, 50, 20);
			}
		}
	}
	
	public void run()
	{
		switch(m_State)
		{
		case BEGIN:
			break;
		case PHASE_1:
			Cast_Spell();
			break;
		case PHASE_2:
			break;
		case ENDING:
			break;
		}
		
		if (m_State == KAREN_STATE.PHASE_2)
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 50);
		else
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 80);
	}
	
	void Cast_Spell()
	{
		int x = -30 + (int)(Math.random() * 1.0 > 0.5 ? 0 : 60);
		int z = -30 + (int)(Math.random() * 1.0 > 0.5 ? 0 : 60);
		Location start = m_First_Loc.clone().add(x, 30, z);
		for (int i = 0; i < 32; i++)
		{
			double dx = -18.0 + Math.random() * 36.0;
			double dz = -18.0 + Math.random() * 36.0;
			Location target = m_First_Loc.clone().add(dx, 0, dz);
			
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, new Death_Ray(start.clone(), target), i * 1);
		}
		
		
//		int random = (int)(Math.random() * 4.0);
//		switch (random)
//		{
//		case 0:
//			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Flare_Disc_Process(m_Boss_Entity));
//			break;
//		case 1:
//			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Process(m_Boss_Entity, 5));
//			break;
//		case 2:
//			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Ice_Spike_Process(m_Boss_Entity, 3));
//			break;
//		case 3:
//			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new EarthQuake_Process(m_Boss_Entity, 100));
//			break;
//		}
	}
	
	// 시작 이펙트
	class Intro_Effect implements Runnable
	{
		Nine_Circle_Karen boss;
		public Intro_Effect(Nine_Circle_Karen _boss)
		{
			boss = _boss;
			boss.m_Boss_Entity.setInvulnerable(true);
			boss.m_Boss_Entity.getWorld().playSound(boss.m_Boss_Entity.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 2.0f, 1.0f);
		}
		
		double timer = 4.0;
		double radius = 0.0;
		public void run()
		{
			if (timer > 0)
			{
				timer -= 0.05;
				radius = radius + (20.0 - radius) * 0.01;
				Particle_Drawer.Draw_Circle(boss.m_Boss_Entity.getLocation().add(0, 0.4, 0), Particle.ENCHANTMENT_TABLE, radius);
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
			}
			else
			{
				boss.m_Boss_Entity.getWorld().playSound(boss.m_Boss_Entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
				Particle_Drawer.Draw_Sphere(boss.m_Boss_Entity.getLocation(), Particle.EXPLOSION_LARGE, radius, 0, 0.5);
				
				for (Entity e : boss.m_Boss_Entity.getNearbyEntities(radius, radius, radius))
				{
					if (!(e instanceof Player))
						continue;
					if (e == boss)
						continue;
					Player p = (Player)e;
					PlayerData data = PlayerData.get(p);
					data.setStamina(0);
					data.setMana(0);
					p.damage(20, boss.m_Boss_Entity);
					p.playEffect(EntityEffect.HURT);
				}
				
				boss.m_Boss_Entity.setInvulnerable(false);
				boss.m_State = KAREN_STATE.PHASE_1;
				Title("§9§l아홉 고리의 카렌", "§7§l황혼의 시작점");
			}
		}
	}

	// 플레이어에게 링을 부여, 플레이어끼리 닿으면 피해를 줌
	static ArrayList<Player> ringed_players = new ArrayList<Player>();
	class Golden_Ring_Process implements Runnable
	{
		DustOptions dop = new DustOptions(Color.YELLOW, 1.0f);
		Player player;
		double radius = 2.5;
		double duration = 10.0;
		boolean cancelled = false;
		
		public Golden_Ring_Process(Player _player, double _radius)
		{
			player = _player;
			radius = _radius;
			if (ringed_players.contains(_player))
				cancelled = true;
			ringed_players.add(_player);
		}
		
		public void run()
		{
			if (cancelled)
				return;
			
			Particle_Drawer.Draw_Circle(player.getLocation(), dop, radius);
			for (Entity e : player.getNearbyEntities(radius, radius, radius))
			{
				if (!(e instanceof Player))
					continue;
				if (e == player)
					continue;
				Player p = (Player)e;
				player.getWorld().playSound(p.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 0.6f, 1.5f);
				PlayerData data = PlayerData.get(p);
				data.giveStamina(-20, UpdateReason.OTHER);
				data.giveMana(-20, UpdateReason.OTHER);
				p.setHealth(Math.max(0, p.getHealth() - 3));
				p.playEffect(EntityEffect.HURT);
			}
			
			if (duration > 0.0)
			{
				duration -= 0.05;
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
			}
		}
	}
	
	// 닿으면 즉사하는 죽음의 광선
	class Death_Ray implements Runnable
	{
		DustTransition dtrStart = new DustTransition(Color.YELLOW, Color.WHITE, 2.0f);
		DustTransition dtrEnd = new DustTransition(Color.ORANGE, Color.BLACK, 2.0f);
		Player player;
		Location loc, target_loc;
		Vector dir;
		
		double speed = 0.5;
		double length = 0.0;
		double time = 2.0;
		
		public Death_Ray(Location _start_loc, Location _target_loc)
		{
			loc = _start_loc;
			target_loc = _target_loc;
			dir = target_loc.clone().subtract(loc).toVector();
			dir.setY(0);
			length = 2.0 * dir.distance(new Vector());
			target_loc.subtract(dir.clone().multiply(1.2));
			dir = dir.normalize().multiply(speed);
			
		}
		
		boolean is_start = false;
		public void run()
		{
			if (!is_start)
			{
				is_start = true;
				Particle_Drawer.Draw_Line(loc, loc.clone().add(dir.clone().multiply(length)),
					dtrStart, 0.2);
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 20);
				return;
			}
			
			// loc.add(dir);
			// length -= speed;
			RayTraceResult rtr = loc.getWorld().rayTrace(loc, dir, length, FluidCollisionMode.NEVER, true, 0.3f, null);
			Particle_Drawer.Draw_Line(loc, loc.clone().add(dir.clone().multiply(length)), dtrEnd, 0.2);
			// Particle_Drawer.Draw_Line(loc, loc.clone().add(0, -100, 0), dop, 0.1);
			if (rtr == null)
				return;
			if (rtr.getHitEntity() != null)
			{
				Entity e = rtr.getHitEntity();
				if (e instanceof Player)
				{
					Player p = (Player)e;
					if (!p.isDead() && p.getNoDamageTicks() < 1)
					{
						p.setHealth(p.getHealth() >= 20 ? 1 : 0);
						p.playEffect(EntityEffect.HURT);
						p.setNoDamageTicks(40);
					}
				}
			}
			if (rtr.getHitPosition() != null)
				loc.getWorld().playSound(rtr.getHitPosition().toLocation(loc.getWorld()), Sound.ENTITY_TNT_PRIMED, 0.3f, 2.0f);
		}
	}
	
	class Flare_Disc_Process implements Runnable
	{	
		Entity caster;
		Location loc;
		
		public Flare_Disc_Process(Entity _caster)
		{
			caster = _caster;
			loc = caster.getLocation().add(0.0, 1.0, 0.0);
		}
		
		@Override
		public void run()
		{
			for (double angle = 0.0; angle <= 360.0; angle += 45.0)
			{
				Vector dir = new Vector(Math.cos(Math.toRadians(angle)), 0.0, Math.sin(Math.toRadians(angle)));
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Flare_Disc_Boss(loc, caster, dir, 80, 0.4));
			}
		}
		class Flare_Disc_Boss implements Runnable
		{
			Entity caster;
			double damage;
			double max_distance = 30;
			double speed;
			Location start_loc;
			Vector dir;

			double current_distance = 0;
			Location before_loc, current_loc;
			Vector[] vecs;
			
			public Flare_Disc_Boss(Location _start_loc, Entity _caster, Vector _dir, double _damage, double _speed)
			{
				start_loc = _start_loc;
				caster = _caster;
				dir = _dir;
				damage = _damage;
				speed = _speed;

				current_loc = start_loc.clone();
				before_loc = start_loc.clone();
				
				vecs = new Vector[36];
				for(int i = 0; i < 36; i ++)
				{
					double angle = i * 360.0 / 36.0;
					vecs[i] = new Vector(Math.cos(Math.toRadians(angle)), 0, Math.sin(Math.toRadians(angle)));
				}
				// vecs = TRS.Rotate_Z(vecs, Math.random() * 360.0);
				// vecs = TRS.Rotate_X(vecs, 90.0);
				vecs = TRS.Rotate_Y(vecs, _start_loc.getYaw());
				vecs = TRS.Scale(vecs, 1.5, 1.5, 1.5);
			}
			
			public void run()
			{
				current_distance += speed;
				if(max_distance < current_distance)
					speed = max_distance - current_distance;
				current_loc.add(dir.clone().multiply(speed));
				
				if (current_loc.getY() <= 0)
					return;
				
				// 라인 그리기
				Vector gap = current_loc.clone().subtract(before_loc).toVector();
				if(gap.length() <= 0.01)
					return;
				
				for(int i = 0; i < vecs.length; i++)
				{
					Location loc = current_loc.clone().add(vecs[i]);
					current_loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
				}
				
				// 주변 적 찾기
				boolean detected = false;
				for(double i = 0; i <= gap.length(); i += 1)
				{
					Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
					if(loc.getBlock().getType().isSolid())
						detected = true;
					else
					{
						for(Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5))
						{
							if(!(e instanceof LivingEntity))
								continue;
							if(e == caster)
								continue;
							
							// 찾은 경우
							detected = true;
							break;
						}
					}
					if(detected)
					{
						current_loc = loc;
						break;
					}
				}
				
				if(detected)
				{
					for(Entity e : current_loc.getWorld().getNearbyEntities(current_loc, 6, 6, 6))
					{
						if (!(e instanceof LivingEntity))
							continue;
						if (e.isDead())
							continue;
						if (e == caster)
							continue;
						
						LivingEntity target = (LivingEntity)e;
						target.damage(damage, caster);
						target.setFireTicks(100);
					}

					current_loc.getWorld().playSound(current_loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
					current_loc.getWorld().playSound(current_loc, Sound.ITEM_TOTEM_USE, 2, 1);
					// current_loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, current_loc, 50, 3, 3, 3, 0);
					current_loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, current_loc, 45, 3, 3, 3, 0);
					current_loc.getWorld().spawnParticle(Particle.LAVA, current_loc, 70, 3, 3, 3, 0);
					current_loc.getWorld().spawnParticle(Particle.FLAME, current_loc, 70, 3, 3, 3, 0);
					return;
				}
				
				// 마무리 전 이걸 계속 해야하나 체크
				if(current_distance >= max_distance)
					return;
							
				// 마무리
				before_loc = current_loc.clone();
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
			}
		}
	}
	class Thunder_Process implements Runnable
	{	
		Entity caster;
		int count = 0, max_count = 0;
		Location player_loc;
		
		public Thunder_Process(Entity _caster, int _max_count)
		{
			caster = _caster;
			max_count = _max_count;
			
			for (Entity e : caster.getNearbyEntities(50, 50, 50))
			{
				if (e instanceof Player)
				{
					player_loc = e.getLocation();
					break;
				}
			}
		}
		
		@Override
		public void run()
		{
			if (player_loc == null)
				return;
			if (count > max_count)
				return;
			
			Location loc = player_loc;
			if (count == 0)
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Boss(caster, loc, 100));
			else
			{
				Location temp_loc = loc.clone().add(6.0 * count, 0.0, 0.0);
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Boss(caster, temp_loc, 100));
				temp_loc = loc.clone().add(-6.0 * count, 0.0, 0.0);
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Boss(caster, temp_loc, 100));
				temp_loc = loc.clone().add(0.0, 0.0, 6.0 * count);
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Boss(caster, temp_loc, 100));
				temp_loc = loc.clone().add(0.0, 0.0, -6.0 * count);
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Boss(caster, temp_loc, 100));
			}

			count += 1;
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 5);
		}
		
		class Thunder_Boss implements Runnable
		{
			Entity caster;
			double damage;
			Location loc;
			
			double radius = 6;
			double timer = 4.0;
			long interval = 20;
			World world;
			
			public Thunder_Boss(Entity _caster, Location _loc, double _damage)
			{
				caster = _caster;
				loc = _loc;
				damage = _damage;
				world = loc.getWorld();
				world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 1.5f);
			}
			
			public void run()
			{
				timer -= interval / 20.0;
				// interval = Math.max(1, interval / 2);
				
				if(timer > 0.0) // 타이머가 남았으면 대기
				{
					Location temp_loc = loc.clone();
					for(double angle = 0; angle < 360.0; angle += 360.0 / 64.0)
					{
						temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
						temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
						world.spawnParticle(Particle.SOUL_FIRE_FLAME, temp_loc, 1, 0, 0, 0, 0);
					}
					world.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.75f, 1.5f);
					Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, interval);
				}
				else // 다 됐으면 폭발
				{
					Location temp_loc = loc.clone();
					for(double angle = 0; angle < 360.0; angle += 360.0 / 64.0)
					{
						temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
						temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
						world.spawnParticle(Particle.LAVA, temp_loc, 1, 0, 0, 0, 0);
					}
					double temp_rad = radius * 20.0;
					for(int i = 0; i < 1; i++)
					{
						temp_loc.setX(loc.getX() + (-temp_rad + Math.random() * temp_rad * 2.0));
						temp_loc.setY(256);
						temp_loc.setZ(loc.getZ() + (-temp_rad + Math.random() * temp_rad * 2.0));
						Lightning_Bolt.Draw_Lightning_Line(temp_loc, loc, Particle.END_ROD);
					}

					for(Entity e : world.getNearbyEntities(loc, radius, radius, radius))
					{
						if(!(e instanceof LivingEntity))
							continue;
						if(e == caster)
							continue;
						
						LivingEntity target = (LivingEntity)e;
						target.damage(damage, caster);
					}

					world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
					world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.1f);
					
					// world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 50, 3, 3, 3, 0);
					world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 25, 3, 3, 3, 0);
					world.spawnParticle(Particle.LAVA, loc, 30, 0.3, 0.3, 0.3, 0);
				}
			}
		}
		
	}
	class Ice_Spike_Process implements Runnable
	{	
		Entity caster;
		Player player;
		int count = 0;
		
		public Ice_Spike_Process(Entity _caster, int _count)
		{
			caster = _caster;
			count = _count;
			
			for (Entity e : caster.getNearbyEntities(50, 50, 50))
			{
				if (e instanceof Player)
				{
					player = (Player)e;
					break;
				}
			}
		}
		
		@Override
		public void run()
		{
			if (player == null)
				return;
			if (count-- <= 0)
				return;
			
			Location loc = player.getLocation();
			Vector dir = loc.clone().subtract(caster.getLocation()).toVector();
			double radian = Math.atan2(dir.getZ(), dir.getX());
			
			Vector temp_dir = new Vector(Math.cos(radian), 0.0, Math.sin(radian));
			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Ice_Spike_Projectile(caster, temp_dir, 100));
			temp_dir = new Vector(Math.cos(radian - Math.toRadians(45.0)), 0.0, Math.sin(radian - Math.toRadians(45.0)));
			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Ice_Spike_Projectile(caster, temp_dir, 100));
			temp_dir = new Vector(Math.cos(radian + Math.toRadians(45.0)), 0.0, Math.sin(radian + Math.toRadians(45.0)));
			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Ice_Spike_Projectile(caster, temp_dir, 100));
			
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 20);
		}
		
		class Ice_Spike_Projectile implements Runnable
		{
			Entity caster;
			double damage;
			Location loc;
			Vector dir;
			
			double radius = 2;
			double distance = 50.0;
			World world;
			
			public Ice_Spike_Projectile(Entity _caster, Vector _dir, double _damage)
			{
				caster = _caster;
				loc = _caster.getLocation();
				damage = _damage;
				dir = _dir;
				
				world = loc.getWorld();
				world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
			}
			
			public void run()
			{
				distance -= radius * 1.75;
				// interval = Math.max(1, interval / 2);
				
				if(distance > 0.0) // 타이머가 남았으면 대기
				{
					loc.add(dir.clone().multiply(2.0));
					Location temp_loc = loc.clone();
					for(double angle = 0; angle < 360.0; angle += 360.0 / 128.0)
					{
						temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
						temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
						world.spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
					}
					Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, new Ice_Spike(caster, loc.clone(), damage), 40);
					Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
				}
			}
		}
		class Ice_Spike implements Runnable
		{
			Entity caster;
			double damage;
			Location loc;
			Vector dir;
			
			double radius = 1.5;
			World world;
			
			public Ice_Spike(Entity _caster, Location _loc, double _damage)
			{
				caster = _caster;
				loc = _caster.getLocation();
				damage = _damage;
				loc = _loc;
				
				world = loc.getWorld();
				
				Location temp_loc = loc.clone();
				for(double angle = 0; angle < 360.0; angle += 360.0 / 128.0)
				{
					temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
					temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
					world.spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
				}
			}
			
			public void run()
			{
				Location start = loc.clone().add(radius, -2.0, radius);
				Location end = loc.clone().add(-2.0 + Math.random() * 4.0, 3.0 + Math.random() * 3.0, -2.0 + Math.random() * 4.0);
				Particle_Drawer.Draw_Line(start, end, Particle.SNOWBALL, 0.2);
				start = loc.clone().add(-radius, -2.0, radius);
				Particle_Drawer.Draw_Line(start, end, Particle.SNOWBALL, 0.2);
				start = loc.clone().add(radius, -2.0, -radius);
				Particle_Drawer.Draw_Line(start, end, Particle.SNOWBALL, 0.2);
				start = loc.clone().add(-radius, -2.0, -radius);
				Particle_Drawer.Draw_Line(start, end, Particle.SNOWBALL, 0.2);
				
				for(Entity e : world.getNearbyEntities(loc, radius, 10, radius))
				{
					if(!(e instanceof LivingEntity))
						continue;
					if(e == caster)
						continue;
					
					LivingEntity target = (LivingEntity)e;
					target.damage(damage, caster);
				}

				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.0f);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
			}
		}
	}
	class EarthQuake_Process implements Runnable
	{	
		Entity caster;
		double damage;
		Location loc;

		World world;
		double radius = 1.0;
		double gap = 1.5;
		ArrayList<Entity> entities = new ArrayList<Entity>();
		int count = 0;
		
		public EarthQuake_Process(Entity _caster, double _damage)
		{
			caster = _caster;
			damage = _damage;
			loc = caster.getLocation().add(0.0, 0.1, 0.0);
			world = loc.getWorld();
		}
		
		@Override
		public void run()
		{
			count += 1;
			for (double angle = 0.0; angle <= 360.0; angle += 45.0 / radius)
			{
				Location temp_loc = loc.clone().add(Math.cos(Math.toRadians(angle)) * radius,
						0.0, Math.sin(Math.toRadians(angle)) * radius);
				world.spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
			}
			double max_radius = radius + gap;
			int sound_count = 0;
			for (double angle = 0.0; angle <= 360.0; angle += 45.0 / max_radius)
			{
				Location temp_loc = loc.clone().add(Math.cos(Math.toRadians(angle)) * max_radius,
						0.0, Math.sin(Math.toRadians(angle)) * max_radius);
				world.spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
				if (count % 4 == 0 && sound_count++ % 4 == 0)
					world.playSound(temp_loc, Sound.BLOCK_SAND_BREAK, 1.0f, 0.4f);
			}
			
			for (Entity e : world.getNearbyEntities(loc, 50.0, 50.0, 50.0))
			{
				if (!(e instanceof LivingEntity))
					continue;
				if (e == caster)
					continue;
				if (entities.contains(e))
					continue;
				
				if (e.isOnGround())
				{
					double dist = e.getLocation().distance(loc);
					if (radius + gap >= dist && dist >= radius)
					{
						entities.add(e);
						LivingEntity target = (LivingEntity)e;
						Location target_loc = e.getLocation();
						Vector vec = target.getLocation().subtract(loc).toVector().normalize().add(new Vector(0.0, 0.5, 0.0));
						vec = new Vector(0.0, 1.0, 0.0);
						target.damage(damage, caster);
						target.setVelocity(vec.multiply(1.8));

						world.playSound(target_loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.0f);
						Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, new Earth_Spike(caster, target_loc, 100), 30);
					}
				}
			}
			
			radius += 10.0 / 20.0;
			
			if (radius < 50.0)
				Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 1);
		}
		class Earth_Spike implements Runnable
		{
			Entity caster;
			double damage;
			Location loc;
			Vector dir;
			
			double radius = 3.5;
			World world;
			
			public Earth_Spike(Entity _caster, Location _loc, double _damage)
			{
				caster = _caster;
				loc = _caster.getLocation();
				damage = _damage;
				loc = _loc;
				
				world = loc.getWorld();
				
				Location temp_loc = loc.clone();
				for(double angle = 0; angle < 360.0; angle += 360.0 / 128.0)
				{
					temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
					temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
					world.spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
				}
			}
			
			public void run()
			{
				Location end = loc.clone().add(-2.0 + Math.random() * 4.0, 20.0, -2.0 + Math.random() * 4.0);

				Location start = loc.clone().add(radius, -2.0, radius);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				start = loc.clone().add(-radius, -2.0, radius);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				start = loc.clone().add(radius, -2.0, -radius);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				start = loc.clone().add(-radius, -2.0, -radius);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);

				start = loc.clone().add(0, -2.0, radius);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				start = loc.clone().add(0, -2.0, -radius);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				start = loc.clone().add(radius, -2.0, 0);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				start = loc.clone().add(-radius, -2.0, 0);
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.2);
				
				for(Entity e : world.getNearbyEntities(loc, radius, 30, radius))
				{
					if(!(e instanceof LivingEntity))
						continue;
					if(e == caster)
						continue;
					
					LivingEntity target = (LivingEntity)e;
					target.damage(damage, caster);
				}

				world.playSound(loc, Sound.ITEM_TOTEM_USE, 2.0f, 1.5f);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
			}
		}
	}
	class Thunder_Shower_Process implements Runnable
	{	
		Entity caster;
		int count = 0, max_count = 0;
		
		ArrayList<Location> locs = new ArrayList<Location>();
		
		public Thunder_Shower_Process(Entity _caster, int _max_count)
		{
			caster = _caster;
			max_count = _max_count;
		}
		
		@Override
		public void run()
		{
			if (count > max_count)
				return;

			
			boolean pass = false;
			Location loc = caster.getLocation().add(-40.0 + Math.random() * 80.0, 0.1, -40.0 + Math.random() * 80.0);
			while (pass == false)
			{
				pass = true;
				for (Location for_loc : locs)
				{
					if (for_loc.distance(loc) < 4.0)
						pass = false;
				}
				loc = caster.getLocation().add(-40.0 + Math.random() * 80.0, 0.1, -40.0 + Math.random() * 80.0);
			}
			
			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Thunder_Boss(caster, loc, 100));
			locs.add(loc);

			count += 1;
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 5);
		}
		
		class Thunder_Boss implements Runnable
		{
			Entity caster;
			double damage;
			Location loc;
			
			double radius = 8;
			double timer = 4.0;
			long interval = 20;
			World world;
			
			public Thunder_Boss(Entity _caster, Location _loc, double _damage)
			{
				caster = _caster;
				loc = _loc;
				damage = _damage;
				world = loc.getWorld();
				world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 1.5f);
			}
			
			public void run()
			{
				timer -= interval / 20.0;
				// interval = Math.max(1, interval / 2);
				
				if(timer > 0.0) // 타이머가 남았으면 대기
				{
					Location temp_loc = loc.clone();
					for(double angle = 0; angle < 360.0; angle += 360.0 / 64.0)
					{
						temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
						temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
						world.spawnParticle(Particle.SOUL_FIRE_FLAME, temp_loc, 1, 0, 0, 0, 0);
					}
					world.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.75f, 1.5f);
					Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, interval);
				}
				else // 다 됐으면 폭발
				{
					Location temp_loc = loc.clone();
					for(double angle = 0; angle < 360.0; angle += 360.0 / 64.0)
					{
						temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
						temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
						world.spawnParticle(Particle.LAVA, temp_loc, 1, 0, 0, 0, 0);
					}
					double temp_rad = radius * 20.0;
					for(int i = 0; i < 1; i++)
					{
						temp_loc.setX(loc.getX() + (-temp_rad + Math.random() * temp_rad * 2.0));
						temp_loc.setY(256);
						temp_loc.setZ(loc.getZ() + (-temp_rad + Math.random() * temp_rad * 2.0));
						Lightning_Bolt.Draw_Lightning_Line(temp_loc, loc, Particle.END_ROD);
					}

					for(Entity e : world.getNearbyEntities(loc, radius, radius, radius))
					{
						if(!(e instanceof LivingEntity))
							continue;
						if(e == caster)
							continue;
						
						LivingEntity target = (LivingEntity)e;
						target.damage(damage, caster);
					}

					world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
					world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.1f);
					
					// world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 50, 3, 3, 3, 0);
					world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 25, 3, 3, 3, 0);
					world.spawnParticle(Particle.LAVA, loc, 30, 0.3, 0.3, 0.3, 0);
				}
			}
		}
		
	}

	class Teleport_Process implements Runnable
	{	
		Entity caster;
		Location center;
		int count = 0, max_count = 0;
		
		public Teleport_Process(Entity _caster, Location _center_loc, int _max_count)
		{
			caster = _caster;
			center = _center_loc;
			max_count = _max_count;
		}
		
		@Override
		public void run()
		{
			if (count > max_count)
				return;
			
			Location loc = center.clone().add(-20.0 + Math.random() * 40.0, 0.0, -20.0 + Math.random() * 40.0);

			// Vector dir = loc.clone().subtract(caster.getLocation()).toVector();
			// double radian = Math.atan2(dir.getZ(), dir.getX());
			
			// Vector temp_dir = new Vector(Math.cos(radian), 0.0, Math.sin(radian));
			// Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Ice_Spike_Projectile(caster, temp_dir, 100));

			Lightning_Bolt.Draw_Lightning_Line(caster.getLocation().add(0.0, 1.0, 0.0), loc.clone().add(0.0, 1.0, 0.0), Particle.SPELL_WITCH);
			center.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);

			caster.teleport(loc);
			center.getWorld().playSound(caster.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);

			count += 1;
			Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin, this, 40);
		}
	}
}
















