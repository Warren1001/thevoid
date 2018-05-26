package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.world.Teleport;
import com.kabryxis.thevoid.VoidRoundManager;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.schematic.BaseArenaData;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;

import java.util.Collections;
import java.util.List;

public class DragonSpleef extends VoidRound {
	
	private final int[] coords = { -50, 0, 50 };
	
	public DragonSpleef(VoidRoundManager roundManager) {
		super(roundManager, "dragonspleef");
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", 60);
		data.addDefault("worlds", Collections.singletonList("world_the_end"));
		data.addDefault("schematics", Collections.singletonList("theend"));
	}
	
	@Override
	public void start(Game game) { // TODO a bit repetitive, think of a way to have a little more automation with methods (only issue being custom teleporting)
		Arena arena = game.getCurrentRoundInfo().getArena();
		BaseArenaData arenaData = arena.getCurrentArenaData();
		List<Gamer> gamers = game.getGamers();
		int size = gamers.size();
		Location[] spawnLocs = Teleport.getEquidistantPoints(arenaData.getCenter(), size, arenaData.getRadius());
		BukkitThreads.sync(() -> {
			for(int i = 0; i < size; i++) {
				Gamer gamer = gamers.get(i);
				Location spawnLoc = spawnLocs[i];
				gamer.setRoundPoints(0, true);
				gamer.setGameMode(GameMode.ADVENTURE);
				gamer.setRoundPoints(1, false);
				gamer.teleport(spawnLoc);
			}
		});
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
