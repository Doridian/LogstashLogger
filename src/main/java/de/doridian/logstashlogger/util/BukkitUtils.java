/**
 * This file is part of LogstashLogger.
 *
 * LogstashLogger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LogstashLogger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LogstashLogger.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.logstashlogger.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BukkitUtils
{
	public static Material getInventoryHolderType(InventoryHolder holder) {
		if (holder instanceof DoubleChest) {
			return ((DoubleChest)holder).getLocation().getBlock().getType();
		} else if (holder instanceof BlockState) {
			return ((BlockState)holder).getType();
		} else {
			return Material.AIR;
		}
	}

	public static Location getInventoryHolderLocation(InventoryHolder holder) {
		if (holder instanceof DoubleChest) {
			return ((DoubleChest)holder).getLocation();
		} else if (holder instanceof BlockState) {
			return ((BlockState)holder).getLocation();
		} else {
			return null;
		}
	}

	public static ItemStack[] compareInventories(ItemStack[] items1, ItemStack[] items2) {
		final ItemStackComparator comperator = new ItemStackComparator();
		final ArrayList<ItemStack> diff = new ArrayList<ItemStack>();
		final int l1 = items1.length, l2 = items2.length;
		int c1 = 0, c2 = 0;
		while (c1 < l1 || c2 < l2) {
			if (c1 >= l1) {
				diff.add(items2[c2]);
				c2++;
				continue;
			}
			if (c2 >= l2) {
				items1[c1].setAmount(items1[c1].getAmount() * -1);
				diff.add(items1[c1]);
				c1++;
				continue;
			}
			final int comp = comperator.compare(items1[c1], items2[c2]);
			if (comp < 0) {
				items1[c1].setAmount(items1[c1].getAmount() * -1);
				diff.add(items1[c1]);
				c1++;
			} else if (comp > 0) {
				diff.add(items2[c2]);
				c2++;
			} else {
				final int amount = items2[c2].getAmount() - items1[c1].getAmount();
				if (amount != 0) {
					items1[c1].setAmount(amount);
					diff.add(items1[c1]);
				}
				c1++;
				c2++;
			}
		}
		return diff.toArray(new ItemStack[diff.size()]);
	}

	public static ItemStack[] compressInventory(ItemStack[] items) {
		final ArrayList<ItemStack> compressed = new ArrayList<ItemStack>();
		for (final ItemStack item : items)
			if (item != null) {
				final int type = item.getTypeId();
				final short data = rawData(item);
				boolean found = false;
				for (final ItemStack item2 : compressed)
					if (type == item2.getTypeId() && data == rawData(item2)) {
						item2.setAmount(item2.getAmount() + item.getAmount());
						found = true;
						break;
					}
				if (!found)
					compressed.add(new ItemStack(type, item.getAmount(), data));
			}
		Collections.sort(compressed, new ItemStackComparator());
		return compressed.toArray(new ItemStack[compressed.size()]);
	}

	public static short rawData(ItemStack item) {
		return item.getType() != null ? item.getData() != null ? item.getDurability() : 0 : 0;
	}

	public static class ItemStackComparator implements Comparator<ItemStack>
	{
		@Override
		public int compare(ItemStack a, ItemStack b) {
			final int aType = a.getTypeId(), bType = b.getTypeId();
			if (aType < bType)
				return -1;
			if (aType > bType)
				return 1;
			final short aData = rawData(a), bData = rawData(b);
			if (aData < bData)
				return -1;
			if (aData > bData)
				return 1;
			return 0;
		}
	}
}