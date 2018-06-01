package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Collections;

public class PetDragons extends SurvivalRound {
	
	//private final Map<Gamer, PetDragon> dragons = new HashMap<>();
	
	public PetDragons(RoundManager<BasicRound> roundManager) {
		super(roundManager, "petdragons", false);
	}
	
	@Override
	public void start(Game game) {
		Location center = game.getCurrentRoundInfo().getArena().getLocation();
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
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", 60);
		data.addDefault("schematics", Collections.singletonList("theend"));
	}
	
	private void setTarget(Player owner, Player target) {
		//dragons.get(Gamer.getGamer(owner)).setTarget(Gamer.getGamer(target));
		owner.sendMessage("Your dragon is now targeting " + target.getDisplayName() + "!");
	}
	
	
	
}
