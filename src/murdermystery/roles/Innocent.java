package murdermystery.roles;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Innocent extends Role {
	public Innocent() {
		super("Innocent",Role.Type.INNOCENT,Material.AIR,"Stay alive as long as possible!",ChatColor.GREEN);
	}
}
