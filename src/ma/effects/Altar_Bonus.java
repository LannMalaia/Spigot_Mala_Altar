package ma.effects;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import laylia_core.main.Lang;
import ma.altar.Altar_MMOItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.experience.EXPSource;

public class Altar_Bonus
{
	public static void Heal_Players(ArrayList<Player> players, String amount)
	{
		boolean is_percent = false;
		double heal_amount = 0;
		int per = 0;
		if (amount.contains("%"))
		{
			is_percent = true;
			heal_amount = Double.parseDouble(amount.replaceAll("%", ""));
			per = (int)heal_amount;
			heal_amount *= 0.01;
		}
		else
			heal_amount = Double.parseDouble(amount);
		
		for (Player player : players)
		{
			double max = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			if (is_percent)
			{
				player.setHealth(Math.min(max, player.getHealth() + max * heal_amount));
				player.sendMessage("§f§l- §a§lHP " + per + "%("
								+ (int)(max * heal_amount) + ")" + " 회복");
			}
			else
			{
				player.setHealth(Math.min(max, player.getHealth() + heal_amount));
				player.sendMessage("§f§l- §a§lHP " + heal_amount + " 회복");
			}
		}
	}
	public static void MP_Heal_Players(ArrayList<Player> players, String amount)
	{
		boolean is_percent = false;
		double heal_amount = 0;
		int per = 0;
		if (amount.contains("%"))
		{
			is_percent = true;
			heal_amount = Double.parseDouble(amount.replaceAll("%", ""));
			per = (int)heal_amount;
			heal_amount *= 0.01;
		}
		else
			heal_amount = Double.parseDouble(amount);
		
		for (Player player : players)
		{
			PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
			double max = p_data.getStats().getStat("MAX_MANA");
			if (is_percent)
			{
				p_data.setMana(Math.min(max, p_data.getMana() + max * heal_amount));
				player.sendMessage("§f§l- §a§lMP " + per + "%("
								+ (int)(max * heal_amount) + ")" + " 회복");
			}
			else
			{
				p_data.setMana(Math.min(max, p_data.getMana() + heal_amount));
				player.sendMessage("§f§l- §a§lMP " + heal_amount + " 회복");
			}
		}
	}
	public static void STA_Heal_Players(ArrayList<Player> players, String amount)
	{
		boolean is_percent = false;
		double heal_amount = 0;
		int per = 0;
		if (amount.contains("%"))
		{
			is_percent = true;
			heal_amount = Double.parseDouble(amount.replaceAll("%", ""));
			per = (int)heal_amount;
			heal_amount *= 0.01;
		}
		else
			heal_amount = Double.parseDouble(amount);
		
		for (Player player : players)
		{
			PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
			double max = p_data.getStats().getStat("MAX_STAMINA");
			if (is_percent)
			{
				p_data.setStamina(Math.min(max, p_data.getStamina() + max * heal_amount));
				player.sendMessage("§f§l- §a§l스태미나 " + per + "%("
								+ (int)(max * heal_amount) + ")" + " 회복");
			}
			else
			{
				p_data.setStamina(Math.min(max, p_data.getStamina() + heal_amount));
				player.sendMessage("§f§l- §a§l스태미나 " + heal_amount + " 회복");
			}
		}
	}
	public static void Give_EXP_Players(ArrayList<Player> players, String amount, double multiplier)
	{
		double exp_amount = Integer.parseInt(amount) * multiplier;
		
		for (Player player : players)
		{
			PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
			p_data.giveExperience(exp_amount, EXPSource.SOURCE);
			player.sendMessage("§f§l- §a§l경험치 " + exp_amount + " 획득");
		}
	}
	public static void Give_FullPlayer_EXP(ArrayList<Player> players, String amount, double multiplier)
	{
		double exp_amount = Integer.parseInt(amount) * multiplier;
		
		for (Player player : players)
		{
			PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
			p_data.giveExperience(exp_amount, EXPSource.SOURCE);
			//player.sendMessage("§d§l[ 11월의 이벤트 :: 경험치 2배 ]");
			player.sendMessage("§f§l- §a§l전원 참여 보너스 경험치 " + (exp_amount) + " 획득");
		}
	}
	public static void Give_Item_Players(ArrayList<Player> players, ArrayList<String> params)
	{
		if (params.get(0).equalsIgnoreCase("minecraft")) // 일반 아이템 지급시
		{
			Material mat = Material.valueOf(params.get(1));
			int min = 1, max = 1;
			if (params.get(2).contains("~"))
			{
				StringTokenizer token = new StringTokenizer(params.get(2), "~");
				min = Integer.parseInt(token.nextToken());
				max = Integer.parseInt(token.nextToken());
			}
			else
			{
				min = Integer.parseInt(params.get(2));
				max = Integer.parseInt(params.get(2));
			}
			double chance = Double.parseDouble(params.get(3).replaceAll("%", "")) * 0.01;
			
			for (Player player : players)
			{
				if (Math.random() <= chance) // 확률 통과시
				{
					int count = min + (int)((max - min + 1) * Math.random());
					player.getInventory().addItem(new ItemStack(mat, count));
					player.sendMessage("§f§l- §b§l" + Lang.Localize(mat)
										+ (count > 1 ? (" " + count + "개") : "") + " 획득");
				}
			}
		}
		else if (params.get(0).equalsIgnoreCase("mmoitems")) // mmoitem 지급시
		{
			ItemStack item = Altar_MMOItem.Get_MMOItem(params.get(1), params.get(2));
			int min = 1, max = 1;
			if (params.get(3).contains("~"))
			{
				StringTokenizer token = new StringTokenizer(params.get(3), "~");
				min = Integer.parseInt(token.nextToken());
				max = Integer.parseInt(token.nextToken());
			}
			else
			{
				min = Integer.parseInt(params.get(3));
				max = Integer.parseInt(params.get(3));
			}
			double chance = Double.parseDouble(params.get(4).replaceAll("%", "")) * 0.01;
			for (Player player : players)
			{
				if (Math.random() <= chance) // 확률 통과시
				{
					int count = min + (int)((max - min + 1) * Math.random());
					item.setAmount(count);
					player.getInventory().addItem(item);
					player.sendMessage("§f§l- §b§l" + item.getItemMeta().getDisplayName()
										+ "§b§l" + (count > 1 ? (" " + count + "개") : "") + " 획득");
				}
			}
		}
	}

}
