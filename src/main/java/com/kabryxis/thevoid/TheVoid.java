package com.kabryxis.thevoid;

import com.kabryxis.kabutils.command.CommandManager;
import com.kabryxis.kabutils.data.file.FileEndingFilter;
import com.kabryxis.kabutils.spigot.command.CommandMapHook;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.event.Listeners;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.object.ArenaDataObjectRegistry;
import com.kabryxis.thevoid.api.arena.object.ArenaWalkable;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;
import com.kabryxis.thevoid.api.schematic.BaseSchematic;
import com.kabryxis.thevoid.api.schematic.Schematic;
import com.kabryxis.thevoid.game.GameThread;
import com.kabryxis.thevoid.game.VoidGame;
import com.kabryxis.thevoid.game.VoidRoundInfoRegistry;
import com.kabryxis.thevoid.listener.BukkitListener;
import com.kabryxis.thevoid.listener.CommandListener;
import com.kabryxis.thevoid.round.wip.KnockbackDisintegrateHybrid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TheVoid extends JavaPlugin {
	
	private CommandManager commandManager;
	private VoidGame game;
	private GameThread thread;
	
	private boolean shouldRun = true;
	
	@Override
	public void onEnable() {
		if(!shouldRun) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		commandManager = new CommandManager();
		commandManager.addExtraWork(new CommandMapHook(commandManager));
		commandManager.registerListener(new CommandListener(this));
		//framework.registerCommands(new UtilityListener(this));
		ArenaDataObjectRegistry objectRegistry = new ArenaDataObjectRegistry();
		objectRegistry.registerDataObjectCreator("walkable", ArenaWalkable.class);
		RoundInfoRegistry infoRegistry = new VoidRoundInfoRegistry();
		infoRegistry.registerRounds(new KnockbackDisintegrateHybrid()/*, new HangingSheep(), new LightningDodge(), new DisintegrateRandom(), new Anvilstorm(), new DisintegrateWalk(), new Spleef()/*, new
		Knockback()*/);
		for(File file : new File(Arena.PATH).listFiles(new FileEndingFilter(".yml"))) {
			Config.get(file).load(config -> infoRegistry.registerArena(new Arena(objectRegistry, config)));
		}
		for(File file : new File(Schematic.PATH).listFiles(new FileEndingFilter(".sch"))) {
			infoRegistry.registerSchematic(new BaseSchematic(file));
		}
		game = new VoidGame(this, infoRegistry, objectRegistry);
		game.setSpawn(new Location(Bukkit.getWorld("world"), 0.5, 101.5, 0.5));
		thread = new GameThread("TheVoid - Game thread", game);
		Listeners.registerListener(new BukkitListener(game, this));
	}
	
	public void shouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
	
	public boolean shouldRun() {
		return shouldRun;
	}
	
	public GameThread getThread() {
		return thread;
	}
	
	public VoidGame getGame() {
		return game;
	}
	
	public void start(int rounds) {
		if(game == null || thread == null) return;
		game.addRounds(rounds);
		thread.start();
	}
	
}
