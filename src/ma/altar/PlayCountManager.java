package ma.altar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import github.scarsz.discordsrv.DiscordSRV;
import ma.main.Mala_Altar;

public class PlayCountManager implements Runnable {
	private static PlayCountManager instance;
	
	public static final int MAX_COUNT = 5;
	public static final int SUPERLEVEL_PLAYCOUNT = 3;
	
	public static PlayCountManager getInstance() {
		if (instance == null)
			instance = new PlayCountManager();
		return instance;
	}
	
	private PlayCountManager() {
		Bukkit.getScheduler().runTaskTimer(Mala_Altar.plugin, this, 0, 100);
	}

	SimpleDateFormat hour = new SimpleDateFormat("hh");
	public void run() {
		Date date = getLastDate(); // 기록된 시간
		Date newDate = Calendar.getInstance().getTime(); // 현재 시간
		
		// 시 다름 (시각이 넘어가는 시점)
		if (!hour.format(date).equals(hour.format(newDate))) {
			// 5시에서 6시로 넘어가는 그 시점인 경우
			if (Integer.parseInt(hour.format(date)) == 8
				&& Integer.parseInt(hour.format(newDate)) == 9) {
				clearAllDatas();
			}
		}
	}
	
	public void clearAllDatas() {
		if (Mala_Altar.plugin.getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
			if (DiscordSRV.isReady) {
				DiscordSRV.getPlugin().getMainTextChannel().sendMessage("```⚔ 제단 수행 횟수가 초기화 되었습니다. ⚔```").queue();
			}
		}
		Bukkit.broadcastMessage("§a[ 도전의 제단 플레이 가능 횟수가 초기화되었습니다. ]");
		clearAllPlayerCount();
	}
	
	public void clearAllPlayerCount() {
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "userdata");
			if (!sub_dir.exists())
				sub_dir.mkdir();
			
			for (File file : sub_dir.listFiles()) {
				file.delete();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getPlayerCount(Player player) {
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "userdata");
			if (!sub_dir.exists())
				sub_dir.mkdir();
			
			File file = new File(sub_dir, player.getUniqueId() + ".yml");
			if (!file.exists())
				return 0;
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			return fc.getInt("playcount", 0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void addPlayerCount(Player player, int count) {
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "userdata");
			if (!sub_dir.exists())
				sub_dir.mkdir();
			
			File file = new File(sub_dir, player.getUniqueId() + ".yml");
			if (!file.exists())
				file.createNewFile();
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			int lastCount = fc.getInt("playcount", 0);
			fc.set("playcount", lastCount + count);
			fc.save(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removePlayerCount(Player player) {
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			File sub_dir = new File(directory, "userdata");
			if (!sub_dir.exists())
				sub_dir.mkdir();
			
			File file = new File(sub_dir, player.getUniqueId() + ".yml");
			if (file.exists())
				file.delete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Date getLastDate() {
		try
		{
			// 폴더 설정
			File directory = Mala_Altar.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			File file = new File(directory, "loaded_date.yml");
			if (!file.exists())
				file.createNewFile();
			
			FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
			long ms = fc.getLong("date", 0);
			Date date = new Date(ms);
			Date newDate = Calendar.getInstance().getTime();
			
			fc.set("date", newDate.getTime());
			fc.save(file);
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new Date(0);
	}
}
