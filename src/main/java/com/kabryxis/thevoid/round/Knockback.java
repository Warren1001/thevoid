package com.kabryxis.thevoid.round;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.AbstractRound;

public class Knockback extends AbstractRound {
	
	public Knockback() {
		super("knockback", 1);
		inventory[0] = new ItemBuilder(Material.STICK).enchant(Enchantment.KNOCKBACK, 3).build();
	}
	
	@Override
	public void load(Game game, Arena arena) {}
	
	@Override
	public void start(Game game, Arena arena) {}
	
	@Override
	public void end(Game game, Arena arena) {}
	
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
	
}
