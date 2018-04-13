package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.Data;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.object.ArenaWalkable;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.VoidRound;
import com.kabryxis.thevoid.round.disintegrate.DisintegrateThread;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.Set;

public class KnockbackDisintegrateHybrid extends VoidRound {
	
	private static final String id = "disintegrateHybrid";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT, Material.SAND };
	private final String metadataKey = id;
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, metadataKey, 750L);
	
	public KnockbackDisintegrateHybrid() {
		super(id, 1);
		inventory[0] = new ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 3).build();
	}
	
	@Override
	public void load(Game game, Arena arena) {
		ArenaWalkable walkData = arena.getCurrentSchematicData().getDataObject(ArenaWalkable.class);
		Location center = arena.getCenter();
		Data.queue(() -> walkData.loadDiamondPatternBlocks(center.getBlockX(), center.getBlockZ()));
	}
	
	@Override
	public void start(Game game, Arena arena) {}
	
	@Override
	public void tick(Game game, Arena arena, int timeLeft) {
		if(timeLeft == 25) {
			if(!thread.isRunning()) thread.start();
			else thread.unpause();
			Threads.start(() -> {
				List<Set<Block>> diamondPatternBlocks = arena.getCurrentSchematicData().getDataObject(ArenaWalkable.class).getDiamondPatternBlocks();
				//for(int i = 0; i < diamondPatternBlocks.size(); i++) {
				for(int i = diamondPatternBlocks.size() - 1; i >= 0 && !thread.isPaused(); i--) {
					Set<Block> blocks = diamondPatternBlocks.get(i);
					if(blocks != null) {
						blocks.forEach(this::disintegrate);
						Threads.sleep(1000);
					}
				}
			});
		}
	}
	
	@Override
	public void end(Game game, Arena arena) {
		thread.pause();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity attacker = event.getDamager();
			if(attacker instanceof Player) {
				event.setCancelled(false);
				event.setDamage(0);
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
	
	public void disintegrate(Block block) {
		if(!block.hasMetadata(metadataKey)) thread.add(block);
	}
	
	@Override
	public void generateDefaults() {
		config.set("round-length", 45);
		config.set("world-names", VoidRound.DEFAULT_worldNames);
		config.set("schematics", VoidRound.DEFAULT_schematics);
	}

}