package ma.main;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import laylia_core.main.Laylia_API;
import ma.altar.Altar_Maker;
import ma.altar.Altar_Manager;
import ma.events.AltarEvent;
import ma.mmoitem.MMOItems_Setter;
import ma.specialmob.Lost_Magician;
import ma.specialmob.SpecialMob_Manager;

public class Mala_Altar extends JavaPlugin
{
	public static JavaPlugin plugin;
	public static Laylia_API core_api;

	SpecialMob_Manager SMM;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Malaia�� ���� Ȱ��ȭ!");
		Bukkit.getPluginManager().registerEvents(new AltarEvent(), this);
		if (!Check_Laylia_API())
		{
			getServer().getConsoleSender().sendMessage("�� �ñ��� ���ϸ��� �ھ� API�� �ʿ��ϴٵ���!");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		
		SMM = new SpecialMob_Manager();
		Bukkit.getPluginManager().registerEvents(new Lost_Magician(), this);
		
		// �Ŵ��� ������
		Bukkit.getScheduler().runTaskTimer(this, new Altar_Manager(), 100, 20);
		new MMOItems_Setter();
	}

	@Override
	public void onDisable()
	{
		Altar_Manager.Instance.Disable_All_Altars();
		Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "Malaia�� ���� bȰ��ȭ..");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("mala_altar_init"))
		{
			Player target;
			if (!sender.hasPermission("*"))
				return false;
			if (args.length == 0) // Ÿ�� ����
			{
				if (!(sender instanceof Player))
					return false;
				target = (Player)sender;
				Altar_Manager.Instance.Player_Erase_Count(target);
				return true;
			}
			if (args.length == 1) // Ÿ�� ����
			{
				target = Bukkit.getPlayer(args[0]);
				if (target != null)
					Altar_Manager.Instance.Player_Erase_Count(target);
				return true;
			}
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("mala_altar_reload_mob"))
		{
			if (!sender.hasPermission("*"))
				return false;
			SMM.Load_Config();
			return true;
		}
		
		if (!(sender instanceof Player))
			return false;
			
		Player player = (Player)sender;

		if (cmd.getName().equalsIgnoreCase("animal_check"))
		{
			if (!sender.hasPermission("*"))
				return false;
			int count_30 = 0, count_60 = 0, count_100 = 0, count_200 = 0;
			int living_count_30 = 0, living_count_60 = 0, living_count_100 = 0, living_count_200 = 0;
			int animal_count_30 = 0, animal_count_60 = 0, animal_count_100 = 0, animal_count_200 = 0;
			for (Entity entity : player.getWorld().getEntities())
			{
				if (player.getLocation().distance(entity.getLocation()) < 30)
				{
					count_30 += 1;
					if (entity instanceof LivingEntity)
						living_count_30 += 1;
					if (entity instanceof Animals)
						animal_count_30 += 1;
				}
				if (player.getLocation().distance(entity.getLocation()) < 60)
				{
					count_60 += 1;
					if (entity instanceof LivingEntity)
						living_count_60 += 1;
					if (entity instanceof Animals)
						animal_count_60 += 1;
				}
				if (player.getLocation().distance(entity.getLocation()) < 100)
				{
					count_100 += 1;
					if (entity instanceof LivingEntity)
						living_count_100 += 1;
					if (entity instanceof Animals)
						animal_count_100 += 1;
				}
				if (player.getLocation().distance(entity.getLocation()) < 200)
				{
					count_200 += 1;
					if (entity instanceof LivingEntity)
						living_count_200 += 1;
					if (entity instanceof Animals)
						animal_count_200 += 1;
				}
			}
			player.sendMessage("��f��l[ ��a��l���� �� üũ ��� ��f��l]");
			player.sendMessage("��f30��� �� : ��7��ƼƼ �� ��f��l" + count_30 + "��7, ����ִ� �� " + living_count_30 + "��7, ���� ��f��l" + animal_count_30);
			player.sendMessage("��f60��� �� : ��7��ƼƼ �� ��f��l" + count_60 + "��7, ����ִ� �� " + living_count_60 + "��7, ���� ��f��l" + animal_count_60);
			player.sendMessage("��f100��� �� : ��7��ƼƼ �� ��f��l" + count_100 + "��7, ����ִ� �� " + living_count_100 + "��7, ���� ��f��l" + animal_count_100);
			player.sendMessage("��f200��� �� : ��7��ƼƼ �� ��f��l" + count_200 + "��7, ����ִ� �� " + living_count_200 + "��7, ���� ��f��l" + animal_count_200);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("altar_playcount"))
		{
			if (args.length == 0) // Ÿ�� ����
			{
				Integer count = Altar_Manager.Instance.Player_Get_Count(player);
				if (count > 0)
					player.sendMessage("��a[ ���ܿ� " + count + "/10 ȸ �����߽��ϴ�. ]");
				else
					player.sendMessage("��a[ ���� ���ܿ� �������� �ʾҽ��ϴ�. ]");
				return true;
			}
		}
		
		if (!player.hasPermission("*"))
			return false;
		if (cmd.getName().equalsIgnoreCase("altar_playcount"))
		{
			Player target;
			if (args.length == 1) // Ÿ�� ����
			{
				target = Bukkit.getPlayer(args[0]);
				if (target != null)
				{
					Integer count = Altar_Manager.Instance.Player_Get_Count(target);
					if (count > 0)
						player.sendMessage("��a[ " + target.getName() + "���� ���ܿ� " + count + "/10 ȸ �����߽��ϴ�. ]");
					else
						player.sendMessage("��a[ " + target.getName() + "���� ���� ���ܿ� �������� �ʾҽ��ϴ�. ]");
					return true;
				}
				return true;
			}
			return false;
		}
		if (cmd.getName().equalsIgnoreCase("mala_altar"))
		{
			if (!sender.hasPermission("*"))
				return false;
			if (args.length == 0)
				return false;
			if (args[0].equals("reload"))
			{
				Altar_Manager.Instance.Disable_All_Altars();
				Altar_Manager.Instance.Read_Altar_List();
				return true;
			}
			if (args[0].equals("list"))
			{
				player.sendMessage(Altar_Manager.Instance.Get_Altar_ListMsg());
				return true;
			}
			if (args[0].equals("create"))
			{
				if (args.length != 2)
				{
					player.sendMessage("/mala_altar create <id>");
					return true;
				}
				Altar_Maker am = Altar_Manager.Instance.Create_AltarMaker(player);
				am.Start_Make(args[1]);
			}
			else if (args[0].equals("endspawn"))
			{
				if (args.length != 1)
				{
					player.sendMessage("/mala_altar endspawn");
					return true;
				}
				Altar_Maker am = Altar_Manager.Instance.Get_AltarMaker(player);
				am.End_Spawn();
			}
			return true;
		}
	    return false;
	}
	// API üũ
	private boolean Check_Laylia_API()
	{
		if (getServer().getPluginManager().getPlugin("Laylia_Core_API") == null)
			return false;
		RegisteredServiceProvider<Laylia_API> rsp = getServer().getServicesManager().getRegistration(Laylia_API.class);
		if (rsp == null)
			return false;
		core_api = (Laylia_API)rsp.getProvider();
		return core_api != null;
	}
}