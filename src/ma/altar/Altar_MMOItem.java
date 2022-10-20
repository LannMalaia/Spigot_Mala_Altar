package ma.altar;

import java.util.StringTokenizer;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class Altar_MMOItem
{
	public static ItemStack Get_MMOItem(String _type, String _id)
	{
		return MMOItems.plugin.getItem(Type.get(_type), _id);
	}
	
	public static ItemStack Get_Pass(String _pass_name)
	{
		StringTokenizer token = new StringTokenizer(_pass_name, ":");
		String type = token.nextToken();
		String name = token.nextToken();
		return MMOItems.plugin.getItem(Type.get(type), name);
	}
}
