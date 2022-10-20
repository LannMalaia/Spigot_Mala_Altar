package ma.altar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import ma.main.Mala_Altar;

// 알터 제작기
public class Altar_Maker
{
	public static ALTAR_MAKE_PHASE Check_Phase(Player _player)
	{
		if (_player.hasMetadata("mala.altar.size"))
			return ALTAR_MAKE_PHASE.SIZE;
		if (_player.hasMetadata("mala.altar.bell_pos"))
			return ALTAR_MAKE_PHASE.BELL_POS;
		if (_player.hasMetadata("mala.altar.spawn_pos"))
			return ALTAR_MAKE_PHASE.SPAWN_POS;
		if (_player.hasMetadata("mala.altar.door_pos"))
			return ALTAR_MAKE_PHASE.DOOR_POS;
		
		return ALTAR_MAKE_PHASE.NONE;
	}
	
	public static void Set_Phase(Player _player, ALTAR_MAKE_PHASE _phase)
	{
		_player.removeMetadata("mala.altar.size", Mala_Altar.plugin);
		_player.removeMetadata("mala.altar.bell_pos", Mala_Altar.plugin);
		_player.removeMetadata("mala.altar.spawn_pos", Mala_Altar.plugin);
		_player.removeMetadata("mala.altar.door_pos", Mala_Altar.plugin);
		switch(_phase)
		{
		case SIZE:
			_player.setMetadata("mala.altar.size", new FixedMetadataValue(Mala_Altar.plugin, true));
			break;
		case BELL_POS:
			_player.setMetadata("mala.altar.bell_pos", new FixedMetadataValue(Mala_Altar.plugin, true));
			break;
		case SPAWN_POS:
			_player.setMetadata("mala.altar.spawn_pos", new FixedMetadataValue(Mala_Altar.plugin, true));
			break;
		case DOOR_POS:
			_player.setMetadata("mala.altar.door_pos", new FixedMetadataValue(Mala_Altar.plugin, true));
			break;
		default:
			break;
		}
	}

	Player m_Player;
	Altar m_TempAltar;
	public Altar_Maker(Player _player)
	{
		m_TempAltar = new Altar();
		m_Player = _player;
	}
	
	public void Start_Make(String _id)
	{
		if (Altar_Manager.Instance.Get_Altar(_id) != null)
		{
			m_Player.sendMessage("§c같은 ID의 제단이 있어서 안돼~");
			return;
		}
		
		m_TempAltar.m_ID = _id;
		
		m_Player.sendMessage("§a응 어서오고~ 제단을 만들어보장~");
		m_Player.getInventory().addItem(new ItemStack(Material.SPECTRAL_ARROW));
		m_Player.sendMessage("§a제단 생성은 §e분광 화살§a로 진행할거야~");
		m_Player.sendMessage("§a화살을 들고 블록에 우클릭해서 제단의 크기를 설정해줘~");
		Set_Phase(m_Player, ALTAR_MAKE_PHASE.SIZE);
	}
	
	public void Set_Altar_Size(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.SIZE)
		{
			m_Player.sendMessage("§c페이즈가 다르잖아~");
			return;
		}
		
