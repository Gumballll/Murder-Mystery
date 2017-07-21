package murdermystery.game;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import murdermystery.Main;
import murdermystery.maps.Acacia;
import murdermystery.maps.Map;
import murdermystery.roles.Role;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;


public class GameManager {
	public static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
	
	public static boolean copyDirectory(String source,String dest) {
		try {
			FileUtils.copyDirectory(new File(source),new File(dest));
			return true;
		} catch(IOException e) {
			Bukkit.getLogger().log(Level.SEVERE,"[Murder Mystery]" + e.getMessage());
			return false;
		}
	}
	
	public static Game newGame(Map map) {
		int gameID = games.size();
		String mapname = map.baseMapName+Integer.toString(gameID);
		boolean ok = copyDirectory('.' + File.separator + map.baseMapName, '.' + File.separator + mapname);
		
		try {
			FileUtils.forceDelete( new File("."+File.separator+mapname+File.separator+"uid.dat") );
		} catch(IOException e) {
		}
		
		if(ok) {
			Bukkit.getLogger().log(Level.WARNING, "ok");
		} else {
			Bukkit.getLogger().log(Level.WARNING, "bad");
		}
		World world = Bukkit.getServer().createWorld(new WorldCreator(mapname));
		world.setMonsterSpawnLimit(0);
		world.setAmbientSpawnLimit(0);
		Game game = new Game(map,gameID,world,Main.maxgamesize);
		games.put(gameID, game);
		return game;
	}
	
	public static void removeGame(Game game) {
		for(int i=0;i<games.size();i++) {
			if(games.get(i) == game) {
				games.remove(i);
				Bukkit.getServer().unloadWorld(game.world,false);
				try {
					FileUtils.deleteDirectory(new File(Main.serverPath+"\\"+game.map.baseMapName+Integer.toString(i)));
				} catch(IOException e) {
					
				}
			}
		}
	}
	
	public static Role getPlayerRoleByUUID(UUID uuid) {
		if(getGameByPlayerUUID(uuid) != null) {
			Game game = getGameByPlayerUUID(uuid);
			GamePlayer player = game.players.get(uuid);
			return player.getRole();
		} else {
			return null;
		}
	}
	
	public static boolean playerIsInGame(UUID uuid) {
		if (uuid == null) {
			return false;
		}
		if (games == null || games.isEmpty()) {
			return false;
		}
		for(int i=0;i<games.size();i++) {
			Game game = games.get(i);
			if (game.players == null || game.players.isEmpty()) {
				continue;
			}
			for(GamePlayer gPlayer : game.players.values()) {
				if(gPlayer.getPlayer().getUniqueId() == uuid) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean playerIsInGameUserList(UUID uuid) {
		for(int i=0;i<games.size();i++) {
			Game game = games.get(i);
			for(UUID id : game.users) {
				if(uuid == id) {
					return true;
				}
 			}
		}
		return false;
	}
	
	public static boolean worldIsGameWorld(World world) {
		for(int i=0;i<games.size();i++) {
			Game game = games.get(i);
			if(game.world == world) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean changePlayerRole(UUID uuid,Role role) {
		for(int i=0;i<games.size();i++) {
			Game game = games.get(i);
			if(game.players.get(uuid) != null) {
				game.players.remove(uuid);
				game.players.put(uuid, new GamePlayer(role,Bukkit.getServer().getPlayer(uuid)));
				return true;
			}
		}
		return false;
	}
	
	public static Game getGameByPlayerUUID(UUID uuid) {
		for(int i=0;i<games.size();i++) {
			Game game = games.get(i);
			for(UUID id : game.users) {
				if(id == uuid) {
					return game;
				}
			}
		}
		return null;
	}
	
	public static Game getOpenGame() {
		for(int i=0;i<games.size();i++) {
			Game game = games.get(i);
			if(game.ingame != true) {
				return game;
			}
		}
		return newGame(new Acacia());
	}
	
	public static void removeGame(Integer i) {
		games.remove(i);
	}
}
