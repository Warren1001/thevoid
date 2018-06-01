package com.kabryxis.thevoid.round.utility;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.concurrent.thread.PausableThread;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.concurrent.DelayedAction;
import com.kabryxis.kabutils.spigot.metadata.Metadata;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DisintegrateThread extends PausableThread {
	
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
			active.add(new DisintegrateBlock(block));
			for(int i = 0; i < 50; i++) {
				Block aboveBlock = block.getRelative(0, i, 0);
				if(aboveBlock.getType() != Material.AIR && !aboveBlock.hasMetadata(key)) active.add(new DisintegrateBlock(aboveBlock));
			}
		}
	}
	
	@Override
	public void onPause() {
		BukkitThreads.sync(active::clear);
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
		
		public DisintegrateBlock(Block block) {
			this.block = block;
		}
		
		@Override
		public boolean test() {
			long currentTime = System.currentTimeMillis();
			if(currentTime - last > interval) {
				if(index == levels.length) {
					block.breakNaturally();
					block.removeMetadata(key, value.getOwningPlugin());
					return true;
				}
				last = currentTime;
				if(index == 0) block.setMetadata(key, value);
				block.setType(levels[index]);
				index++;
			}
			return false;
		}
		
	}
	
}
