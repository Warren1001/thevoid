package com.kabryxis.thevoid.listener;

import com.kabryxis.kabutils.spigot.event.GlobalListener;
import com.kabryxis.thevoid.api.game.Game;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class VoidListener implements GlobalListener {
	
	private final Game game;
	private final Plugin plugin;
	
	public VoidListener(Game game, Plugin plugin) {
		this.game = game;
		this.plugin = plugin;
	}
	
	@Override
	public void onEvent(Event event) {
		game.callEvent(event);
	}
	
	@Override
	public Plugin getPlugin() {
		return plugin;
	}
	
}
