package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.VoidRound;
import org.bukkit.event.Event;

import java.util.Collections;

public class DragonSpleef extends VoidRound {
	
	private final int[] coords = { -50, 0, 50 };
	
	public DragonSpleef() {
		super("theend", 1);
	}
	
	@Override
	public void generateDefaults() {
		config.set("round-length", 60);
		config.set("world-names", Collections.singletonList("world_the_end"));
		config.set("schematics", Collections.singletonList("theend"));
	}
	
	@Override
	public void start(Game game, Arena arena) {
		BukkitThreads.syncLater(() -> {
			for(int x : coords) {
				for(int z : coords) {
					//if(x != 0 && z != 0) arena.spawnEntity(x, 1.75, z, EnderDragon.class);
				}
			}
		}, 30L);
		
	}
	
	@Override
	public void event(Game game, Event eve) {}
	
	@Override
	public void fell(Game game, Gamer gamer) {
		gamer.decrementRoundPoints(false);
		gamer.kill();
		gamer.teleport(20);
	}
	
	@Override
	public void load(Game game, Arena arena) {}
	
	@Override
	public void end(Game game, Arena arena) {}
	
	@Override
	public void tick(Game game, Arena arena, int i) {}
	
}
