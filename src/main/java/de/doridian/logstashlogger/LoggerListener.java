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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoggerListener implements Listener {
	private void addBlockChange(Player user, Location location, Material materialBefore, Material materialAfter) {
		RedisQueueThread.queueAction(new PlayerBlockAction(user, location, materialBefore, materialAfter));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.isCancelled())
			return;

		addBlockChange(event.getPlayer(), event.getBlockPlaced().getLocation(), event.getBlockReplacedState().getType(), event.getBlockPlaced().getType());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled())
			return;

		addBlockChange(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getType(), Material.AIR);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		RedisQueueThread.queueAction(new PlayerAction(event.getPlayer(), "join"));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		RedisQueueThread.queueAction(new PlayerAction(event.getPlayer(), "quit"));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		RedisQueueThread.queueAction(new PlayerChatAction(event.getPlayer(), event.getMessage()));
	}
}
