package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.round.utility.DisintegrateThread;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class DisintegrateWalk extends SurvivalRound {
	
	private static final String id = "disintegrateWalk";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT };
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, id, 500L);
	private final ItemStack[] items = new ItemStack[36];
	
	private boolean started = false;
	
	public DisintegrateWalk(RoundManager<BasicRound> roundManager) {
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
			Threads.sleep(1500);
			while(thread.isRunning() && !thread.isPaused()) {
				Threads.sleep(1000);
				game.getPlayerManager().forEachAlivePlayer(gamePlayer -> gamePlayer.getStandingBlocks().forEach(thread::add));
			}
		});
	}
	
	@Override
	public void end(Game game) {
		thread.pause();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof PlayerMoveEvent) {
			PlayerMoveEvent event = (PlayerMoveEvent)eve;
			GamePlayer gamePlayer = game.getPlayerManager().getPlayer(event.getPlayer());
			if(!thread.isPaused() && gamePlayer.isAlive()
					&& (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())) {
				gamePlayer.getStandingBlocks().forEach(thread::add);
			}
		}
	}
	
}
