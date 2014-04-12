package de.doridian.logstashlogger;

import de.doridian.logstashlogger.config.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogstashLoggerListener extends Thread implements Listener {
	private static final String HOSTNAME;
	private static final DateFormat JSON_DATE_FORMAT;

	private final Queue<BlockChangeObject> blockChangesQueue = new ConcurrentLinkedQueue<>();

	static {
		String _hostname;
		try {
			_hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			e.printStackTrace();
			_hostname = "N/A";
		}
		HOSTNAME = _hostname;

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		JSON_DATE_FORMAT = df;
	}

	private void addBlockChange(Player user, String action, Block block) {
		blockChangesQueue.add(new BlockChangeObject(user, action, block.getLocation(), block.getType()));
	}

	@Override
	public void run() {
		while(true) {
			if (!blockChangesQueue.isEmpty()) {
				final Jedis jedis = RedisManager.jedisPool.getResource();
				try {
					while (!blockChangesQueue.isEmpty()) {
						BlockChangeObject blockChangeObject = blockChangesQueue.poll();
						if (blockChangeObject == null)
							continue;
						jedis.lpush("logstash", blockChangeObject.toJSONObject().toJSONString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				RedisManager.jedisPool.returnResource(jedis);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { }
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.isCancelled())
			return;

		addBlockChange(event.getPlayer(), "place", event.getBlockPlaced());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled())
			return;

		addBlockChange(event.getPlayer(), "break", event.getBlock());
	}

	private static class BlockChangeObject {
		private final Date timestamp = new Date();
		private final Player user;
		private final String action;
		private final Location location;
		private final Material material;

		private BlockChangeObject(Player user, String action, Location location, Material material) {
			this.user = user;
			this.action = action;
			this.location = location;
			this.material = material;
		}

		JSONObject toJSONObject() {
			final JSONObject thisBlockChange = new JSONObject();

			thisBlockChange.put("@version", "1");
			thisBlockChange.put("@timestamp", JSON_DATE_FORMAT.format(timestamp));
			thisBlockChange.put("type", "minecraft_action");

			thisBlockChange.put("username", user.getName());
			thisBlockChange.put("useruuid", user.getUniqueId().toString());
			thisBlockChange.put("action", action);

			thisBlockChange.put("host", HOSTNAME);
			thisBlockChange.put("server", Configuration.getValue("server-name", "N/A"));

			thisBlockChange.put("block", material.name());
			thisBlockChange.put("x", location.getX());
			thisBlockChange.put("y", location.getY());
			thisBlockChange.put("z", location.getZ());
			thisBlockChange.put("world", location.getWorld().getName());

			return thisBlockChange;
		}
	}
}
