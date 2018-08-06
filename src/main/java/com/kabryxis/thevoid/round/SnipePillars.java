package com.kabryxis.thevoid.round;

import com.boydti.fawe.FaweCache;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.kabutils.spigot.version.wrapper.entity.arrow.WrappedEntityArrow;
import com.kabryxis.kabutils.spigot.world.Teleport;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.schematic.BaseArenaData;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
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
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class SnipePillars extends SurvivalRound {
	
	private final ItemStack[] items = new ItemStack[36];
	
	public SnipePillars(RoundManager<BasicRound> roundManager) {
		super(roundManager, "snipepillars", true);
		items[0] = new ItemBuilder(Material.BOW).name(ChatColor.GOLD + "Shoot the pillars!").enchant(Enchantment.ARROW_INFINITE, 1).build();
		items[17] = new ItemBuilder(Material.ARROW).build();
	}
	
	@Override
	public void setup(GamePlayer gamePlayer) {
		super.setup(gamePlayer);
		gamePlayer.getInventory().setContents(items);
	}
	
	@Override
	public void start(Game game) {
		Arena arena = game.getCurrentRoundInfo().getArena();
		Location[] pillarLocations = Teleport.getEquidistantPoints(arena.getLocation(), game.getPlayerManager().getAlivePlayers().size(), getData().getInt("radius"));
		EditSession editSession = arena.getEditSession();
		int pillarHeight = getData().getInt("pillar-height");
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
				BukkitThreads.sync(() -> {
					Block blockHit = WrappedEntityArrow.newInstance(arrow).getHitBlock();
					if(blockHit != null) blockHit.setType(Material.AIR);
					//else System.out.println("ProjectileHitEvent was called but no block was hit.");
					arrow.remove();
				});
			}
		}
	}
	
	@Override
	public Location[] getSpawns(Game game, int size) {
		BaseArenaData baseArenaData = game.getCurrentRoundInfo().getArena().getCurrentArenaData();
		return Teleport.getEquidistantPoints(baseArenaData.getCenter().clone().add(0, 11, 0), size, getData().getInt("radius"));
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("schematics", Collections.singletonList("empty"));
		data.addDefault("pillar-height", 10);
		data.addDefault("radius", 10);
	}

}
