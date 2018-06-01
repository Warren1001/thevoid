package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.data.Lists;
import com.kabryxis.kabutils.random.weighted.conditional.ConditionalWeightedRandomArrayList;
import com.kabryxis.thevoid.VoidRoundManager;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.schematic.BaseSchematic;
import com.kabryxis.thevoid.api.impl.arena.schematic.VoidEmptySchematic;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidRoundInfoRegistry implements RoundInfoRegistry<Arena, BaseSchematic, BasicRound> {
	
	private final ConditionalWeightedRandomArrayList<Arena> arenas = new ConditionalWeightedRandomArrayList<>(2);
	private final ConditionalWeightedRandomArrayList<BaseSchematic> schematics = new ConditionalWeightedRandomArrayList<>(2);
	
	private final Plugin owner;
	private final VoidRoundManager roundManager;
	
	public VoidRoundInfoRegistry(Plugin owner) {
		this.owner = owner;
		this.roundManager = new VoidRoundManager(owner);
		roundManager.addGlobalRequiredDefault("round-length", 30);
		roundManager.addGlobalRequiredDefault("weight", 100);
		roundManager.addGlobalRequiredDefault("worlds", Arrays.asList("void_overworld", "void_nether", "void_end"));
		roundManager.addGlobalRequiredDefault("schematics", Arrays.asList("rainbow", "halfsphere"));
		registerSchematic(new VoidEmptySchematic());
	}
	
	public VoidRoundManager getRoundManager() {
		return roundManager;
	}
	
	@Override
	public void registerArena(Arena arena) {
		arenas.add(arena);
	}
	
	@Override
	public void registerSchematic(BaseSchematic schematic) {
		schematics.add(schematic);
	}
	
	@Override
	public void registerRound(BasicRound round) {
		roundManager.registerRound(round);
	}
	
	@Override
	public void queueArenaData(List<RoundInfo> infos, int i) {
		Map<Arena, List<BaseSchematic>> schems = new HashMap<>();
		for(int amount = 0; amount < i; amount++) {
			BasicRound round = roundManager.getRandomRound();
			Arena arena = arenas.random(round);
			BaseSchematic schematic = schematics.random(round, arena);
			schems.computeIfAbsent(arena, Lists.getGenericCreator()).add(schematic);
			infos.add(new VoidRoundInfo(round, arena, schematic));
		}
		schems.forEach(Arena::queueSchematics);
	}
	
}
