package com.kabryxis.thevoid;

import com.kabryxis.kabutils.random.RandomArrayList;
import com.kabryxis.kabutils.random.weighted.conditional.SelfConditionalWeightedRandomArrayList;
import com.kabryxis.kabutils.spigot.data.Config;
import com.kabryxis.thevoid.api.round.BasicRound;
import com.kabryxis.thevoid.api.round.RoundManager;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VoidRoundManager implements RoundManager<BasicRound> {
	
	private final RandomArrayList<BasicRound> registeredRounds = new SelfConditionalWeightedRandomArrayList<>(2);
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
	public void registerRound(BasicRound round) {
		registeredRounds.add(round);
	}
	
	@Override
	public void registerRounds(BasicRound... rounds) {
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
	public BasicRound getRandomRound() {
		return registeredRounds.random();
	}
	
	@Override
	public Config getData(BasicRound round) {
		String roundFileName = round.getName() + ".yml";
		File file = new File(folder, roundFileName);
		Config data;
		if(!file.exists()) {
			InputStream is = owner.getResource(roundFileName);
			data = is == null ? new Config(file) : new Config(file, is);
			checkData(round, data);
		}
		else {
			data = new Config(file);
			data.load(data0 -> checkData(round, data0));
		}
		return data;
	}
	
	protected void checkData(BasicRound round, Config data) {
		data.addDefaults(globalDefaults);
		round.setCustomDefaults(data);
		data.options().copyDefaults(true);
		String roundName = round.getName();
		Map<String, Class<?>> combinedObjects = new HashMap<>();
		combinedObjects.putAll(globalRequiredObjects);
		combinedObjects.putAll(round.getRequiredObjects());
		for(Iterator<Map.Entry<String, Class<?>>> iterator = combinedObjects.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Class<?>> entry = iterator.next();
			String key = entry.getKey();
			if(data.isSet(key) && entry.getValue().isInstance(data.get(key))) iterator.remove();
		}
		if(combinedObjects.size() > 0) {
			owner.getLogger().warning("Disabling Round '" + roundName + "' because it does not have the required non-default entries that follow:");
			StringBuilder builder = new StringBuilder("Entries: [(template)entryName, objectType], ");
			for(Iterator<Map.Entry<String, Class<?>>> iterator = combinedObjects.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Class<?>> entry = iterator.next();
				String key = entry.getKey();
				String objectTypeName = entry.getValue().getSimpleName();
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
		data.save();
	}
	
}
