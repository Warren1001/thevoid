package com.kabryxis.thevoid;

import com.kabryxis.kabutils.command.CommandManager;
import com.kabryxis.kabutils.data.file.FileEndingFilter;
import com.kabryxis.kabutils.spigot.command.CommandMapHook;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.event.GlobalListener;
import com.kabryxis.kabutils.spigot.event.Listeners;
import com.kabryxis.kabutils.spigot.world.ChunkLoader;
import com.kabryxis.thevoid.api.impl.arena.VoidArena;
import com.kabryxis.thevoid.api.impl.arena.object.VoidArenaDataObjectRegistry;
import com.kabryxis.thevoid.api.impl.arena.schematic.VoidBaseSchematic;
import com.kabryxis.thevoid.api.impl.arena.schematic.VoidSchematic;
import com.kabryxis.thevoid.api.impl.arena.schematic.VoidSchematicCreator;
import com.kabryxis.thevoid.api.impl.game.VoidGame;
import com.kabryxis.thevoid.api.util.arena.object.ArenaWalkable;
import com.kabryxis.thevoid.api.util.arena.schematic.CreatorWalkable;
import com.kabryxis.thevoid.api.util.game.VoidGameGenerator;
import com.kabryxis.thevoid.game.GameThread;
import com.kabryxis.thevoid.game.VoidRoundInfoRegistry;
import com.kabryxis.thevoid.listener.GameListener;
import com.kabryxis.thevoid.listener.CommandListener;
import com.kabryxis.thevoid.round.wip.SnipePillars;
import org.bukkit.*;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
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
		TheVoid plugin = this;
		setupWorlds();
		commandManager = new CommandManager();
		commandManager.addExtraWork(new CommandMapHook(commandManager));
		commandManager.registerListener(new CommandListener(this));
		//framework.registerCommands(new UtilityListener(this));
		VoidSchematicCreator.registerExtraWork("walkable", new CreatorWalkable());
		VoidArenaDataObjectRegistry objectRegistry = new VoidArenaDataObjectRegistry();
		objectRegistry.register("walkable", ArenaWalkable.class, ArenaWalkable::new);
		VoidRoundManager roundManager = new VoidRoundManager(this);
		ChunkLoader chunkLoader = new ChunkLoader(this);
		VoidRoundInfoRegistry infoRegistry = new VoidRoundInfoRegistry(this);
		infoRegistry.registerRounds(new SnipePillars(roundManager)/*, new HangingSheep(), new KnockbackDisintegrateHybrid(), new HotPotato(),
			new DisintegrateRandom(), new DisintegrateWalk(), new Spleef(), new DragonSpleef(), new Knockback(), new LightningDodge(), new Anvilstorm()*/);
		for(File file : new File(VoidArena.PATH).listFiles(new FileEndingFilter(".yml"))) {
			new Config(file).load(config -> infoRegistry.registerArena(new VoidArena(objectRegistry, chunkLoader, config)));
		}
		for(File file : new File(VoidSchematic.PATH).listFiles(new FileEndingFilter(".sch"))) {
			infoRegistry.registerSchematic(new VoidBaseSchematic(file));
		}
		//infoRegistry.registerSchematic(BaseSchematic.EMPTY);
		game = new VoidGame(this, chunkLoader, infoRegistry, objectRegistry);
		game.setSpawn(new Location(Bukkit.getWorld("lobby"), 0.5, 101.5, 0.5));
		thread = new GameThread("TheVoid - Game thread", game);
		game.registerEventHandler(new GameListener(game));
		Listeners.registerListener(new GlobalListener() {
			
			@Override
			public void onEvent(Event event) {
				game.callEvent(event);
			}
			
			@Override
			public Plugin getPlugin() {
				return plugin;
			}
			
		});
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
	
	public void setupWorlds() {
		VoidGameGenerator worldGenerator = new VoidGameGenerator();
		WorldCreator overworldCreator = new WorldCreator("void_overworld");
		overworldCreator.environment(World.Environment.NORMAL);
		overworldCreator.type(WorldType.FLAT);
		overworldCreator.generator(worldGenerator);
		overworldCreator.createWorld();
		WorldCreator netherCreator = new WorldCreator("void_nether");
		netherCreator.environment(World.Environment.NETHER);
		netherCreator.generator(worldGenerator);
		netherCreator.createWorld();
		WorldCreator endCreator = new WorldCreator("void_end");
		endCreator.environment(World.Environment.THE_END);
		endCreator.generator(worldGenerator);
		endCreator.createWorld();
	}
	
}
