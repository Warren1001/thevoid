package com.kabryxis.thevoid.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.kabryxis.kabutils.string.IncrementalString;
import com.kabryxis.thevoid.api.game.Game;

public class Board {
	
	private final Game game;
	private final IncrementalString roundEntry;
	private final Scoreboard board;
	
	private Objective obj;
	
	public Board(Game game) {
		this.game = game;
		this.roundEntry = new IncrementalString(ChatColor.GOLD + "Round [x]", 1);
		this.board = Bukkit.getScoreboardManager().getMainScoreboard();
		reset();
	}
	
	public Scoreboard getScoreboard() {
		return board;
	}
	
	public void reset() {
		this.obj = board.getObjective("thevoid-points");
		if(obj != null) obj.unregister();
		this.obj = board.registerNewObjective("thevoid-points", "dummy");
		obj.setDisplayName(ChatColor.BOLD.toString() + ChatColor.GREEN + "TheVoid Test");
		obj.getScore(roundEntry.reset()).setScore(-1);
	}
	
	public void nextRound() {
		board.resetScores(roundEntry.get());
		obj.getScore(roundEntry.increment()).setScore(-1);
	}
	
	public void start() {
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		game.forEachGamer(g -> g.setGamePoints(0));
	}
	
}
