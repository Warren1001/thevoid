package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.concurrent.Threads;
import com.kabryxis.kabutils.data.Data;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.kabutils.time.TimeLeft;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.api.util.arena.object.ArenaWalkable;
import com.kabryxis.thevoid.round.utility.DisintegrateThread;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public class KnockbackDisintegrateHybrid extends SurvivalRound {
	
	private static final String id = "disintegrateHybrid";
	
	private final Material[] levels = { Material.OBSIDIAN, Material.NETHERRACK, Material.TNT };
	private final DisintegrateThread thread = new DisintegrateThread(id, levels, id, 750L);
	private final ItemStack stick = new ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 3).build();
	
	public KnockbackDisintegrateHybrid(RoundManager<BasicRound> roundManager) {
		super(roundManager, id, false);
	}
	
	@Override
	public void load(Game game) {
		Arena arena = game.getCurrentRoundInfo().getArena();
		ArenaWalkable walkData = arena.getCurrentArenaData().getDataObject(ArenaWalkable.class);
		Location center = arena.getLocation();
		Data.queue(() -> walkData.loadDiamondPatternBlocks(center.getBlockX(), center.getBlockZ()));
	}
	
	@Override
	public void tick(Game game, int time, TimeLeft timeLeft) {
		if(timeLeft == TimeLeft.HALF) {
			if(!thread.isRunning()) thread.start();
			else thread.unpause();
			Threads.start(() -> {
				List<Set<Block>> diamondPatternBlocks = game.getCurrentRoundInfo().getArena().getCurrentArenaData().getDataObject(ArenaWalkable.class).getDiamondPatternBlocks();
				//for(int i = 0; i < diamondPatternBlocks.size() && !thread.isPaused(); i++) {
				for(int i = diamondPatternBlocks.size() - 1; i >= 0 && !thread.isPaused(); i--) {
					Set<Block> blocks = diamondPatternBlocks.get(i);
					if(blocks != null) {
						blocks.forEach(thread::add);
						Threads.sleep(1000);
					}
				}
			});
		}
	}
	
	@Override
	public void end(Game game) {
		thread.pause();
	}
	
	@Override
	public void setup(GamePlayer gamePlayer) {
		super.setup(gamePlayer);
		gamePlayer.getInventory().setItem(0, stick);
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity attacker = event.getDamager();
			if(attacker instanceof Player) {
				event.setCancelled(false);
				event.setDamage(0);
			}
		}
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", 45);
	}

}
