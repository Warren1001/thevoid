package com.kabryxis.thevoid.round;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.kabryxis.kabutils.spigot.inventory.itemstack.Items;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.AbstractRound;

public class Spleef extends AbstractRound {
	
	public Spleef() {
		super("spleef", 1);
		inventory[0] = new ItemStack(Material.WOOD_SPADE);
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof PlayerInteractEvent) {
			PlayerInteractEvent event = (PlayerInteractEvent)eve;
			if((event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && Items.isType(event.getPlayer().getInventory().getItemInHand(), Material.WOOD_SPADE)) {
				event.getClickedBlock().setType(Material.AIR);
				event.setCancelled(true);
			}
		}
	}
	
	@Override
	public void fell(Game game, Gamer gamer) {
		gamer.decrementRoundPoints(false);
		gamer.kill();
		gamer.teleport(20);
	}
	
	@Override
	public void generateDefaults() {
		useDefaults();
	}
	
	@Override
	public void load(Game game, Arena arena) {}
	
	@Override
	public void start(Game game, Arena arena) {}
	
	@Override
	public void end(Game game, Arena arena) {}
	
}
