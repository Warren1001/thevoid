package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.version.object.dragon.pet.PetDragons;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.Location;

import java.util.Collections;

public class DragonSpleef extends SurvivalRound {
	
	private final int[] coords = { -50, 0, 50 };
	
	public DragonSpleef(RoundManager<BasicRound> roundManager) {
		super(roundManager, "dragonspleef", false);
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", 60);
		data.addDefault("worlds", Collections.singletonList("void_end"));
		data.addDefault("schematics", Collections.singletonList("halfsphere"));
	}
	
	@Override
	public void start(Game game) {
		Arena arena = game.getCurrentRoundInfo().getArena();
		BukkitThreads.syncLater(() -> {
			Location center = arena.getLocation();
			for(int x : coords) {
				for(int z : coords) {
					if(x != 0 && z != 0) arena.spawnedEntity(PetDragons.newInstance(center.clone().add(x, 1.75, z), null, center).getBukkit());
				}
			}
		}, 30L);
		
	}
	
}
