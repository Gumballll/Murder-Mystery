package murdermystery.roles;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Role {
	
	String rolename;
	int roleid;
	ItemStack item;
	String objective;
	ChatColor color;
	
	public Role(String name,int id,Material i,String o,ChatColor c) {
		this.rolename = name;
		this.roleid = id;
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
	
	public int getRoleId() {
		return this.roleid;
	}
	
	public String getObjective() {
		return objective;
	}
	
	public ChatColor getColor() {
		return color;
	}
}
