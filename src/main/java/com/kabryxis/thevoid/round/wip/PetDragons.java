package com.kabryxis.thevoid.round.wip;

import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.round.VoidRound;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Collections;

public class PetDragons extends VoidRound {
	
	//private final Map<Gamer, PetDragon> dragons = new HashMap<>();
	
	public PetDragons() {
		super("petdragons", 1);
	}
	
	@Override
	public void generateDefaults() {
		config.set("round-length", 60);
		config.set("world-names", Collections.singletonList("world_the_end"));
		config.set("schematics", Collections.singletonList("theend"));
	}
	
	@Override
	public void start(Game game, Arena arena) {
		Location center = arena.getCenter();
		/*BukkitThreads.sync(new Runnable() {
			
			@Override
			public void run() {
				game.forEachGamer(g -> {
					PetDragon dragon = new PetDragon(g.getWorldLocation().clone().add(0, 60, 0), g, new Vector(center.getX(), center.getY(), center.getZ()));
					dragons.put(g, dragon);
					arena.spawnedCustomEntity(dragon.getBukkitEntity());
				});
			}
			
		});*/
		
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof PlayerInteractEntityEvent) {
			PlayerInteractEntityEvent event = (PlayerInteractEntityEvent)eve;
			Entity entity = event.getRightClicked();
			if(entity instanceof Player) {
				Player clicked = (Player)entity, player = event.getPlayer();
				setTarget(player, clicked);
			}
		}
		else if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity entity = event.getEntity(), damager = event.getDamager();
			if(entity instanceof Player && damager instanceof Player) {
				Player attacked = (Player)entity, player = (Player)damager;
				setTarget(player, attacked);
			}
		}
	}
	
	private void setTarget(Player owner, Player target) {
		//dragons.get(Gamer.getGamer(owner)).setTarget(Gamer.getGamer(target));
		owner.sendMessage("Your dragon is now targeting " + target.getDisplayName() + "!");
	}
	
	
	
}
