package fr.badblock.faction.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BadPlayer
{

	public String							 name;
	public Map<String, Long>    data;
	public File								 file;
	public FileConfiguration		 config;

	public BadPlayer(File config)
	{
		if (!config.exists())
		{
			try
			{
				config.createNewFile();
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}

		this.file = config;
		this.config = YamlConfiguration.loadConfiguration(config);
		this.data = new HashMap<>();

		for (String key : this.config.getKeys(false))
		{
			if (key.equals("name"))
			{
				name = this.config.getString(key).toLowerCase();
				continue;
			}

			long l = this.config.getLong(key);
			data.put(key, l);
		}

		String fileNameWithoutExt = FilenameUtils.removeExtension(config.getName()).toLowerCase();

		save();
		FactionLeaderboard.instance.badPlayers.put(fileNameWithoutExt, this);
	}

	public BadPlayer(Player player)
	{
		File folder = new File(FactionLeaderboard.instance.getDataFolder(), "data");
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		File config = new File(folder, player.getUniqueId().toString() + ".yml");
		if (!config.exists())
		{
			try
			{
				config.createNewFile();
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}

		this.file = config;
		this.config = YamlConfiguration.loadConfiguration(config);
		this.data = new HashMap<>();

		if (this.config.contains("name"))
		{
			this.name = this.config.getString("name").toLowerCase();
		}
		else
		{
			this.name = player.getName().toLowerCase();
		}

		for (String key : this.config.getKeys(false))
		{
			if (key.equals("name"))
			{
				continue;
			}
			long l = this.config.getLong(key);
			data.put(key, l);
		}

		setData(player);

		FactionLeaderboard.instance.badPlayers.put(player.getName().toLowerCase(), this);
	}
	
	public Double getRatio()
	{
		if (!data.containsKey("kills"))
		{
			data.put("kills", 0L);
		}

		if (!data.containsKey("deaths"))
		{
			data.put("deaths", 0L);
		}
		
		double d = ((double) data.get("kills")) / Math.max(1, (double) data.get("deaths")); 
		return (Double) MathUtils.round(d, 2);
	}

	void incrementKey(Player player)
	{
		if (!data.containsKey("key"))
		{
			data.put("key", 0L);
		}
		
		data.put("key", data.get("key") + 1);
		save();
	}

	void incrementBrokenBlocks()
	{
		data.put("brokenBlocks", data.get("brokenBlocks") + 1);
	}

	void incrementKills()
	{
		data.put("kills", data.get("kills") + 1);
	}

	void incrementDeaths()
	{
		data.put("deaths", data.get("deaths") + 1);
	}

	void setData(Player player)
	{
		config.set("name", player.getName().toLowerCase());
		this.name = player.getName().toLowerCase();
		
		data.put("playTime", Integer.toUnsignedLong(player.getStatistic(Statistic.PLAY_ONE_TICK) / 20 / 60));
		if (!data.containsKey("kills"))
		{
			data.put("kills", 0L);
		}
		
		if (!data.containsKey("deaths"))
		{
			data.put("deaths", 0L);
		}

		if (!data.containsKey("key"))
		{
			data.put("key", 0L);
		}

		data.put("balance", Math.round(FactionLeaderboard.instance.economy.getBalance(player)));

		save();
	}

	public void save()
	{
		for (Entry<String, Long> entry : data.entrySet())
		{
			config.set(entry.getKey(), entry.getValue());
		}

		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}