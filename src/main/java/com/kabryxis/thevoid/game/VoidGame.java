package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.DataQueue;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.version.WrappableCache;
import com.kabryxis.kabutils.spigot.version.wrapper.packet.out.chat.WrappedPacketPlayOutChat;
import com.kabryxis.kabutils.spigot.world.ChunkLoader;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
	private Set<Gamer> aliveGamers = ConcurrentHashMap.newKeySet();
	
	public VoidGame(TheVoid plugin, RoundInfoRegistry infoRegistry, ArenaDataObjectRegistry objectRegistry) {
		this.plugin = plugin;
		this.infoRegistry = infoRegistry;
		this.objectRegistry = objectRegistry;
		cm.constructNewCountdown("preGame", 1000, false, (time, timeLeft) -> broadcastMessage("Starting in " + time + " seconds."));
		cm.constructNewCountdown("gameTimer", 1000, true, (time, timeLeft) -> {
			RoundInfo roundInfo = getCurrentRoundInfo();
			roundInfo.getRound().tick(this, roundInfo.getArena(), time, timeLeft);
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
			g.setGame(this);
			g.setScoreboard(board.getScoreboard());
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
		aliveGamers.clear();
		aliveGamers.addAll(gamers);
	}
	
	@Override
	public void start() {
		RoundInfo info = getCurrentRoundInfo();
		Round round = info.getRound();
		Arena arena = info.getArena();
		round.start(this, arena);
		Location[] spawns = round.getSpawns(this);
		BukkitThreads.sync(() -> {
			for(int i = 0; i < spawns.length; i++) {
				gamers.get(i).nextRound(round, spawns[i]);
			}
		});
		if(infos.hasNext()) infos.getNext().load(this);
	}
	
	@Override
	public void timer() {
		RoundInfo info = getCurrentRoundInfo();
		Round round = info.getRound();
		int roundLength = round.getRoundLength();
		if(roundLength == -1) round.customTimer();
		else cm.count("gameTimer", (int)Math.ceil((double)roundLength * info.getSchematic().getTimeModifier()));
	}
	
	@Override
	public void end() {
		RoundInfo info = getCurrentRoundInfo();
		Round round = info.getRound();
		Arena arena = info.getArena();
		round.end(this, arena);
		arena.endOfRound();
		round.getRoundWinners(this).forEach(Gamer::incrementGamePoints);
		forEachGamer(gamer -> {
			gamer.setRoundPoints(0);
			gamer.clearEffects();
			gamer.clearInventory();
		});
		Threads.sleep(3500);
		forEachGamer(gamer -> gamer.setAlive(true));
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
		BukkitThreads.sync(() -> forEachGamer(gamer -> {
			gamer.reset();
			gamer.teleport(spawn);
			gamer.setGame(null);
		}));
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
		gamer.setGame(this);
		BukkitThreads.sync(() -> {
			if(!isInProgress()) gamer.teleport(spawn);
			else {
				if(gamer.getGamePoints() == 0) gamer.setGamePoints(0);
				gamer.kill();
				gamer.teleport(getCurrentRoundInfo().getArena().getLocation().clone().add(0, 20, 0));
			}
		});
	}
	
	@Override
	public void removeGamer(Gamer gamer) {
		gamers.remove(gamer);
		gamer.setGame(null);
	}
	
	@Override
	public Collection<Gamer> getGamers() {
		return gamers;
	}
	
	@Override
	public Collection<Gamer> getAliveGamers() {
		return aliveGamers;
	}
	
	@Override
	public boolean kill(Gamer gamer) {
		aliveGamers.remove(gamer);
		if(aliveGamers.size() < 2) {
			cm.getCountdown("gameTimer").setCurrentTime(0);
			return true;
		}
		return false;
	}
	
	@Override
	public void revive(Gamer gamer) {
		// TODO
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
