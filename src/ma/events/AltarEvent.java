package ma.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import ma.altar.Altar;
import ma.altar.Altar_Maker;
import ma.altar.Altar_Manager;
import ma.altar.Stage_Data;
import ma.main.Mala_Altar;
import ma.mmoitem.MMOItems_Setter;

public class AltarEvent implements Listener
{
	@EventHandler
	public void Player_Breath_Damage(EntityDamageEvent event)
	{
		if (event.getEntityType() == EntityType.PLAYER
				&& event.getCause() == DamageCause.DROWNING)
		{
			Player player = (Player)event.getEntity();
			if (Altar_Manager.Instance.Get_Altar(player) == null)
				return;
			player.setHealth(Math.max(0, player.getHealth() - player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.05));
		}
	}
	
	@EventHandler
	public void Player_Wolf_Damage(EntityDamageByEntityEvent event)
	{
		Player player = null;
		if (event.getDamager() instanceof Projectile)
		{
			Projectile proj = (Projectile)event.getDamager();
			if (proj.getShooter() instanceof Player)
				player = (Player)proj.getShooter();
		}
		if (event.getDamager() instanceof Player)
			player = (Player)event.getDamager();
		if (player == null)
			return;

		Altar altar = Altar_Manager.Instance.Get_Altar(player);
		if (altar == null)
			return;
		
		if (event.getEntity() instanceof Tameable)
		{
			Tameable mob = (Tameable)event.getEntity();
			
			if (!(mob.getOwner() instanceof Player))
				return;
			
			Player tamer = (Player)mob.getOwner();
			Altar altar_2 = Altar_Manager.Instance.Get_Altar(tamer);
			if (altar_2 == null)
				return;
			if (altar == altar_2)
				event.setCancelled(true);
		}
		if (event.getEntity() instanceof Horse)
		{
			Horse horse = (Horse)event.getEntity();
			for (Entity e : horse.getPassengers())
			{
				if (e instanceof Player)
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void Player_Damage(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			Altar altar = Altar_Manager.Instance.Get_Altar(player);
			if (altar != null)
			{
				int ndt = altar.m_CurrentStageData.m_PlayerNoDamageTicks;
				{
					Bukkit.getScheduler().runTaskLater(Mala_Altar.plugin,
					new Runnable()
					{
						public void run()
						{
							if (player.getNoDamageTicks() > ndt)
								player.setNoDamageTicks(ndt);
						}
					}, 1);
				}
			}
		}
	}
	
	@EventHandler
	public void Player_ItemDamage(PlayerItemDamageEvent event)
	{
		if (Altar_Manager.Instance.Get_Altar(event.getPlayer()) != null)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void Player_Altar_Pass_Check(PlayerInteractEvent event)
	{
		if (event.getHand() != EquipmentSlot.HAND)
			return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			// 종에 우클했는지를 체크
			Block block = event.getClickedBlock();
			if (block == null)
				return;
			if (block.getType() != Material.BELL)
				return;
			
			// 종 위치값으로 알터 획득
			Altar altar = Altar_Manager.Instance.Get_Altar(block.getLocation());
			if (altar == null)
				return;
			
			// 손에 든 입장권 체크
			Stage_Data stagedata = altar.Check_Altar_Access(event.getPlayer());
			Bukkit.getConsoleSender().sendMessage("[제단] 제단 체크 - " + (stagedata != null));
			if (stagedata == null)
				return;
			
			altar.Start(event.getPlayer(), stagedata);
		}
	}

	@EventHandler
	public void Player_Altar_Make_Check(PlayerInteractEvent event)
	{
		if (event.getHand() != EquipmentSlot.HAND)
			return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
			if (item == null)
				return;
			if (item.getType() != Material.SPECTRAL_ARROW)
				return;
			
			Altar_Maker am = Altar_Manager.Instance.Get_AltarMaker(event.getPlayer());
			if (am == null)
				return;
			
			event.setCancelled(true);
			switch (Altar_Maker.Check_Phase(event.getPlayer()))
			{
			case SIZE:
				am.Set_Altar_Size(event.getClickedBlock().getLocation());
				break;
			case BELL_POS:
				am.Set_BellPosition(event.getClickedBlock().getLocation());
				break;
			case SPAWN_POS:
				am.Set_SpawnPositions(event.getClickedBlock().getLocation());
				break;
			case DOOR_POS:
				am.Set_Door_Size(event.getClickedBlock().getLocation());
				break;
			default:
				break;
			}
		}
	}

	@EventHandler
	public void Player_Element_Armor(MythicMobSpawnEvent event)
	{
		if (event.getEntity() instanceof LivingEntity)
		{
			MMOItems_Setter.Instance.Equip(event.getMobType().getInternalName(), (LivingEntity)event.getEntity());
		}
	}
}
