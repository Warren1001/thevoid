package com.kabryxis.thevoid.round.utility;

import com.kabryxis.thevoid.api.arena.schematic.util.SchematicWork;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class HangingSheepWork implements SchematicWork {
	
	private Set<Block> fenceBlocks = new HashSet<>();
	
	@Override
	public void doExtra(Block block, Material type, int data) {
		if(type == Material.FENCE) fenceBlocks.add(block);
	}
	
	public Set<Block> getFenceBlocks() {
		return fenceBlocks;
	}
	
}
