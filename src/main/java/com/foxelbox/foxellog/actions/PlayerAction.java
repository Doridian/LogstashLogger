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
import org.bukkit.entity.HumanEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public abstract class PlayerAction extends BaseAction {
	private final HumanEntity user;

    protected PlayerAction(HumanEntity user) {
		this.user = user;
	}

    protected PlayerAction(Map<String, Object> fields) {
        super(fields);
        user = FoxelLog.instance.getServer().getPlayer(UUID.fromString((String)fields.get("user_uuid")));
    }

    @Override
    protected Map<String, Map<String, Object>> getCustomMappings() throws IOException {
        Map<String, Map<String, Object>> retMap = super.getCustomMappings();

        retMap.put("user_uuid", builderBasicTypeMapping("string", "not_analyzed", null));

        return retMap;
    }

	@Override
    public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder = super.toJSONObject(builder);

		//builder.field("user_name", user.getName());
		builder.field("user_uuid", user.getUniqueId().toString());

		return builder;
	}

    public HumanEntity getUser() {
        return user;
    }
}
