package ma.altar;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import ma.effects.Altar_Bonus;
import ma.effects.Altar_Gimmicks;
import ma.effects.Altar_Start_Effect;
import ma.effects.Enemy_Order;
import ma.main.Mala_Altar;
import ma.util.Hitbox;
import mala.advancement.Criteria_Manager;
import mala.advancement.managers.MalaNormal_Advancement;
import net.Indyuce.mmocore.api.player.PlayerData;

public class Altar
{
	public String m_ID; // �׳� �ĺ��� ID
	
	// ������
	public String m_World; // �Ҽ� ����
	public Vector m_Min, m_Max; // ����
	public ArrayList<Vector> m_SpawnPoints; // ���� ��
	public Vector m_BellPosition; // �����ϴ� �� ��ġ
	public Vector m_DoorMin, m_DoorMax;
	public Material m_DoorMaterial; // �� ����
	public Material m_AirMaterial; // �� �������� �� ����
	public ArrayList<Stage_Data> m_StageDatas; // ���ܿ� ��ϵ� �������� �����͵�

	// �ǽð�
	String m_Challengers_Name; // ������ ����Ʈ
	ALTAR_STATE m_State = ALTAR_STATE.STANDBY; // ������?
	public ArrayList<Player> m_Players; // �����ڵ�
	public ArrayList<Enemy_Order> m_Enemies; // ����
	public Stage_Data m_CurrentStageData; // �̹� �������� ����Ǵ� �������� ������
	public Wave_Data m_CurrentWaveData; // �̹� �������� ����Ǵ� ���� ����
	public int m_Round = 0; // ���� �� ��° ����?
	public int m_Timer_Ticks = 0; // ���� �ð�
	public int m_TimeOut_Ticks = 3600; // Ÿ�Ӿƿ����� ���� �ð�
	
	boolean adv_Solo_NoInvPlay = false;
	public Scoreboard m_HP_Board;
	Objective obj;
	
	// ���
	boolean m_SpawnRandomized = false;
	boolean m_SpawnStronglyRandomized = false;
	
	public Altar()
	{
		m_SpawnPoints = new ArrayList<Vector>();

		m_DoorMaterial = Material.STONE_BRICKS;
		m_HP_Board = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = m_HP_Board.registerNewObjective("Health", "health", "Health");
		obj.setDisplayName(" ��c��lHP");
		obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
		
		Initialize();
	}
	
	public void When_Destroy()
	{
		obj.unregister();
	}
	
	public void Write_File(FileConfiguration _file)
	{
		_file.set("World", m_World);
		_file.set("Min", m_Min);
		_file.set("Max", m_Max);
		_file.set("SpawnPoints", m_SpawnPoints);
		_file.set("BellPosition", m_BellPosition);
		
		_file.set("DoorMin", m_DoorMin);
		_file.set("DoorMax", m_DoorMax);
		_file.set("DoorMaterial", m_DoorMaterial.toString());
		_file.set("AirMaterial", Material.AIR.toString());
		
		ArrayList<String> stagedatas = new ArrayList<String>();
		stagedatas.add("Sample_1");
		stagedatas.add("Sample_2");
		stagedatas.add("Sample_3");
		_file.set("StageDatas", stagedatas);
	}
	public void Read_File(String _id, FileConfiguration _file)
	{
		m_ID = _id;

		m_World = _file.getString("World");
		m_Min = _file.getVector("Min");
		m_Max = _file.getVector("Max");
		m_SpawnPoints = (ArrayList<Vector>)_file.getList("SpawnPoints");
		m_BellPosition = _file.getVector("BellPosition");
				
		m_DoorMin = _file.getVector("DoorMin");
		m_DoorMax = _file.getVector("DoorMax");
		m_DoorMaterial = Material.valueOf(_file.getString("DoorMaterial"));
		m_AirMaterial = Material.valueOf(_file.getString("AirMaterial", "AIR"));

		m_StageDatas = new ArrayList<Stage_Data>();
		for (String s : (ArrayList<String>)_file.getStringList("StageDatas"))
		{
			m_StageDatas.add(new Stage_Data(s));
			Bukkit.getConsoleSender().sendMessage("[����] " + m_ID + " - " + s + " �ε�");
		}
	}
	
