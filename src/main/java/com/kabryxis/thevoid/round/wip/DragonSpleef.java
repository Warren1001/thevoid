package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;

import java.util.Collections;

public class DragonSpleef extends VoidRound {
	
	private final int[] coords = { -50, 0, 50 };
	
	public DragonSpleef() {
		super("theend");
	}
	
	@Override
	public void generateDefaults() {
		config.set("round-length", 60);
		config.set("world-names", Collections.singletonList("world_the_end"));
		config.set("schematics", Collections.singletonList("theend"));
		config.set("weight", VoidRound.DEFAULT_weight);
	}
	
	@Override
	public void start(Game game, Arena arena) {
		BukkitThreads.syncLater(() -> {
			Location center = arena.getLocation();
			for(int x : coords) {
				for(int z : coords) {
					if(x != 0 && z != 0) arena.spawnedEntity(center.getWorld().spawn(center.clone().add(x, 1.75, z), EnderDragon.class));
				}
			}
		}, 30L);
		
	}
	
}
