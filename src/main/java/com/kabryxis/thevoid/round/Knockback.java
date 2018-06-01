package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Knockback extends SurvivalRound {
	
	private final ItemStack stick = new ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 3).build();
	
	public Knockback(RoundManager<BasicRound> roundManager) {
		super(roundManager, "knockback", false);
	}
	
	@Override
	public void setup(GamePlayer voidPlayer) {
		super.setup(voidPlayer);
		voidPlayer.getInventory().setItem(0, stick);
	}
	
	@Override
	public void event(Game game, Event event) {
		if(event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)event;
			Entity attacker = edbee.getDamager();
			if(attacker instanceof Player) {
				edbee.setCancelled(false);
				edbee.setDamage(0);
			}
		}
	}
	
}
