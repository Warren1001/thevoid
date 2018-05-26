package com.kabryxis.thevoid;

import com.kabryxis.kabutils.command.CommandManager;
import com.kabryxis.kabutils.data.file.FileEndingFilter;
import com.kabryxis.kabutils.spigot.command.CommandMapHook;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.event.Listeners;
import com.kabryxis.thevoid.api.arena.impl.VoidArena;
import com.kabryxis.thevoid.api.arena.object.impl.ArenaWalkable;
import com.kabryxis.thevoid.api.arena.object.impl.VoidArenaDataObjectRegistry;
import com.kabryxis.thevoid.api.arena.schematic.impl.VoidBaseSchematic;
import com.kabryxis.thevoid.api.arena.schematic.impl.VoidSchematic;
import com.kabryxis.thevoid.api.arena.schematic.util.CreatorWalkable;
import com.kabryxis.thevoid.api.arena.schematic.util.SchematicCreator;
import com.kabryxis.thevoid.api.game.VoidGameGenerator;
import com.kabryxis.thevoid.api.round.RoundInfoRegistry;
import com.kabryxis.thevoid.game.GameThread;
import com.kabryxis.thevoid.game.VoidGame;
import com.kabryxis.thevoid.game.VoidRoundInfoRegistry;
import com.kabryxis.thevoid.listener.BukkitListener;
import com.kabryxis.thevoid.listener.CommandListener;
import com.kabryxis.thevoid.round.wip.SnipePillars;
import org.bukkit.*;
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
		setupWorlds();
		commandManager = new CommandManager();
		commandManager.addExtraWork(new CommandMapHook(commandManager));
		commandManager.registerListener(new CommandListener(this));
		//framework.registerCommands(new UtilityListener(this));
		SchematicCreator.registerExtraWork("walkable", new CreatorWalkable());
		VoidArenaDataObjectRegistry objectRegistry = new VoidArenaDataObjectRegistry();
		objectRegistry.register("walkable", ArenaWalkable.class, ArenaWalkable::new);
		RoundInfoRegistry infoRegistry = new VoidRoundInfoRegistry(this);
		infoRegistry.registerRounds(new SnipePillars()/*, new HangingSheep(), new KnockbackDisintegrateHybrid(), new HotPotato(),
			new DisintegrateRandom(), new DisintegrateWalk(), new Spleef(), new DragonSpleef(), new Knockback(), new LightningDodge(), new Anvilstorm()*/);
		for(File file : new File(VoidArena.PATH).listFiles(new FileEndingFilter(".yml"))) {
			new Config(file).load(config -> infoRegistry.registerArena(new VoidArena(objectRegistry, config)));
		}
		for(File file : new File(VoidSchematic.PATH).listFiles(new FileEndingFilter(".sch"))) {
			infoRegistry.registerSchematic(new VoidBaseSchematic(file));
		}
		//infoRegistry.registerSchematic(BaseSchematic.EMPTY);
		game = new VoidGame(this, infoRegistry, objectRegistry);
		game.setSpawn(new Location(Bukkit.getWorld("lobby"), 0.5, 101.5, 0.5));
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