	public boolean Check_Bell_Position(Location _block_pos)
	{
		// Bukkit.broadcastMessage("bell check = " + m_World.equals(_block_pos.getWorld().getName()) + " // " + (m_BellPosition.equals(_block_pos.toVector())));
		return m_World.equals(_block_pos.getWorld().getName()) && m_BellPosition.equals(_block_pos.toVector());
	}
	
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote �÷��̾ ���� ������ �õ��ߴ��� üũ
	 * @return ���� ������ NONE, �ƴ϶�� �ش��ϴ� enum ��ȯ
	 */
	public Stage_Data Check_Altar_Access(Player _player)
	{
		// �������ΰ�� ���� X
		if (m_State != ALTAR_STATE.STANDBY)
			return null;
		
		// ������ üũ
		ItemStack item = _player.getInventory().getItemInMainHand();
		if (item == null)
			return null;

		// ����� üũ
		for (Stage_Data sd : m_StageDatas)
		{
			// _player.sendMessage("������ üũ - " + item.isSimilar(sd.m_PassItem));
			if (item.isSimilar(sd.m_PassItem))
			{
				return sd;
			}
		}
		
		// �� �ش�ȵǸ� ���°��� ��
		return null;
	}
	
	public void Initialize()
	{
		m_Challengers_Name = "";
		m_State = ALTAR_STATE.STANDBY;
		m_Players = new ArrayList<Player>();
		m_Enemies = new ArrayList<Enemy_Order>();
		m_Round = 1;
		m_Timer_Ticks = 0;
		adv_Solo_NoInvPlay = false;
	}
	
	public void BroadcastMSG(String _msg)
	{
		for (Player player : m_Players)
			player.sendMessage(_msg);
	}
	
	public void BroadcastTitle(String _main, String _sub, int _fadein, int _duration, int _fadeout)
	{
		for (Player player : m_Players)
			player.sendTitle(_main, _sub, _fadein, _duration, _fadeout);
	}
	
