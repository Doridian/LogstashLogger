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

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Date;

public abstract class BaseAction {
	private final Date timestamp = new Date();

    public abstract String getType();

	public XContentBuilder toJSONObject(XContentBuilder builder) throws IOException {
        builder.field("date", timestamp);
		return builder;
	}

}
