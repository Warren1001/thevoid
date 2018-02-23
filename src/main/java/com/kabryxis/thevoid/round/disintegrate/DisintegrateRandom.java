package com.kabryxis.thevoid.round.disintegrate;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.AbstractRound;

public class DisintegrateRandom extends AbstractRound {
	
	private static final String id = "disintegrateRandom";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT, Material.SAND };
	private final String metadataKey = id;
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, metadataKey, 2000L);
	
	private boolean started = false;
	
	public DisintegrateRandom() {
		super(id, 1);
		ItemBuilder builder = new ItemBuilder(levels[0]).name(ChatColor.DARK_GRAY + "Level 1");
		inventory[0] = builder.build();
		inventory[1] = builder.material(levels[1]).name(ChatColor.DARK_RED + "Level 2").build();
		inventory[2] = builder.material(levels[2]).name(ChatColor.RED + "Level 3").build();
		inventory[3] = builder.material(levels[3]).name(ChatColor.GREEN + "Level 4").build();
	}
	
	@Override
	public void start(Game game, Arena arena) {
		if(!started) {
			started = true;
			thread.start();
		}
		else thread.unpause();
		Threads.start(() -> {
			List<Location> locs = arena.getWalkableLocations();
			Threads.sleep(1500);
			while(thread.isRunning() && !thread.isPaused() && !locs.isEmpty()) {
				Threads.sleep(50);
				for(int i = 0; i < 4; i++) {
					if(locs.isEmpty()) break;
					disintegrate(Randoms.getRandomAndRemove(locs).getBlock());
				}
			}
		});
	}
	
	@Override
	public void end(Game game, Arena arena) {
		thread.pause();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityChangeBlockEvent) {
			EntityChangeBlockEvent event = (EntityChangeBlockEvent)eve;
			if(event.getEntityType() == EntityType.FALLING_BLOCK) {
				BukkitThreads.syncLater(() -> event.getEntity().remove(), 10L);
			}
		}
	}
	
	@Override
	public void fell(Game game, Gamer gamer) {
		gamer.decrementRoundPoints(false);
		gamer.kill();
		gamer.teleport(20);
	}
	
	public void disintegrate(Block block) {
		if(!block.hasMetadata(metadataKey)) thread.add(block);
	}
	
	@Override
	public void generateDefaults() {
		useDefaults();
	}
	
	@Override
	public void load(Game game, Arena arena) {}
	
}
