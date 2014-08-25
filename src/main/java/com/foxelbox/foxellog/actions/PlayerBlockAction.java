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

public class PlayerBlockAction extends BaseAction {
	private final Material blockFrom;
	private final Material blockTo;

	public PlayerBlockAction(HumanEntity user, Location location, Material blockFrom, Material blockTo) {
		super(user, location);
		this.blockFrom = blockFrom;
		this.blockTo = blockTo;
	}
    protected PlayerBlockAction(DBObject fields) {
        super(fields);
        this.blockFrom = getMaterial(fields, "blockFrom");
        this.blockTo = getMaterial(fields, "blockTo");
    }

    @Override
    public String getActionType() {
        return "player_block_change";
    }

    protected BasicDBObject toBasicDBObject(BasicDBObject builder) {
        builder = super.toBasicDBObject(builder);

        storeMaterial(builder, "blockFrom", blockFrom);
        storeMaterial(builder, "blockTo", blockTo);

		return builder;
	}

    public Material getBlockFrom() {
        return blockFrom;
    }

    public Material getBlockTo() {
        return blockTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlayerBlockAction that = (PlayerBlockAction) o;

        if (blockFrom != that.blockFrom) return false;
        if (blockTo != that.blockTo) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + blockFrom.hashCode();
        result = 31 * result + blockTo.hashCode();
        return result;
    }
}
