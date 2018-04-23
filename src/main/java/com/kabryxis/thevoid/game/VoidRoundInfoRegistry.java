package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.random.MultiRandomArrayList;
import com.kabryxis.kabutils.random.RandomArrayList;
import com.kabryxis.kabutils.random.WeightedMultiRandomArrayList;
import com.kabryxis.kabutils.random.WeightedRandomArrayList;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.impl.VoidArena;
import com.kabryxis.thevoid.api.arena.schematic.IBaseSchematic;
import com.kabryxis.thevoid.api.arena.schematic.impl.VoidBaseSchematic;
import com.kabryxis.thevoid.api.round.Round;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidRoundInfoRegistry implements RoundInfoRegistry {
	
	private final MultiRandomArrayList<String, VoidArena> arenas = new WeightedMultiRandomArrayList<>(() -> new HashMap<>(), Arena::getWorldName, 2);
	private final MultiRandomArrayList<String, VoidBaseSchematic> schematics = new WeightedMultiRandomArrayList<>(() -> new HashMap<>(), IBaseSchematic::getName, 2);
	private final RandomArrayList<Round> rounds = new WeightedRandomArrayList<>(2);
	
	@Override
	public void registerArena(Arena arena) {
		if(!(arena instanceof VoidArena)) throw new IllegalArgumentException("This RoundInfoRegistry instance only accepts Arena objects of VoidArena type.");
		arenas.addToList(arena.getWorldName(), (VoidArena)arena);
	}
	
	@Override
	public void registerSchematic(IBaseSchematic schematic) {
		if(!(schematic instanceof VoidBaseSchematic)) throw new IllegalArgumentException("This RoundInfoRegistry instance only accepts BaseSchematic objects of VoidBaseSchematic type.");
		schematics.addToList(schematic.getName(), (VoidBaseSchematic)schematic);
	}
	
	@Override
	public void registerRound(Round round) {
		rounds.add(round);
	}
	
	@Override
	public void queueArenaData(List<RoundInfo> infos, int i) {
		Map<Arena, List<IBaseSchematic>> schems = new HashMap<>();
		for(int amount = 0; amount < i; amount++) {
			Round round = rounds.random();
			Arena arena = arenas.random(round.getWorldNames());
			IBaseSchematic schematic = schematics.random(round.getSchematics());
			schems.computeIfAbsent(arena, a -> new ArrayList<>()).add(schematic);
			infos.add(new VoidRoundInfo(round, arena, schematic));
		}
		schems.forEach(Arena::queueSchematics);
	}
	
}
