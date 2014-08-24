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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;

public class PlayerBlockAction extends PlayerAndLocationAction {
	private final Material blockFrom;
	private final Material blockTo;

	public PlayerBlockAction(HumanEntity user, Location location, Material blockFrom, Material blockTo) {
		super(user, location);
		this.blockFrom = blockFrom;
		this.blockTo = blockTo;
	}

    protected PlayerBlockAction(Map<String, Object> fields) {
        super(fields);
        this.blockFrom = Material.getMaterial((String)fields.get("blockFrom"));
        this.blockTo = Material.getMaterial((String)fields.get("blockTo"));
    }

    @Override
    protected Map<String, Map<String, Object>> getCustomMappings() throws IOException {
        Map<String, Map<String, Object>> retMap = super.getCustomMappings();

        retMap.put("blockFrom", builderBasicTypeMapping("string", "not_analyzed", null));
        retMap.put("blockTo", builderBasicTypeMapping("string", "not_analyzed", null));

        return retMap;
    }

    @Override
    public String getType() {
        return "player_block_change";
    }

    public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder = super.toJSONObject(builder);

		builder.field("blockFrom", blockFrom.name());
		builder.field("blockTo", blockTo.name());

		return builder;
	}

    public Material getBlockFrom() {
        return blockFrom;
    }

    public Material getBlockTo() {
        return blockTo;
    }
}
