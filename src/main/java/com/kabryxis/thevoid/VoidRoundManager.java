package com.kabryxis.thevoid;

import com.kabryxis.kabutils.random.RandomArrayList;
import com.kabryxis.kabutils.random.SelfPredicateWeightedRandomArrayList;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.thevoid.api.round.RoundManager;
import com.kabryxis.thevoid.api.round.impl.VoidRound;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VoidRoundManager implements RoundManager<VoidRound> {
	
	private final RandomArrayList<VoidRound> registeredRounds = new SelfPredicateWeightedRandomArrayList<>(2);
	private final Map<String, Object> globalDefaults = new HashMap<>();
	private final Map<String, Class<?>> globalRequiredObjects = new HashMap<>();
	
	private final Plugin owner;
	private final File folder;
	
	public VoidRoundManager(Plugin owner) {
		this.owner = owner;
		this.folder = new File(owner.getDataFolder(), "rounds");
		folder.mkdirs();
	}
	
	@Override
	public void registerRound(VoidRound round) {
		registeredRounds.add(round);
	}
	
	@Override
	public void registerRounds(VoidRound... rounds) {
		registerRounds(rounds);
	}
	
	@Override
	public void addGlobalRequiredDefault(String key, Object object) {
		globalDefaults.put(key, object);
	}
	
	@Override
	public void addGlobalRequiredObject(String key, Class<?> objectType) {
		globalRequiredObjects.put(key, objectType);
	}
	
	@Override
	public VoidRound getRandomRound() {
		return registeredRounds.random();
	}
	
	@Override
	public Config getData(VoidRound round) {
		String roundFileName = round.getName() + ".yml";
		File file = new File(folder, roundFileName);
		Config data;
		if(!file.exists()) {
			InputStream is = owner.getResource(roundFileName);
			if(is == null) {
				data = new Config(file);
				data.addDefaults(globalDefaults);
				round.setCustomDefaults(data);
				data.options().copyDefaults(true);
				data.save();
			}
			else data = new Config(file, is);
			checkData(round, data);
		}
		else {
			data = new Config(file);
			data.load(data0 -> checkData(round, data0));
		}
		return data;
	}
	
	protected void checkData(VoidRound round, Config data) {
		String roundName = round.getName();
		Map<String, Class<?>> requiredObjects = round.getRequiredObjects();
		if(globalRequiredObjects.size() + requiredObjects.size() > 0) {
			owner.getLogger().warning("Disabling Round '" + roundName + "' because it does not have the required non-default entries that follow:");
			StringBuilder builder = new StringBuilder("Entries: [(template)entryName, objectType], ");
			for(Iterator<Map.Entry<String, Class<?>>> iterator = globalRequiredObjects.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Class<?>> globalRequiredObject = iterator.next();
				String key = globalRequiredObject.getKey();
				String objectTypeName = globalRequiredObject.getValue().getSimpleName();
				builder.append("[");
				builder.append(key);
				builder.append(", ");
				builder.append(objectTypeName);
				builder.append("]");
				if(iterator.hasNext()) builder.append(", ");
				data.set(key, "CHANGE ME TO A " + objectTypeName);
			}
			if(requiredObjects.size() > 0 && globalRequiredObjects.size() > 0) builder.append(", ");
			for(Iterator<Map.Entry<String, Class<?>>> iterator = requiredObjects.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Class<?>> requiredObject = iterator.next();
				String key = requiredObject.getKey();
				String objectTypeName = requiredObject.getValue().getSimpleName();
				builder.append("[");
				builder.append(key);
				builder.append(", ");
				builder.append(objectTypeName);
				builder.append("]");
				if(iterator.hasNext()) builder.append(", ");
				data.set(key, "CHANGE ME TO A " + objectTypeName);
			}
			owner.getLogger().warning(builder.toString());
			owner.getLogger().warning("Check the Round '" + roundName + "''s data file at " + data.getFile().getPath() + " and configure the values necessary.");
			round.setEnabled(false);
		}
	}
	
}
