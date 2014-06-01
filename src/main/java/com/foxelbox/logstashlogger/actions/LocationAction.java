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
package com.foxelbox.logstashlogger.actions;

import org.bukkit.Location;
import org.json.simple.JSONObject;

public abstract class LocationAction extends BaseAction {
	private final Location location;

	public LocationAction(String action, Location location) {
		super(action);
		this.location = location;
	}

	@Override
	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = super.toJSONObject();
		thisBlockChange.put("x", location.getX());
		thisBlockChange.put("y", location.getY());
		thisBlockChange.put("z", location.getZ());
		thisBlockChange.put("world", location.getWorld().getName());
		return thisBlockChange;
	}
}
