package ma.altar;

import java.util.ArrayList;

public class Wave_Data
{
	public ArrayList<String> m_Gimmicks;
	public ArrayList<String> m_SpawnOrders;
	public ArrayList<String> m_Bonuses;
	
	public Wave_Data(ArrayList<String> _gimmicks, ArrayList<String> _spawnorders, ArrayList<String> _bonuses)
	{
		m_Gimmicks = _gimmicks;
		m_SpawnOrders = _spawnorders;
		m_Bonuses = _bonuses;
	}
	
	public boolean isDeathMatchStage() {
		for (String str : m_Gimmicks) {
			if (str.equalsIgnoreCase("DEATHMATCH"))
				return true;
		}
		return false;
	}
}
