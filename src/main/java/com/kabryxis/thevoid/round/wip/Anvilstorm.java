package com.kabryxis.thevoid.round.wip;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.AbstractRound;

public class Anvilstorm extends AbstractRound { // TODO needs a bit of visual/audio tweaking
	
	private final Random rand = new Random();
	private final ItemStack air = new ItemStack(Material.AIR);
	private final Vector velocity = new Vector(0, -1.25, 0);
	
	private List<Location> locs = null;
	private BukkitTask task = null;
	
	public Anvilstorm() {
		super("anvilstorm", 1);
	}
	
	@Override
	public void load(Game game, Arena arena) {}
	
	@Override
	public void start(Game game, Arena arena) {
		locs = arena.getWalkableLocations();
		task = BukkitThreads.syncTimer(new BukkitRunnable() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				for(int i = 0; i < 3 && !locs.isEmpty(); i++) {
					FallingBlock fb = arena.getWorld().getWorld().spawnFallingBlock(Randoms.getRandomAndRemove(locs).clone().add(0, 40 + rand.nextInt(10) - 5, 0), Material.ANVIL, (byte)0);
					fb.setVelocity(velocity);
					arena.spawnedCustomEntity(fb);
				}
				if(locs.isEmpty()) cancel();
			}
			
		}, 20L, 1L);
	}
	
	@Override
	public void end(Game game, Arena arena) {
		task.cancel();
		task = null;
		locs = null;
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityChangeBlockEvent) {
			EntityChangeBlockEvent event = (EntityChangeBlockEvent)eve;
			if(event.getEntityType() == EntityType.FALLING_BLOCK) {
				event.getBlock().getRelative(BlockFace.DOWN).breakNaturally(air);
			}
			else if(event.getBlock().getType() == Material.ANVIL) {
				BukkitThreads.syncLater(() -> { // TODO optimize with singular thread
					event.getEntity().remove();
				}, 30L);
			}
		}
		else if(eve instanceof EntityDamageEvent) { // TODO might not work
			System.out.println("1");
			EntityDamageEvent event = (EntityDamageEvent)eve;
			if(event.getCause() == DamageCause.FALLING_BLOCK) {
				System.out.println("2");
				Gamer gamer = Gamer.getGamer((Player)event.getEntity());
				gamer.decrementRoundPoints(false);
				gamer.kill();
			}
		}
	}
	
	@Override
	public void fell(Game game, Gamer gamer) {
		gamer.decrementRoundPoints(false);
		gamer.kill();
		gamer.teleport(20);
	}
	
	@Override
	public void generateDefaults() {
		config.set("round-length", 45);
		config.set("world-names", Collections.singletonList("world"));
		config.set("schematics", Collections.singletonList("rainbow"));
	}
	
}
