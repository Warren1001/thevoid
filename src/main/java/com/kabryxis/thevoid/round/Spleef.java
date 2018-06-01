package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.kabutils.spigot.inventory.itemstack.Items;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Spleef extends SurvivalRound {
	
	private final ItemStack shovel = new ItemBuilder(Material.WOOD_SPADE).name(ChatColor.GOLD + "Spleef!").build();
	
	public Spleef(RoundManager<BasicRound> roundManager) {
		super(roundManager, "spleef", true);
	}
	
	@Override
	public void setup(GamePlayer gamePlayer) {
		super.setup(gamePlayer);
		gamePlayer.getInventory().setItem(0, shovel);
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
