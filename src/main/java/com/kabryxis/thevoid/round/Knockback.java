package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Knockback extends VoidRound {
	
	public Knockback() {
		super("knockback");
		inventory[0] = new ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 3).build();
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
