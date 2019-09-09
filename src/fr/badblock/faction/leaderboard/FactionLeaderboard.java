package fr.badblock.faction.leaderboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import fr.xmalware.badblock.keys.listeners.PlayerOpenKeyEvent;
import net.milkbowl.vault.economy.Economy;

public class FactionLeaderboard extends JavaPlugin implements Runnable, Listener
{

	public static FactionLeaderboard		instance;

	public Economy								economy;
	public Map<String, BadPlayer>	badPlayers = new HashMap<>();

	@Override
	public void onEnable()
	{
		instance = this;
		Bukkit.getScheduler().runTaskTimer(this, this, 20, 20 * 300);

		File folder = new File(getDataFolder(), "data");
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		setupEconomy();

		for (File file : folder.listFiles())
		{
			new BadPlayer(file);
		}

		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable()
	{
	}

	private boolean setupEconomy() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}

		economy = rsp.getProvider();
		return economy != null;
	}

	@Override
	public void run()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!badPlayers.containsKey(player.getName().toLowerCase()))
			{
				badPlayers.put(player.getName().toLowerCase(), new BadPlayer(player));
			}
			else
			{
				BadPlayer badPlayer = badPlayers.get(player.getName().toLowerCase());
				badPlayer.setData(player);
			}
		}

		workTimetop();
		workTopKills();
		workTopBalance();
		workTopDonator();
		workTopRatio();
		workTopClaims();
	}

	private void workTimetop()
	{
		List<BadPlayer> tempList = new ArrayList<>(badPlayers.values());
		Collections.sort(tempList, new Comparator<BadPlayer>()
		{

			@Override
			public int compare(BadPlayer player1, BadPlayer player2) {
				return player2.data.get("playTime").compareTo(player1.data.get("playTime"));
			}

		});

		LinkedHashMap<String, String> top = new LinkedHashMap<>();

		for (BadPlayer badPlayer : tempList)
		{
			long l = badPlayer.data.get("playTime");
			top.put(badPlayer.name, l + "m");
		}

		showTop("Toptime", top);
	}

	private void workTopClaims()
	{
		List<Faction> tempList = new ArrayList<>(Factions.getInstance().getAllFactions().parallelStream().filter(faction ->
		!faction.isSafeZone() && !faction.isWarZone() && !faction.isWilderness()).collect(Collectors.toList()));

		Collections.sort(tempList, new Comparator<Faction>()
		{

			@Override
			public int compare(Faction faction1, Faction faction2) {
				Integer faction1Claims = faction1.getAllClaims() != null ? faction1.getAllClaims().size() : 0;
				Integer faction2Claims = faction2.getAllClaims() != null ? faction2.getAllClaims().size() : 0;

				return faction2Claims.compareTo(faction1Claims);
			}

		});

		LinkedHashMap<String, String> top = new LinkedHashMap<>();

		for (Faction faction : tempList)
		{
			int claims = faction.getAllClaims() != null ? faction.getAllClaims().size() : 0;
			top.put(faction.getTag(), claims + " claims");
		}

		showTop("Topclaims", top);
	}

	private void workTopKills()
	{
		List<BadPlayer> tempList = new ArrayList<>(badPlayers.values());
		Collections.sort(tempList, new Comparator<BadPlayer>()
		{

			@Override
			public int compare(BadPlayer player1, BadPlayer player2) {
				return player2.data.get("kills").compareTo(player1.data.get("kills"));
			}

		});

		LinkedHashMap<String, String> top = new LinkedHashMap<>();

		for (BadPlayer badPlayer : tempList)
		{
			top.put(badPlayer.name, Long.toString(badPlayer.data.get("kills")));
		}

		showTop("Topkills", top);
	}

	private void workTopDonator()
	{
		List<BadPlayer> tempList = new ArrayList<>(badPlayers.values());
		tempList = tempList.parallelStream().filter(pl -> pl.data.containsKey("key")).collect(Collectors.toList());
		Collections.sort(tempList, new Comparator<BadPlayer>()
		{

			@Override
			public int compare(BadPlayer player1, BadPlayer player2) {
				return player2.data.get("key").compareTo(player1.data.get("key"));
			}

		});

		LinkedHashMap<String, String> top = new LinkedHashMap<>();

		for (BadPlayer badPlayer : tempList)
		{
			top.put(badPlayer.name, Long.toString(badPlayer.data.get("key")) + " clés");
		}

		showTop("Topdonator", top);
	}

	private void workTopRatio()
	{
		List<BadPlayer> tempList = new ArrayList<>(badPlayers.values());
		tempList = tempList.parallelStream().filter(pl -> pl.data.containsKey("key")).collect(Collectors.toList());
		Collections.sort(tempList, new Comparator<BadPlayer>()
		{

			@Override
			public int compare(BadPlayer player1, BadPlayer player2) {
				return player2.getRatio().compareTo(player1.getRatio());
			}

		});

		LinkedHashMap<String, String> top = new LinkedHashMap<>();

		for (BadPlayer badPlayer : tempList)
		{
			top.put(badPlayer.name, Long.toString(badPlayer.data.get("key")) + " clés");
		}

		showTop("Topratio", top);
	}

	private void workTopBalance()
	{
		List<BadPlayer> tempList = new ArrayList<>(badPlayers.values());
		Collections.sort(tempList, new Comparator<BadPlayer>()
		{

			@Override
			public int compare(BadPlayer player1, BadPlayer player2) {
				return player2.data.get("balance").compareTo(player1.data.get("balance"));
			}

		});

		LinkedHashMap<String, String> top = new LinkedHashMap<>();

		for (BadPlayer badPlayer : tempList)
		{
			top.put(badPlayer.name, Long.toString(badPlayer.data.get("balance")));
		}

		showTop("Topmoney", top);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onOpenKey(PlayerOpenKeyEvent event)
	{
		if (event.getKeyId() < 2)
		{
			return;
		}

		Player player = event.getPlayer();

		if (!badPlayers.containsKey(player.getName().toLowerCase()))
		{
			BadPlayer bPlayer = new BadPlayer(player);
			bPlayer.incrementKey(player);
		}
		else
		{
			BadPlayer badPlayer = badPlayers.get(player.getName().toLowerCase());
			badPlayer.incrementKey(player);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		if (player.getKiller() == null)
		{
			return;
		}

		String playerName = player.getName().toLowerCase();
		if (!badPlayers.containsKey(playerName))
		{
			BadPlayer bPlayer = new BadPlayer(player);
			bPlayer.incrementDeaths();
		}
		else
		{
			BadPlayer badPlayer = badPlayers.get(playerName);
			badPlayer.incrementDeaths();
		}

		String killerName = player.getKiller().getName().toLowerCase();
		if (!badPlayers.containsKey(killerName))
		{
			BadPlayer bPlayer = new BadPlayer(player.getKiller());
			bPlayer.incrementKills();
		}
		else
		{
			BadPlayer badPlayer = badPlayers.get(killerName);
			badPlayer.incrementKills();
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event)
	{
		new BadPlayer(event.getPlayer());
	}

	private void showTop(String leaderboard, LinkedHashMap<String, String> map)
	{
		int desiredSize = 10;
		int li = 0;
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext())
		{
			iter.next();
			li++;
			if (desiredSize < li)
			{
				iter.remove();
			}	
		}

		if (map.size() < desiredSize)
		{
			for (int i = 0; i < desiredSize - map.size(); i++)
			{
				map.put("Aucun", "-");
			}
		}

		int i = 1;
		for (Entry<String, String> entry : map.entrySet())
		{
			i++;
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holograms setline " + leaderboard + " " + i +" &d" + (i - 1) + "e. &e" + entry.getKey() + " &7(" + entry.getValue() + ")");
		}
		
		while (i < 12)
		{
			i++;
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holograms setline " + leaderboard + " " + i +" &d" + (i - 1) + "e. &eInconnu &7(-)");
		}
	}

}
