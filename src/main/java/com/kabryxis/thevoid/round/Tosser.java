package com.kabryxis.thevoid.round;

import com.comphenix.protocol.ProtocolLibrary;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.api.util.game.DeathReason;
import com.kabryxis.thevoid.round.utility.DismountPacketAdapter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Tosser extends SurvivalRound {
	
	private final Map<Entity, Long> timePicked = new HashMap<>();
	private final Map<Entity, Entity> ridingMap = new HashMap<>();
	private final Set<BukkitTask> tasks = new HashSet<>();
	
	private DismountPacketAdapter dismountListener;
	
	public Tosser(RoundManager<BasicRound> roundManager) {
		super(roundManager, "tosser", true);
	}
	
	@Override
	public void start(Game game) {
		if(dismountListener == null) dismountListener = new DismountPacketAdapter();
		ProtocolLibrary.getProtocolManager().addPacketListener(dismountListener);
	}
	
	@Override
	public void end(Game game) {
		ProtocolLibrary.getProtocolManager().removePacketListener(dismountListener);
		List<? extends GamePlayer> gamePlayers = game.getPlayerManager().getAlivePlayers();
		BukkitThreads.sync(() -> gamePlayers.forEach(gamePlayer -> {
			Player player = gamePlayer.getPlayer();
			player.setAllowFlight(false);
			eject(player);
		}));
		timePicked.clear();
		tasks.forEach(BukkitTask::cancel);
		tasks.clear();
	}
	
	@Override
	public void kill(GamePlayer gamePlayer, DeathReason reason) {
		Player player = gamePlayer.getPlayer();
		eject(player);
		if(hasPassenger(player)) eject(player.getPassenger().getPassenger());
		super.kill(gamePlayer, reason);
	}
	
	@Override
	public void event(Game game, Event eve) {
		if(eve instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)eve;
			Entity defender = event.getEntity();
			Entity attacker = event.getDamager();
			if(!isPassenger(attacker) && defender instanceof Player) ride(attacker, defender);
		}
		else if(eve instanceof PlayerInteractEvent) {
			PlayerInteractEvent event = (PlayerInteractEvent)eve;
			Player player = event.getPlayer();
			Action action = event.getAction();
			if(action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
				if(hasPassenger(player) && System.currentTimeMillis() - timePicked.getOrDefault(player, 0L) > 500L) {
					Entity passenger = player.getPassenger().getPassenger();
					eject(passenger);
					Vector velocity = player.getLocation().getDirection().normalize().multiply(1.5);
					velocity.setY(velocity.getY() * 1.5);
					passenger.setVelocity(velocity);
				}
			}
		}
		else if(eve instanceof PlayerToggleFlightEvent) {
			PlayerToggleFlightEvent event = (PlayerToggleFlightEvent)eve;
			Player player = event.getPlayer();
			if(player.getGameMode() == GameMode.CREATIVE) return;
			event.setCancelled(true);
			player.setAllowFlight(false);
			tasks.add(BukkitThreads.syncLater(() -> {
				player.setAllowFlight(true);
				tasks.remove(this);
			}, 5 * 20L));
			player.setVelocity(player.getLocation().getDirection().normalize().setY(1.25));
		}
	}
	
	public boolean isPassenger(Entity entity) {
		return ridingMap.containsKey(entity);
	}
	
	public void eject(Entity entity) {
		entity.eject();
		if(entity instanceof Player) ((Player)entity).setAllowFlight(true);
		Entity ridee = ridingMap.remove(entity);
		if(ridee != null) removeStand(ridee);
	}
	
	private Entity getStand(Entity entity) {
		Entity stand = entity.getPassenger();
		if(stand == null) {
			ArmorStand armorStand = entity.getWorld().spawn(entity.getLocation().add(0, 1.5, 0), ArmorStand.class);
			armorStand.setVisible(false);
			armorStand.setSmall(true);
			stand = armorStand;
			entity.setPassenger(stand);
		}
		return stand;
	}
	
	private void removeStand(Entity entity) {
		Entity stand = entity.getPassenger();
		if(stand != null) {
			stand.eject();
			stand.remove();
		}
	}
	
	private boolean hasPassenger(Entity entity) {
		return entity.getPassenger() != null && entity.getPassenger().getPassenger() != null;
	}
	
	public void ride(Entity ridee, Entity rider) {
		Entity standPassenger = getStand(ridee);
		if(hasPassenger(ridee)) {
			Entity rideePassenger = standPassenger.getPassenger();
			if(rideePassenger.equals(rider)) return;
			eject(rideePassenger);
		}
		Entity riderPassenger = rider.getPassenger();
		standPassenger.setPassenger(rider);
		ridingMap.put(rider, ridee);
		timePicked.put(ridee, System.currentTimeMillis());
		if(riderPassenger != null) rider.setPassenger(riderPassenger);
	}
	
	@Override
	public boolean test(Object obj) {
		return (!(obj instanceof Tosser) || Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) && super.test(obj);
	}
	
}