	public void Player_Out(Player _player)
	{
		// �÷��̾� (����)���� ó��
		if (_player.isOnline())
		{
			_player.sendMessage("��7�������� �����ƽ��ϴ�...");
			_player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		if (m_Players.contains(_player))
			m_Players.remove(_player);
	}
	
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote ���� ���� ó��
	 * @param _difficulty ���̵�
	 */
	public void Start(Player _starter, Stage_Data _stage)
	{
		Initialize();

		m_CurrentStageData = _stage;
		m_CurrentWaveData = _stage.Get_WaveData(0);

		// �÷��̾� ���
		World world = Bukkit.getWorld(m_World);
		for (Player player : world.getPlayers())
		{
			if (Hitbox.Entity_In_Box(player, m_Min, m_Max))
			{
				if (!m_Players.contains(player))
					m_Players.add(player);
			}
		}
		// �ο� üũ
		if (!_starter.hasPermission("*"))
		{
			if (m_Players.size() > m_CurrentStageData.m_MaxPlayers)
			{
				BroadcastMSG("��c�ִ� " + m_CurrentStageData.m_MaxPlayers + "������� ������ �� �ֽ��ϴ�.");
				return;
			}
			if (m_Players.size() < m_CurrentStageData.m_MinPlayers)
			{
				BroadcastMSG("��c�ּ� " + m_CurrentStageData.m_MinPlayers + "���� �����ؾ� ������ �� �ֽ��ϴ�.");
				return;
			}
		}
		if (m_Players.size() == 0)
			return;

		// ī��Ʈ �� ����, IP ��ħ üũ
		ArrayList<String> addressList = new ArrayList<String>();
		for (Player player : m_Players)
		{
			PlayerData data = PlayerData.get(player);
			String ip = player.getAddress().getAddress().getHostAddress();
			if (addressList.contains(ip))
			{
				BroadcastMSG("��c���� PC���� �����ϴ� �÷��̾ �ֽ��ϴ�.");
				BroadcastMSG("��c���� �� �ΰ����� Ȱ���� ���� ����ġ ������ �������Դϴ�.");
				BroadcastMSG("��c���߽� �������� ������ ��� �����Ͱ� �ʱ�ȭ�ǹǷ� �������ּ���.");
				return;
			}
			addressList.add(ip);
			if (data.getLevel() < m_CurrentStageData.m_Need_Level)
			{
				BroadcastMSG("��c�������� �䱸�ϴ� ������ �������� ���� �÷��̾ �ֽ��ϴ�.");
				BroadcastMSG("��b��l" + m_CurrentStageData.m_Need_Level + "��c ������ �Ѿ����� Ȯ���غ�����.");
				return;
			}
			if (Altar_Manager.Instance.Player_Get_Count(player) >= 10)
			{
				BroadcastMSG("��c����ġ�� ���� ������ �õ��ϴ� �÷��̾ �ֽ��ϴ�.");
				BroadcastMSG("��c/altar_playcount ��ɾ�� �ڽ��� ���� Ƚ���� Ȯ���غ�����.");
				return;
			}
		}
		
		// ��¥ ���� ó��
		ItemStack ticket = _starter.getInventory().getItemInMainHand();

		if (ticket.getAmount() > 1)
		{
			_starter.sendMessage("��d��l[ 11���� �̺�Ʈ :: ������ �Ҹ� ���� ]");
			// ticket.setAmount(ticket.getAmount() - 1);
			// _starter.getInventory().setItemInMainHand(ticket);
		}
		else
		{
			// �ƹ��͵� ������ üũ�� �� �ִ� ������ ����
			_starter.getInventory().setItemInMainHand(null);
			if (m_Players.size() == 1 && m_CurrentStageData.can_GetAdvancement)
			{
				adv_Solo_NoInvPlay = _starter.getInventory().isEmpty();
				if (adv_Solo_NoInvPlay)
					_starter.sendMessage("��d��l����� GO!!!!");
			}
		}
		
		// �÷��̾� �̵� �� HP ���
		for (Player player : m_Players)
		{
			if (player != _starter)
				player.teleport(_starter.getLocation(), TeleportCause.PLUGIN);

			player.setScoreboard(m_HP_Board);
			Altar_Manager.Instance.Player_Count_Up(player);
		}
		for (Player player : m_Players)
		{
			if (player != _starter)
			{
				player.teleport(_starter.getLocation(), TeleportCause.PLUGIN);
				Criteria_Manager.instance.addCriteria(player, "malanormal", "altarparticipate", 1);
			}
			else
			{
				Criteria_Manager.instance.addCriteria(player, "malanormal", "altarstarter", 1);
			}
		}
		
		// �̸� �߰�
		for (int i = 0; i < m_Players.size(); i++)
			m_Challengers_Name += m_Players.get(i).getDisplayName() + (i + 1 == m_Players.size() ? "��f" : "��f, ");

		// ���� ���� ����
		m_State = ALTAR_STATE.FIGHTING;

		// �� �ݱ�
		for(int x = (int)m_DoorMin.getX(); x <= (int)m_DoorMax.getX(); x++)
			for(int y = (int)m_DoorMin.getY(); y <= (int)m_DoorMax.getY(); y++)
				for(int z = (int)m_DoorMin.getZ(); z <= (int)m_DoorMax.getZ(); z++)
				{
					Location loc = new Location(world, x, y, z);
					loc.getBlock().setType(m_DoorMaterial);
				}
		
		Location bell = new Location(world, m_BellPosition.getX() + 0.5, m_BellPosition.getY() + 0.5, m_BellPosition.getZ() + 0.5);
		Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Altar_Start_Effect(bell));
		BroadcastMSG(m_Challengers_Name + "���� " + m_CurrentStageData.m_StageName + " ��f������ �����մϴ�.");
		BroadcastMSG("��c��l[ �� ������ ��Ÿ���ϴ�. ������ �غ��ϼ���. ]");

		for (Player player : m_Players)
		{
			for (PotionEffect pes : player.getActivePotionEffects())
				player.removePotionEffect(pes.getType());
		}
		
		Spawn_Enemies();
	}
	
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote ���� ������Ʈ ó��
	 */
	public void Update()
	{
		if (m_State == ALTAR_STATE.STANDBY)
			return;
		
		// Ÿ�� ���� Ÿ�̸�
		m_Timer_Ticks += 20;
		m_TimeOut_Ticks -= 20;
		
		int m_TimeOut_Sec = m_TimeOut_Ticks / 20;
		if (m_TimeOut_Sec <= 180 && m_TimeOut_Sec % 60 == 0)
			BroadcastMSG("��c��l[ Ŭ���� ���� �ð��� " + m_TimeOut_Sec + "�� ���ҽ��ϴ�. �ð� ���� ���带 Ŭ�������� ���ϸ� ���з� ���ֵ˴ϴ�. ]");
		
		
		// ������ ���� �÷��̾� ���� üũ
		World world = Bukkit.getWorld(m_World);
		for (Player player : world.getPlayers())
		{
			if (Hitbox.Entity_In_Box(player, m_Min, m_Max))
			{
				if (!m_Players.contains(player))
				{
					if (player.getGameMode() != GameMode.SURVIVAL)
						continue;
					if (player.hasPermission("*"))
						continue;
					player.sendMessage("��cȯ������ ���� �մ�!");
					player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
				}
			}
		}

		// ���� �˻�
		ArrayList<Player> out_list = new ArrayList<Player>();
		for (Player player : m_Players)
		{
			if (!player.isOnline()) // ���� ���
				out_list.add(player);
			else if (!(m_World.equals(player.getWorld().getName()) // ���尡 �ٸ��ų�
					&& Hitbox.Entity_In_Box(player, m_Min, m_Max, 2.0))) // ������ ��� ���
				out_list.add(player);
			if (m_Players.size() == 0)
				break;
		}
		for (Player player : out_list)
			Player_Out(player);
		
		// ���� ��ȯ���� ������ ���
		for (Enemy_Order order : m_Enemies)
		{
			if (order.Get_Entity() == null)
				continue;
			
			if (!Hitbox.Entity_In_Box(order.Get_Entity(), m_Min, m_Max, 2.0)) // ������ ��� ���
			{
				order.TP_Mob_to_Original_Location();
				// order.Kill_Mob();
				// m_Enemies.remove(order);
				// break;
			}
		}
		

		// ���� �ο�� ���ΰ��
		if (m_State == ALTAR_STATE.FIGHTING)
		{
			if (Check_Wave_Clear()) // �� ����?
			{
				// ���ʽ� ����
				Give_Bonuses();
				// �� ���ҳ�?
				if (m_Round < m_CurrentStageData.m_Waves.size())
				{
					m_CurrentWaveData = m_CurrentStageData.Get_WaveData(m_Round);
					m_Round += 1;
					// �� ����
					Spawn_Enemies();
					// ��� ��Ÿ��
					Start_Gimmicks();
				}
				else
				{
					m_Round += 1;
				}
			}
			else // �� ������?
			{
				// �ý��� ������
				Update_Systems();
				// ��� ������
				Update_Gimmicks();
			}
		}

		// ������ ������ ��Ȳ���� üũ
		ALTAR_END_REASON reason = End_Check();
		if (reason != ALTAR_END_REASON.NOT_END)
			End(reason);
		
	}
	

