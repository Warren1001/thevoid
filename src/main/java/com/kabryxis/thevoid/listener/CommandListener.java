package com.kabryxis.thevoid.listener;

import com.kabryxis.kabutils.command.Com;
import com.kabryxis.kabutils.spigot.command.BukkitCommandIssuer;
import com.kabryxis.kabutils.spigot.world.WorldManager;
import com.kabryxis.thevoid.TheVoid;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.schematic.BaseSchematic;
import com.kabryxis.thevoid.api.schematic.Schematic;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CommandListener {
	
	private final TheVoid plugin;
	
	public CommandListener(TheVoid plugin) {
		this.plugin = plugin;
	}
	
	@Com(aliases = {"start"})
	public boolean onStart(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(args.length == 1) {
			plugin.start(Integer.parseInt(args[0]));
			return true;
		}
		return false;
	}
	
	@Com(aliases = {"buildmode"})
	public boolean onBuildMode(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length == 0) {
			Player player = issuer.getPlayer();
			player.setGameMode(player.getGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE);
			Gamer gamer = Gamer.getGamer(player);
			gamer.setBuilderMode(!gamer.isInBuilderMode());
			return true;
		}
		return false;
	}
	
	@Com(aliases = {"sch"})
	public boolean onSch(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length == 2) {
			new Schematic(args[0], Gamer.getGamer(issuer.getPlayer()).getSelection(), Boolean.parseBoolean(args[1]));
		}
		else if(args.length == 5) {
			new BaseSchematic(args[0], Gamer.getGamer(issuer.getPlayer()).getSelection(), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Integer.parseInt(args[1]));
			return true;
		}
		return false;
	}
	
	@Com(aliases = {"erase"})
	public boolean onErase(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length == 0) {
			Chunk chunk = issuer.getPlayer().getLocation().getChunk();
			WorldManager.getWorld(chunk.getWorld()).eraseChunk(chunk.getX(), chunk.getZ());
			return true;
		}
		return false;
	}
	
	@Com(aliases = {"refresh"})
	public boolean onRefresh(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length == 0) {
			Chunk chunk = issuer.getPlayer().getLocation().getChunk();
			WorldManager.getWorld(chunk.getWorld()).refreshChunk(chunk.getX(), chunk.getZ());
			return true;
		}
		return false;
	}
	
	@Com(aliases = {"tpw"})
	public boolean onTpw(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length == 4) {
			String worldName = args[0];
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			int z = Integer.parseInt(args[3]);
			issuer.getPlayer().teleport(new Location(Bukkit.getWorld(worldName), x, y, z));
			return true;
		}
		return false;
	}
	
	private BukkitTask task;
	
	@Com(aliases = {"timertest"})
	public boolean onTimerTest(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		Player player = issuer.getPlayer();
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("stop")) {
				if(task != null) task.cancel();
				return true;
			}
		}
		else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("start")) {
				int i = Integer.parseInt(args[1]);
				task = new BukkitRunnable() {
					
					final long base = 24000 * 4, start = 14500 + base, end = 21500 + base;
					
					long time = start - i;
					
					@Override
					public void run() {
						time += i;
						if(time >= end) time = start;
						player.getWorld().setFullTime(time);
					}
					
				}.runTaskTimer(plugin, 0, 1);
				return true;
			}
		}
		return false;
	}
	
	@Com(aliases = {"particletest"})
	public boolean onParticleTest(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		Player player = issuer.getPlayer();
		if(args.length == 0) {
			Location loc = new Location(player.getWorld(), 0, 100, 0);
			new BukkitRunnable() {
				
				int stages = 3, delay = 3;
				
				int currentStage = stages, currentDelay = 0;
				
				@Override
				public void run() {
					spawnParticles(loc, currentStage * 0.8, 12);
					if(currentDelay == delay) {
						currentDelay = 0;
						currentStage--;
						if(currentStage < 0) {
							loc.getWorld().strikeLightning(loc);
							cancel();
						}
					}
					else currentDelay++;
				}
				
			}.runTaskTimer(plugin, 0L, 3L);
			return true;
		}
		return false;
	}
	
	public void spawnParticles(Location center, double radius, int points) {
		double slice = 2 * Math.PI / points, centerX = center.getX(), centerZ = center.getZ();
		for(int point = 0; point < points; point++) {
			double angle = slice * point;
			double x = centerX + radius * Math.cos(angle);
			double z = centerZ + radius * Math.sin(angle);
			Location loc = new Location(center.getWorld(), x, center.getY(), z);
			loc.getWorld().playEffect(loc, Effect.COLOURED_DUST, 2);
		}
	}
	
	@Com(aliases = {"game"})
	public boolean onGame(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length > 0) {
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("pause")) {
					plugin.getThread().pause();
					return true;
				}
				if(args[0].equalsIgnoreCase("unpause")) {
					plugin.getThread().unpause();
					return true;
				}
			}
			else if(args[0].equalsIgnoreCase("set") && args.length == 3) {
				if(args[1].equalsIgnoreCase("time")) {
					plugin.getGame().getCountdownManager().getCurrentlyActive(plugin.getThread()).setTime(Integer.parseInt(args[2]) + 1);
					return true;
				}
			}
		}
		return false;
	}
	
	@Com(aliases = {"tpcenter"})
	public boolean onTpCenter(BukkitCommandIssuer issuer, String alias, String[] args) {
		if(!issuer.isPlayer()) return false;
		if(args.length == 0) {
			Gamer gamer = Gamer.getGamer(issuer.getPlayer());
			gamer.teleport(gamer.getGame().getCurrentRoundInfo().getArena().getCurrentSchematicData().getCenter());
		}
		return false;
	}
	
}
