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
package de.doridian.logstashlogger.actions;

import org.bukkit.entity.HumanEntity;
import org.json.simple.JSONObject;

public class PlayerChatAction extends PlayerAction {
	private final String message;

	public PlayerChatAction(HumanEntity user, String message) {
		super(user, "chat");
		this.message = message;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("message", message);
		return thisBlockChange;
	}
}
