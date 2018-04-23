package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.spigot.inventory.itemstack.Items;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Spleef extends VoidRound {
	
	public Spleef() {
		super("spleef", 1, GameMode.SURVIVAL);
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
	
}