		if (m_TempAltar.m_Min == null)
		{
			m_TempAltar.m_World = _loc.getWorld().getName();
			m_TempAltar.m_Min = _loc.toVector();
			m_Player.sendMessage("§a첫번째 위치를 선택했어~ 두번째 위치도 선택해줘~");
		}
		else if (m_TempAltar.m_Max == null)
		{
			if (m_TempAltar.m_World != _loc.getWorld().getName())
			{
				m_Player.sendMessage("§e월드가 다르잖아~ 제대로 좀 선택하라구~");
				return;
			}
			m_TempAltar.m_Max = _loc.toVector();

			Vector temp_min = new Vector(
					Math.min(m_TempAltar.m_Min.getX(), m_TempAltar.m_Max.getX()),
					Math.min(m_TempAltar.m_Min.getY(), m_TempAltar.m_Max.getY()),
					Math.min(m_TempAltar.m_Min.getZ(), m_TempAltar.m_Max.getZ())
					);
			Vector temp_max = new Vector(
					Math.max(m_TempAltar.m_Min.getX(), m_TempAltar.m_Max.getX()),
					Math.max(m_TempAltar.m_Min.getY(), m_TempAltar.m_Max.getY()),
					Math.max(m_TempAltar.m_Min.getZ(), m_TempAltar.m_Max.getZ())
					);
			m_TempAltar.m_Min = temp_min;
			m_TempAltar.m_Max = temp_max;
			
			m_Player.sendMessage("§a두번째 위치를 선택했어~");
			m_Player.sendMessage("§a이제 종의 위치를 지정해줘~");
			Set_Phase(m_Player, ALTAR_MAKE_PHASE.BELL_POS);
		}
	}
	
	public void Set_BellPosition(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.BELL_POS)
		{
			m_Player.sendMessage("§c페이즈가 다르잖아~");
			return;
		}
		if (m_TempAltar.m_World != _loc.getWorld().getName())
		{
			m_Player.sendMessage("§e월드가 다르잖아~ 제대로 좀 선택하라구~");
			return;
		}
		m_TempAltar.m_BellPosition = _loc.toVector();
		m_Player.sendMessage("§a종의 위치를 지정했어~");
		m_Player.sendMessage("§a이제 몬스터들이 나타날 위치를 지정해줘~");
		m_Player.sendMessage("§a블록의 위치보다 2칸 위에서 나타나니까 주의하라구~");
		m_Player.sendMessage("§a몬스터 소환 위치 지정이 끝났으면 /mala_altar endspawn 을 입력해~");
		Set_Phase(m_Player, ALTAR_MAKE_PHASE.SPAWN_POS);
	}
	
	public void Set_SpawnPositions(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.SPAWN_POS)
		{
			m_Player.sendMessage("§c페이즈가 다르잖아~");
			return;
		}
		if (m_TempAltar.m_World != _loc.getWorld().getName())
		{
			m_Player.sendMessage("§e월드가 다르잖아~ 제대로 좀 선택하라구~");
			return;
		}
		m_TempAltar.m_SpawnPoints.add(_loc.toVector());
		m_Player.sendMessage("§a스폰 위치를 지정했어~ 총 " + m_TempAltar.m_SpawnPoints.size() + " 개의 위치를 지정한 상태야~");
	}
	
	public void End_Spawn()
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.SPAWN_POS)
		{
			m_Player.sendMessage("§c페이즈가 다르잖아~");
			return;
		}
		if (m_TempAltar.m_SpawnPoints.size() == 0)
		{
			m_Player.sendMessage("§c스폰 위치를 못해도 하나는 지정해야 돼~");
			return;
		}
		m_Player.sendMessage("§a스폰 위치 지정이 끝났어~");
		m_Player.sendMessage("§a이제 제단이 실행됐을 때 닫힐 문의 크기를 지정해줘~");
		Set_Phase(m_Player, ALTAR_MAKE_PHASE.DOOR_POS);
	}
	
	public void Set_Door_Size(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.DOOR_POS)
		{
			m_Player.sendMessage("§c페이즈가 다르잖아~");
			return;
		}
		if (m_TempAltar.m_World != _loc.getWorld().getName())
		{
			m_Player.sendMessage("§e월드가 다르잖아~ 제대로 좀 선택하라구~");
			return;
		}
		if (m_TempAltar.m_DoorMin == null)
		{
			m_TempAltar.m_DoorMin = _loc.toVector();
			m_Player.sendMessage("§a첫번째 문 위치를 선택했어~ 두번째 문 위치도 선택해줘~");
		}
		else if (m_TempAltar.m_DoorMax == null)
		{
			m_TempAltar.m_DoorMax = _loc.toVector();

			Vector temp_min = new Vector(
					Math.min(m_TempAltar.m_DoorMin.getX(), m_TempAltar.m_DoorMax.getX()),
					Math.min(m_TempAltar.m_DoorMin.getY(), m_TempAltar.m_DoorMax.getY()),
					Math.min(m_TempAltar.m_DoorMin.getZ(), m_TempAltar.m_DoorMax.getZ())
					);
			Vector temp_max = new Vector(
					Math.max(m_TempAltar.m_DoorMin.getX(), m_TempAltar.m_DoorMax.getX()),
					Math.max(m_TempAltar.m_DoorMin.getY(), m_TempAltar.m_DoorMax.getY()),
					Math.max(m_TempAltar.m_DoorMin.getZ(), m_TempAltar.m_DoorMax.getZ())
					);
			m_TempAltar.m_DoorMin = temp_min;
			m_TempAltar.m_DoorMax = temp_max;
			
			m_Player.sendMessage("§a두번째 문 위치를 선택했어~");
			m_Player.sendMessage("§a제단 설정이 완전히 끝났어~! 나머지는 플러그인 폴더에 들어가서 일일히 써주길 바래~");
			
			Altar_Manager.Instance.m_AltarList.add(m_TempAltar);
			Altar_Manager.Instance.Save_Altar_List();
		}
	}
}












