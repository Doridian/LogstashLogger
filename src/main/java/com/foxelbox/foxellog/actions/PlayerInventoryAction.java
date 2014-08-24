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
package com.foxelbox.foxellog.actions;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;

public class PlayerInventoryAction extends BaseAction {
	private final Material block;
	private final Material container;
	private final int amount;

	public PlayerInventoryAction(HumanEntity user, Location location, Material container, Material block, int amount) {
		super(user, location);
		this.block = block;
		this.container = container;
		this.amount = amount;
	}

    protected PlayerInventoryAction(DBObject fields) {
        super(fields);
        this.amount = (Integer)fields.get("amount");
        this.block = Material.getMaterial((String)fields.get("block"));
        this.container = Material.getMaterial((String)fields.get("container"));
    }

    @Override
    public String getActionType() {
        return "player_inventory_change";
    }

    @Override
    protected BasicDBObject toBasicDBObject(BasicDBObject builder) {
        builder = super.toBasicDBObject(builder);

		builder.append("block", block.name());
		builder.append("container", container.name());
		builder.append("amount", amount);

		return builder;
	}
}
