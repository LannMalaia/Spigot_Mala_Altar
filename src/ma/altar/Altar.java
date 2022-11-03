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
	public String m_ID; // 그냥 식별용 ID
	
	// 데이터
	public String m_World; // 소속 월드
	public Vector m_Min, m_Max; // 구역
	public ArrayList<Vector> m_SpawnPoints; // 스폰 존
	public Vector m_BellPosition; // 시작하는 종 위치
	public Vector m_DoorMin, m_DoorMax;
	public Material m_DoorMaterial; // 문 소재
	public Material m_AirMaterial; // 문 열려있을 때 소재
	public ArrayList<Stage_Data> m_StageDatas; // 제단에 등록된 스테이지 데이터들

	// 실시간
	String m_Challengers_Name; // 도전자 리스트
	ALTAR_STATE m_State = ALTAR_STATE.STANDBY; // 진행중?
	public ArrayList<Player> m_Players; // 참여자들
	public ArrayList<Enemy_Order> m_Enemies; // 몹들
	public Stage_Data m_CurrentStageData; // 이번 도전에서 진행되는 스테이지 데이터
	public Wave_Data m_CurrentWaveData; // 이번 도전에서 진행되는 스폰 오더
	public int m_Round = 0; // 현재 몇 번째 라운드?
	public int m_Timer_Ticks = 0; // 남은 시간
	public int m_TimeOut_Ticks = 3600; // 타임아웃까지 남은 시간
	
	boolean adv_Solo_NoInvPlay = false;
	public Scoreboard m_HP_Board;
	Objective obj;
	
	// 기믹
	boolean m_SpawnRandomized = false;
	boolean m_SpawnStronglyRandomized = false;
	
	public Altar()
	{
		m_SpawnPoints = new ArrayList<Vector>();

		m_DoorMaterial = Material.STONE_BRICKS;
		m_HP_Board = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = m_HP_Board.registerNewObjective("Health", "health", "Health");
		obj.setDisplayName(" §c§lHP");
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
			Bukkit.getConsoleSender().sendMessage("[제단] " + m_ID + " - " + s + " 로드");
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
	 * @apiNote 플레이어가 제단 시작을 시도했는지 체크
	 * @return 템이 없으면 NONE, 아니라면 해당하는 enum 반환
	 */
	public Stage_Data Check_Altar_Access(Player _player)
	{
		// 게임중인경우 강제 X
		if (m_State != ALTAR_STATE.STANDBY)
			return null;
		
		// 아이템 체크
		ItemStack item = _player.getInventory().getItemInMainHand();
		if (item == null)
			return null;

		// 입장권 체크
		for (Stage_Data sd : m_StageDatas)
		{
			// _player.sendMessage("통행증 체크 - " + item.isSimilar(sd.m_PassItem));
			if (item.isSimilar(sd.m_PassItem))
			{
				return sd;
			}
		}
		
		// 다 해당안되면 없는거지 뭐
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
		// 플레이어 (강제)퇴장 처리
		if (_player.isOnline())
		{
			_player.sendMessage("§7도전에서 도망쳤습니다...");
			_player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		if (m_Players.contains(_player))
			m_Players.remove(_player);
	}
	
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote 제단 시작 처리
	 * @param _difficulty 난이도
	 */
	public void Start(Player _starter, Stage_Data _stage)
	{
		Initialize();

		m_CurrentStageData = _stage;
		m_CurrentWaveData = _stage.Get_WaveData(0);

		// 플레이어 취득
		World world = Bukkit.getWorld(m_World);
		for (Player player : world.getPlayers())
		{
			if (Hitbox.Entity_In_Box(player, m_Min, m_Max))
			{
				if (!m_Players.contains(player))
					m_Players.add(player);
			}
		}
		// 인원 체크
		if (!_starter.hasPermission("*"))
		{
			if (m_Players.size() > m_CurrentStageData.m_MaxPlayers)
			{
				BroadcastMSG("§c최대 " + m_CurrentStageData.m_MaxPlayers + "명까지만 입장할 수 있습니다.");
				return;
			}
			if (m_Players.size() < m_CurrentStageData.m_MinPlayers)
			{
				BroadcastMSG("§c최소 " + m_CurrentStageData.m_MinPlayers + "명이 참여해야 입장할 수 있습니다.");
				return;
			}
		}
		if (m_Players.size() == 0)
			return;

		// 카운트 및 레벨, IP 겹침 체크
		ArrayList<String> addressList = new ArrayList<String>();
		for (Player player : m_Players)
		{
			PlayerData data = PlayerData.get(player);
			String ip = player.getAddress().getAddress().getHostAddress();
			if (addressList.contains(ip))
			{
				BroadcastMSG("§c같은 PC에서 접속하는 플레이어가 있습니다.");
				BroadcastMSG("§c서버 내 부계정을 활용한 제단 경험치 수급은 제재대상입니다.");
				BroadcastMSG("§c적발시 본계정을 포함한 모든 데이터가 초기화되므로 주의해주세요.");
				return;
			}
			addressList.add(ip);
			if (data.getLevel() < m_CurrentStageData.m_Need_Level)
			{
				BroadcastMSG("§c도전에서 요구하는 레벨에 도달하지 못한 플레이어가 있습니다.");
				BroadcastMSG("§b§l" + m_CurrentStageData.m_Need_Level + "§c 레벨을 넘었는지 확인해보세요.");
				return;
			}
			if (Altar_Manager.Instance.Player_Get_Count(player) >= 10)
			{
				BroadcastMSG("§c지나치게 많이 도전을 시도하는 플레이어가 있습니다.");
				BroadcastMSG("§c/altar_playcount 명령어로 자신의 도전 횟수를 확인해보세요.");
				return;
			}
		}
		
		// 진짜 시작 처리
		ItemStack ticket = _starter.getInventory().getItemInMainHand();

		if (ticket.getAmount() > 1)
		{
			_starter.sendMessage("§d§l[ 11월의 이벤트 :: 통행증 소모 없음 ]");
			// ticket.setAmount(ticket.getAmount() - 1);
			// _starter.getInventory().setItemInMainHand(ticket);
		}
		else
		{
			// 아무것도 없는지 체크할 수 있는 유일한 구간
			_starter.getInventory().setItemInMainHand(null);
			if (m_Players.size() == 1 && m_CurrentStageData.can_GetAdvancement)
			{
				adv_Solo_NoInvPlay = _starter.getInventory().isEmpty();
				if (adv_Solo_NoInvPlay)
					_starter.sendMessage("§d§l전라로 GO!!!!");
			}
		}
		
		// 플레이어 이동 및 HP 등록
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
		
		// 이름 추가
		for (int i = 0; i < m_Players.size(); i++)
			m_Challengers_Name += m_Players.get(i).getDisplayName() + (i + 1 == m_Players.size() ? "§f" : "§f, ");

		// 제단 상태 설정
		m_State = ALTAR_STATE.FIGHTING;

		// 문 닫기
		for(int x = (int)m_DoorMin.getX(); x <= (int)m_DoorMax.getX(); x++)
			for(int y = (int)m_DoorMin.getY(); y <= (int)m_DoorMax.getY(); y++)
				for(int z = (int)m_DoorMin.getZ(); z <= (int)m_DoorMax.getZ(); z++)
				{
					Location loc = new Location(world, x, y, z);
					loc.getBlock().setType(m_DoorMaterial);
				}
		
		Location bell = new Location(world, m_BellPosition.getX() + 0.5, m_BellPosition.getY() + 0.5, m_BellPosition.getZ() + 0.5);
		Bukkit.getScheduler().runTask(Mala_Altar.plugin, new Altar_Start_Effect(bell));
		BroadcastMSG(m_Challengers_Name + "님이 " + m_CurrentStageData.m_StageName + " §f도전을 시작합니다.");
		BroadcastMSG("§c§l[ 곧 적들이 나타납니다. 전투를 준비하세요. ]");

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
	 * @apiNote 제단 업데이트 처리
	 */
	public void Update()
	{
		if (m_State == ALTAR_STATE.STANDBY)
			return;
		
		// 타임 어택 타이머
		m_Timer_Ticks += 20;
		m_TimeOut_Ticks -= 20;
		
		int m_TimeOut_Sec = m_TimeOut_Ticks / 20;
		if (m_TimeOut_Sec <= 180 && m_TimeOut_Sec % 60 == 0)
			BroadcastMSG("§c§l[ 클리어 제한 시간이 " + m_TimeOut_Sec + "초 남았습니다. 시간 내에 라운드를 클리어하지 못하면 실패로 간주됩니다. ]");
		
		
		// 허용되지 않은 플레이어 접근 체크
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
					player.sendMessage("§c환영받지 못한 손님!");
					player.teleport(world.getSpawnLocation(), TeleportCause.PLUGIN);
				}
			}
		}

		// 퇴장 검사
		ArrayList<Player> out_list = new ArrayList<Player>();
		for (Player player : m_Players)
		{
			if (!player.isOnline()) // 나간 경우
				out_list.add(player);
			else if (!(m_World.equals(player.getWorld().getName()) // 월드가 다르거나
					&& Hitbox.Entity_In_Box(player, m_Min, m_Max, 2.0))) // 범위를 벗어난 경우
				out_list.add(player);
			if (m_Players.size() == 0)
				break;
		}
		for (Player player : out_list)
			Player_Out(player);
		
		// 제단 소환몹이 제단을 벗어남
		for (Enemy_Order order : m_Enemies)
		{
			if (order.Get_Entity() == null)
				continue;
			
			if (!Hitbox.Entity_In_Box(order.Get_Entity(), m_Min, m_Max, 2.0)) // 범위를 벗어난 경우
			{
				order.TP_Mob_to_Original_Location();
				// order.Kill_Mob();
				// m_Enemies.remove(order);
				// break;
			}
		}
		

		// 현재 싸우는 중인경우
		if (m_State == ALTAR_STATE.FIGHTING)
		{
			if (Check_Wave_Clear()) // 다 깼냐?
			{
				// 보너스 지급
				Give_Bonuses();
				// 더 남았냐?
				if (m_Round < m_CurrentStageData.m_Waves.size())
				{
					m_CurrentWaveData = m_CurrentStageData.Get_WaveData(m_Round);
					m_Round += 1;
					// 몹 스폰
					Spawn_Enemies();
					// 기믹 스타또
					Start_Gimmicks();
				}
				else
				{
					m_Round += 1;
				}
			}
			else // 다 못깼냥?
			{
				// 시스템 업데또
				Update_Systems();
				// 기믹 업데또
				Update_Gimmicks();
			}
		}

		// 제단이 끝나는 상황인지 체크
		ALTAR_END_REASON reason = End_Check();
		if (reason != ALTAR_END_REASON.NOT_END)
			End(reason);
		
	}
	

	/**
	 * @apiNote 웨이브 클리어 체크
	 */
	public boolean Check_Wave_Clear()
	{
		// debug
//		for (Player player : m_Players)
//		{
//			player.sendMessage("[ 몹 일람 (" + m_Enemies.size() + ")마리 ]");
//			for (Enemy_Order eo : m_Enemies)
//			{
//				player.sendMessage(String.format("%.3f", eo.m_WaitingTime) + "초 후 - "
//						+ eo.m_MobName + " :: "
//						+ (eo.Spawned ? "[스폰됨] " : "[스폰안됨] ")
//						+ (eo.Check_Entity_Dead() ? "[뒤짐]" : "[살었음]"));
//			}
//			player.sendMessage("--------종료");
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
	 * @apiNote 스테이지 클리어 보너스 지급
	 */
	public void Give_Bonuses()
	{
		ArrayList<String> bonus_list = m_CurrentWaveData.m_Bonuses;

		if (bonus_list == null || bonus_list.size() == 0)
			return;
			
		// 클리어 보너스 주기
		BroadcastMSG("§f§l[ §b§l클리어 보너스 §f§l]");
		
		for (String bonus : bonus_list)
		{
			StringTokenizer token = new StringTokenizer(bonus, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken()); 
			
			switch (type)
			{
			// HP 회복
			case "REGEN_HP":
				Altar_Bonus.Heal_Players(m_Players, params.get(0));
				break;
			// MP 회복
			case "REGEN_MP":
				Altar_Bonus.MP_Heal_Players(m_Players, params.get(0));
				break;
			// STA 회복
			case "REGEN_STA":
				Altar_Bonus.STA_Heal_Players(m_Players, params.get(0));
				break;
			// EXP 획득
			case "GAIN_EXP":
				Altar_Bonus.Give_EXP_Players(m_Players, params.get(0));
				break;
				// EXP 획득
			case "FULL_PLAYER_EXP":
				if (m_Players.size() == m_CurrentStageData.m_MaxPlayers)
					Altar_Bonus.Give_FullPlayer_EXP(m_Players, params.get(0));
				break;
			// 아이템 취득
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
			
		// 클리어 보너스 주기
		// BroadcastMSG("§f§l[ §b§l라운드 특성 §f§l]");
		
		for (String gimmick : gimmick_list)
		{
			StringTokenizer token = new StringTokenizer(gimmick, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken()); 
			
			switch (type)
			{
			// 타임 설정
			case "TIME_LIMIT":
				m_TimeOut_Ticks = Integer.parseInt(params.get(0));
				break;
			// 좋은 버프들 제거
			case "REMOVE_BUFF":
				Altar_Gimmicks.Remove_Good_Buffs(m_Players);
				break;
			// 나쁜 버프들 제거
			case "REMOVE_DEBUFF":
				Altar_Gimmicks.Remove_Bad_Buffs(m_Players);
				break;
			// 모든 버프를 제거
			case "REMOVE_ALL_BUFF":
				Altar_Gimmicks.Remove_All_Buffs(m_Players);
				break;
			// 특정 버프를 추가
			case "ADD_BUFF":
				Altar_Gimmicks.Add_Buff(m_Players, params.get(1), params.get(2), params.get(3));
				break;
			// 특정 버프를 추가 (적들)
			case "ADD_BUFF_ENEMY":
				Altar_Gimmicks.Add_Buff_Enemies(m_Enemies, params.get(1), params.get(2), params.get(3));
				break;
			// 스폰 포인트 랜덤화
			case "RANDOMIZE":
				m_SpawnRandomized = true;
				break;
			// 스폰 포인트 랜덤화(개체 단위)
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
			
		// 클리어 보너스 주기
		
		for (String gimmick : gimmick_list)
		{
			StringTokenizer token = new StringTokenizer(gimmick, ":");
			String type = token.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			while (token.hasMoreTokens())
				params.add(token.nextToken()); 
			
			switch (type)
			{
			// 좋은 버프들 제거
			case "REMOVE_BUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Remove_Good_Buffs(m_Players);
				break;
			// 나쁜 버프들 제거
			case "REMOVE_DEBUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Remove_Bad_Buffs(m_Players);
				break;
			// 모든 버프를 제거
			case "REMOVE_ALL_BUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Remove_All_Buffs(m_Players);
				break;
			// 특정 버프를 추가
			case "ADD_BUFF":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Add_Buff(m_Players, params.get(1), params.get(2), params.get(3));
				break;
			// 특정 버프를 추가 (적들)
			case "ADD_BUFF_ENEMY":
				if (params.get(0).equals("REPEAT"))
					Altar_Gimmicks.Add_Buff_Enemies(m_Enemies, params.get(1), params.get(2), params.get(3));
				break;
			}
		}
	}
	
	public void Update_Systems()
	{
		// 비행검사
		for (Player player : m_Players)
		{
			if (m_CurrentStageData.is_NoFly && player.isFlying())
			{
				player.setFlying(false);
				player.sendMessage("§c제단 진행중에는 비행이 금지됩니다.");
			}
			if (m_CurrentStageData.is_NoElytra && player.isGliding())
			{
				player.setGliding(false);
				player.sendMessage("§c제단 진행중에는 비행이 금지됩니다.");
			}
		}
	}
	
	/**
	 * @apiNote 엔티티 스폰
	 */
	public void Spawn_Enemies()
	{
		// 라운드 알림
		String main_text = "§c§l라운드 " + m_Round;
		String sub_text = "";
		if (m_Round >= m_CurrentStageData.m_Waves.size())
			sub_text = "§c§l마지막 라운드";
		BroadcastTitle(main_text, sub_text, 10, 50, 10);
		
		// 시간 설정
		m_TimeOut_Ticks = 6000;
		
		// 스폰
		// n번 위치에 n초 후 minecraft//mythicmobs 몹의 name을 n마리 소환할 것
		ArrayList<String> orders = m_CurrentWaveData.m_SpawnOrders;
		if (orders == null)
			return;
		for (String order : orders)
		{
			StringTokenizer token_tok = new StringTokenizer(order, ":");
			// 스폰 지점 인덱스
			int index = Integer.parseInt(token_tok.nextToken());
			if (m_SpawnRandomized) // 기믹으로 인한 랜덤화
				index = Get_Randomized_Spawn_Point_Index();
			// 대기 시간
			double wait_time = Double.parseDouble(token_tok.nextToken());
			// 미띡 몹 여부
			boolean is_mythic = token_tok.nextToken().equals("mythicmobs");
			// 몹 이름
			String mob_name = token_tok.nextToken();
			int count = Integer.parseInt(token_tok.nextToken());
			for (int i = 0; i < count; i++)
			{
				if (m_SpawnStronglyRandomized) // 기믹으로 인한 랜덤화(개체 단위)
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
	 * @apiNote 끝나는 조건에 맞는지 확인
	 * @return
	 */
	public ALTAR_END_REASON End_Check()
	{
		if (m_Players.size() == 0) // 플레이어 수가 적음
			return ALTAR_END_REASON.PLAYER_LESS;
		if (m_TimeOut_Ticks <= 0) // 타임 아웃
			return ALTAR_END_REASON.TIMEOUT;
		if (m_Round > m_CurrentStageData.m_Waves.size())// 올클리어
			return ALTAR_END_REASON.ALL_CLEAR;
		return ALTAR_END_REASON.NOT_END;
	}
	
	/**
	 * @author jimja
	 * @version 2020. 12. 17.
	 * @apiNote 제단 종료 및 마무리 처리
	 */
	public void End(ALTAR_END_REASON _reason)
	{
		String altar_name = m_ID;
		
		switch(_reason)
		{
		case ALL_CLEAR:
			altar_name = m_ID + " " + m_CurrentStageData.m_StageName;
			Bukkit.broadcastMessage(m_Challengers_Name + "님이 " + altar_name + " §f도전에 성공하셨습니다.");
			BroadcastTitle("§b§l■ 올 클리어 ■", altar_name + "§f도전에 성공했습니다!", 20, 200, 40);

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
			BroadcastTitle("§c§l■ 전멸 ■", altar_name + "§f도전에 실패했습니다...", 20, 200, 40);
			
			if (m_Round == 1)
			{
				for (Player player : m_Players)
					 MalaNormal_Advancement.grantADV("malanormal", "altarfirstdeath", player);
			}
			
			break;
		case TIMEOUT:
			altar_name = m_ID + " " + m_CurrentStageData.m_StageName;
			BroadcastTitle("§b§l■ 시간 초과 ■", altar_name + "§f도전에 실패했습니다...", 20, 200, 40);
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
		
		Bukkit.broadcastMessage(m_ID + "의 도전이 가능해졌습니다.");
		Initialize();
	}
	
	protected int Get_Randomized_Spawn_Point_Index()
	{
		return (int)(Math.random() * m_SpawnPoints.size());
	}
}
