package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.kabutils.spigot.version.object.dragon.pet.PetDragon;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PetDragons extends SurvivalRound {
	
	private final Map<GamePlayer, PetDragon> dragons = new HashMap<>();
	
	public PetDragons(RoundManager<BasicRound> roundManager) {
		super(roundManager, "petdragons", false);
	}
	
	@Override
	public void setup(GamePlayer gamePlayer) {
		super.setup(gamePlayer);
		Game game = gamePlayer.getGame();
		PetDragon dragon = com.kabryxis.kabutils.spigot.version.object.dragon.pet.PetDragons.newInstance(
				gamePlayer.getLocation().clone().add(0, 60, 0), gamePlayer.getPlayer(), game.getCurrentRoundInfo().getArena().getCurrentArenaData().getCenter());
		dragons.put(gamePlayer, dragon);
		game.getCurrentRoundInfo().getArena().spawnedEntity(dragon.getBukkit());
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof PlayerInteractEntityEvent) {
			PlayerInteractEntityEvent event = (PlayerInteractEntityEvent)eve;
			Entity entity = event.getRightClicked();
			if(entity instanceof Player) {
				Player clicked = (Player)entity, player = event.getPlayer();
				setTarget(game, player, clicked);
			}
		}
		else if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity entity = event.getEntity(), damager = event.getDamager();
			if(entity instanceof Player && damager instanceof Player) {
				Player attacked = (Player)entity, player = (Player)damager;
				setTarget(game, player, attacked);
			}
		}
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", 60);
		data.addDefault("worlds", Collections.singletonList("void_end"));
		data.addDefault("schematics", Collections.singletonList("halfsphere"));
	}
	
	private void setTarget(Game game, Player owner, Player target) {
		dragons.get(game.getPlayerManager().getPlayer(owner)).setTarget(target);
		owner.sendMessage("Your dragon is now targeting " + target.getDisplayName() + "!");
	}
	
	
	
}
