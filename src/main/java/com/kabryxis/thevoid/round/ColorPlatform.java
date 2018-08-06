package com.kabryxis.thevoid.round;

import com.boydti.fawe.FaweCache;
import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.MathHelp;
import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.world.Teleport;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.api.util.arena.ArenaEntry;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ColorPlatform extends SurvivalRound {
	
	private final BaseBlock air = ArenaEntry.AIR;
	private final BaseBlock[] baseBlocks = { FaweCache.getBlock(35, 0), FaweCache.getBlock(35, 1), FaweCache.getBlock(35, 2),
			FaweCache.getBlock(35, 3), FaweCache.getBlock(35, 4), FaweCache.getBlock(35, 6),
			FaweCache.getBlock(35, 7), FaweCache.getBlock(35, 12), FaweCache.getBlock(35, 15) };
	private final ItemStack[] itemStacks = { getWool(DyeColor.WHITE), getWool(DyeColor.ORANGE), getWool(DyeColor.MAGENTA),
			getWool(DyeColor.LIGHT_BLUE), getWool(DyeColor.YELLOW), getWool(DyeColor.PINK),
			getWool(DyeColor.GRAY), getWool(DyeColor.BROWN), getWool(DyeColor.BLACK)};
	private final Map<Arena, Map<Region, BaseBlock>> arenaSections = new HashMap<>();
	
	private int noEraseIndex;
	
	public ColorPlatform(RoundManager<BasicRound> roundManager) {
		super(roundManager, "colorplatform", false);
	}
	
	@Override
	public void load(Game game, RoundInfo info) {
		Arena arena = info.getArena();
		if(!arenaSections.containsKey(arena)) {
			Map<Region, BaseBlock> sections = new HashMap<>(9);
			Location loc = arena.getLocation();
			int platformSize = getData().getInt("platform-size");
			int minX = loc.getBlockX() - (platformSize + MathHelp.floor(platformSize / 2.0));
			int minZ = loc.getBlockZ() - (platformSize + MathHelp.floor(platformSize / 2.0));
			int y = loc.getBlockY();
			for(int ix = 0; ix < 3; ix++) {
				for(int iz = 0; iz < 3; iz++) {
					int mx = minX + (platformSize * ix);
					int mz = minZ + (platformSize * iz);
					int lx = minX + (platformSize * (ix + 1)) - 1;
					int lz = minZ + (platformSize * (iz + 1)) - 1;
					int index = (ix * 3) + iz;
					sections.put(new CuboidRegion(new Vector(mx, y, mz),
							new Vector(lx, y, lz)), baseBlocks[index]);
				}
			}
			arenaSections.put(arena, sections);
		}
		noEraseIndex = Randoms.getRandomIndex(baseBlocks);
		fillArena(arena);
	}
	
	@Override
	public void start(Game game) {
		ItemStack item = itemStacks[noEraseIndex];
		BukkitThreads.sync(() -> game.getPlayerManager().forEachAlivePlayer(gamePlayer -> gamePlayer.getInventory().setItem(0, item)));
	}
	
	@Override
	public void customTimer(Game game) {
		Arena arena = game.getCurrentRoundInfo().getArena();
		int attempts = getData().getInt("attempts");
		int speedUp = getData().getInt("speed-up");
		for(int i = 0; i < attempts; i++) {
			if(i != 0) fillArena(arena);
			Threads.sleep(4000 - i * speedUp);
			eraseMostArena(arena);
			Threads.sleep(2000);
			if(i != attempts - 1) {
				noEraseIndex = Randoms.getRandomIndex(baseBlocks, noEraseIndex);
				ItemStack item = itemStacks[noEraseIndex];
				BukkitThreads.sync(() -> game.getPlayerManager().forEachAlivePlayer(gamePlayer -> gamePlayer.getInventory().setItem(0, item)));
			}
		}
	}
	
	@Override
	public void unload(Game game) {
		eraseArena(game.getCurrentRoundInfo().getArena());
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("schematics", Collections.singletonList("empty"));
		data.addDefault("round-length", -1);
		data.addDefault("platform-size", 9);
		data.addDefault("attempts", 3);
		data.addDefault("speed-up", 500);
		data.addDefault("radius", 2.75);
	}
	
	@Override
	public Location[] getSpawns(Game game, int amount) {
		return Teleport.getEquidistantPoints(game.getCurrentRoundInfo().getArena().getCurrentArenaData().getCenter(), amount, getData().getDouble("radius"));
	}
	
	protected void fillArena(Arena arena) {
		EditSession editSession = arena.getEditSession();
		arenaSections.get(arena).forEach((region, block) -> { // for whatever reason, cuboidregions dont place all blocks..
			Vector min = region.getMinimumPoint(), max = region.getMaximumPoint();
			for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					editSession.setBlock(x, min.getBlockY(), z, block);
				}
			}
		});
		editSession.flushQueue();
	}
	
	protected void eraseMostArena(Arena arena) {
		EditSession editSession = arena.getEditSession();
		BaseBlock noErase = baseBlocks[noEraseIndex];
		arenaSections.get(arena).entrySet().stream().filter(entry -> !entry.getValue().equals(noErase)).forEach(entry -> { // for whatever reason, cuboidregions dont place all blocks..
			Region region = entry.getKey();
			Vector min = region.getMinimumPoint(), max = region.getMaximumPoint();
			for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					editSession.setBlock(x, min.getBlockY(), z, air);
				}
			}
		});
		editSession.flushQueue();
	}
	
	protected void eraseArena(Arena arena) {
		EditSession editSession = arena.getEditSession();
		Location loc = arena.getLocation();
		int platformSize = getData().getInt("platform-size");
		int radiusDown = platformSize + MathHelp.floor(platformSize / 2.0);
		int radiusUp = platformSize + MathHelp.ceil(platformSize / 2.0);
		int minX = loc.getBlockX() - radiusDown;
		int minZ = loc.getBlockZ() - radiusDown;
		int maxX = loc.getBlockX() + radiusUp;
		int maxZ = loc.getBlockZ() + radiusUp;
		int y = loc.getBlockY();
		editSession.setBlocks(new CuboidRegion(new Vector(minX, y, minZ), new Vector(maxX, y, maxZ)), air);
		editSession.flushQueue();
	}
	
	private ItemStack getWool(DyeColor color) {
		return new Wool(color).toItemStack(1);
	}
	
}
