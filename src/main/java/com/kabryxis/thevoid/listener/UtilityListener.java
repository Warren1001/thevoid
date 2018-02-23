package com.kabryxis.thevoid.listener;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class UtilityListener implements Listener {
	
	private final Plugin plugin;
	
	public UtilityListener(Plugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	/*@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(mode) {
			event.setCancelled(true);
			Block block = event.getBlock();
			new BukkitRunnable() {
				
				@Override
				public void run() {
					block.setType(Material.OBSIDIAN);
					block.setMetadata("hourglass", new FixedMetadataValue(plugin, true));
				}
				
			}.runTaskLater(plugin, 1L);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if(block.getType() == Material.OBSIDIAN && block.hasMetadata("hourglass")) {
			if(!mode) event.setCancelled(true);
			else block.removeMetadata("hourglass", plugin);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(chatMode) {
			Player player = event.getPlayer();
			chances[chanceIndex] = Integer.parseInt(event.getMessage());
			chanceIndex++;
			if(chanceIndex == chances.length) {
				chatMode = false;
				chanceIndex = 0;
				ConfigurationSection section = config.getConfigurationSection("levels");
				for(int i = 0; i < chances.length; i++) {
					section.set(i + ".chance", chances[i]);
				}
				chances = null;
				config.save();
				player.sendMessage("Done!");
			}
			else player.sendMessage("Chance for level " + (chanceIndex + 1) + " out of " + chances.length + "?");
			event.setCancelled(true);
		}
	}
	
	private int[] chances;
	private int chanceIndex = 0;
	
	private Config config;
	private boolean mode = false, chatMode = false;
	
	@Com(aliases = {"hourglass")
	public boolean onHourglass(Command command) {
		if(!arg.isPlayer()) return false;
		Player player = arg.getPlayer();
		String[] args = arg.getArgs();
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("mode")) {
				mode = !mode;
				player.sendMessage("Mode is now: " + mode);
				return true;
			}
			else if(args[0].equalsIgnoreCase("done")) {
				ConfigurationSection section = config.createSection("levels");
				BlockSelection clip = Gamer.getGamer(player).getClipboard();
				clip.extreme();
				Vector min = clip.getLeft(), max = clip.getRight();
				World world = player.getWorld();
				List<Integer> trueYList = new ArrayList<>();
				boolean found = false;
				for(int y = max.getBlockY(); y >= min.getBlockY(); y--) {
					for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
						for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
							Block block = world.getBlockAt(x, y, z);
							if(block.hasMetadata("hourglass")) {
								trueYList.add(y);
								found = true;
								break;
							}
						}
						if(found) {
							found = false;
							break;
						}
					}
				}
				int size = trueYList.size();
				chances = new int[size];
				for(int y = 0; y < size; y++) {
					int trueY = trueYList.get(y);
					List<String> list = new ArrayList<>(size);
					for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
						for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
							Block block = world.getBlockAt(x, trueY, z);
							if(block.hasMetadata("hourglass")) {
								block.setType(Material.AIR);
								block.removeMetadata("hourglass", plugin);
								list.add((block.getX() - min.getBlockX()) + "," + (block.getY() - min.getBlockY()) + "," + (block.getZ() - min.getBlockZ()));
							}
						}
					}
					section.set(y + ".data", list);
				}
				chatMode = true;
				mode = false;
				player.sendMessage("Chance for level " + (chanceIndex + 1) + " out of " + size + "?");
				return true;
			}
		}
		else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("create")) {
				config = Config.get(new File(Schematic.PATH + args[1].toLowerCase() + "-hg.yml"));
				return true;
			}
		}
		return false;
	}*/
	
}
