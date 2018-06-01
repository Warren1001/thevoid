package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.data.MathHelp;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.arena.schematic.Schematic;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.arena.schematic.VoidSchematic;
import com.kabryxis.thevoid.api.impl.round.PointsRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.round.utility.HangingSheepWork;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class HangingSheep extends PointsRound {
	
	private final Schematic fenceSchematic = new VoidSchematic(new File("plugins" + File.separator + "TheVoid" + File.separator + "sheepstand.sch"));
	private final ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD).name(ChatColor.GOLD + "Kill the Sheep!").build();
	
	public HangingSheep(RoundManager<BasicRound> roundManager) {
		super(roundManager, "hangingsheep", false);
		fenceSchematic.addSchematicWork(HangingSheepWork::new);
	}
	
	@Override
	public void load(Game game) {
		game.getCurrentRoundInfo().getArena().loadAnotherSchematic(fenceSchematic, -MathHelp.floor(fenceSchematic.getSizeX() / 2), 9, -MathHelp.floor(fenceSchematic.getSizeZ() / 2));
	}
	
	@Override
	public void start(Game game) {
		Arena arena = game.getCurrentRoundInfo().getArena();
		World world = arena.getWorld();
		BukkitThreads.sync(() -> arena.getArenaData(fenceSchematic).getSchematicWork(HangingSheepWork.class).getFenceBlocks().forEach(block -> {
			for(int i = 0; i < 3; i++) {
				Sheep sheep = world.spawn(block.getLocation().subtract(0, 1.5, 0), Sheep.class);
				LeashHitch lh = world.spawn(block.getLocation(), LeashHitch.class);
				arena.spawnedEntity(lh);
				arena.spawnedEntity(sheep);
				sheep.setLeashHolder(lh);
			}
		}));
	}
	
	@Override
	public void setup(GamePlayer gamePlayer) {
		super.setup(gamePlayer);
		gamePlayer.getInventory().setItem(0, sword);
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			if(event.getEntity() instanceof Sheep && event.getDamager() instanceof Player) {
				event.setCancelled(false);
				pointManager.incrementPoints(game.getPlayerManager().getPlayer((Player)event.getDamager()));
			}
		}
	}
	
}
