package murdermystery.roles;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Murderer extends Role {
	public Murderer() {
		super("Murderer",Role.Type.MURDERER,Material.IRON_SWORD,"Kill everyone!",ChatColor.RED);
	}
}
