package com.foxelbox.foxellog;

import com.foxelbox.foxellog.actions.BaseAction;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
