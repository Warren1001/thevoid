package com.kabryxis.thevoid.round.utility;

import com.kabryxis.kabutils.random.Randoms;
import com.kabryxis.kabutils.spigot.concurrent.BukkitThreads;
import com.kabryxis.kabutils.spigot.inventory.itemstack.ItemBuilder;
import com.kabryxis.kabutils.string.IncrementalString;
import com.kabryxis.thevoid.api.game.Gamer;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public class HotPotatoItem implements Runnable {
	
	private IncrementalString is = new IncrementalString(ChatColor.GOLD + "Hot Potato: " + ChatColor.WHITE + "[x]", 4);
	private ItemStack item = new ItemBuilder(Material.POTATO_ITEM).enchant(Enchantment.DURABILITY, 1).name(is.get()).build();
	private long lastTick = 0L;
	
	private BukkitTask task;
	private Collection<Gamer> gamers;
	private Gamer currentHolder, previousHolder;
	
	@Override
	public void run() {
		long current = System.currentTimeMillis();
		if(current - lastTick >= 1000) {
			if(is.getNumber() == 0) {
				explode();
				return;
			}
			lastTick = current;
			ItemStack item = currentHolder.getInventory().getItem(0);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(is.decrement());
			item.setItemMeta(meta);
		}
	}
	
	public void start(Collection<Gamer> gamers) {
		this.gamers = gamers;
		currentHolder = Randoms.getRandom(gamers);
		currentHolder.getInventory().setItem(0, item);
		lastTick = System.currentTimeMillis();
		task = BukkitThreads.syncTimer(this, 10L, 1L);
	}
	
	public void stop() {
		task.cancel();
		is.reset();
	}
	
	public boolean isCurrentHolder(Player player) {
		return currentHolder.getPlayer() == player;
	}
	
	public void changeHolder(Gamer newHolder) {
		//if(newHolder == previousHolder) return;
		previousHolder = currentHolder;
		currentHolder = newHolder;
		previousHolder.getInventory().clear();
		is.reset();
		currentHolder.getInventory().setItem(0, item);
		lastTick = System.currentTimeMillis();
	}
	
	public void explode() {
		Player player = currentHolder.getPlayer();
		player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0);
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1F, 1F);
		currentHolder.decrementRoundPoints(false);
		boolean ending = currentHolder.kill();
		currentHolder.teleport(20);
		if(ending) {
			stop();
			return;
		}
		changeHolder(Randoms.getRandom(gamers));
	}
	
	public void cache() {
		is.reset();
		task = null;
		gamers = null;
		currentHolder = null;
		previousHolder = null;
	}
	
}