	/**
	 * @apiNote ���̺� Ŭ���� üũ
	 */
	public boolean Check_Wave_Clear()
	{
		// debug
//		for (Player player : m_Players)
//		{
//			player.sendMessage("[ �� �϶� (" + m_Enemies.size() + ")���� ]");
//			for (Enemy_Order eo : m_Enemies)
//			{
//				player.sendMessage(String.format("%.3f", eo.m_WaitingTime) + "�� �� - "
//						+ eo.m_MobName + " :: "
//						+ (eo.Spawned ? "[������] " : "[�����ȵ�] ")
//						+ (eo.Check_Entity_Dead() ? "[����]" : "[�����]"));
//			}
//			player.sendMessage("--------����");
//		}
		for (int i = 0; i < m_Enemies.size(); i++)
		{
			Enemy_Order eo = m_Enemies.get(i);
			if (eo.Check_Entity_Dead())
			{
				m_Enemies.remove(eo);
				i -= 1;
			}
			if (m_Enemies.size() == 0)
				break;
		}
		return m_Enemies.size() == 0;
	}
	
	/**
	 * @author jimja
	 * @version 2021. 6. 17.
	 * @apiNote �������� Ŭ���� ���ʽ� ����
	 */
	public void Give_Bonuses()
	{
		ArrayList<String> bonus_list = m_CurrentWaveData.m_Bonuses;

		if (bonus_list == null || bonus_list.size() == 0)
			return;
			
		// Ŭ���� ���ʽ� �ֱ�
		BroadcastMSG("��f��l[ ��b��lŬ���� ���ʽ� ��f��l]");
		
		for (String bonus : bonus_list)
		{
			StringTokenizer token = new StringTokenizer(bonus, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken()); 
			
			switch (type)
			{
			// HP ȸ��
			case "REGEN_HP":
				Altar_Bonus.Heal_Players(m_Players, params.get(0));
				break;
			// MP ȸ��
			case "REGEN_MP":
				Altar_Bonus.MP_Heal_Players(m_Players, params.get(0));
				break;
			// STA ȸ��
			case "REGEN_STA":
				Altar_Bonus.STA_Heal_Players(m_Players, params.get(0));
				break;
			// EXP ȹ��
			case "GAIN_EXP":
				Altar_Bonus.Give_EXP_Players(m_Players, params.get(0));
				break;
				// EXP ȹ��
			case "FULL_PLAYER_EXP":
				if (m_Players.size() == m_CurrentStageData.m_MaxPlayers)
					Altar_Bonus.Give_FullPlayer_EXP(m_Players, params.get(0));
				break;
			// ������ ���
			case "GAIN_ITEM":
				Altar_Bonus.Give_Item_Players(m_Players, params);
				break;
			}
		}
	}
	
