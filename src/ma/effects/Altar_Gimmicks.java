package ma.effects;

import java.util.ArrayList;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import ma.util.Buff_Remover;
import mala.mmoskill.util.Buff_Manager;

public class Altar_Gimmicks
{
	public static void Remove_Good_Buffs(ArrayList<Player> players)
	{
		for (Player player : players)
			Buff_Remover.Remove_Player_Good_Buff(player);
	}
	public static void Remove_Bad_Buffs(ArrayList<Player> players)
	{
		for (Player player : players)
			Buff_Remover.Remove_Player_Good_Buff(player);
	}
	public static void Remove_All_Buffs(ArrayList<Player> players)
	{
		for (Player player : players)
			Buff_Remover.Remove_Player_All_Buff(player);
	}
	public static void Add_Buff(ArrayList<Player> players, String _type, String _amp, String _ticks)
	{
		PotionEffectType type = PotionEffectType.getByName(_type);
		int amp = Integer.parseInt(_amp);
		int ticks = Integer.parseInt(_ticks);
		
		for (Player player : players)
			Buff_Manager.Add_Buff(player, type, amp, ticks, null, true);
	}
	public static void Add_Buff_Enemies(ArrayList<Enemy_Order> enemies, String _type, String _amp, String _ticks)
	{
		PotionEffectType type = PotionEffectType.getByName(_type);
		int amp = Integer.parseInt(_amp);
		int ticks = Integer.parseInt(_ticks);
		
		for (Enemy_Order eo : enemies)
		{
			if (eo.Get_Entity() instanceof LivingEntity)
			Buff_Manager.Add_Buff((LivingEntity)eo.Get_Entity(), type, amp, ticks, null, true);
		}
	}
}
