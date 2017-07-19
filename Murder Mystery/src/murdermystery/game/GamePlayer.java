package murdermystery.game;

import org.bukkit.entity.Player;

import murdermystery.roles.Role;

public class GamePlayer {
	private Role role;
	private Player player;
	
	public GamePlayer(Role r,Player player) {
		this.role = r;
		this.player = player;
	}
	
	public Role getRole() {
		return role;
	}
	
	public Player getPlayer() {
		return player;
	}
}
