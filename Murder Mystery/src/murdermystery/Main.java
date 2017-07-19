package murdermystery;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import murdermystery.game.GameManager;
import murdermystery.maps.Acacia;

public class Main extends JavaPlugin {
	public static String serverPath;
	
	public void onEnable() {		
		new EventListener(this);
		saveDefaultConfig();
		Main.serverPath = getConfig().getString("serverpath");
		if(serverPath == "default") {
			Bukkit.getLogger().log(Level.SEVERE, "You must set the root path of the server in order to use Murder Mystery! Murder Mystery has been disabled.");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
		GameManager.newGame(new Acacia());
	}

	public void onDisable() {
		
	}
}
