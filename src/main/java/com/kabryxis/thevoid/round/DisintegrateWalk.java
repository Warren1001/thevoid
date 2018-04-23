package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import com.kabryxis.thevoid.round.utility.DisintegrateThread;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

public class DisintegrateWalk extends VoidRound {
	
	private static final String id = "disintegrateWalk";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT };
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, id, 500L);
	
	private boolean started = false;
	
	public DisintegrateWalk() {
		super(id);
		ItemBuilder builder = new ItemBuilder(levels[0]).name(ChatColor.DARK_GRAY + "Level 1");
		inventory[0] = builder.build();
		inventory[1] = builder.material(levels[1]).name(ChatColor.DARK_RED + "Level 2").build();
		inventory[2] = builder.material(levels[2]).name(ChatColor.RED + "Level 3").build();
	}
	
	@Override
	public void start(Game game, Arena arena) {
		if(!started) {
			started = true;
			thread.start();
		}
		else thread.unpause();
		Threads.start(() -> {
			Threads.sleep(1500);
			while(thread.isRunning() && !thread.isPaused()) {
				Threads.sleep(1000);
				game.forEachGamer(gamer -> gamer.getStandingBlocks().forEach(thread::add));
			}
		});
	}
	
	@Override
	public void end(Game game, Arena arena) {
		thread.pause();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof PlayerMoveEvent) {
			PlayerMoveEvent event = (PlayerMoveEvent)eve;
			Gamer gamer = Gamer.getGamer(event.getPlayer());
			if(!thread.isPaused() && gamer.isAlive()
					&& (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())) {
				gamer.getStandingBlocks().forEach(thread::add);
			}
		}
	}
	
}
