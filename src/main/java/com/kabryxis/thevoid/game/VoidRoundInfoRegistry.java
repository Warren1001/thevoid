package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.random.MultiRandomArrayList;
import com.kabryxis.kabutils.random.RandomArrayList;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.round.Round;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;
import com.kabryxis.thevoid.api.schematic.BaseSchematic;
import com.kabryxis.thevoid.api.schematic.Schematic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidRoundInfoRegistry implements RoundInfoRegistry {
	
	private final MultiRandomArrayList<String, Arena> arenas = new MultiRandomArrayList<>(() -> new HashMap<>(), Arena::getWorldName, 2);
	private final MultiRandomArrayList<String, BaseSchematic> schematics = new MultiRandomArrayList<>(() -> new HashMap<>(), Schematic::getName, 2);
	private final RandomArrayList<Round> rounds = new RandomArrayList<>(2);
	
	@Override
	public void registerArena(Arena arena) {
		arenas.addToList(arena.getWorldName(), arena);
	}
	
	@Override
	public void registerSchematic(BaseSchematic schematic) {
		schematics.addToList(schematic.getName(), schematic);
	}
	
	@Override
	public void registerRound(Round round) {
		rounds.add(round);
	}
	
	@Override
	public void registerRounds(Round... rounds) {
		for(Round round : rounds) {
			registerRound(round);
		}
	}
	
	@Override
	public void queueArenaData(List<RoundInfo> infos, int i) {
		Map<Arena, List<BaseSchematic>> schems = new HashMap<>();
		for(int amount = 0; amount < i; amount++) {
			Round round = rounds.random();
			Arena arena = arenas.random(round.getWorldNames());
			BaseSchematic schematic = schematics.random(round.getSchematics());
			schems.computeIfAbsent(arena, a -> new ArrayList<>()).add(schematic);
			infos.add(new VoidRoundInfo(round, arena, schematic));
		}
		schems.forEach(Arena::queueSchematics);
	}
	
}
