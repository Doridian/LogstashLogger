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
package com.foxelbox.foxellog;

import com.foxelbox.foxellog.actions.BaseAction;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class ChangeQueryInterface {
    private final FoxelLog plugin;

    public ChangeQueryInterface(FoxelLog plugin) {
        this.plugin = plugin;
    }

    public List<BaseAction> queryActions(final DBObject query) {
        return queryActions(query, new BasicDBObject().append("date", 1));
    }

    public List<BaseAction> queryActions(final DBObject query, final DBObject sortBy) {
        DBCursor cursor = plugin.getMongoDB().getCollection(BaseAction.getCollection()).find(query);
        if(sortBy != null)
            cursor = cursor.sort(sortBy);

        final List<BaseAction> ret = new ArrayList<>();

        for(DBObject dbObject : cursor)
            ret.add(BaseAction.craftActionByTypeAndDBObject((String)dbObject.get("type"), dbObject));

        return ret;
    }
}
