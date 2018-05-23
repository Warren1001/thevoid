package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.data.Lists;
import com.kabryxis.kabutils.random.MultiRandomArrayList;
import com.kabryxis.kabutils.random.RandomArrayList;
import com.kabryxis.kabutils.random.WeightedMultiRandomArrayList;
import com.kabryxis.kabutils.random.WeightedRandomArrayList;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.impl.VoidArena;
import com.kabryxis.thevoid.api.arena.schematic.BaseSchematic;
import com.kabryxis.thevoid.api.arena.schematic.impl.VoidBaseSchematic;
import com.kabryxis.thevoid.api.arena.schematic.impl.VoidEmptySchematic;
import com.kabryxis.thevoid.api.round.Round;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoidRoundInfoRegistry implements RoundInfoRegistry {
	
	private final MultiRandomArrayList<String, VoidArena> arenas = new WeightedMultiRandomArrayList<>(() -> new HashMap<>(), Arena::getWorldName, 2);
	private final MultiRandomArrayList<String, VoidBaseSchematic> schematics = new WeightedMultiRandomArrayList<>(() -> new HashMap<>(), BaseSchematic::getName, 2);
	private final VoidEmptySchematic emptySchematic = new VoidEmptySchematic();
	private final RandomArrayList<Round> rounds = new WeightedRandomArrayList<>(2);
	
	@Override
	public void registerArena(Arena arena) {
		if(!(arena instanceof VoidArena)) throw new IllegalArgumentException("This RoundInfoRegistry instance only accepts Arena objects of VoidArena type.");
		arenas.addToList(arena.getWorldName(), (VoidArena)arena);
	}
	
	@Override
	public void registerSchematic(BaseSchematic schematic) {
		if(!(schematic instanceof VoidBaseSchematic)) throw new IllegalArgumentException("This RoundInfoRegistry instance only accepts BaseSchematic objects of VoidBaseSchematic type.");
		schematics.addToList(schematic.getName(), (VoidBaseSchematic)schematic);
	}
	
	@Override
	public void registerRound(Round round) {
		rounds.add(round);
	}
	
	@Override
	public void queueArenaData(List<RoundInfo> infos, int i) {
		Map<Arena, List<BaseSchematic>> schems = new HashMap<>();
		for(int amount = 0; amount < i; amount++) {
			Round round = rounds.random();
			Arena arena = arenas.random(round.getWorldNames());
			BaseSchematic schematic = getSchematic(round, arena);
			schems.computeIfAbsent(arena, Lists.getGenericCreator()).add(schematic);
			infos.add(new VoidRoundInfo(round, arena, schematic));
		}
		schems.forEach(Arena::queueSchematics);
	}
	
	private BaseSchematic getSchematic(Round round, Arena arena) {
		List<String> schematicsList = round.getSchematics();
		return schematicsList.isEmpty() || (schematicsList.size() == 1 && schematicsList.get(0).equalsIgnoreCase("empty")) ? emptySchematic : schematics.random(schematicsList);
	}
	
}
