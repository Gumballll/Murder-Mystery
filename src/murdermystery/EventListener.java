package murdermystery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import murdermystery.game.Game;
import murdermystery.game.GameManager;
import murdermystery.roles.Role;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class EventListener implements Listener {
	public Random r = new Random();
	
	public static final ArrayList<PotionType> potions = new ArrayList<PotionType>();
	
	Plugin plugin;
	
	public EventListener(Plugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		potions.add(PotionType.SPEED);
		potions.add(PotionType.SLOWNESS);
		potions.add(PotionType.INVISIBILITY);
	}
	
	@EventHandler
	public void teleportEvent(PlayerTeleportEvent e) {
		
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Game game = GameManager.getGameByPlayerUUID(player.getUniqueId());
		if (game != null) {
			game.removePlayer(player.getUniqueId());
		}
		World world = Main.gamelobby;
		if (world == null) {
			world = Bukkit.getServer().getWorlds().get(0);
		}
		double x = world.getSpawnLocation().getX();
		double y = world.getSpawnLocation().getY();
		double z = world.getSpawnLocation().getZ();
		player.teleport(new Location(world, x, y, z));
	}
	
	@EventHandler
	public void playerChangeWorld(PlayerChangedWorldEvent e) {
		if(GameManager.playerIsInGame(e.getPlayer().getUniqueId())) {
			Game game = GameManager.getGameByPlayerUUID(e.getPlayer().getUniqueId());
			game.removePlayer(e.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		if(GameManager.playerIsInGame(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void interactEvent(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) {
			return;
		}
		if(GameManager.playerIsInGame(e.getPlayer().getUniqueId())) {
			if(e.getClickedBlock().getType() == Material.CAULDRON) {
				Player player = e.getPlayer();
				if(player.getInventory().contains(Material.EMERALD, 1)) {
					player.getInventory().remove(new ItemStack(Material.EMERALD,1));
					ItemStack potion = new ItemStack(Material.POTION,1);
					PotionMeta meta = (PotionMeta) potion.getItemMeta();
					PotionData data = new PotionData(getRandomPotion(),false,false);
					meta.setBasePotionData(data);
					meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
					meta.setDisplayName(ChatColor.RESET+"Mystery Potion");
					meta.setLore(Arrays.asList(ChatColor.GOLD+"This is a Mystery Potion!",ChatColor.RED+"Use at your own risk!"));
					potion.setItemMeta(meta);
				} else {
					player.sendMessage(ChatColor.RED+"You need at least one emerald to use a cauldron!");
				}
			} else {
				e.setCancelled(true);
			}
		} else if(e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
			Sign sign = (Sign) e.getClickedBlock().getState();
			if(sign.getLine(0).equalsIgnoreCase(ChatColor.RED+"[Murder]")){
				Game game = GameManager.getOpenGame();
				if(game != null) {
					game.addUser(e.getPlayer().getUniqueId());
				} else {
					e.getPlayer().sendMessage(ChatColor.RED+"Couldn't find an open game.");
				}
			}
		} else {
			
		}
	}
	
	@EventHandler
	public void signChange(SignChangeEvent e) {
		if(e.getLine(0).equalsIgnoreCase("[Murder]")) {
			e.setLine(0, ChatColor.RED+"[Murder]");
		}
	}
	
	@EventHandler
	public void entityDeathEvent(EntityDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (GameManager.playerIsInGame(player.getUniqueId())) {
				Game game = GameManager.getGameByPlayerUUID(player.getUniqueId());
				if (game != null && game.ingame) {
					player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				}
			}
		}
	}
	
	@EventHandler
	public void entityDamageEntity(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damager = (Player) e.getDamager();
			Player damaged = (Player) e.getEntity();
			Role damagerRole = GameManager.getPlayerRoleByUUID(damager.getUniqueId());
			Role damagedRole = GameManager.getPlayerRoleByUUID(damaged.getUniqueId());
			if(damagerRole != null && damagedRole != null) {
				Game game = GameManager.getGameByPlayerUUID(damaged.getUniqueId());
				if (game == null) {
					return;
				} else if (!game.isPlaying()) {
					e.setCancelled(true);
					return;
				}
				if(damagerRole.getRoleType() == Role.Type.MURDERER) {
					if(damager.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD && damagedRole.getRoleType() != Role.Type.DEAD) {
						game.killPlayer(damaged.getUniqueId());
						damaged.sendTitle(ChatColor.RED+"You died!",ChatColor.RED+"The murderer killed you!",20,100,20);
					}
				}
			}
		} else if(e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
			Projectile projectile = (Projectile) e.getDamager();
			if(projectile.getShooter() instanceof Player) {
				//Player shot arrow at player
				Player shooter = (Player) projectile.getShooter();
				Role shooterRole = GameManager.getPlayerRoleByUUID(shooter.getUniqueId());
				Player damaged = (Player) e.getEntity();
				Role damagedRole = GameManager.getPlayerRoleByUUID(damaged.getUniqueId());
				if(shooterRole != null && damagedRole != null) {
					if(shooterRole.getRoleType() == Role.Type.DETECTIVE) {
						//Detective shot an arrow
						if(damagedRole.getRoleType() == Role.Type.INNOCENT) {
							//Detective shot an innocent
							
							Game game = GameManager.getGameByPlayerUUID(shooter.getUniqueId());
							if (game == null) {
								return;
							} else if (!game.isPlaying()) {
								e.setCancelled(true);
								return;
							}
							game.killPlayer(shooter.getUniqueId());
							shooter.sendTitle(ChatColor.RED+"You died!", ChatColor.GREEN+"You killed an innocent!",20,100,20);
							game.killPlayer(damaged.getUniqueId());
							damaged.sendTitle(ChatColor.RED+"You died!",ChatColor.AQUA+"The Detective mistook you for the "+ChatColor.RED+"Murderer!",20,100,20);
						} else if(damagedRole.getRoleType() == Role.Type.MURDERER) {
							Game game = GameManager.getGameByPlayerUUID(damaged.getUniqueId());
							if (game == null) {
								return;
							} else if (!game.isPlaying()) {
								e.setCancelled(true);
								return;
							}
							game.killPlayer(damaged.getUniqueId());
							damaged.sendTitle(ChatColor.RED+"You died!", ChatColor.AQUA+"The detective shot you!",20,100,20);
							shooter.sendTitle(ChatColor.YELLOW+"You killed the "+ChatColor.RED+"Murderer!","",20,100,20);
							game.gameEnd("§aInnocent");
						}
						shooter.getInventory().addItem(new ItemStack(Material.ARROW,1));
					}
				}
			}
		} else if (e.getDamager() instanceof Firework && e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			if (GameManager.playerIsInGame(player.getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}
	
	public PotionType getRandomPotion() {
		return potions.get(r.nextInt(potions.size()-1));
	}

	@EventHandler
	public void itemDrop(PlayerDropItemEvent e) {
		if(e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
			e.setCancelled(true);
		}
	}
}
