package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.VoidRound;
import com.kabryxis.thevoid.api.schematic.Schematic;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;

public class HangingSheep extends VoidRound {
	
	private final Schematic fenceSchematic;
	
	public HangingSheep() {
		super("hangingsheep", 0);
		inventory[0] = new ItemBuilder(Material.DIAMOND_SWORD).name(ChatColor.GOLD + "Kill the Sheep!").build();
		fenceSchematic = new Schematic(new File("plugins" + File.separator + "TheVoid" + File.separator + "test.sch"));
		fenceSchematic.addSchematicWork(HangingSheepWork.class);
	}
	
	@Override
	public void load(Game game, Arena arena) {
		arena.loadAnotherSchematic(fenceSchematic);
	}
	
	@Override
	public void start(Game game, Arena arena) {
		World world = arena.getWorld();
		BukkitThreads.sync(() -> arena.getSchematicData(fenceSchematic).getExtraWork(HangingSheepWork.class).getFenceBlocks().forEach(block -> {
			for(int i = 0; i < 3; i++) {
				Sheep sheep = world.spawn(block.getLocation().subtract(0, 1.5, 0), Sheep.class);
				LeashHitch lh = world.spawn(block.getLocation(), LeashHitch.class);
				arena.spawnedCustomEntity(lh);
				arena.spawnedCustomEntity(sheep);
				sheep.setLeashHolder(lh);
			}
		}));
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			if(event.getEntity() instanceof Sheep && event.getDamager() instanceof Player) {
				event.setCancelled(false);
				Gamer.getGamer((Player)event.getDamager()).incrementRoundPoints(true);
			}
		}
	}
	
	@Override
	public void fell(Game game, Gamer gamer) {
		gamer.kill();
		gamer.teleport(20);
	}
	
}
