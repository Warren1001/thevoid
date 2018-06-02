package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.round.utility.HotPotatoItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HotPotato extends SurvivalRound {
	
	private HotPotatoItem potatoItem = new HotPotatoItem();
	
	public HotPotato(RoundManager<BasicRound> roundManager) {
		super(roundManager, "hotpotato", false);
	}
	
	@Override
	public void start(Game game) {
		BukkitThreads.sync(() -> potatoItem.start(game.getPlayerManager().getAlivePlayers()));
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity clickedEntity = event.getEntity(), damagingEntity = event.getDamager();
			if(clickedEntity instanceof Player && damagingEntity instanceof Player) {
				Player clicked = (Player)clickedEntity, attacker = (Player)damagingEntity;
				if(potatoItem.isCurrentHolder(attacker)) potatoItem.changeHolder(game.getPlayerManager().getPlayer(clicked));
			}
		}
	}
	
	@Override
	public void end(Game game) {
		potatoItem.stop();
	}
	
}
