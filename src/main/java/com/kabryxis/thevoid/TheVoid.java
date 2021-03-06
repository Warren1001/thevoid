package com.kabryxis.thevoid;

import com.kabryxis.kabutils.command.CommandManager;
import com.kabryxis.kabutils.data.file.FileEndingFilter;
import com.kabryxis.kabutils.spigot.command.CommandMapHook;
import com.kabryxis.kabutils.spigot.data.Config;
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
import com.kabryxis.thevoid.listener.CommandListener;
import com.kabryxis.thevoid.listener.GameListener;
import com.kabryxis.thevoid.listener.VoidListener;
import com.kabryxis.thevoid.round.*;
import com.kabryxis.thevoid.round.wip.PetDragons;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class TheVoid extends JavaPlugin {
	
	private CommandManager commandManager;
	private VoidGame game;
	private GameThread thread;
	
	private Location spawn;
	
	private boolean shouldRun = true;
	
	@Override
	public void onEnable() {
		if(!shouldRun) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		setupWorlds();
		spawn = new Location(Bukkit.getWorld("lobby"), 0.5, 101.5, 0.5);
		commandManager = new CommandManager();
		commandManager.addExtraWork(new CommandMapHook(commandManager));
		commandManager.registerListener(new CommandListener(this));
		//framework.registerCommands(new UtilityListener(this));
		VoidSchematicCreator.registerExtraWork("walkable", new CreatorWalkable());
		VoidArenaDataObjectRegistry objectRegistry = new VoidArenaDataObjectRegistry();
		objectRegistry.register("walkable", ArenaWalkable.class, ArenaWalkable::new);
		ChunkLoader chunkLoader = new ChunkLoader(this);
		VoidRoundInfoRegistry infoRegistry = new VoidRoundInfoRegistry(this);
		VoidRoundManager roundManager = infoRegistry.getRoundManager();
		infoRegistry.registerRounds(new PetDragons(roundManager), new Tosser(roundManager), new ColorPlatform(roundManager), new SnipePillars(roundManager), new HangingSheep(roundManager),
			new KnockbackDisintegrateHybrid(roundManager), new DisintegrateRandom(roundManager), new DisintegrateWalk(roundManager), new Spleef(roundManager), new Knockback(roundManager),
				new Anvilstorm(roundManager)/*, new LightningDodge(), new DragonSpleef(), new HotPotato(roundManager)*/);
		for(File file : Objects.requireNonNull(new File(VoidArena.PATH).listFiles(new FileEndingFilter(".yml")))) {
			new Config(file).load(config -> infoRegistry.registerArena(new VoidArena(objectRegistry, chunkLoader, config)));
		}
		for(File file : Objects.requireNonNull(new File(VoidSchematic.PATH).listFiles(new FileEndingFilter(".sch")))) {
			infoRegistry.registerSchematic(new VoidBaseSchematic(file));
		}
		game = new VoidGame(this, chunkLoader, infoRegistry, objectRegistry);
		game.registerEventHandler(new GameListener(this, game));
		game.setSpawn(spawn);
		thread = new GameThread("TheVoid - Game thread", game);
		Listeners.registerListener(new VoidListener(game, this));
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
	
	public Location getSpawn() {
		return spawn;
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
