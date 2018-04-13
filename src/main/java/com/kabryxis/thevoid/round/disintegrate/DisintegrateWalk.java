package com.kabryxis.thevoid.round.disintegrate;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.VoidRound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class DisintegrateWalk extends VoidRound {
	
	private static final String id = "disintegrateWalk";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT, Material.SAND };
	private final String metadataKey = id;
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, metadataKey, 500L);
	
	private boolean started = false;
	
	public DisintegrateWalk() {
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
			Threads.sleep(1500);
			while(thread.isRunning() && !thread.isPaused()) {
				Threads.sleep(1000);
				game.forEachGamer(this::disintegrate);
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
				disintegrate(gamer);
			}
		}
		else if(eve instanceof EntityChangeBlockEvent) {
			EntityChangeBlockEvent event = (EntityChangeBlockEvent)eve;
			if(event.getEntityType() == EntityType.FALLING_BLOCK) thread.queueSand(event.getEntity());
		}
	}
	
	@Override
	public void fell(Game game, Gamer gamer) {
		gamer.decrementRoundPoints(false);
		gamer.kill();
		gamer.teleport(20);
	}
	
	public void disintegrate(Gamer gamer) {
		gamer.forEachBlockStanding(this::canDisintegrate, thread::add);
	}
	
	public boolean canDisintegrate(Block block) {
		return !block.hasMetadata(metadataKey) && block.getType() != Material.AIR;
	}
	
	@Override
	public void generateDefaults() {
		useDefaults();
	}
	
	@Override
	public void load(Game game, Arena arena) {}
	
	@Override
	public void tick(Game game, Arena arena, int i) {}
	
}
