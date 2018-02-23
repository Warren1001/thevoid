package com.kabryxis.thevoid.game;

import com.kabryxis.kabutils.concurrent.thread.PausableThread;
import com.kabryxis.thevoid.api.game.Game;

public class GameThread extends PausableThread {
	
	private Game game;
	
	private boolean isGameRunning = false;
	
	public GameThread() {
		super();
	}
	
	public GameThread(String name) {
		super(name);
	}
	
	public GameThread(Game game) {
		this();
		this.game = game;
	}
	
	public GameThread(String name, Game game) {
		this(name);
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}
	
	@Override
	public void start() {
		if(isAlive()) unpause();
		else super.start();
	}
	
	@Override
	public void run() {
		while(isRunning()) {
			isGameRunning = true;
			game.threadStart();
			while(isRunning() && game.canRun()) {
				pauseCheck();
				game.next();
				game.start();
				game.timer();
				game.end();
			}
			game.threadEnd();
			isGameRunning = false;
			pause();
			pauseCheck();
		}
	}
	
	@Override
	public void onPause() {
		if(isGameRunning) game.pause();
	}
	
	@Override
	public void onUnpause() {
		if(isGameRunning) game.unpause();
	}
	
}
