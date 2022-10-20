package ma.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Buff_Remover
{
	public static void Remove_Player_Good_Buff(Player player)
	{
		for (PotionEffect pes : player.getActivePotionEffects())
		{
			PotionEffectType pet = pes.getType();
			
			if (pet == PotionEffectType.ABSORPTION ||
				pet == PotionEffectType.CONDUIT_POWER ||
				pet == PotionEffectType.DAMAGE_RESISTANCE ||
				pet == PotionEffectType.DOLPHINS_GRACE ||
				pet == PotionEffectType.FAST_DIGGING ||
				pet == PotionEffectType.FIRE_RESISTANCE ||
				pet == PotionEffectType.HEALTH_BOOST ||
				pet == PotionEffectType.HERO_OF_THE_VILLAGE ||
				pet == PotionEffectType.INCREASE_DAMAGE ||
				pet == PotionEffectType.INVISIBILITY ||
				pet == PotionEffectType.JUMP ||
				pet == PotionEffectType.LUCK ||
				pet == PotionEffectType.NIGHT_VISION ||
				pet == PotionEffectType.REGENERATION||
				pet == PotionEffectType.SATURATION ||
				pet == PotionEffectType.SPEED ||
				pet == PotionEffectType.WATER_BREATHING)
				player.removePotionEffect(pes.getType());
		}
	}
	public static void Remove_Player_Bad_Buff(Player player)
	{
		for (PotionEffect pes : player.getActivePotionEffects())
		{
			PotionEffectType pet = pes.getType();
			
			if (pet == PotionEffectType.BAD_OMEN ||
				pet == PotionEffectType.BLINDNESS ||
				pet == PotionEffectType.CONFUSION ||
				pet == PotionEffectType.GLOWING ||
				pet == PotionEffectType.HUNGER ||
				pet == PotionEffectType.LEVITATION ||
				pet == PotionEffectType.POISON ||
				pet == PotionEffectType.SLOW ||
				pet == PotionEffectType.SLOW_DIGGING ||
				pet == PotionEffectType.UNLUCK ||
				pet == PotionEffectType.WEAKNESS ||
				pet == PotionEffectType.WITHER)
				player.removePotionEffect(pes.getType());
		}
	}
	public static void Remove_Player_All_Buff(Player player)
	{
		for (PotionEffect pes : player.getActivePotionEffects())
		{
			PotionEffectType pet = pes.getType();
			player.removePotionEffect(pes.getType());
		}
	}

}
