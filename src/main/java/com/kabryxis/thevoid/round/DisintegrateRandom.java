package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.api.util.arena.object.ArenaWalkable;
import com.kabryxis.thevoid.round.utility.DisintegrateThread;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DisintegrateRandom extends SurvivalRound {
	
	private static final String id = "disintegrateRandom";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT };
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, id, 2000L);
	private final ItemStack[] items = new ItemStack[36];
	
	private boolean started = false;
	
	public DisintegrateRandom(RoundManager<BasicRound> roundManager) {
		super(roundManager, id, false);
		ItemBuilder builder = new ItemBuilder(levels[0]).name(ChatColor.DARK_GRAY + "Level 1");
		items[0] = builder.build();
		items[1] = builder.material(levels[1]).name(ChatColor.DARK_RED + "Level 2").build();
		items[2] = builder.material(levels[2]).name(ChatColor.RED + "Level 3").build();
	}
	
	@Override
	public void start(Game game) {
		if(!started) {
			started = true;
			thread.start();
		}
		else thread.unpause();
		Threads.start(() -> {
			List<Location> locs = game.getCurrentRoundInfo().getArena().getCurrentArenaData().getDataObject(ArenaWalkable.class).getWalkableLocations();
			Threads.sleep(1500);
			while(thread.isRunning() && !thread.isPaused() && !locs.isEmpty()) {
				Threads.sleep(50);
				for(int i = 0; i < 4; i++) {
					if(locs.isEmpty()) break;
					thread.add(Randoms.getRandomAndRemove(locs).getBlock());
				}
			}
		});
	}
	
	@Override
	public void end(Game game) {
		thread.pause();
	}
	
}
