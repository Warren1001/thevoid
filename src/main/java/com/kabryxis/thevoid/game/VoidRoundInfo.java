package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.round.Round;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.schematic.BaseSchematic;

public class VoidRoundInfo implements RoundInfo {
	
	private Round round;
	private Arena arena;
	private BaseSchematic schematic;
	
	public VoidRoundInfo(Round round, Arena arena, BaseSchematic schematic) {
		setRound(round);
		setArena(arena);
		setSchematic(schematic);
	}
	
	public void setRound(Round round) {
		this.round = round;
	}
	
	public void setArena(Arena arena) {
		this.arena = arena;
	}
	
	public void setSchematic(BaseSchematic schematic) {
		this.schematic = schematic;
	}
	
	@Override
	public Round getRound() {
		return round;
	}
	
	@Override
	public Arena getArena() {
		return arena;
	}
	
	@Override
	public BaseSchematic getSchematic() {
		return schematic;
	}
	
	@Override
	public void load(Game game) {
		arena.nextSchematic();
		BukkitThreads.syncLater(() -> {
			arena.loadChunks();
			BukkitThreads.syncLater(() -> {
				arena.loadSchematic();
				round.load(game, arena);
			}, 20 * 2);
		}, 20 * 1);
	}
	
}
