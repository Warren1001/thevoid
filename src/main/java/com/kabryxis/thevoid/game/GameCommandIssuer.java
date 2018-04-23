package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.spigot.command.BukkitCommandIssuer;
import com.kabryxis.thevoid.api.game.Gamer;

public class GameCommandIssuer extends BukkitCommandIssuer {
	
	public Gamer getGamer() {
		return isPlayer() ? Gamer.getGamer(getPlayer()) : null;
	}
	
}
