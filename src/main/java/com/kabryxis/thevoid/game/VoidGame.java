package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.DataQueue;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.version.WrappableCache;
import com.kabryxis.kabutils.spigot.version.wrapper.packet.out.chat.WrappedPacketPlayOutChat;
import com.kabryxis.kabutils.spigot.world.ChunkLoader;
import com.kabryxis.kabutils.spigot.world.Teleport;
import com.kabryxis.kabutils.time.CountdownManager;
import com.kabryxis.thevoid.TheVoid;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.object.ArenaDataObjectRegistry;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.Round;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VoidGame implements Game {
	
	private final String name = "thevoid-game";
	private final Logger logger = Logger.getLogger(name);
	private final List<Gamer> gamers = new ArrayList<>();
	private final DataQueue<RoundInfo> infos = new DataQueue<>();
	private final WrappedPacketPlayOutChat<?> actionMessage = WrappableCache.get(WrappedPacketPlayOutChat.class);
	private final Board board = new Board(this);
	private final CountdownManager cm = new CountdownManager();
	
	private final TheVoid plugin;
	private final RoundInfoRegistry infoRegistry;
	private final ArenaDataObjectRegistry objectRegistry;
	
	private Location spawn;
	private int alive;
	
	public VoidGame(TheVoid plugin, RoundInfoRegistry infoRegistry, ArenaDataObjectRegistry objectRegistry) {
		this.plugin = plugin;
		this.infoRegistry = infoRegistry;
		this.objectRegistry = objectRegistry;
		cm.constructNewCountdown("preGame", 1000, false, time -> broadcastMessage("Starting in " + time + " seconds."));
		cm.constructNewCountdown("gameTimer", 1000, true, time -> {
			RoundInfo roundInfo = getCurrentRoundInfo();
			roundInfo.getRound().tick(this, roundInfo.getArena(), time);
			actionMessage.newInstance(ChatColor.GOLD.toString() + time);
			forEachGamer(gamer -> {
				gamer.sendPacket(actionMessage);
				if(time <= 5) gamer.playSound(Sound.NOTE_PLING, 0.7F, 1F);
			});
		}, () -> {
			actionMessage.newInstance("");
			forEachGamer(gamer -> {
				gamer.sendPacket(actionMessage);
				gamer.playSound(Sound.NOTE_PLING, 0.7F, 0.5F);
			});
			actionMessage.clear();
		});
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Plugin getOwner() {
		return plugin;
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}
	
	@Override
	public void threadStart() {
		infos.getNext().load(this);
		forEachGamer(g -> {
			g.setScoreboard(board.getScoreboard());
			g.setGame(this);
		});
		cm.count("preGame", 5);
		board.start();
	}
	
	@Override
	public boolean canRun() {
		return infos.hasNext();
	}
	
	@Override
	public void next() {
		infos.nextIndex();
		Collections.shuffle(gamers);
		alive = gamers.size();
	}
	
	@Override
	public void start() {
		RoundInfo info = getCurrentRoundInfo();
		Round round = info.getRound();
		Arena arena = info.getArena();
		Location[] spawns = Teleport.getEquidistantPoints(arena.getCurrentSchematicData().getCenter().clone().add(0, 0.75, 0), gamers.size(), arena.getCurrentSchematicData().getSchematic().getRadius());
		BukkitThreads.sync(() -> {
			for(int i = 0; i < spawns.length; i++) {
				Gamer gamer = gamers.get(i);
				gamer.setRoundPoints(0, true);
				Location spawn = spawns[i];
				if(gamer.isAlive()) gamer.teleport(spawn);
				else gamer.revive(spawn);
				gamer.setInventory(round.getInventory(), round.getArmor());
				gamer.setRoundPoints(round.getStartingPoints(), false);
			}
		});
		round.start(this, arena);
		if(infos.hasNext()) infos.getNext().load(this);
	}
	
	@Override
	public void timer() {
		RoundInfo info = getCurrentRoundInfo();
		Round round = info.getRound();
		int roundLength = round.getRoundLength();
		if(roundLength == -1) round.customTimer();
		else cm.count("gameTimer", roundLength);
	}
	
	@Override
	public void end() {
		RoundInfo info = getCurrentRoundInfo();
		Round round = info.getRound();
		Arena arena = info.getArena();
		round.end(this, arena);
		arena.endOfRound();
		calculateRoundWinners();
		forEachGamer(gamer -> {
			gamer.setRoundPoints(0);
			gamer.clearEffects();
			gamer.clearInventory();
		});
		Threads.sleep(3500);
		board.nextRound();
		//BukkitThreads.syncLater(() -> info.getArena().eraseSchematic(), 5);
		info.getArena().eraseSchematic();
	}
	
	@Override
	public void threadEnd() {
		infos.resetIndex();
		infos.clear();
		broadcastMessage(getGameWinner().getName() + " wins!");
		board.reset(); // Must be after game winner announcer method.
		forEachGamer(gamer -> {
			gamer.reset();
			gamer.setGame(null);
			gamer.teleport(spawn);
		});
	}
	
	@Override
	public void pause() {
		cm.pauseAll();
	}
	
	@Override
	public void unpause() {
		cm.unpauseAll();
	}
	
	@Override
	public RoundInfo getCurrentRoundInfo() {
		return infos.getCurrent();
	}
	
	@Override
	public void forEachGamer(Consumer<? super Gamer> action) {
		gamers.forEach(action);
	}
	
	@Override
	public void callEvent(Event event) {
		if(isInProgress()) getCurrentRoundInfo().getRound().event(this, event);
	}
	
	@Override
	public boolean isInProgress() {
		return infos.getCurrentIndex() != -1;
	}
	
	@Override
	public void addGamer(Gamer gamer) {
		gamers.add(gamer);
		if(!isInProgress()) gamer.teleport(spawn);
		else {
			if(gamer.getGamePoints() == 0) gamer.setGamePoints(0);
			gamer.kill(getCurrentRoundInfo().getArena().getCenter().clone().add(0, 20, 0));
		}
	}
	
	@Override
	public void removeGamer(Gamer gamer) {
		gamers.remove(gamer);
	}
	
	@Override
	public void died(Gamer gamer) {
		alive--;
		if(alive < 2) cm.getCountdown("gameTimer").setTime(0);
	}
	
	public ArenaDataObjectRegistry getRegistry() {
		return objectRegistry;
	}
	
	public CountdownManager getCountdownManager() {
		return cm;
	}
	
	public void setSpawn(Location loc) {
		if(spawn != null) ChunkLoader.releaseFromMemory(this);
		spawn = loc;
		ChunkLoader.keepInMemory(this, spawn.getChunk());
	}
	
	public void addRounds(int amount) {
		infoRegistry.queueArenaData(infos, amount);
	}
	
	public void broadcastMessage(String message) {
		forEachGamer(g -> g.message(message));
	}
	
	private void calculateRoundWinners() {
		int mostPoints = 0;
		for(Gamer gamer : gamers) {
			int points = gamer.getRoundPoints();
			if(points > mostPoints) mostPoints = points;
		}
		if(mostPoints > 0) {
			for(Gamer gamer : gamers) {
				if(gamer.getRoundPoints() == mostPoints) gamer.incrementGamePoints();
			}
		}
	}
	
	private Gamer getGameWinner() {
		int topPoints = 0;
		long timeAchieved = 0;
		Gamer winner = null;
		for(Gamer gamer : gamers) {
			int gamerPoints = gamer.getGamePoints();
			long gamerTimeAchieved = gamer.getGamePointsTimeAchieved();
			if(winner == null || gamerPoints > topPoints || (gamerPoints == topPoints && gamerTimeAchieved < timeAchieved)) {
				topPoints = gamerPoints;
				timeAchieved = gamerTimeAchieved;
				winner = gamer;
			}
		}
		return winner;
	}
	
	private List<Gamer> getGameWinners() {
		int topPoints = 0;
		for(Gamer gamer : gamers) {
			int gamerPoints = gamer.getGamePoints();
			if(gamerPoints > topPoints) topPoints = gamerPoints;
		}
		int finalTopPoints = topPoints;
		return gamers.stream().filter(g -> g.getGamePoints() == finalTopPoints).collect(Collectors.toCollection(ArrayList::new));
	}
	
}
