package murdermystery.roles;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Detective extends Role {
	public Detective() {
		super("Detective",Role.Type.DETECTIVE,Material.BOW,"Find and kill the murderer!",ChatColor.AQUA);
	}
}
