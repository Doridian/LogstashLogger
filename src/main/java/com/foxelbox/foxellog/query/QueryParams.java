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
package com.foxelbox.foxellog.query;

import com.mongodb.BasicDBObject;
import org.bukkit.Location;

import java.io.Serializable;

public class QueryParams implements Serializable {
    public enum AggregationMode {
        PLAYERS,
        BLOCKS
    }

    public enum PerformMode {
        ROLLBACK,
        REDO,
        GET
    }

    public BasicDBObject query = new BasicDBObject();
    public BasicDBObject sort = new BasicDBObject("date", -1);

    public AggregationMode aggregationMode = null;
    public PerformMode performMode = PerformMode.GET;

    public boolean worldSet = false;
    public Location setLocation = null;
    public int area = -1;
}