	public void Start_Gimmicks()
	{
		ArrayList<String> gimmick_list = m_CurrentWaveData.m_Gimmicks;

		m_SpawnRandomized = false;
		m_SpawnStronglyRandomized = false;
		
		if (gimmick_list == null || gimmick_list.size() == 0)
			return;
			
		// Ŭ���� ���ʽ� �ֱ�
		// BroadcastMSG("��f��l[ ��b��l���� Ư�� ��f��l]");
		
		for (String gimmick : gimmick_list)
		{
			StringTokenizer token = new StringTokenizer(gimmick, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken()); 
			
			switch (type)
			{
			// Ÿ�� ����
			case "TIME_LIMIT":
				m_TimeOut_Ticks = Integer.parseInt(params.get(0));
				break;
			// ���� ������ ����
			case "REMOVE_BUFF":
				Altar_Gimmicks.Remove_Good_Buffs(m_Players);
				break;
			// ���� ������ ����
			case "REMOVE_DEBUFF":
				Altar_Gimmicks.Remove_Bad_Buffs(m_Players);
				break;
			// ��� ������ ����
			case "REMOVE_ALL_BUFF":
				Altar_Gimmicks.Remove_All_Buffs(m_Players);
				break;
			// Ư�� ������ �߰�
			case "ADD_BUFF":
				Altar_Gimmicks.Add_Buff(m_Players, params.get(1), params.get(2), params.get(3));
				break;
			// Ư�� ������ �߰� (����)
			case "ADD_BUFF_ENEMY":
				Altar_Gimmicks.Add_Buff_Enemies(m_Enemies, params.get(1), params.get(2), params.get(3));
				break;
			// ���� ����Ʈ ����ȭ
			case "RANDOMIZE":
				m_SpawnRandomized = true;
				break;
			// ���� ����Ʈ ����ȭ(��ü ����)
			case "RANDOMIZE_STRONG":
				m_SpawnRandomized = true;
				m_SpawnStronglyRandomized = true;
				break;
			}
		}
	}
	
