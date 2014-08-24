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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class BaseAction {
	private final Date timestamp = new Date();
	private final String action;

    private static final String INDEX_NAME = "foxellog_" + FoxelLog.instance.configuration.getValue("server-name", "N/A").toLowerCase();
    public static String getIndexName() {
        return INDEX_NAME;
    }

	public BaseAction(String action) {
		this.action = action;
	}

	public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder.field("date", timestamp);
        builder.field("action", action);

		return builder;
	}

}
