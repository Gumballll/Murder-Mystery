package murdermystery.game;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import murdermystery.Main;
import murdermystery.maps.Map;
import murdermystery.roles.Role;

public class GameManager {
	public static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
	
	public static boolean copyDirectory(String source,String dest) {
		try {
			FileUtils.copyDirectoryToDirectory(new File(source),new File(dest));
			return true;
		} catch(IOException e) {
			Bukkit.getLogger().log(Level.SEVERE,"[Murder Mystery]" + e.getMessage());
			return false;
		}
	}
	
	public static boolean newGame(Map map) {
		if(games.size() < 1)  {
			Integer gameID = games.size();
			String mapname = map.baseMapName+Integer.toString(gameID);
			boolean ok = copyDirectory('.' + File.separator + map.baseMapName, '.' + File.separator + mapname);
			if(ok) {
				Bukkit.getLogger().log(Level.WARNING, "ok");
			} else {
				Bukkit.getLogger().log(Level.WARNING, "bad");
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public static void removeGame(Game game) {
		for(Integer i=0;i<games.size();i++) {
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
		for(Integer i=0;i<games.size();i++) {
			Game game = games.get(i);
			for(UUID id : game.users) {
				if(id == uuid) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean worldIsGameWorld(World world) {
		for(Integer i=0;i<games.size();i++) {
			Game game = games.get(i);
			if(game.world == world) {
				return true;
			}
		}
		return false;
	}
	
	public static Game getGameByPlayerUUID(UUID uuid) {
		for(Integer i=0;i<games.size();i++) {
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
		for(Integer i=0;i<4;i++) {
			Game game = games.get(i);
			if(game.ingame != true) {
				return game;
			}
		}
		return null; //TODO: Generate a new world and game if all games are full.
	}
	
	public static void removeGame(Integer i) {
		games.remove(i);
	}
}
