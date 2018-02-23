package com.kabryxis.thevoid.round.disintegrate;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.concurrent.thread.PausableThread;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.metadata.Metadata;

public class DisintegrateThread extends PausableThread {
	
	private final Queue<DisintegrateBlock> cache = new ConcurrentLinkedQueue<>();
	
	private final Material[] levels;
	private final String key;
	private final long interval;
	private final MetadataValue value = Metadata.getEmptyMetadataValue();
	private final Set<DisintegrateBlock> active = new HashSet<>(), queue = new HashSet<>();
	
	private final Runnable action = () -> {
		long t = System.currentTimeMillis();
		active.removeIf(block -> block.update(t));
		active.addAll(queue);
		queue.clear();
	};
	
	public DisintegrateThread(String name, Material[] levels, String key, long interval) {
		super(name);
		this.levels = levels;
		this.key = key;
		this.interval = interval;
	}
	
	@Override
	public void run() {
		while(isRunning()) {
			pauseCheck();
			BukkitThreads.sync(action);
			Threads.sleep(50);
		}
		clear();
	}
	
	@Override
	public void onPause() {
		clear();
	}
	
	@Override
	public void onUnpause() {}
	
	public void add(Block block) {
		if(!isPaused() && isRunning()) {
			DisintegrateBlock db = cache.isEmpty() ? new DisintegrateBlock() : cache.poll();
			db.reuse(block);
			queue.add(db);
		}
	}
	
	private void clear() {
		BukkitThreads.sync(() -> {
			active.forEach(DisintegrateBlock::cache);
			active.clear();
		});
	}
	
	private class DisintegrateBlock {
		
		private Block block;
		
		private int index = -1;
		private long last = 0L;
		
		private boolean update(long currentTime) {
			if(index == levels.length - 1) {
				cache();
				return true;
			}
			if(currentTime - last > interval) {
				last = currentTime;
				if(index == -1) block.setMetadata(key, value);
				index++;
				block.setType(levels[index]);
			}
			return false;
		}
		
		private void reuse(Block block) {
			this.block = block;
		}
		
		private void cache() {
			block.removeMetadata(key, value.getOwningPlugin());
			block = null;
			index = -1;
			last = 0L;
			cache.add(this);
		}
		
	}
	
}
