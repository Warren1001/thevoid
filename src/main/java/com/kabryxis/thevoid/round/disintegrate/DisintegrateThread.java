package com.kabryxis.thevoid.round.disintegrate;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.concurrent.thread.PausableThread;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.concurrent.DelayedAction;
import com.kabryxis.kabutils.spigot.metadata.Metadata;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DisintegrateThread extends PausableThread {
	
	private final Queue<DisintegrateBlock> cache = new ConcurrentLinkedQueue<>();
	private final Set<DisintegrateBlock> active = ConcurrentHashMap.newKeySet();
	private final Runnable action = () -> active.removeIf(DisintegrateBlock::test);
	
	private final Material[] levels;
	private final String key;
	private final long interval;
	private final MetadataValue value = Metadata.getEmptyMetadataValue();
	
	public DisintegrateThread(String name, Material[] levels, String key, long interval) {
		super(name);
		this.levels = levels;
		this.key = key;
		this.interval = interval;
	}
	
	public void add(Block block) {
		if(isRunning() && !isPaused() && block.getType() != Material.AIR && !block.hasMetadata(key)) {
			DisintegrateBlock db = cache.isEmpty() ? new DisintegrateBlock() : cache.poll();
			db.reuse(block);
			active.add(db);
			for(int i = 0; i < 50; i++) {
				Block aboveBlock = block.getRelative(0, i, 0);
				if(aboveBlock.getType() != Material.AIR && !aboveBlock.hasMetadata(key)) {
					DisintegrateBlock aboveDb = cache.isEmpty() ? new DisintegrateBlock() : cache.poll();
					aboveDb.reuse(aboveBlock);
					active.add(aboveDb);
				}
			}
		}
	}
	
	@Override
	public void onPause() {
		BukkitThreads.sync(() -> {
			active.forEach(DisintegrateBlock::cache);
			active.clear();
		});
	}
	
	@Override
	public void onUnpause() {}
	
	@Override
	public void run() {
		while(isRunning()) {
			pauseCheck();
			BukkitThreads.sync(action);
			Threads.sleep(50);
		}
		onPause();
	}
	
	private class DisintegrateBlock implements DelayedAction {
		
		private Block block;
		
		private int index = 0;
		private long last = 0L;
		
		public void reuse(Block block) {
			this.block = block;
		}
		
		@Override
		public boolean test() {
			long currentTime = System.currentTimeMillis();
			if(currentTime - last > interval) {
				if(index == levels.length) {
					block.breakNaturally();
					cache();
					return true;
				}
				last = currentTime;
				if(index == 0) block.setMetadata(key, value);
				block.setType(levels[index]);
				index++;
			}
			return false;
		}
		
		@Override
		public void cache() {
			block.removeMetadata(key, value.getOwningPlugin());
			block = null;
			index = 0;
			last = 0L;
			cache.add(this);
		}
		
	}
	
}
