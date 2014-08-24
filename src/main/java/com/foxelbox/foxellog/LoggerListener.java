/**
 * This file is part of FoxelLog.
 *
 * FoxelLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxelLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxelLog.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxellog;

import com.foxelbox.foxellog.actions.*;
import com.foxelbox.foxellog.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.util.HashMap;
import java.util.Map;

public class LoggerListener implements Listener {
    private final FoxelLog plugin;
    public LoggerListener(FoxelLog plugin) {
        this.plugin = plugin;
    }

    private void queueAction(BaseAction action) {
        try {
            plugin.elasticsearchClient
                    .prepareIndex(plugin.getIndexName(), plugin.getTypeName())
                    .setSource(
                            action.toJSONObject(
                                    XContentFactory.jsonBuilder().startObject()
                            ).endObject()
                    ).execute().actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private void addBlockChange(HumanEntity user, Location location, Material materialBefore, Material materialAfter) {
		queueAction(new PlayerBlockAction(user, location, materialBefore, materialAfter));
	}

	//BLOCK PLAYER EVENTS
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

	//BASE PLAYER EVENTS
	/*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		queueAction(new PlayerAction(event.getPlayer(), "join"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		queueAction(new PlayerAction(event.getPlayer(), "quit"));
	}

	@EventHandler(priority = EventPriority.MONITOR) //DO NOT ignoreCancelled = true!!!
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		queueAction(new PlayerChatAction(event.getPlayer(), event.getMessage()));
	}

	@EventHandler(priority = EventPriority.MONITOR) //DO NOT ignoreCancelled = true!!!
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		queueAction(new PlayerChatAction(event.getPlayer(), event.getMessage()));
	}*/

	//INVENTORY PLAYER EVENTS
	private final Map<HumanEntity, ItemStack[]> containers = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		final InventoryHolder holder = event.getInventory().getHolder();
		if (holder instanceof BlockState || holder instanceof DoubleChest) {
			final HumanEntity player = event.getPlayer();
			final ItemStack[] before = containers.get(player);
			if (before != null) {
				final ItemStack[] after = BukkitUtils.compressInventory(event.getInventory().getContents());
				final ItemStack[] diff = BukkitUtils.compareInventories(before, after);
				final Location loc = BukkitUtils.getInventoryHolderLocation(holder);
				final Material block = loc.getBlock().getType();
				for (final ItemStack item : diff) {
					queueAction(new PlayerInventoryAction(player, loc, block, item.getType(), item.getAmount()));
				}
				containers.remove(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.getInventory() != null) {
			final InventoryHolder holder = event.getInventory().getHolder();
			if (holder instanceof BlockState || holder instanceof DoubleChest) {
				if (!BukkitUtils.getInventoryHolderType(holder).equals(Material.WORKBENCH)) {
					containers.put(event.getPlayer(), BukkitUtils.compressInventory(event.getInventory().getContents()));
				}
			}
		}
	}
}
