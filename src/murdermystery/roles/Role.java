package murdermystery.roles;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Role {
	
	public static enum Type {
		INNOCENT,
		DETECTIVE,
		MURDERER,
		DEAD;
	}
	
	String rolename;
	Type roleType;
	ItemStack item;
	String objective;
	ChatColor color;
	
	public Role(String name,Role.Type roleAsType,Material i,String o,ChatColor c) {
		this.rolename = name;
		this.roleType = roleAsType;
		this.item = new ItemStack(i,1);
		this.color = c;
		this.objective = o;
	}
	
	public ItemStack getItem() {
		return this.item;
	}
	
	public String getRoleName() {
		return this.rolename;
	}
	
	public Role.Type getRoleType() {
		return this.roleType;
	}
	
	public String getObjective() {
		return objective;
	}
	
	public ChatColor getColor() {
		return color;
	}
}
