package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.random.MultiRandomArrayList;
import com.kabryxis.kabutils.random.RandomArrayList;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.version.wrapper.WrapperCache;
import com.kabryxis.kabutils.spigot.version.wrapper.packet.out.chat.WrappedPacketPlayOutChat;
import com.kabryxis.kabutils.spigot.world.Teleport;
import com.kabryxis.kabutils.spigot.world.WorldManager;
import com.kabryxis.kabutils.time.CountdownManager;
import com.kabryxis.thevoid.TheVoid;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.object.ArenaDataObjectRegistry;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.AbstractRound;
import com.kabryxis.thevoid.api.round.Round;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.schematic.Schematic;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VoidGame implements Game {
	
	private final String name = "thevoid-game";
	private final Logger logger = Logger.getLogger(name);
	private final List<Gamer> gamers = new ArrayList<>();
	private final List<RoundInfo> infos = new ArrayList<>();
	private final MultiRandomArrayList<String, Arena> arenas = new MultiRandomArrayList<>(() -> new HashMap<>(), Arena::getWorldName, 2);
	private final MultiRandomArrayList<String, Schematic> schematics = new MultiRandomArrayList<>(() -> new HashMap<>(), Schematic::getName, 2);
	private final RandomArrayList<Round> rounds = new RandomArrayList<>(2);
	private final ArenaDataObjectRegistry objectRegistry = new ArenaDataObjectRegistry();
	private final WrappedPacketPlayOutChat<?> actionMessage = WrapperCache.get(WrappedPacketPlayOutChat.class);
	private final Board board = new Board(this);
	private final CountdownManager cm = new CountdownManager();
	
	private final TheVoid plugin;
	
	private Location spawn;
	private int index = -1;
	
	public VoidGame(TheVoid plugin) {
		this.plugin = plugin;
		cm.constructNewCountdown("preGame", 1000, false, time -> broadcastMessage("Starting in " + time + " seconds."));
		cm.constructNewCountdown("gameTimer", 1000, true, time -> {
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
	
	public ArenaDataObjectRegistry getRegistry() {
		return objectRegistry;
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
	
	public void setSpawn(Location loc) {
		if(spawn != null) WorldManager.removeChunkFromMemory(name, spawn.getChunk());
		spawn = loc;
		WorldManager.keepChunkInMemory(name, spawn.getChunk());
	}
	
	@Override
	public void threadStart() {
		getNextRoundInfo().load(this);
		forEachGamer(g -> {
			g.setScoreboard(board.getScoreboard());
			g.setGame(this);
		});
		cm.count("preGame", 5);
		board.start();
	}
	
	@Override
	public boolean canRun() {
		return hasNextRoundInfo();
	}
	
	@Override
	public void next() {
		index++;
		Collections.shuffle(gamers);
	}
	
	@Override
	public void start() {
		RoundInfo info = infos.get(index);
		Round round = info.getRound();
		Arena arena = info.getArena();
		Location[] spawns = Teleport.getEquidistantPoints(arena.getCurrentArenaData().getCenter(), gamers.size(), arena.getCurrentArenaData().getSchematic().getRadius());
		for(int i = 0; i < spawns.length; i++) {
			Gamer gamer = gamers.get(i);
			Location spawn = spawns[i];
			if(gamer.isAlive()) gamer.teleport(spawn);
			else gamer.revive(spawn);
		}
		if(round instanceof AbstractRound) {
			AbstractRound baseRound = (AbstractRound)round;
			forEachGamer(gamer -> {
				gamer.setInventory(baseRound.getInventory(), baseRound.getArmor());
				gamer.setRoundPoints(baseRound.getStartingPoints(), false);
			});
		}
		round.start(this, arena);
		if(hasNextRoundInfo()) getNextRoundInfo().load(this);
	}
	
	@Override
	public void timer() {
		RoundInfo info = infos.get(index);
		Round round = info.getRound();
		int roundLength = round.getRoundLength();
		if(roundLength == -1) round.customTimer();
		else cm.count("gameTimer", roundLength);
	}
	
	@Override
	public void end() {
		RoundInfo info = infos.get(index);
		Round round = info.getRound();
		Arena arena = info.getArena();
		forEachGamer(Gamer::clearInventory);
		round.end(this, arena);
		arena.endOfRound();
		calculateRoundWinners();
		Threads.sleep(3500);
		board.nextRound();
		BukkitThreads.syncLater(() -> info.getArena().eraseSchematic(), 5);
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
	
	@SuppressWarnings("unused")
	private List<Gamer> getGameWinners() {
		int topPoints = 0;
		for(Gamer gamer : gamers) {
			int gamerPoints = gamer.getGamePoints();
			if(gamerPoints > topPoints) topPoints = gamerPoints;
		}
		int finalTopPoints = topPoints;
		return gamers.stream().filter(g -> g.getGamePoints() == finalTopPoints).collect(Collectors.toCollection(ArrayList::new));
	}
	
	@Override
	public void threadEnd() {
		index = -1;
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
	
	public CountdownManager getCountdownManager() {
		return cm;
	}
	
	@Override
	public RoundInfo getCurrentRoundInfo() {
		return infos.get(index);
	}
	
	public Arena getCurrentArena() {
		return getCurrentRoundInfo().getArena();
	}
	
	public Round getCurrentRound() {
		return getCurrentRoundInfo().getRound();
	}
	
	public RoundInfo getNextRoundInfo() {
		return infos.get(index + 1);
	}
	
	public boolean hasNextRoundInfo() {
		return index != infos.size() - 1;
	}
	
	@Override
	public void forEachGamer(Consumer<? super Gamer> action) {
		gamers.forEach(action);
	}
	
	public void broadcastMessage(String message) {
		forEachGamer(g -> g.message(message));
	}
	
	public void registerArena(Arena arena) {
		arenas.addToList(arena.getWorldName(), arena);
	}
	
	public void registerArenas(Arena... arenas) {
		for(Arena arena : arenas) {
			registerArena(arena);
		}
	}
	
	public void registerRound(Round round) {
		rounds.add(round);
	}
	
	public void registerRounds(Round... rounds) {
		for(Round round : rounds) {
			registerRound(round);
		}
	}
	
	public void registerSchematic(Schematic schematic) {
		schematics.addToList(schematic.getName(), schematic);
	}
	
	public void registerSchematics(Schematic... schematics) {
		for(Schematic schematic : schematics) {
			registerSchematic(schematic);
		}
	}
	
	public void addRounds(int amount) {
		Map<Arena, List<Schematic>> schematics = new HashMap<>();
		for(; amount > 0; amount--) {
			Round round = rounds.random();
			Arena arena = generateArenaForRound(round);
			Schematic schematic = generateSchematicForRound(round);
			schematics.computeIfAbsent(arena, a -> new ArrayList<>()).add(schematic);
			infos.add(new VoidRoundInfo(round, arena, schematic));
		}
		schematics.forEach(Arena::queueSchematics);
	}
	
	public Arena generateArenaForRound(Round round) {
		return arenas.random(round.getWorldNames());
	}
	
	public Schematic generateSchematicForRound(Round round) {
		return schematics.random(round.getSchematics());
	}
	
	@Override
	public void callEvent(Event event) {
		if(isInProgress()) getCurrentRoundInfo().getRound().event(this, event);
	}
	
	@Override
	public boolean isInProgress() {
		return index != -1;
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
	
}
