package de.doridian.logstashlogger;

import de.doridian.logstashlogger.actions.BlockChangeAction;
import de.doridian.logstashlogger.redis.RedisQueueThread;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class LoggerListener implements Listener {
	private void addBlockChange(Player user, String action, Block block) {
		RedisQueueThread.queueAction(new BlockChangeAction(user, action, block.getLocation(), block.getType()));
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
}
