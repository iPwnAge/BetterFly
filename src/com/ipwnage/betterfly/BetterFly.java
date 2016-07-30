package com.ipwnage.betterfly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterFly extends JavaPlugin implements Listener {
	File dataFile = new File(getDataFolder(), "data.yml");
	private HashMap<UUID, PlayerPrefs> flyingPlayers = new HashMap<UUID, PlayerPrefs>();
	FileConfiguration data = new YamlConfiguration();
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		try {
			data.load(dataFile);
			for (String key : data.getConfigurationSection("players").getKeys(false)) {
				flyingPlayers.put(UUID.fromString(key), new PlayerPrefs((String) data.get("players."+key)));
				}
			getLogger().info("Loaded " + flyingPlayers.size() + " player's flying preferences.");
		} catch (IOException e) {
			getLogger().info("Didn't find a data file, making one now.");
			dataFile.getParentFile().mkdirs();
			saveResource("data.yml", false);
		} catch (InvalidConfigurationException|NumberFormatException e) {
			getLogger().log(Level.SEVERE, "The data file was corrupt, there will be no player preferences for flying this launch.", e);
			return;
		} catch (NullPointerException e) {
			//Probably a new config, ignore these
		}
		
		this.getCommand("fly").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
				Player player = (Player) sender;
				if(player.hasPermission("betterfly.canfly") || player.isOp()) {
					UUID puuid = player.getPlayer().getUniqueId();
					if(flyingPlayers.containsKey(puuid)) {
						PlayerPrefs user = flyingPlayers.get(puuid);
						user.toggleFly();
						player.getPlayer().setFlySpeed(user.getSpeed());
						player.getPlayer().setAllowFlight(user.getFly());
						player.getPlayer().setFlying(user.getFly());
						if (user.getFly()) {
							player.sendMessage(ChatColor.YELLOW + "You can now fly!");
						} else {
							player.sendMessage(ChatColor.YELLOW + "You can no longer fly!");
						}
					} else {
						flyingPlayers.put(puuid, new PlayerPrefs(true, 0.1f));
						player.getPlayer().setFlySpeed(0.1f);
						player.getPlayer().setAllowFlight(true);
						player.getPlayer().setFlying(true);
						player.sendMessage(ChatColor.YELLOW + "You can now fly!");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to fly.");
				}
				
		
				return true;
			}
		});
		
		this.getCommand("flyspeed").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
				Player player = (Player) sender;
				UUID puuid = player.getPlayer().getUniqueId();
				if(player.hasPermission("betterfly.speed") || player.isOp()) {
					if(!flyingPlayers.containsKey(puuid)) {
						flyingPlayers.put(puuid, new PlayerPrefs(true, 0.1f));
						player.getPlayer().setAllowFlight(false);
						player.getPlayer().setFlying(false);
					}
					if (args.length == 1 && StringUtils.isNumeric(args[0]) && 10 >= Integer.parseInt(args[0]) && Integer.parseInt(args[0]) >= 1) {
						flyingPlayers.get(puuid).setSpeed(Integer.parseInt(args[0])/10f);
						player.getPlayer().setFlySpeed(flyingPlayers.get(puuid).getSpeed());
						player.sendMessage(ChatColor.YELLOW + "Your flight speed is now " + args[0]);
					} else {
						player.sendMessage(ChatColor.RED + "Please choose a number, 1 through 10 for flight speed.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to set your flight speed.");
				}
				return true;
			}
		});
	}
	
	public void onDisable() {
		for (Object key : flyingPlayers.keySet()) {
			data.set("players."+key, flyingPlayers.get(key).toString());
			}
			try {
				data.save(dataFile);
			} catch (IOException e) {
				getLogger().info("Couldn't save the data file! There will be no player preferences next server start.");
				e.printStackTrace();
			}
	}

	
	
	@EventHandler
	void onPlayerJoin(PlayerJoinEvent player) {
		UUID puuid = player.getPlayer().getUniqueId();
		if(flyingPlayers.containsKey(puuid) && flyingPlayers.get(puuid).getFly()) {
			player.getPlayer().setAllowFlight(true);
			player.getPlayer().setFlying(true);
			player.getPlayer().setFlySpeed(flyingPlayers.get(puuid).getSpeed());
		}
	
	}
	
}
