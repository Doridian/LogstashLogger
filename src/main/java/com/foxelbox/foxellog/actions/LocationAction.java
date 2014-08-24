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

import com.foxelbox.foxellog.FoxelLog;
import org.bukkit.Location;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHitField;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class LocationAction extends BaseAction {
	private final Location location;

    protected LocationAction(Location location) {
		this.location = location;
	}

    protected LocationAction(Map<String, Object> fields) {
        super(fields);
        location = new Location(FoxelLog.instance.getServer().getWorld((String)fields.get("world")), (double)fields.get("x"), (double)fields.get("y"), (double)fields.get("z"));
    }

    @Override
    protected Map<String, Map<String, Object>> getCustomMappings() throws IOException {
        Map<String, Map<String, Object>> retMap = super.getCustomMappings();

        retMap.put("x", builderBasicTypeMapping("double", null, null));
        retMap.put("y", builderBasicTypeMapping("double", null, null));
        retMap.put("z", builderBasicTypeMapping("double", null, null));

        retMap.put("world", builderBasicTypeMapping("string", "not_analyzed", null));

        return retMap;
    }

    @Override
	public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder = super.toJSONObject(builder);
        
		builder.field("x", location.getX());
		builder.field("y", location.getY());
		builder.field("z", location.getZ());

		builder.field("world", location.getWorld().getName());

		return builder;
	}
}
