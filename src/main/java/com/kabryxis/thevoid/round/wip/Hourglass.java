package com.kabryxis.thevoid.round.wip;

import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.thevoid.api.game.Game;
import com.kabryxis.thevoid.api.impl.arena.schematic.VoidSchematic;
import com.kabryxis.thevoid.api.impl.round.SurvivalRound;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundInfo;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Hourglass extends SurvivalRound {
	
	private final Random rand = new Random();
	
	private int[][][] data;
	private int[] chances;
	
	public Hourglass(RoundManager<BasicRound> roundManager) {
		super(roundManager, "hourglass", false);
	}
	
	@Override
	public void load(Game game, RoundInfo info) {
		loadHourglassData(info.getArena().getCurrentArenaData().getSchematic().getName());
	}
	
	@Override
	public void start(Game game) {
		/*for(int i = 0; i < data.length; i++) {
			int level = i;
			int[][] locs = data[i];
			BukkitThreads.syncTimer(new BukkitRunnable() {
				
				int index = 0;
				int chance = chances[level];
				int amount = Math.max(1, locs.length / 12);
				
				@Override
				public void run() {
					if(!canRun()) {
						cancel();
						return;
					}
					for(int i = 0; i < amount; i++) {
						int[] loc = locs[rand.nextInt(locs.length)];
						Block block = arena.getBlock(loc[0], loc[1] + 1, loc[2]);
						if(rand.nextInt(100) + 1 < chance && block.getType() == Material.AIR) block.setType(Material.SAND);
					}
					if(index == locs.length - 1) index = 0;
					else index++;
				}
				
			}, i * 40L, 3L);
		}*/
	}
	
	@Override
	public void end(Game game) {
		data = null;
		chances = null;
	}
	
	@Override
	public void setCustomDefaults(Config data) {
		data.addDefault("round-length", -1);
		data.addDefault("schematics", Collections.singletonList("hourglass"));
	}
	
	private boolean canRun() {
		return data != null;
	}
	
	private void loadHourglassData(String name) {
		new Config(new File(VoidSchematic.PATH + name + "-hg.yml")).load(config -> {
			ConfigurationSection section = config.getConfigurationSection("levels");
			Set<String> levels = section.getKeys(false);
			int size = levels.size();
			data = new int[size][][];
			chances = new int[size];
			for(String key : levels) {
				int level = Integer.parseInt(key);
				chances[level] = section.getInt(key + ".chance");
				List<String> list = section.getStringList(key + ".data");
				int[][] levelData = new int[list.size()][3];
				for(int i = 0; i < list.size(); i++) {
					String[] args = list.get(i).split(",");
					int[] array = levelData[i];
					array[0] = Integer.parseInt(args[0]);
					array[1] = Integer.parseInt(args[1]);
					array[2] = Integer.parseInt(args[2]);
				}
				data[level] = levelData;
			}
		});
	}
	
}
