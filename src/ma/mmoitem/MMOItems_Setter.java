package ma.mmoitem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import ma.main.Mala_Altar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

// 미띡몹 장비에 아이템 / 내성 넣기
public class MMOItems_Setter
{
	public static MMOItems_Setter Instance;
	// ArrayList<Equipments> m_Item_Dict;
	File saveto;
	FileConfiguration file;
	
	public MMOItems_Setter()
	{
		Instance = this;
		Read();
	}
	public void Save()
	{
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();

			saveto = new File(directory, "MythicMobs_MMOItem_Set.yml");
			if (!saveto.exists())
			{
				saveto.createNewFile();

				Equipments eq = new Equipments("SAMPLE", "WEAPON:IRON_SWORD", "NONE", "NONE", "ARMOR:SUPER_ARMOR", "NONE", "ARMOR:SUPER_BOOTS");
				// 파일 작성
				file = YamlConfiguration.loadConfiguration(saveto);
				file.load(saveto);
				file.set(eq.mythic_name + ".mainhand", eq.main);
				file.set(eq.mythic_name + ".offhand", eq.off);
				file.set(eq.mythic_name + ".helmet", eq.helmet);
				file.set(eq.mythic_name + ".chestplate", eq.chestplate);
				file.set(eq.mythic_name + ".leggings", eq.leggings);
				file.set(eq.mythic_name + ".boots", eq.boots);
				file.set(eq.mythic_name + ".physical_protect", 0.0);
				file.set(eq.mythic_name + ".projectile_protect", 0.0);
				file.set(eq.mythic_name + ".magic_protect", 0.0);
				file.save(saveto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void Read()
	{
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, "MythicMobs_MMOItem_Set.yml");
			if (!saveto.exists())
			{
				Save();
				return;
			}

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean Check(String _mythic_name)
	{
		file = YamlConfiguration.loadConfiguration(saveto);
		try
		{
			file.load(saveto);
			if (file.contains(_mythic_name))
				return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void Equip(String _mythic_name, LivingEntity _entity)
	{
		if (Check(_mythic_name))
		{
			EntityEquipment ee = _entity.getEquipment();
			ItemStack i = Equipments.Parse_Item(file.getString(_mythic_name + ".mainhand"));
			if (i != null)
				ee.setItemInMainHand(i);
			i = Equipments.Parse_Item(file.getString(_mythic_name + ".offhand"));
			if (i != null)
				ee.setItemInOffHand(i);
			i = Equipments.Parse_Item(file.getString(_mythic_name + ".helmet"));
			if (i != null)
				ee.setHelmet(i);
			i = Equipments.Parse_Item(file.getString(_mythic_name + ".chestplate"));
			if (i != null)
				ee.setChestplate(i);
			i = Equipments.Parse_Item(file.getString(_mythic_name + ".leggings"));
			if (i != null)
				ee.setLeggings(i);
			i = Equipments.Parse_Item(file.getString(_mythic_name + ".boots"));
			if (i != null)
				ee.setBoots(i);
			double resist = file.getDouble(_mythic_name + ".physical_protect");
			_entity.setMetadata("mmoitems.physical_protection", new FixedMetadataValue(Mala_Altar.plugin, resist));
			resist = file.getDouble(_mythic_name + ".projectile_protect");
			_entity.setMetadata("mmoitems.projectile_protection", new FixedMetadataValue(Mala_Altar.plugin, resist));
			resist = file.getDouble(_mythic_name + ".magic_protect");
			_entity.setMetadata("mmoitems.magic_protection", new FixedMetadataValue(Mala_Altar.plugin, resist));
		}
	}
}

class Equipments
{
	public String mythic_name;
	public String main;
	public String off;
	public String helmet;
	public String chestplate;
	public String leggings;
	public String boots;
	
	public Equipments(String _name, String _main, String _off, String _helmet, String _chestplate, String _leggings, String _boots)
	{
		mythic_name = _name;
		main = _main; off = _off;
		helmet = _helmet; chestplate = _chestplate;
		leggings = _leggings; boots = _boots;
	}
	
	public static ItemStack Parse_Item(String _msg)
	{
		if (_msg.equals("NONE") || _msg.equals(""))
			return null;
		StringTokenizer token = new StringTokenizer(_msg, ":");
		String type = token.nextToken();
		String id = token.nextToken();
		return MMOItems.plugin.getItem(Type.get(type), id);
	}
}