	public void Update_Gimmicks()
	{
		ArrayList<String> gimmick_list = m_CurrentWaveData.m_Gimmicks;

		if (gimmick_list == null || gimmick_list.size() == 0)
			return;
			
		// Ŭ���� ���ʽ� �ֱ�
		
		for (String gimmick : gimmick_list)
		{
			StringTokenizer token = new StringTokenizer(gimmick, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken()); 
			
			switch (type)
			{
			// ���� ������ ����
			case "REMOVE_BUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Remove_Good_Buffs(m_Players);
				break;
			// ���� ������ ����
			case "REMOVE_DEBUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Remove_Bad_Buffs(m_Players);
				break;
			// ��� ������ ����
			case "REMOVE_ALL_BUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Remove_All_Buffs(m_Players);
				break;
			// Ư�� ������ �߰�
			case "ADD_BUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Add_Buff(m_Players, params.get(1), params.get(2), params.get(3));
				break;
			// Ư�� ������ �߰� (����)
			case "ADD_BUFF_ENEMY":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Add_Buff_Enemies(m_Enemies, params.get(1), params.get(2), params.get(3));
				break;
			}
		}
	}
	
	public void Update_Systems()
	{
		// ����˻�
		for (Player player : m_Players)
		{
			if (m_CurrentStageData.is_NoFly && player.isFlying())
			{
				player.setFlying(false);
				player.sendMessage("��c���� �����߿��� ������ �����˴ϴ�.");
			}
			if (m_CurrentStageData.is_NoElytra && player.isGliding())
			{
				player.setGliding(false);
				player.sendMessage("��c���� �����߿��� ������ �����˴ϴ�.");
			}
		}
	}
	
