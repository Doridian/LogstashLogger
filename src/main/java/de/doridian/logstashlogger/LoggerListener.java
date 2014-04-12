package de.doridian.logstashlogger;

import de.doridian.logstashlogger.actions.PlayerAction;
import de.doridian.logstashlogger.actions.PlayerBlockAction;
import de.doridian.logstashlogger.actions.PlayerChatAction;
import de.doridian.logstashlogger.actions.PlayerInventoryAction;
import de.doridian.logstashlogger.redis.RedisQueueThread;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class LoggerListener implements Listener {
	private void addBlockChange(Player user, String action, Block block) {
		RedisQueueThread.queueAction(new PlayerBlockAction(user, action, block));
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

	private Player getOwnerOfInventory(Inventory inventory) {
		return (Player)inventory.getHolder();
	}

	private static class LocationAndMaterial {
		public final Location location;
		public final Material material;

		private LocationAndMaterial(Location location, Material material) {
			this.location = location;
			this.material = material;
		}
	}

	private LocationAndMaterial getLocationAndMaterialOfInventory(Inventory inventory) {
		final InventoryHolder holder = inventory.getHolder();

		switch(inventory.getType()) {
			case BEACON:
				Beacon beacon = (Beacon)holder;
				return new LocationAndMaterial(beacon.getLocation(), beacon.getType());
			case BREWING:
				BrewingStand brewingStand = (BrewingStand)holder;
				return new LocationAndMaterial(brewingStand.getLocation(), brewingStand.getType());
			case CHEST:
				if(holder instanceof DoubleChest) {
					DoubleChest doubleChest = (DoubleChest)holder;
					return new LocationAndMaterial(doubleChest.getLocation(), doubleChest.getLocation().getBlock().getType());
				} else {
					Chest chest = (Chest)holder;
					return new LocationAndMaterial(chest.getLocation(), chest.getType());
				}
			case DISPENSER:
				Dispenser dispenser = (Dispenser)holder;
				return new LocationAndMaterial(dispenser.getLocation(), dispenser.getType());
			case DROPPER:
				Dropper dropper = (Dropper)holder;
				return new LocationAndMaterial(dropper.getLocation(), dropper.getType());
			case FURNACE:
				Furnace furnace = (Furnace)holder;
				return new LocationAndMaterial(furnace.getLocation(), furnace.getType());
			case HOPPER:
				Hopper hopper = (Hopper)holder;
				return new LocationAndMaterial(hopper.getLocation(), hopper.getType());
			case PLAYER:
				return new LocationAndMaterial(getOwnerOfInventory(inventory).getLocation(), Material.AIR);

			case CREATIVE:
			case ENDER_CHEST:
			case ENCHANTING:
			case WORKBENCH:
			case ANVIL:
			case CRAFTING:
				return null;
		}

		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInventoryMove(InventoryMoveItemEvent event) {
		if(event.isCancelled())
			return;
		try {
			final Inventory source = event.getSource();
			final Inventory destination = event.getDestination();
			final ItemStack itemStack = event.getItem();
			if (source == null || destination == null || itemStack == null || source.equals(destination) || itemStack.getAmount() == 0)
				return;
			final int amount;
			final Player player;
			final LocationAndMaterial locationAndMaterial;
			if (destination.getType() == InventoryType.PLAYER) {
				amount = -itemStack.getAmount();
				player = getOwnerOfInventory(destination);
				locationAndMaterial = getLocationAndMaterialOfInventory(source);
			} else if (source.getType() == InventoryType.PLAYER) {
				amount = itemStack.getAmount();
				player = getOwnerOfInventory(source);
				locationAndMaterial = getLocationAndMaterialOfInventory(destination);
			} else {
				return;
			}
			if (locationAndMaterial == null)
				return;
			RedisQueueThread.queueAction(new PlayerInventoryAction(player, locationAndMaterial.location, locationAndMaterial.material, amount));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
