package com.kabryxis.thevoid.round;

import com.kabryxis.kabutils.cache.Cache;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.arena.Arena;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import com.kabryxis.thevoid.round.utility.HotPotatoItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HotPotato extends VoidRound {
	
	private HotPotatoItem potatoItem = Cache.get(HotPotatoItem.class);
	
	public HotPotato() {
		super("hotpotato");
	}
	
	@Override
	public void start(Game game, Arena arena) {
		BukkitThreads.sync(() -> potatoItem.start(game.getAliveGamers()));
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity clickedEntity = event.getEntity(), damagingEntity = event.getDamager();
			if(clickedEntity instanceof Player && damagingEntity instanceof Player) {
				Player clicked = (Player)clickedEntity, attacker = (Player)damagingEntity;
				if(potatoItem.isCurrentHolder(attacker)) potatoItem.changeHolder(Gamer.getGamer(clicked));
			}
		}
	}
	
	@Override
	public void end(Game game, Arena arena) {
		potatoItem.stop();
	}
	
}