	/**
	 * @apiNote ��ƼƼ ����
	 */
	public void Spawn_Enemies()
	{
		// ���� �˸�
		String main_text = "��c��l���� " + m_Round;
		String sub_text = "";
		if (m_Round >= m_CurrentStageData.m_Waves.size())
			sub_text = "��c��l������ ����";
		BroadcastTitle(main_text, sub_text, 10, 50, 10);
		
		// �ð� ����
		m_TimeOut_Ticks = 6000;
		
		// ����
		// n�� ��ġ�� n�� �� minecraft//mythicmobs ���� name�� n���� ��ȯ�� ��
		ArrayList<String> orders = m_CurrentWaveData.m_SpawnOrders;
		if (orders == null)
			return;
		for (String order : orders)
		{
			StringTokenizer token_tok = new StringTokenizer(order, ":");
			// ���� ���� �ε���
			int index = Integer.parseInt(token_tok.nextToken());
			if (m_SpawnRandomized) // ������� ���� ����ȭ
				index = Get_Randomized_Spawn_Point_Index();
			// ��� �ð�
			double wait_time = Double.parseDouble(token_tok.nextToken());
			// �̍� �� ����
			boolean is_mythic = token_tok.nextToken().equals("mythicmobs");
			// �� �̸�
			String mob_name = token_tok.nextToken();
			int count = Integer.parseInt(token_tok.nextToken());
			for (int i = 0; i < count; i++)
			{
				if (m_SpawnStronglyRandomized) // ������� ���� ����ȭ(��ü ����)
					index = Get_Randomized_Spawn_Point_Index();
				Location loc = new Location(Bukkit.getWorld(m_World),
						m_SpawnPoints.get(index).getX(), m_SpawnPoints.get(index).getY(), m_SpawnPoints.get(index).getZ());
				Enemy_Order eo = new Enemy_Order(is_mythic, mob_name, loc, wait_time);
				m_Enemies.add(eo);
				Bukkit.getScheduler().runTask(Mala_Altar.plugin, eo);
			}
		}
	}
		
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote ������ ���ǿ� �´��� Ȯ��
	 * @return
	 */
	public ALTAR_END_REASON End_Check()
	{
		if (m_Players.size() == 0) // �÷��̾� ���� ����
			return ALTAR_END_REASON.PLAYER_LESS;
		if (m_TimeOut_Ticks <= 0) // Ÿ�� �ƿ�
			return ALTAR_END_REASON.TIMEOUT;
		if (m_Round > m_CurrentStageData.m_Waves.size())// ��Ŭ����
			return ALTAR_END_REASON.ALL_CLEAR;
		return ALTAR_END_REASON.NOT_END;
	}
	
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote ���� ���� �� ������ ó��
	 */
	public void End(ALTAR_END_REASON _reason)
	{
		String altar_name = m_ID;
		
		switch(_reason)
		{
		case ALL_CLEAR:
			altar_name = m_ID + " " + m_CurrentStageData.m_StageName;
			Bukkit.broadcastMessage(m_Challengers_Name + "���� " + altar_name + " ��f������ �����ϼ̽��ϴ�.");
			BroadcastTitle("��b��l�� �� Ŭ���� ��", altar_name + "��f������ �����߽��ϴ�!", 20, 200, 40);

			for (Player player : m_Players)
			{
				if (adv_Solo_NoInvPlay)
					MalaNormal_Advancement.grantADV("malanormal", "altarnoarmor", m_Players.get(0));
			}
			
			if (m_CurrentStageData.m_ClearTeleport != null)
			{
				for (Player player : m_Players)
					player.teleport(m_CurrentStageData.m_ClearTeleport);
			}
			
			break;
		case PLAYER_LESS:
			altar_name = m_ID + " " + m_CurrentStageData.m_StageName;
			BroadcastTitle("��c��l�� ���� ��", altar_name + "��f������ �����߽��ϴ�...", 20, 200, 40);
			
			if (m_Round == 1)
			{
				for (Player player : m_Players)
					 MalaNormal_Advancement.grantADV("malanormal", "altarfirstdeath", player);
			}
			
			break;
		case TIMEOUT:
			altar_name = m_ID + " " + m_CurrentStageData.m_StageName;
			BroadcastTitle("��b��l�� �ð� �ʰ� ��", altar_name + "��f������ �����߽��ϴ�...", 20, 200, 40);
			break;
		case PLUGIN_DISABLE:
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		for (Player player : m_Players)
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		
		World world = Bukkit.getWorld(m_World);
		for(int x = (int)m_DoorMin.getX(); x <= (int)m_DoorMax.getX(); x++)
			for(int y = (int)m_DoorMin.getY(); y <= (int)m_DoorMax.getY(); y++)
				for(int z = (int)m_DoorMin.getZ(); z <= (int)m_DoorMax.getZ(); z++)
				{
					Location loc = new Location(world, x, y, z);
					loc.getBlock().setType(m_AirMaterial);
				}
		
		for (int i = 0; i < m_Enemies.size(); i++)
		{
			Enemy_Order eo = m_Enemies.get(i);
			eo.Kill_Mob();
			m_Enemies.remove(eo);
			i -= 1;
			if (m_Enemies.size() == 0)
				break;
		}
		
		Bukkit.broadcastMessage(m_ID + "�� ������ �����������ϴ�.");
		Initialize();
	}
	
	protected int Get_Randomized_Spawn_Point_Index()
	{
		return (int)(Math.random() * m_SpawnPoints.size());
	}
}
