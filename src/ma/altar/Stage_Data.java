package ma.altar;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ma.main.Mala_Altar;

// 스폰 순서
public class Stage_Data
{
	public String m_FileName;
	FileConfiguration file = null;
	
	public int m_MinPlayers = 1;
	public int m_MaxPlayers = 3;
	public boolean is_NoFly = false;
	public boolean is_NoElytra = false;
	public boolean can_GetAdvancement = false;
	public int m_PlayerNoDamageTicks = 10;
	public int m_Need_Level = 0;
	// public int m_Restricted_Level = 0;
	public Location m_ClearTeleport = null;
	
	public String m_StageName;
	public ItemStack m_PassItem;
	public ArrayList<String> m_Systems = new ArrayList<String>();
	public ArrayList<Wave_Data> m_Waves = new ArrayList<Wave_Data>();
	
	public Stage_Data(String _name)
	{
		m_FileName = _name;
		Save_Sample();
		Load_Stage_Data();
		Setup_Stage_System();
	}

	public void Load_Stage_Data()
	{
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "StageDatas");
			if (!sub_dir.exists())
				sub_dir.mkdir();

			File saveto = new File(sub_dir, m_FileName + ".yml");

			// 파일 작성
			FileConfiguration file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);

			m_StageName = file.getString("name");
			String passitem_name = file.getString("pass_item");
			// mmoitems로 통행증 아이템 얻어오기
			m_PassItem = Altar_MMOItem.Get_Pass(passitem_name);
			
			m_Systems = (ArrayList<String>)file.getList("system");
			
			for (int i = 1; file.contains("" + i); i++)
			{
				ArrayList<String> gimmick = new ArrayList<String>();
				ArrayList<String> spawn_orders = new ArrayList<String>();
				ArrayList<String> bonus = new ArrayList<String>();
				
				if (file.contains(i + ".gimmick"))
					gimmick = (ArrayList<String>)file.getList(i + ".gimmick");
				if (file.contains(i + ".wave"))
					spawn_orders = (ArrayList<String>)file.getList(i + ".wave");
				if (file.contains(i + ".bonus"))
					bonus = (ArrayList<String>)file.getList(i + ".bonus");
				
				m_Waves.add(new Wave_Data(gimmick, spawn_orders, bonus));
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	void Setup_Stage_System()
	{
		for (String sys : m_Systems)
		{
			StringTokenizer token = new StringTokenizer(sys, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken());
			
			switch (type)
			{
			case "MIN_PLAYER":
			case "MIN_PLAYERS":
				m_MinPlayers = Integer.parseInt(params.get(0));
				break;
			case "MAX_PLAYER":
			case "MAX_PLAYERS":
				m_MaxPlayers = Integer.parseInt(params.get(0));
				break;
			case "NEED_LEVEL":
				m_Need_Level = Integer.parseInt(params.get(0));
				break;
			case "RESTRICT_FLY":
				is_NoFly = true;
				break;
			case "RESTRICT_ELYTRA":
				is_NoElytra = true;
				break;
			case "CAN_GET_ADVANCEMENT":
				can_GetAdvancement = true;
				break;
			case "NO_DAMAGE_TICKS":
				m_PlayerNoDamageTicks = Integer.parseInt(params.get(0));
				break;
			case "CLEAR_TELEPORT":
				World world = Bukkit.getWorld(params.get(0));
				double x = Double.parseDouble(params.get(1));
				double y = Double.parseDouble(params.get(2));
				double z = Double.parseDouble(params.get(3));
				m_ClearTeleport = new Location(world, x, y, z);
				break;
			}
		}
	}
	
	public void Save_Sample()
	{
		ArrayList<String> system = new ArrayList<String>();
		ArrayList<String> wave = new ArrayList<String>();
		ArrayList<String> bonus = new ArrayList<String>();
		ArrayList<String> gimmick = new ArrayList<String>();
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "StageDatas");
			if (!sub_dir.exists())
				sub_dir.mkdir();

			File saveto = new File(sub_dir, "Sample.yml");
			if (saveto.exists())
				return;

			saveto.createNewFile();
			// 파일 작성
			FileConfiguration file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			system.add("MAX_PLAYERS:3");
			system.add("RESTRICT_FLY");
			system.add("RESTRICT_ELYTRA");
			system.add("CAN_GET_ADVANCEMENT");
			system.add("NO_DAMAGE_TICKS:10");
			system.add("CLEAR_TELEPORT:world:0.0:0.0:0.0");
			wave.add("0:0.0:minecraft:ZOMBIE:4");
			wave.add("1:1.0:mythicmobs:ZOMBIE:4");
			wave.add("2:2.0:minecraft:ZOMBIE:4");
			bonus.add("REGEN_HP:20%");
			bonus.add("REGEN_MP:20%");
			bonus.add("REGEN_STA:20%");
			bonus.add("GAIN_EXP:500");
			bonus.add("GAIN_ITEM:minecraft:STICK:1");
			bonus.add("GAIN_ITEM:mmoitems:TYPE:ID:1");
			gimmick.add("REMOVE_BUFF:REPEAT");
			gimmick.add("REMOVE_DEBUFF:ONCE");
			gimmick.add("REMOVE_ALL_BUFF:ONCE");
			file.set("name", "test_level");
			file.set("pass_item", "MISCELLANEOUS:SAMPLE_PASS");
			file.set("system", system);
			file.set("1.gimmick", gimmick);
			file.set("1.wave", wave);
			file.set("1.bonus", bonus);
			file.set("2.gimmick", gimmick);
			file.set("2.wave", wave);
			file.set("2.bonus", bonus);
			file.set("3.gimmick", gimmick);
			file.set("3.wave", wave);
			file.set("3.bonus", bonus);
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Wave_Data Get_WaveData(int _index)
	{
		return m_Waves.get(_index);
	}
	public ArrayList<String> Get_Gimmicks(int _index)
	{
		return m_Waves.get(_index).m_SpawnOrders;
	}
	public ArrayList<String> Get_Waves(int _index)
	{
		return m_Waves.get(_index).m_SpawnOrders;
	}
	public ArrayList<String> Get_Bonuses(int _index)
	{
		return m_Waves.get(_index).m_SpawnOrders;
	}
	

	
}
