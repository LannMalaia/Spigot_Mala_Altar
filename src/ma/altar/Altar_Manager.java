package ma.altar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import ma.effects.Enemy_Order;
import ma.main.Mala_Altar;

public class Altar_Manager implements Runnable
{
	public static Altar_Manager Instance;
	
	public ArrayList<Altar> m_AltarList;
	public ArrayList<Altar_Maker> m_MakerList;
	
	public Altar_Manager()
	{
		m_AltarList = new ArrayList<Altar>();
		m_MakerList = new ArrayList<Altar_Maker>();
		Instance = this;
		
		Read_Altar_List();
	}
	
	public String Get_Altar_ListMsg()
	{
		String result = "";
		for (Altar altar : m_AltarList)
			result += altar.m_ID + " // ";
		return result;
	}
	
	public void Disable_All_Altars()
	{
		for (Altar altar : m_AltarList)
		{
			altar.End(ALTAR_END_REASON.PLUGIN_DISABLE);
			altar.When_Destroy();
		}
	}
	
	// 파일 입출력
	public void Save_Altar_List()
	{
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "Altars");
			if (!sub_dir.exists())
				sub_dir.mkdir();
			
			for (int i = 0; i < m_AltarList.size(); i++)
			{
				File saveto = new File(sub_dir, m_AltarList.get(i).m_ID + ".yml");
				if (!saveto.exists())
				{
					saveto.createNewFile();
					
					// 파일 작성
					FileConfiguration file = YamlConfiguration.loadConfiguration(saveto);
					file.load(saveto);
					m_AltarList.get(i).Write_File(file);
					file.save(saveto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void Read_Altar_List()
	{
		m_AltarList = new ArrayList<Altar>();
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "Altars");
			if (!sub_dir.exists())
				sub_dir.mkdir();
			
			if (sub_dir.listFiles().length == 0)
				return;
			for (int i = 0; i < sub_dir.listFiles().length; i++)
			{
				File saveto = new File(sub_dir, sub_dir.listFiles()[i].getName());

				// 파일 작성
				FileConfiguration file = YamlConfiguration.loadConfiguration(saveto);
				file.load(saveto);
				Altar altar = new Altar();
				String id = sub_dir.listFiles()[i].getName().replace(".yml", "");
				altar.Read_File(id, file);
				m_AltarList.add(altar);
				file.save(saveto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer Player_Get_Count(Player _player)
	{
		return PlayCountManager.getInstance().getPlayerCount(_player);
	}
	public void Player_Count_Up(Player _player, int _count)
	{
		PlayCountManager.getInstance().addPlayerCount(_player, _count);
	}
	public void Player_Erase_Count(Player _player)
	{
		if (!_player.isOp()) {
			_player.sendMessage("§c[ 권한이 없습니다. ]");
			return;
		}

		_player.sendMessage("§b[ 제단 플레이 횟수가 초기화되었습니다. ]");
		PlayCountManager.getInstance().removePlayerCount(_player);
	}
	
	// 제단 취득
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote 벨 위치로 제단 얻기
	 * @return
	 */
	public Altar Get_Altar(Location _bell_loc)
	{
		for (Altar altar : m_AltarList)
		{
			if (altar.Check_Bell_Position(_bell_loc))
				return altar;
		}
		return null;
	}
	public Altar Get_Altar(String _id)
	{
		for (Altar altar : m_AltarList)
		{
			if (altar.m_ID.equals(_id))
				return altar;
		}
		return null;
	}
	public Altar Get_Altar(Player _player)
	{
		for (Altar altar : m_AltarList)
		{
			if (altar.m_Players.contains(_player))
				return altar;
		}
		return null;
	}
	public Altar Get_Altar(Enemy_Order _eo)
	{
		for (Altar altar : m_AltarList)
		{
			if (altar.m_Enemies.contains(_eo))
				return altar;
		}
		return null;
	}
	
	// 제단 제작기
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote 플레이어로 제단 제작기 열기
	 * @return
	 */
	public Altar_Maker Get_AltarMaker(Player _player)
	{
		for (Altar_Maker maker : m_MakerList)
		{
			if (maker.m_Player == _player)
				return maker;
		}
		return null;
	}
	public Altar_Maker Create_AltarMaker(Player _player)
	{
		Remove_AltarMaker(_player);
		Altar_Maker am = new Altar_Maker(_player);
		m_MakerList.add(am);
		return am;
	}
	public void Remove_AltarMaker(Player _player)
	{
		Altar_Maker am = Get_AltarMaker(_player);
		if (am != null)
			m_MakerList.remove(am);
	}

	public void run()
	{
		for (Altar altar : m_AltarList)
			altar.Update();
		
		return;
	}
}
