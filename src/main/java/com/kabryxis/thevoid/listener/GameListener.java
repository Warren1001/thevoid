package com.kabryxis.thevoid.listener;

import com.kabryxis.kabutils.spigot.inventory.itemstack.Items;
import com.kabryxis.kabutils.spigot.metadata.Metadata;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.game.GamePlayer;
import com.kabryxis.thevoid.api.game.PlayerManager;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.util.game.DeathReason;
import com.kabryxis.thevoid.api.util.game.GameEventHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class GameListener implements GameEventHandler {
	
	private final Game game;
	private final PlayerManager playerManager;
	
	private final Map<GamePlayer, Long> pmeTimestamps = new HashMap<>();
	
	public GameListener(Game game) {
		this.game = game;
		this.playerManager = game.getPlayerManager();
	}
	
	@Override
	public void onEvent(Event event) {
		String eventName = event.getClass().getSimpleName();
		//System.out.println(eventName);
		switch(eventName) {
		case "PlayerJoinEvent":
			PlayerJoinEvent pje = (PlayerJoinEvent)event;
			GamePlayer pjePlayer = playerManager.getPlayer(pje.getPlayer());
			// TODO perhaps some initialization / data loading
			break;
		case "PlayerQuitEvent":
			PlayerQuitEvent pqe = (PlayerQuitEvent)event;
			GamePlayer pqePlayer = playerManager.getPlayer(pqe.getPlayer());
			// TODO perhaps save some data
			break;
		case "PlayerMoveEvent":
			PlayerMoveEvent pme = (PlayerMoveEvent)event;
			GamePlayer pmePlayer = playerManager.getPlayer(pme.getPlayer());
			if(!pmePlayer.isInBuilderMode()) {
				if(game.isInProgress()) {
					Location from = pme.getFrom(), to = pme.getTo();
					RoundInfo info = game.getCurrentRoundInfo();
					if(from.getY() > to.getY() && to.getY() < info.getArena().getCurrentArenaData().getLowestY() && System.currentTimeMillis() - pmeTimestamps.getOrDefault(pmePlayer, 0L) > 1000) {
						pmeTimestamps.put(pmePlayer, System.currentTimeMillis());
						info.getRound().kill(pmePlayer, DeathReason.FELL);
					}
				}
				else {
					Location to = pme.getTo();
					if(to.getY() <= 70) {
						Vector vel = pmePlayer.getPlayer().getVelocity();
						vel.setY(10);
						pmePlayer.getPlayer().setVelocity(vel);
					}
				}
			}
			break;
		case "CreatureSpawnEvent":
			CreatureSpawnEvent cse = (CreatureSpawnEvent)event;
			if(cse.getSpawnReason() != SpawnReason.CUSTOM/* && !(((CraftEntity)cse.getEntity()).getHandle() instanceof PetDragon)*/) cse.setCancelled(true);
			break;
		case "ItemSpawnEvent":
		case "EntitySpawnEvent":
			EntitySpawnEvent ese = (EntitySpawnEvent)event;
			if(!(ese.getEntity() instanceof Player)) ese.setCancelled(true);
			break;
		case "EntityDeathEvent": // might need a more permanent solution
			EntityDeathEvent ede2 = (EntityDeathEvent)event;
			ede2.setDroppedExp(0);
			break;
		//case "PlayerInteractEntityEvent":
		case "PlayerInteractEvent":
			PlayerInteractEvent pie = (PlayerInteractEvent)event;
			GamePlayer piePlayer = playerManager.getPlayer(pie.getPlayer());
			if(piePlayer.isInBuilderMode()) {
				if(Items.isType(piePlayer.getInventory().getItemInHand(), Material.STONE_AXE)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						piePlayer.getSelection().setLeft(pie.getClickedBlock().getLocation());
						pie.setCancelled(true);
					}
					else if(pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
						piePlayer.getSelection().setRight(pie.getClickedBlock().getLocation());
						pie.setCancelled(true);
					}
				}
				else if(Items.isType(piePlayer.getInventory().getItemInHand(), Material.STICK)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						piePlayer.getSelection().addBlock(pie.getClickedBlock());
						pie.setCancelled(true);
					}
					else if(pie.getAction() == Action.RIGHT_CLICK_BLOCK || pie.getAction() == Action.RIGHT_CLICK_AIR) {
						if(piePlayer.getPlayer().isSneaking()) piePlayer.getSelection().addSelection(false);
						else piePlayer.getSelection().addSelection(true);
						pie.setCancelled(true);
					}
				}
				else if(Items.isType(piePlayer.getInventory().getItemInHand(), Material.STRING)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						piePlayer.getSelection().removeBlock(pie.getClickedBlock());
						pie.setCancelled(true);
					}
					else if(pie.getAction() == Action.RIGHT_CLICK_BLOCK || pie.getAction() == Action.RIGHT_CLICK_AIR) {
						piePlayer.getSelection().removeSelection();
						pie.setCancelled(true);
					}
				}
				else if(Items.isType(piePlayer.getInventory().getItemInHand(), Material.GOLD_SWORD)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						pie.getClickedBlock().setMetadata("walkable", Metadata.getEmptyMetadataValue());
						pie.setCancelled(true);
					}
				}
			}
			break;
		case "EntityDamageByEntityEvent":
		case "EntityDamageByBlockEvent":
		case "EntityDamageEvent":
			EntityDamageEvent ede = (EntityDamageEvent)event;
			Entity entity = ede.getEntity();
			if(entity instanceof Player) {
				GamePlayer edePlayer = playerManager.getPlayer((Player)entity);
				if(edePlayer.isInBuilderMode()) break;
			}
			ede.setCancelled(true);
			break;
		case "PlayerDropItemEvent":
			PlayerDropItemEvent pdie = (PlayerDropItemEvent)event;
			if(!playerManager.getPlayer(pdie.getPlayer()).isInBuilderMode()) pdie.setCancelled(true);
			break;
		case "BlockBreakEvent":
			BlockBreakEvent bbe = (BlockBreakEvent)event;
			if(!playerManager.getPlayer(bbe.getPlayer()).isInBuilderMode()) bbe.setCancelled(true);
			break;
		case "BlockPlaceEvent":
			BlockPlaceEvent bpe = (BlockPlaceEvent)event;
			if(!playerManager.getPlayer(bpe.getPlayer()).isInBuilderMode()) bpe.setCancelled(true);
			break;
		case "FoodLevelChangeEvent":
		case "BlockFormEvent":
		case "WeatherChangeEvent":
			((Cancellable)event).setCancelled(true);
			break;
		default:
			break;
		}
	}
	
}
