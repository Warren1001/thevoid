package com.kabryxis.thevoid.round.disintegrate;

import com.kabryxis.kabutils.cache.Cache;
import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.concurrent.thread.PausableThread;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.concurrent.DelayedAction;
import com.kabryxis.kabutils.spigot.entity.DelayedEntityRemover;
import com.kabryxis.kabutils.spigot.metadata.Metadata;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DisintegrateThread extends PausableThread {
	
	private final Queue<DisintegrateBlock> cache = new ConcurrentLinkedQueue<>();
	private final Set<DisintegrateBlock> active = ConcurrentHashMap.newKeySet();
	private final Set<DelayedEntityRemover> sandSet = ConcurrentHashMap.newKeySet();
	private final Runnable action = () -> {
		//synchronized(active) {
			active.removeIf(DisintegrateBlock::test);
		//}
		//synchronized(sandSet) {
			sandSet.removeIf(DelayedEntityRemover::test);
		//}
	};
	
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
		if(!isPaused() && isRunning()) {
			DisintegrateBlock db = cache.isEmpty() ? new DisintegrateBlock() : cache.poll();
			db.reuse(block);
			//synchronized(active) {
				active.add(db);
			//}
		}
	}
	
	public void queueSand(Entity sand) {
		DelayedEntityRemover remover = Cache.get(DelayedEntityRemover.class);
		remover.reuse(sand, System.currentTimeMillis() + 500L);
		//synchronized(sandSet) {
			sandSet.add(remover);
		//}
	}
	
	@Override
	public void onPause() {
		BukkitThreads.sync(() -> {
			//synchronized(active) {
				active.forEach(DisintegrateBlock::cache);
				active.clear();
			//}
			//synchronized(sandSet) {
				sandSet.forEach(DelayedEntityRemover::cache);
				sandSet.clear();
			//}
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
		
		private int index = -1;
		private long last = 0L;
		
		public void reuse(Block block) {
			this.block = block;
		}
		
		@Override
		public boolean test() {
			if(index == levels.length - 1) {
				cache();
				return true;
			}
			long currentTime = System.currentTimeMillis();
			if(currentTime - last > interval) {
				last = currentTime;
				if(index == -1) block.setMetadata(key, value);
				index++;
				block.setType(levels[index]);
			}
			return false;
		}
		
		@Override
		public void cache() {
			block.removeMetadata(key, value.getOwningPlugin());
			block = null;
			index = -1;
			last = 0L;
			cache.add(this);
		}
		
	}
	
}
