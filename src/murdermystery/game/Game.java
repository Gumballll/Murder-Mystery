package murdermystery.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import murdermystery.maps.Map;
import murdermystery.roles.Detective;
import murdermystery.roles.Innocent;
import murdermystery.roles.Murderer;

public class Game {
	public HashMap<UUID,GamePlayer> players = new HashMap<UUID,GamePlayer>();
	public ArrayList<UUID> users = new ArrayList<UUID>();
	public ArrayList<UUID> spectators = new ArrayList<UUID>();
	public Map map;
	public World world;
	public Integer id;
	public boolean ingame = false;
	public Random random = new Random();
	public Integer maxplayers;
	
	public Game(Map m,Integer id,World world,Integer maxplayers) {
		this.map = m;
		this.id = id;
		this.world = world;
		this.maxplayers = maxplayers;
	}
	
	public void killPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		players.remove(uuid);
		players.put(uuid, new GamePlayer(new Dead(),player));
		player.setGameMode(GameMode.SPECTATOR);
	}
	
	public void removePlayer(UUID uuid) {
		if(users.contains(uuid)) {
		users.remove(uuid);
		}
		if(players.containsKey(uuid)) {
			players.remove(uuid);
		}
	}
	
	public boolean addUser(UUID id) {
		if(users.size() < maxplayers) {
			if(!users.contains(id)) {
				users.add(id);
				for(UUID uuid : users) {
					Bukkit.getPlayer(uuid).sendMessage(ChatColor.GOLD+Bukkit.getPlayer(id).getName()+" has joined the game! "+users.size()+"/"+maxplayers);
				}
				Location loc = new Location(world,map.spawnPoint[0],map.spawnPoint[1],map.spawnPoint[2]);
				Bukkit.getPlayer(id).teleport(loc);
				Bukkit.getPlayer(id).sendMessage(ChatColor.LIGHT_PURPLE+"Teleporting you to a game...");
				Bukkit.getPlayer(id).setGameMode(GameMode.ADVENTURE);
				Bukkit.getPlayer(id).getInventory().clear();
				if(users.size() == maxplayers) {
					startGame();
				}
				return true;
			} else {
				Bukkit.getPlayer(id).sendMessage(ChatColor.RED+"You are already in that game!");
				return false;
			}
		} else {
			return false;
		}
	}
	
	public void displayRoles() {
		for(UUID uuid : users) {
			GamePlayer player = players.get(uuid);
			if(player.getRole().getRoleType() != Role.Type.INNOCENT) {
				player.getPlayer().sendMessage("You are the "+player.getRole().getRoleName());
				player.getPlayer().sendTitle(ChatColor.GOLD+"You are the "+player.getRole().getColor()+player.getRole().getRoleName(), ChatColor.GOLD+player.getRole().getObjective(),20,100,20);
			} else {
				player.getPlayer().sendMessage(player.getRole().getColor()+"You are "+player.getRole().getRoleName());
				player.getPlayer().sendTitle("You are "+ChatColor.GREEN+"Innocent", ChatColor.GOLD+"Stay alive as long as possible!",20,100,20);
			}
		}
	}
	
	public void startGame() {	
		this.ingame = true;
		
		HashMap<UUID,GamePlayer> rolegen = new HashMap<UUID,GamePlayer>();
		
		for(UUID uuid : users) {
			GamePlayer gp = new GamePlayer(new Innocent(),Bukkit.getPlayer(uuid));
			rolegen.put(uuid, gp);
			gp.getPlayer().sendMessage(Integer.toString(users.size()));
		}
		
		
		
		Integer dIndex = random.nextInt(users.size()-1);
		Player detective = Bukkit.getPlayer(users.get(dIndex));
		users.remove(detective.getUniqueId());
		
		Integer userSize = users.size() == 1 ? 2 : users.size();
		Integer mIndex = random.nextInt(userSize-1);
		Player murderer = Bukkit.getPlayer(users.get(mIndex));
		users.add(users.size(),detective.getUniqueId());
		
		rolegen.remove(users.get(mIndex));
		rolegen.remove(users.get(dIndex));
		
		GamePlayer gamedetective = new GamePlayer(new Detective(), detective);
		GamePlayer gamemurderer = new GamePlayer(new Murderer(), murderer);
		
		rolegen.put(murderer.getUniqueId(), gamemurderer);
		rolegen.put(detective.getUniqueId(), gamedetective);
		
		players = rolegen;
		
		displayRoles();
		
		Location loc = new Location(world,map.startPoint[0],map.startPoint[1],map.startPoint[2]);
		for(UUID uuid : users) {
			Bukkit.getPlayer(uuid).teleport(loc);
		}
		
		for(UUID uuid : users) {
			Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN+"The game is now starting!");
			Bukkit.getPlayer(uuid).sendMessage(users.toString());
			Bukkit.getPlayer(uuid).setGameMode(GameMode.ADVENTURE);
		}
		
		murderer.getInventory().addItem(new ItemStack(Material.IRON_SWORD,1));
		detective.getInventory().addItem(new ItemStack(Material.BOW,1));
		detective.getInventory().addItem(new ItemStack(Material.ARROW,1));
	}
	
	public void teleportUsersToStart() {
	}
	
	public void movePlayerToSpectator(UUID id) {
		if(players.containsKey(id)) {
			players.remove(id);
			spectators.add(id);
		}
	}
	
	public ArrayList<UUID> getUsers() {
		return users;
	}
	
	public World getWorld() {
		return world;
	}
	
	public Integer[] getSpawnPoint() {
		return map.spawnPoint;
	}
	
	public int size() {
		return users.size();
	}
}
