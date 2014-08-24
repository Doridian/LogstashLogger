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
import org.bukkit.entity.HumanEntity;

import java.util.Map;

public class PlayerChatAction extends BaseAction {
	private final String message;

	public PlayerChatAction(HumanEntity user, String message) {
		super(user, user.getLocation());
		this.message = message;
	}

    protected PlayerChatAction(DBObject fields) {
        super(fields);
        this.message = (String)fields.get("message");
    }

    @Override
    public String getActionType() {
        return "player_chat";
    }

    @Override
    protected BasicDBObject toBasicDBObject(BasicDBObject builder) {
        builder = super.toBasicDBObject(builder);

		builder.append("message", message);

		return builder;
	}
}
