package murdermystery.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import murdermystery.Main;
import murdermystery.maps.Map;
import murdermystery.roles.Dead;
import murdermystery.roles.Detective;
import murdermystery.roles.Innocent;
import murdermystery.roles.Murderer;
import murdermystery.roles.Role;
import net.minecraft.server.v1_12_R1.EntityFireworks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFirework;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Game {
	public HashMap<UUID,GamePlayer> players = new HashMap<UUID,GamePlayer>();
	public ArrayList<UUID> users = new ArrayList<UUID>();
	public ArrayList<UUID> spectators = new ArrayList<UUID>();
	private boolean playing;
	public Map map;
	public World world;
	public Integer id;
	public boolean ingame = false;
	public Random random = new Random();
	public Integer maxplayers;

	public Game(Map m,Integer id,World world,Integer maxplayers) {
		this.map = m;
		this.id = id;
		this.world = world;
		this.maxplayers = maxplayers;
		this.playing = false;
	}

	public boolean allPlayersAreDead() {
		for(UUID uuid : users) {
			GamePlayer gp = players.get(uuid);
			if(gp.getRole().getRoleType() == Role.Type.DETECTIVE || gp.getRole().getRoleType() == Role.Type.INNOCENT) {
				return false; //Detects if everyone BUT the murderer is dead.
			}
		}
		return true;
	}

	public void removePlayer(UUID uuid) {
		if(users.contains(uuid)) {
			users.remove(uuid);
		}
		if(players.containsKey(uuid)) {
			players.remove(uuid);
		}
		if (isPlaying() && GameManager.getPlayerRoleByUUID(uuid).getRoleType() == Role.Type.MURDERER) {
			gameEnd("§aInnocent");
		}
	}

	public void killPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		players.remove(uuid);
		players.put(uuid, new GamePlayer(new Dead(),player));
		player.setGameMode(GameMode.SPECTATOR);
		if(allPlayersAreDead()) {
			gameEnd("§c§lMurderer");
			for (GamePlayer pl : players.values()) {
				if (pl.getRole().getRoleType() == Role.Type.MURDERER) {
					Firework fw = pl.getPlayer().getWorld().spawn(pl.getPlayer().getLocation(), Firework.class);
					FireworkMeta meta = fw.getFireworkMeta();
					FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(Color.RED).with(Type.BALL).build();
					meta.addEffect(fireworkEffect);
					fw.setFireworkMeta(meta);
					
					EntityFireworks entFire = ((CraftFirework) fw).getHandle();
					entFire.expectedLifespan = 4;
				}
			}
		}
	}

	public void gameEnd(String winner) {
		setPlaying(false);
		for(UUID uuid : users) {
			Player player = Bukkit.getPlayer(uuid);
			player.sendMessage("§e====================");
			player.sendMessage("");
			player.sendMessage("      §aGame End");
			player.sendMessage("       §6Winner:");
			player.sendMessage("      " + winner);
			player.sendMessage("");
			player.sendMessage("§e====================");
		}
		
		Game game = this;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = Main.gamelobby.getSpawnLocation();
				for (Player pl : world.getPlayers()) {
					pl.teleport(loc);
					pl.setGameMode(GameMode.SURVIVAL);
				}
				for(UUID uuid : users) {
					Player player = Bukkit.getPlayer(uuid);
					if (player.isOnline() == false || player == null) {
						continue;
					}
					player.getInventory().clear();
				}
				ingame = false;
				GameManager.removeGame(game);
			}
		}.runTaskLater(Main.self, 20*10);
	}

	public boolean addUser(UUID id) {
		if(users.size() < maxplayers) {
			if(!users.contains(id)) {
				users.add(id);
				for(UUID uuid : users) {
					Bukkit.getServer().getPlayer(uuid).sendMessage(ChatColor.GOLD+Bukkit.getServer().getPlayer(id).getName()+" has joined the game! "+users.size()+"/"+maxplayers);
				}
				Location loc = new Location(world,map.spawnPoint[0],map.spawnPoint[1],map.spawnPoint[2]);
				Bukkit.getServer().getPlayer(id).teleport(loc);
				Bukkit.getServer().getPlayer(id).sendMessage(ChatColor.LIGHT_PURPLE+"Teleporting you to a game...");
				Bukkit.getServer().getPlayer(id).setGameMode(GameMode.ADVENTURE);
				Bukkit.getServer().getPlayer(id).getInventory().clear();
				if(users.size() == maxplayers) {
					startGame();
				}
				return true;
			} else {
				Bukkit.getServer().getPlayer(id).sendMessage(ChatColor.RED+"You are already in that game!");
				return false;
			}
		} else {
			return false;
		}
	}

	public void displayRoles() {
		for(UUID uuid : users) {
			GamePlayer player = players.get(uuid);
			if(player.getRole().getRoleType() != Role.Type.INNOCENT) {
				player.getPlayer().sendMessage("You are the "+player.getRole().getRoleName());
				player.getPlayer().sendTitle(ChatColor.GOLD+"You are the "+player.getRole().getColor()+player.getRole().getRoleName(), ChatColor.GOLD+player.getRole().getObjective(),20,100,20);
			} else {
				player.getPlayer().sendMessage(player.getRole().getColor()+"You are "+player.getRole().getRoleName());
				player.getPlayer().sendTitle("You are "+ChatColor.GREEN+"Innocent", ChatColor.GOLD+"Stay alive as long as possible!",20,100,20);
			}
		}
	}

	public void startGame() {	
		this.ingame = true;

		HashMap<UUID,GamePlayer> rolegen = new HashMap<UUID,GamePlayer>();

		for(UUID uuid : users) {
			GamePlayer gp = new GamePlayer(new Innocent(),Bukkit.getPlayer(uuid));
			rolegen.put(uuid, gp);
		}

		Integer dIndex = random.nextInt(users.size()-1);
		Player detective = Bukkit.getPlayer(users.get(dIndex));
		users.remove(detective.getUniqueId());

		Integer userSize = users.size() == 1 ? 2 : users.size();
		Integer mIndex = random.nextInt(userSize-1);
		Player murderer = Bukkit.getPlayer(users.get(mIndex));
		users.add(detective.getUniqueId());

		rolegen.remove(users.get(mIndex));
		rolegen.remove(users.get(dIndex));

		GamePlayer gamedetective = new GamePlayer(new Detective(), detective);
		GamePlayer gamemurderer = new GamePlayer(new Murderer(), murderer);

		rolegen.put(murderer.getUniqueId(), gamemurderer);
		rolegen.put(detective.getUniqueId(), gamedetective);

		players = rolegen;

		displayRoles();

		Location loc = new Location(world,map.startPoint[0],map.startPoint[1],map.startPoint[2]);
		for(UUID uuid : users) {
			Player player = Bukkit.getPlayer(uuid);
			player.sendMessage(ChatColor.GREEN+"The game will start in ten seconds!");
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HAT, 100, 8);
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				setPlaying(true);
				for(UUID uuid : users) {
					Player player = Bukkit.getPlayer(uuid);
					player.sendMessage(ChatColor.GREEN+"The game is now starting!");
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HAT, 100, 8);
					player.teleport(loc);
				}

				ItemStack bow = new ItemStack(Material.BOW,1);
				ItemMeta meta = bow.getItemMeta();
				meta.setDisplayName("§r§3Detective's Bow");
				bow.setItemMeta(meta);

				ItemStack knife = new ItemStack(Material.IRON_SWORD,1);
				meta = knife.getItemMeta();
				meta.setDisplayName("§r§c§lKnife");

				if (murderer.isOnline() && murderer != null) {
					murderer.getInventory().setHeldItemSlot(1);
					murderer.getInventory().addItem(knife);
				} else {
					gameEnd("§aInnocent");
				}
				if (detective.isOnline() && detective != null) {
					detective.getInventory().addItem(bow);
					detective.getInventory().addItem(new ItemStack(Material.ARROW,1));
				}
			}
		}.runTaskLater(Main.self, 20*10); //20 ticks in a second. 200 ticks = 10 seconds.
	}

	public ArrayList<UUID> getUsers() {
		return users;
	}

	public World getWorld() {
		return world;
	}

	public Integer[] getSpawnPoint() {
		return map.spawnPoint;
	}

	public int size() {
		return users.size();
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
}
