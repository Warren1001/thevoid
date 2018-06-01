package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.concurrent.DelayedActionThread;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.entity.DelayedEntityRemover;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.api.util.arena.object.ArenaWalkable;
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

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Anvilstorm extends SurvivalRound { // TODO needs a bit of visual/audio tweaking
	
	private final Random rand = new Random();
	private final ItemStack air = new ItemStack(Material.AIR);
	private final Vector velocity = new Vector(0, -1.25, 0);
	private final DelayedActionThread sandRemover = new DelayedActionThread("Anvil remover - Anvilstorm");
	
	private List<Location> locs = null;
	private BukkitTask task = null;
	
	public Anvilstorm(RoundManager<BasicRound> roundManager) {
		super(roundManager, "anvilstorm", false);
	}
	
	@Override
	public void start(Game game) {
		if(!sandRemover.isRunning()) sandRemover.start();
		else sandRemover.unpause();
		Arena arena = game.getCurrentRoundInfo().getArena();
		locs = arena.getCurrentArenaData().getDataObject(ArenaWalkable.class).getWalkableLocations();
		task = BukkitThreads.syncTimer(new BukkitRunnable() {
			
			@Override
			public void run() {
				for(int i = 0; i < 3 && !locs.isEmpty(); i++) {
					FallingBlock fb = arena.getWorld().spawnFallingBlock(Randoms.getRandomAndRemove(locs).clone().add(0, 40 + rand.nextInt(10) - 5, 0), Material.ANVIL, (byte)0);
					fb.setVelocity(velocity);
					arena.spawnedEntity(fb);
				}
				if(locs.isEmpty()) cancel();
			}
			
		}, 20L, 1L);
	}
	
	@Override
	public void end(Game game) {
		task.cancel();
		task = null;
		locs = null;
		sandRemover.pause();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityChangeBlockEvent) {
			EntityChangeBlockEvent event = (EntityChangeBlockEvent)eve;
			if(event.getEntityType() == EntityType.FALLING_BLOCK) BukkitThreads.syncLater(() -> event.getBlock().getRelative(BlockFace.DOWN).breakNaturally(air), 1L);
			else if(event.getBlock().getType() == Material.ANVIL) sandRemover.add(new DelayedEntityRemover(event.getEntity(), System.currentTimeMillis() + 500L));
		}
		else if(eve instanceof EntityDamageEvent) { // TODO doesnt work??
			System.out.println("1");
			EntityDamageEvent event = (EntityDamageEvent)eve;
			if(event.getCause() == DamageCause.FALLING_BLOCK) {
				System.out.println("2");
				game.getPlayerManager().getPlayer((Player)event.getEntity()).kill();
			}
		}
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", 45);
		data.addDefault("schematics", Collections.singletonList("rainbow"));
	}
	
}
