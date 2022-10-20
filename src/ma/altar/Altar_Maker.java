package ma.altar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import ma.main.Mala_Altar;

// ���� ���۱�
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
			m_Player.sendMessage("��c���� ID�� ������ �־ �ȵ�~");
			return;
		}
		
		m_TempAltar.m_ID = _id;
		
		m_Player.sendMessage("��a�� �����~ ������ ������~");
		m_Player.getInventory().addItem(new ItemStack(Material.SPECTRAL_ARROW));
		m_Player.sendMessage("��a���� ������ ��e�б� ȭ���a�� �����Ұž�~");
		m_Player.sendMessage("��aȭ���� ��� ��Ͽ� ��Ŭ���ؼ� ������ ũ�⸦ ��������~");
		Set_Phase(m_Player, ALTAR_MAKE_PHASE.SIZE);
	}
	
	public void Set_Altar_Size(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.SIZE)
		{
			m_Player.sendMessage("��c����� �ٸ��ݾ�~");
			return;
		}
		
		if (m_TempAltar.m_Min == null)
		{
			m_TempAltar.m_World = _loc.getWorld().getName();
			m_TempAltar.m_Min = _loc.toVector();
			m_Player.sendMessage("��aù��° ��ġ�� �����߾�~ �ι�° ��ġ�� ��������~");
		}
		else if (m_TempAltar.m_Max == null)
		{
			if (m_TempAltar.m_World != _loc.getWorld().getName())
			{
				m_Player.sendMessage("��e���尡 �ٸ��ݾ�~ ����� �� �����϶�~");
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
			
			m_Player.sendMessage("��a�ι�° ��ġ�� �����߾�~");
			m_Player.sendMessage("��a���� ���� ��ġ�� ��������~");
			Set_Phase(m_Player, ALTAR_MAKE_PHASE.BELL_POS);
		}
	}
	
	public void Set_BellPosition(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.BELL_POS)
		{
			m_Player.sendMessage("��c����� �ٸ��ݾ�~");
			return;
		}
		if (m_TempAltar.m_World != _loc.getWorld().getName())
		{
			m_Player.sendMessage("��e���尡 �ٸ��ݾ�~ ����� �� �����϶�~");
			return;
		}
		m_TempAltar.m_BellPosition = _loc.toVector();
		m_Player.sendMessage("��a���� ��ġ�� �����߾�~");
		m_Player.sendMessage("��a���� ���͵��� ��Ÿ�� ��ġ�� ��������~");
		m_Player.sendMessage("��a����� ��ġ���� 2ĭ ������ ��Ÿ���ϱ� �����϶�~");
		m_Player.sendMessage("��a���� ��ȯ ��ġ ������ �������� /mala_altar endspawn �� �Է���~");
		Set_Phase(m_Player, ALTAR_MAKE_PHASE.SPAWN_POS);
	}
	
	public void Set_SpawnPositions(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.SPAWN_POS)
		{
			m_Player.sendMessage("��c����� �ٸ��ݾ�~");
			return;
		}
		if (m_TempAltar.m_World != _loc.getWorld().getName())
		{
			m_Player.sendMessage("��e���尡 �ٸ��ݾ�~ ����� �� �����϶�~");
			return;
		}
		m_TempAltar.m_SpawnPoints.add(_loc.toVector());
		m_Player.sendMessage("��a���� ��ġ�� �����߾�~ �� " + m_TempAltar.m_SpawnPoints.size() + " ���� ��ġ�� ������ ���¾�~");
	}
	
	public void End_Spawn()
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.SPAWN_POS)
		{
			m_Player.sendMessage("��c����� �ٸ��ݾ�~");
			return;
		}
		if (m_TempAltar.m_SpawnPoints.size() == 0)
		{
			m_Player.sendMessage("��c���� ��ġ�� ���ص� �ϳ��� �����ؾ� ��~");
			return;
		}
		m_Player.sendMessage("��a���� ��ġ ������ ������~");
		m_Player.sendMessage("��a���� ������ ������� �� ���� ���� ũ�⸦ ��������~");
		Set_Phase(m_Player, ALTAR_MAKE_PHASE.DOOR_POS);
	}
	
	public void Set_Door_Size(Location _loc)
	{
		if (Check_Phase(m_Player) != ALTAR_MAKE_PHASE.DOOR_POS)
		{
			m_Player.sendMessage("��c����� �ٸ��ݾ�~");
			return;
		}
		if (m_TempAltar.m_World != _loc.getWorld().getName())
		{
			m_Player.sendMessage("��e���尡 �ٸ��ݾ�~ ����� �� �����϶�~");
			return;
		}
		if (m_TempAltar.m_DoorMin == null)
		{
			m_TempAltar.m_DoorMin = _loc.toVector();
			m_Player.sendMessage("��aù��° �� ��ġ�� �����߾�~ �ι�° �� ��ġ�� ��������~");
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
			
			m_Player.sendMessage("��a�ι�° �� ��ġ�� �����߾�~");
			m_Player.sendMessage("��a���� ������ ������ ������~! �������� �÷����� ������ ���� ������ ���ֱ� �ٷ�~");
			
			Altar_Manager.Instance.m_AltarList.add(m_TempAltar);
			Altar_Manager.Instance.Save_Altar_List();
		}
	}
}












