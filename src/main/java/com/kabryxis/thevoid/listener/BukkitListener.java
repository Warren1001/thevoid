package com.kabryxis.thevoid.listener;

import com.kabryxis.kabutils.spigot.event.GlobalListener;
import com.kabryxis.kabutils.spigot.inventory.itemstack.Items;
import com.kabryxis.kabutils.spigot.world.ChunkLoader;
import com.kabryxis.thevoid.api.game.Gamer;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.game.VoidGame;
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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class BukkitListener implements GlobalListener {
	
	private final VoidGame game;
	private final Plugin plugin;
	
	private final Map<Gamer, Long> pmeTimestamps = new HashMap<>();
	
	public BukkitListener(VoidGame game, Plugin plugin) {
		this.game = game;
		this.plugin = plugin;
	}
	
	@Override
	public void onEvent(Event event) {
		String eventName = event.getClass().getSimpleName();
		//System.out.println(eventName);
		switch(eventName) {
		case "PlayerJoinEvent":
			PlayerJoinEvent pje = (PlayerJoinEvent)event;
			Gamer pjeGamer = Gamer.getGamer(pje.getPlayer());
			pjeGamer.updatePlayer();
			game.addGamer(pjeGamer);
			break;
		case "PlayerQuitEvent":
			PlayerQuitEvent pqe = (PlayerQuitEvent)event;
			Gamer pqeGamer = Gamer.getGamer(pqe.getPlayer());
			if(pqeGamer.isInGame()) pqeGamer.getGame().removeGamer(pqeGamer);
			pqeGamer.setRoundPoints(0, true);
			break;
		case "PlayerMoveEvent":
			PlayerMoveEvent pme = (PlayerMoveEvent)event;
			Gamer pmeGamer = Gamer.getGamer(pme.getPlayer());
			if(!pmeGamer.isInBuilderMode()) {
				if(pmeGamer.isInGame() && pmeGamer.getGame().isInProgress()) {
					Location from = pme.getFrom(), to = pme.getTo();
					RoundInfo info = game.getCurrentRoundInfo();
					if(from.getY() > to.getY() && to.getY() < info.getArena().getCurrentArenaData().getLowestY() && System.currentTimeMillis() - pmeTimestamps.getOrDefault(pmeGamer, 0L) > 1000) {
						pmeTimestamps.put(pmeGamer, System.currentTimeMillis());
						info.getRound().fell(pmeGamer);
					}
				}
				else {
					Location to = pme.getTo();
					if(to.getY() <= 70) {
						Vector vel = pmeGamer.getPlayer().getVelocity();
						vel.setY(10);
						pmeGamer.getPlayer().setVelocity(vel);
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
			Gamer pieGamer = Gamer.getGamer(pie.getPlayer());
			if(pieGamer.isInBuilderMode()) {
				if(Items.isType(pieGamer.getInventory().getItemInHand(), Material.STONE_AXE)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						pieGamer.setLeftSelection(pie.getClickedBlock().getLocation());
						pie.setCancelled(true);
					}
					else if(pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
						pieGamer.setRightSelection(pie.getClickedBlock().getLocation());
						pie.setCancelled(true);
					}
				}
				else if(Items.isType(pieGamer.getInventory().getItemInHand(), Material.STICK)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						pieGamer.getSelection().addBlock(pie.getClickedBlock());
						pie.setCancelled(true);
					}
					else if(pie.getAction() == Action.RIGHT_CLICK_BLOCK || pie.getAction() == Action.RIGHT_CLICK_AIR) {
						if(pieGamer.getPlayer().isSneaking()) pieGamer.getSelection().addSelection(false);
						else pieGamer.getSelection().addSelection(true);
						pie.setCancelled(true);
					}
				}
				else if(Items.isType(pieGamer.getInventory().getItemInHand(), Material.STRING)) {
					if(pie.getAction() == Action.LEFT_CLICK_BLOCK) {
						pieGamer.getSelection().removeBlock(pie.getClickedBlock());
						pie.setCancelled(true);
					}
					else if(pie.getAction() == Action.RIGHT_CLICK_BLOCK || pie.getAction() == Action.RIGHT_CLICK_AIR) {
						pieGamer.getSelection().removeSelection();
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
				Gamer edeGamer = Gamer.getGamer((Player)entity);
				if(edeGamer.isInBuilderMode()) break;
			}
			ede.setCancelled(true);
			break;
		case "PlayerDropItemEvent":
			PlayerDropItemEvent pdie = (PlayerDropItemEvent)event;
			if(!Gamer.getGamer(pdie.getPlayer()).isInBuilderMode()) pdie.setCancelled(true);
			break;
		case "BlockBreakEvent":
			BlockBreakEvent bbe = (BlockBreakEvent)event;
			if(!Gamer.getGamer(bbe.getPlayer()).isInBuilderMode()) bbe.setCancelled(true);
			break;
		case "BlockPlaceEvent":
			BlockPlaceEvent bpe = (BlockPlaceEvent)event;
			if(!Gamer.getGamer(bpe.getPlayer()).isInBuilderMode()) bpe.setCancelled(true);
			break;
		case "ChunkUnloadEvent":
			ChunkUnloadEvent cue = (ChunkUnloadEvent)event;
			if(ChunkLoader.shouldStayLoaded(cue.getChunk())) cue.setCancelled(true);
			break;
		case "FoodLevelChangeEvent":
		case "BlockFormEvent":
		case "WeatherChangeEvent":
			((Cancellable)event).setCancelled(true);
			break;
		default:
			break;
		}
		game.callEvent(event);
	}
	
	@Override
	public Plugin getPlugin() {
		return plugin;
	}
	
}
