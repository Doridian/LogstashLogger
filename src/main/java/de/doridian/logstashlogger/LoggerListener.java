package de.doridian.logstashlogger;

import de.doridian.logstashlogger.actions.PlayerAction;
import de.doridian.logstashlogger.actions.PlayerBlockAction;
import de.doridian.logstashlogger.actions.PlayerChatAction;
import de.doridian.logstashlogger.redis.RedisQueueThread;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoggerListener implements Listener {
	private void addBlockChange(Player user, Location location, Material materialBefore, Material materialAfter) {
		RedisQueueThread.queueAction(new PlayerBlockAction(user, location, materialBefore, materialAfter));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		addBlockChange(event.getPlayer(), event.getBlockPlaced().getLocation(), event.getBlockReplacedState().getType(), event.getBlockPlaced().getType());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		addBlockChange(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getType(), Material.AIR);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBucketFill(PlayerBucketFillEvent event) {
		addBlockChange(event.getPlayer(), event.getBlockClicked().getLocation(), event.getBlockClicked().getType(), Material.AIR);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketFillEvent event) {
		Material material = event.getBucket();
		switch(event.getBucket()) {
			case WATER_BUCKET:
				material = Material.WATER;
				break;
			case LAVA_BUCKET:
				material = Material.LAVA;
				break;
		}
		addBlockChange(event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()).getLocation(), Material.AIR, material);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		RedisQueueThread.queueAction(new PlayerAction(event.getPlayer(), "join"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		RedisQueueThread.queueAction(new PlayerAction(event.getPlayer(), "quit"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		RedisQueueThread.queueAction(new PlayerChatAction(event.getPlayer(), event.getMessage()));
	}
}
