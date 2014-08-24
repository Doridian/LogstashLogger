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
import com.foxelbox.foxellog.util.ClassUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class BaseAction {
	private final Date date;
    private final HumanEntity user;
    private final Location location;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public abstract String getActionType();

    public static String getCollection() {
        return "actions";
    }

    protected BaseAction(HumanEntity user, Location location) {
        this.date = new Date();
        this.user = user;
        this.location = location;
    }

    protected BaseAction(DBObject fields) {
        date = (Date)fields.get("date");
        user = FoxelLog.instance.getServer().getPlayer((UUID)fields.get("user_uuid"));

        final DBObject locationFields = (DBObject)fields.get("location");
        location = new Location(FoxelLog.instance.getServer().getWorld((String)locationFields.get("world")), (double)locationFields.get("x"), (double)locationFields.get("y"), (double)locationFields.get("z"));
    }

    public final DBObject toDBObject() {
        return toBasicDBObject(new BasicDBObject());
    }

    protected BasicDBObject toBasicDBObject(BasicDBObject builder) {
        builder.append("date", date);

        builder.append("type", getActionType());

        builder.append("location", new BasicDBObject()
                .append("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("world", location.getWorld().getName())
        );

        //builder.field("user_name", user.getName());
        builder.append("user_uuid", user.getUniqueId());

		return builder;
	}

    public Date getDate() {
        return date;
    }

    public HumanEntity getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }

    private final static Map<String, Class<? extends BaseAction>> typeToClassMap = new HashMap<>();
    private final static Map<String, Constructor<? extends BaseAction>> typeToCtorMap = new HashMap<>();
    static {
        final ObjenesisStd objenesisStd = new ObjenesisStd();
        for(Class<? extends BaseAction> clazz : ClassUtils.getSubClasses(BaseAction.class, BaseAction.class.getPackage().getName())) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    final String classType = objenesisStd.newInstance(clazz).getActionType();
                    typeToClassMap.put(classType, clazz);
                    typeToCtorMap.put(classType, clazz.getDeclaredConstructor(DBObject.class));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Set<String> getTypes() {
        return typeToClassMap.keySet();
    }

    public static BaseAction craftActionByTypeAndDBObject(String type, DBObject fields) {
        try {
            return typeToCtorMap.get(type).newInstance(fields);
        } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
