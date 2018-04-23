package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.object.impl.ArenaWalkable;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightningDodge extends VoidRound {
	
	private final Set<BukkitTask> tasks = new HashSet<>();
	
	private BukkitTask mainTask = null;
	
	public LightningDodge() {
		super("lightningdodge");
	}
	
	@Override
	public void start(Game game, Arena arena) {
		List<Location> locs = arena.getCurrentArenaData().getDataObject(ArenaWalkable.class).getWalkableLocations();
		mainTask = BukkitThreads.syncTimer(() -> {
			for(int i = 0; i < 2; i++) {
				Location loc = Randoms.getRandom(locs).clone().add(0, 1.25, 0);
				tasks.add(BukkitThreads.syncTimer(new BukkitRunnable() {
					
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
								tasks.remove(this);
								cancel();
							}
						}
						else currentDelay++;
					}
					
				}, 0L, 3L));
			}
		}, 30L, 3L);
	}
	
	@Override
	public void end(Game game, Arena arena) {
		mainTask.cancel();
		mainTask = null;
		tasks.forEach(BukkitTask::cancel);
		tasks.clear();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageEvent) {
			EntityDamageEvent event = (EntityDamageEvent)eve;
			Entity entity = event.getEntity();
			if(entity instanceof Player) {
				Player player = (Player)entity;
				if(event.getCause() == DamageCause.LIGHTNING) fell(Gamer.getGamer(player));
			}
		}
		else if(eve instanceof BlockIgniteEvent) {
			BlockIgniteEvent event = (BlockIgniteEvent)eve;
			if(event.getCause() == IgniteCause.LIGHTNING) event.setCancelled(true);
		}
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
	
}
