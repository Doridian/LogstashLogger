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
import org.json.simple.JSONObject;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class BaseAction {
	private final Date timestamp = new Date();
	private final String action;

	private static final String HOSTNAME;
	private static final DateFormat JSON_DATE_FORMAT;

	static {
		String _hostname;
		try {
			_hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			e.printStackTrace();
			_hostname = "N/A";
		}
		HOSTNAME = _hostname;

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		JSON_DATE_FORMAT = df;
	}

	public BaseAction(String action) {
		this.action = action;
	}

	public JSONObject toJSONObject() {
		final JSONObject thisBlockChange = new JSONObject();

		thisBlockChange.put("@version", "1");
		thisBlockChange.put("@timestamp", JSON_DATE_FORMAT.format(timestamp));
		thisBlockChange.put("type", "minecraft_action");

		thisBlockChange.put("action", action);

		thisBlockChange.put("host", HOSTNAME);
		thisBlockChange.put("server", FoxelLog.instance.configuration.getValue("server-name", "N/A"));

		return thisBlockChange;
	}

}
