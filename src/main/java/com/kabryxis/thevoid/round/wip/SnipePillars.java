package com.kabryxis.thevoid.round.wip;

import com.boydti.fawe.FaweCache;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.kabutils.spigot.version.WrappableCache;
import com.kabryxis.kabutils.spigot.version.wrapper.entity.arrow.WrappedEntityArrow;
import com.kabryxis.kabutils.spigot.world.Teleport;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.impl.VoidArena;
import com.kabryxis.thevoid.api.arena.schematic.BaseArenaData;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Collections;

public class SnipePillars extends VoidRound {
	
	public SnipePillars() {
		super("snipepillars");
		inventory[0] = new ItemBuilder(Material.BOW).name(ChatColor.GOLD + "Shoot the pillars!").enchant(Enchantment.ARROW_INFINITE, 1).build();
		inventory[1] = new ItemBuilder(Material.ARROW).build();
	}
	
	@Override
	public void start(Game game, Arena arena) {
		Location[] pillarLocations = Teleport.getEquidistantPoints(arena.getLocation(), game.getGamers().size(), config.get("radius", Integer.class));
		EditSession editSession = ((VoidArena)arena).getEditSession();
		int pillarHeight = config.get("pillar-height", Integer.class);
		for(Location pillarLoc : pillarLocations) {
			int locX = pillarLoc.getBlockX(), minY = pillarLoc.getBlockY(), maxY = minY + pillarHeight, locZ = pillarLoc.getBlockZ();
			editSession.setBlocks(new CuboidRegion(new Vector(locX, minY, locZ), new Vector(locX, maxY, locZ)), FaweCache.getBlock(Material.SMOOTH_BRICK.getId(), 0));
		}
		editSession.flushQueue();
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof ProjectileHitEvent) {
			ProjectileHitEvent event = (ProjectileHitEvent)eve;
			Projectile proj = event.getEntity();
			if(proj instanceof Arrow) {
				Arrow arrow = (Arrow)proj;
				BukkitThreads.sync(() -> { // TODO might need a 1 tick delay
					WrappedEntityArrow<?> entityArrow = WrappableCache.get(WrappedEntityArrow.class);
					entityArrow.set(arrow);
					Block blockHit = entityArrow.getHitBlock();
					if(blockHit != null) blockHit.setType(Material.AIR);
					else System.out.println("ProjectileHitEvent was called but no block was hit.");
					entityArrow.cache();
					arrow.remove();
				});
			}
		}
	}
	
	@Override
	public Location[] getSpawns(Game game) {
		BaseArenaData baseArenaData = game.getCurrentRoundInfo().getArena().getCurrentArenaData();
		return Teleport.getEquidistantPoints(baseArenaData.getCenter().clone().add(0, 11, 0), game.getGamers().size(), config.get("radius", Integer.class));
	}
	
	@Override
	public void generateDefaults() {
		super.generateDefaults();
		config.set("schematics", Collections.singletonList("empty"));
		config.set("pillar-height", 10);
		config.set("radius", 10);
	}
}
