package murdermystery;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import murdermystery.game.Game;
import murdermystery.game.GameManager;

public class Main extends JavaPlugin {
	public static String serverPath;
	public static Integer maxgamesize;
	public static Plugin self;
	public static World gamelobby;
	
	public void onEnable() {		
		new EventListener(this);
		saveDefaultConfig();
		Main.serverPath = getConfig().getString("serverpath");
		Main.maxgamesize = getConfig().getInt("gamesize");
		Main.gamelobby = Bukkit.getWorld(getConfig().getString("gamelobby"));
		if(serverPath == "default") {
			Bukkit.getLogger().log(Level.SEVERE, "You must set the root path of the server in order to use Murder Mystery! Murder Mystery has been disabled.");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
		Main.self = this;
	}

	public void onDisable() {
		for(Integer i=0;i<GameManager.games.size();i++) {
			Game game = GameManager.games.get(i);
			String gamename = game.map.baseMapName+Integer.toString(game.id);
			Bukkit.unloadWorld(gamename, false);
			try {
				FileUtils.forceDelete(new File(Main.serverPath+File.separator+gamename));
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, e.getMessage());
			}
		}
	}
}
