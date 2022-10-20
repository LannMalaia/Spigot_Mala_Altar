package ma.specialmob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import ma.main.Mala_Altar;

public class SpecialMob_Manager implements Listener
{
	List<String> physical = new ArrayList<String>();
	List<String> magic = new ArrayList<String>();
	List<String> skill = new ArrayList<String>();
	List<String> weapon = new ArrayList<String>();
	List<String> projectile = new ArrayList<String>();
	
	public SpecialMob_Manager()
	{
		Bukkit.getPluginManager().registerEvents(this, Mala_Altar.plugin);
		Load_Config();
	}

	public void Load_Config()
	{
		physical = new ArrayList<String>();
		magic = new ArrayList<String>();
		skill = new ArrayList<String>();
		weapon = new ArrayList<String>();
		projectile = new ArrayList<String>();
		
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, "MythicMobs_Only_Set.yml");
			if (!saveto.exists())
				return;

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			
			physical = file.getStringList("Physical");
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[ 물리 공격만! ]");
			for (String s : physical)
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "- " + s);
			magic = file.getStringList("Magic");
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[ 마법 공격만! ]");
			for (String s : magic)
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "- " + s);
			skill = file.getStringList("Skill");
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[ 스킬 공격만! ]");
			for (String s : skill)
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "- " + s);
			weapon = file.getStringList("Weapon");
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[ 무기 공격만! ]");
			for (String s : weapon)
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "- " + s);
			projectile = file.getStringList("Projectile");
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[ 투사체 공격만! ]");
			for (String s : projectile)
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "- " + s);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "공격이 통하지 않는 미띡몹 리스트를 갱신했어요.");
	}
	

	@EventHandler
	public void When_Spawn(MythicMobSpawnEvent event)
	{
		for (String type : physical)
		{
			if (type.equals(event.getMob().getMobType()))
				event.getEntity().setMetadata("altar.only_physical", new FixedMetadataValue(Mala_Altar.plugin, true));
		}
		for (String type : magic)
		{
			if (type.equals(event.getMob().getMobType()))
				event.getEntity().setMetadata("altar.only_magic", new FixedMetadataValue(Mala_Altar.plugin, true));
		}
		for (String type : skill)
		{
			if (type.equals(event.getMob().getMobType()))
				event.getEntity().setMetadata("altar.only_skill", new FixedMetadataValue(Mala_Altar.plugin, true));
		}
		for (String type : weapon)
		{
			if (type.equals(event.getMob().getMobType()))
				event.getEntity().setMetadata("altar.only_weapon", new FixedMetadataValue(Mala_Altar.plugin, true));
		}
		for (String type : projectile)
		{
			if (type.equals(event.getMob().getMobType()))
				event.getEntity().setMetadata("altar.only_projectile", new FixedMetadataValue(Mala_Altar.plugin, true));
		}
		
		// Bukkit.broadcastMessage(event.getMob().getMobType());
		if (event.getMob().getMobType().equals("The_Lost_Wizard"))
		{
			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Lost_Magician_Process(event.getMob()));
		}
		if (event.getMob().getMobType().equals("Nine_Circle_Karen"))
		{
			Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Nine_Circle_Karen(event.getMob()));
		}
		if (event.getMob().getMobType().equals("The_Tyrant"))
		{
			event.getEntity().setMetadata("altar.only_projectile", new FixedMetadataValue(Mala_Altar.plugin, true));
		}
		if (event.getEntity().getType() == EntityType.DOLPHIN)
		{
			Dolphin d = (Dolphin)event.getEntity();
			d.setAware(true);
			for (Entity e : d.getNearbyEntities(30, 30, 30))
			{
				if (e instanceof Player)
				{
					d.setTarget((Player)e);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void Boss_Attack(PlayerAttackEvent event)
	{
		// 완전 차단
		if (event.getEntity().hasMetadata("altar.only_physical"))
		{
			if (!event.getDamage().hasType(DamageType.PHYSICAL))
			{
				event.getPlayer().sendMessage("§c§l[ 대상에게 공격이 통하지 않습니다. 물리 공격을 해야합니다! ]");
				event.setCancelled(true);
			}
		}
		if (event.getEntity().hasMetadata("altar.only_magic"))
		{
			if (!event.getDamage().hasType(DamageType.MAGIC))
			{
				event.getPlayer().sendMessage("§c§l[ 대상에게 공격이 통하지 않습니다. 마법 공격을 해야합니다! ]");
				event.setCancelled(true);
			}
		}
		if (event.getEntity().hasMetadata("altar.only_skill"))
		{
			if (!event.getDamage().hasType(DamageType.SKILL))
			{
				event.getPlayer().sendMessage("§c§l[ 대상에게 공격이 통하지 않습니다. 스킬 공격을 해야합니다! ]");
				event.setCancelled(true);
			}
		}
		if (event.getEntity().hasMetadata("altar.only_weapon"))
		{
			if (!event.getDamage().hasType(DamageType.WEAPON))
			{
				event.getPlayer().sendMessage("§c§l[ 대상에게 공격이 통하지 않습니다. 무기 공격을 해야합니다! ]");
				event.setCancelled(true);
			}
		}
		if (event.getEntity().hasMetadata("altar.only_projectile"))
		{
			if (!event.getDamage().hasType(DamageType.PROJECTILE))
			{
				event.getPlayer().sendMessage("§c§l[ 대상에게 공격이 통하지 않습니다. 투사체 공격을 해야합니다! ]");
				event.setCancelled(true);
			}
		}
		
		// 내성에 의한 피해량 감소
		if (event.getEntity().hasMetadata("mmoitems.physical_protection")
				&& event.getDamage().hasType(DamageType.PHYSICAL))
		{
			double value = event.getEntity().getMetadata("mmoitems.physical_protection").get(0).asDouble();
			double percentage = 1.0 - (value * 0.01); // 내성이 100% 면 피해량은 0%
			event.getDamage().multiplicativeModifier(percentage);
			if (percentage < 1.0)
				event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
		}
		if (event.getEntity().hasMetadata("mmoitems.projectile_protection")
				&& event.getDamage().hasType(DamageType.PROJECTILE))
		{
			double value = event.getEntity().getMetadata("mmoitems.projectile_protection").get(0).asDouble();
			double percentage = 1.0 - (value * 0.01); // 내성이 100% 면 피해량은 0%
			event.getDamage().multiplicativeModifier(percentage);
			if (percentage < 1.0)
				event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
		}
		if (event.getEntity().hasMetadata("mmoitems.magic_protection")
				&& event.getDamage().hasType(DamageType.MAGIC))
		{
			double value = event.getEntity().getMetadata("mmoitems.magic_protection").get(0).asDouble();
			double percentage = 1.0 - (value * 0.01); // 내성이 100% 면 피해량은 0%
			event.getDamage().multiplicativeModifier(percentage);
			if (percentage < 1.0)
				event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
		}
	}
	
}
